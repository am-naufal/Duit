package com.alenza.duit.ui.layar.ekspor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.alenza.duit.data.DuitRepository
import com.alenza.duit.data.Periode
import com.alenza.duit.data.Preferensi
import com.alenza.duit.data.ekspor.LaporanBulanan
import com.alenza.duit.data.ekspor.PenulisXlsx
import com.alenza.duit.data.ekspor.RakitLaporan
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class EksporViewModel(
    app: Application,
    simpanan: SavedStateHandle,
) : AndroidViewModel(app) {

    private val repo = DuitRepository.dari(app)
    private val preferensi = Preferensi.dari(app)

    /**
     * Null berarti "bulan berjalan" — dikirim dari Pengaturan (tak tahu bulan mana
     * yang sedang dilihat). Baru bisa dihitung setelah `hariAwalBulan` (§8) dibaca,
     * karena itu belum tersedia sinkron seperti label eksplisit dari Layar utama.
     */
    private val bulanEksplisit: YearMonth? = simpanan.get<String>(ARG_BULAN)
        ?.takeIf { it != SEKARANG }
        ?.let { runCatching { YearMonth.parse(it) }.getOrNull() }

    private var laporan: LaporanBulanan? = null
    private var tugas: Job? = null
    private var uriTarget: Uri? = null

    private val bulanAwal = bulanEksplisit ?: YearMonth.now()

    private val _state = MutableStateFlow(
        EksporState.awal(bulanAwal, labelBulan(bulanAwal), namaFile(bulanAwal)),
    )
    val state: StateFlow<EksporState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val hariAwalBulan = preferensi.aliran.first().hariAwalBulan
            val bulan = bulanEksplisit ?: Periode.labelUntuk(LocalDate.now(), hariAwalBulan)
            val periode = Periode.dariLabel(bulan, hariAwalBulan)
            val l = withContext(Dispatchers.IO) {
                RakitLaporan.dari(
                    periode,
                    repo.transaksiPeriodeSekali(periode),
                    repo.semuaKategoriSekali(),
                )
            }
            laporan = l
            _state.update {
                it.copy(
                    bulan = bulan,
                    labelBulan = labelBulan(bulan),
                    namaFile = namaFile(bulan),
                    jumlahTransaksi = l.jumlahTransaksi,
                    kosong = l.kosong,
                )
            }
        }
    }

    fun simpanKe(uri: Uri) {
        val l = laporan ?: return
        uriTarget = uri
        _state.update { it.copy(fase = FaseEkspor.MENULIS, progres = 0f, pesanGalat = null) }
        tugas = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val keluaran = getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?: error("Tidak bisa membuka lokasi yang dipilih")
                    keluaran.use { os ->
                        PenulisXlsx.tulis(l, os) { p ->
                            _state.update { it.copy(progres = p) }
                        }
                    }
                }
                _state.update { it.copy(fase = FaseEkspor.SELESAI, progres = 1f) }
            } catch (batal: kotlinx.coroutines.CancellationException) {
                hapusFileSetengah(uri)
                throw batal
            } catch (e: Exception) {
                hapusFileSetengah(uri)
                _state.update {
                    it.copy(fase = FaseEkspor.GAGAL, pesanGalat = e.message ?: "Ekspor gagal")
                }
            }
        }
    }

    /** Membatalkan proses menghapus file setengah jadi (PRD F-6). */
    fun batal() {
        tugas?.cancel()
        tugas = null
        uriTarget?.let { hapusFileSetengah(it) }
        _state.update { it.copy(fase = FaseEkspor.SIAP, progres = 0f) }
    }

    fun uriHasil(): Uri? = if (_state.value.fase == FaseEkspor.SELESAI) uriTarget else null

    private fun hapusFileSetengah(uri: Uri) {
        runCatching { getApplication<Application>().contentResolver.delete(uri, null, null) }
    }

    companion object {
        const val ARG_BULAN = "bulan"

        /** Sentinel [ARG_BULAN]: "bulan berjalan" dihitung dari `hariAwalBulan`, bukan label tetap. */
        const val SEKARANG = "sekarang"

        private val LOKAL = Locale("in", "ID")
        private val LABEL = DateTimeFormatter.ofPattern("MMMM yyyy", LOKAL)

        private fun labelBulan(ym: YearMonth): String =
            ym.atDay(1).format(LABEL).replaceFirstChar { it.uppercase() }

        private fun namaFile(ym: YearMonth): String = "Laporan-Keuangan-$ym.xlsx"
    }
}
