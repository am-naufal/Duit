package com.alenza.duit.ui.layar.ekspor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.alenza.duit.data.DuitRepository
import com.alenza.duit.data.Periode
import com.alenza.duit.data.Preferensi
import com.alenza.duit.data.ekspor.EksporWorker
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
import java.util.UUID

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
    private var periodeSaatIni: Periode? = null
    private var tugas: Job? = null
    private var uriTarget: Uri? = null
    private var workId: UUID? = null

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
            periodeSaatIni = periode
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
        val periode = periodeSaatIni
        if (l.jumlahTransaksi > AMBANG_WORKMANAGER && periode != null) {
            simpanLewatWorkManager(uri, periode)
        } else {
            simpanLangsung(uri, l)
        }
    }

    /** Jalur normal: cukup instan untuk volume biasa, tak perlu bertahan dari kematian proses. */
    private fun simpanLangsung(uri: Uri, l: LaporanBulanan) {
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

    /**
     * Jalur >5.000 baris (PRD F-6): lewat WorkManager, bukan `viewModelScope`,
     * supaya penulisan file tetap selesai walau aplikasi ditutup paksa di
     * tengah proses. [EksporWorker] merakit ulang laporan dari Room sendiri.
     */
    private fun simpanLewatWorkManager(uri: Uri, periode: Periode) {
        val wm = WorkManager.getInstance(getApplication())
        val permintaan = OneTimeWorkRequestBuilder<EksporWorker>()
            .setInputData(EksporWorker.data(uri, periode))
            .build()
        workId = permintaan.id
        wm.enqueue(permintaan)
        tugas = viewModelScope.launch {
            wm.getWorkInfoByIdFlow(permintaan.id).collect { info ->
                when (info?.state) {
                    WorkInfo.State.RUNNING -> {
                        val p = info.progress.getFloat(EksporWorker.KEY_PROGRES, 0f)
                        _state.update { it.copy(fase = FaseEkspor.MENULIS, progres = p) }
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        _state.update { it.copy(fase = FaseEkspor.SELESAI, progres = 1f) }
                    }
                    WorkInfo.State.FAILED -> {
                        val pesan = info.outputData.getString(EksporWorker.KEY_ERROR) ?: "Ekspor gagal"
                        _state.update { it.copy(fase = FaseEkspor.GAGAL, pesanGalat = pesan) }
                    }
                    WorkInfo.State.CANCELLED -> {
                        _state.update { it.copy(fase = FaseEkspor.SIAP, progres = 0f) }
                    }
                    else -> Unit
                }
            }
        }
    }

    /** Membatalkan proses menghapus file setengah jadi (PRD F-6). */
    fun batal() {
        workId?.let { WorkManager.getInstance(getApplication()).cancelWorkById(it) }
        workId = null
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

        /** PRD F-6: di atas ini, ekspor lewat WorkManager, bukan coroutine ViewModel biasa. */
        private const val AMBANG_WORKMANAGER = 5_000

        private val LOKAL = Locale("in", "ID")
        private val LABEL = DateTimeFormatter.ofPattern("MMMM yyyy", LOKAL)

        private fun labelBulan(ym: YearMonth): String =
            ym.atDay(1).format(LABEL).replaceFirstChar { it.uppercase() }

        private fun namaFile(ym: YearMonth): String = "Laporan-Keuangan-$ym.xlsx"
    }
}
