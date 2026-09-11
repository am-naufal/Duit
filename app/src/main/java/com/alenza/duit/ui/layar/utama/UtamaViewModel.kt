package com.alenza.duit.ui.layar.utama

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.alenza.duit.data.DuitRepository
import com.alenza.duit.data.FormatTanggal
import com.alenza.duit.data.Kategori
import com.alenza.duit.data.Periode
import com.alenza.duit.data.Preferensi
import com.alenza.duit.data.PreferensiApp
import com.alenza.duit.data.Ringkasan
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.data.Transaksi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Menyusun [UtamaState] dari Flow DAO. Tidak menyimpan salinan transaksi/kategori
 * yang bisa basi (CLAUDE.md aturan 6) — hanya bulan yang sedang dilihat dan
 * penghapusan yang masih bisa diurungkan.
 */
class UtamaViewModel(
    app: Application,
    simpanan: SavedStateHandle,
) : AndroidViewModel(app) {

    private val repo = DuitRepository.dari(app)
    private val preferensi = Preferensi.dari(app)

    /**
     * Null = ikuti periode "sekarang" mengikuti `hariAwalBulan` yang sedang
     * berlaku. Terisi begitu pengguna menavigasi panah bulan (design-spec §9),
     * dan sejak itu tetap sebagai label eksplisit — tidak dihitung ulang kalau
     * preferensi berubah.
     */
    private val bulanManual = MutableStateFlow<YearMonth?>(null)

    /** Null bila tak ada yang bisa diurungkan. */
    private val undo = MutableStateFlow<HapusTertunda?>(null)
    private var jamUndo: Job? = null

    init {
        // Hasil "hapus" dari form (TambahScreen) dikirim balik lewat SavedStateHandle.
        viewModelScope.launch {
            simpanan.getStateFlow(KUNCI_HAPUS_DARI_FORM, 0L).collect { id ->
                if (id != 0L) {
                    simpanan[KUNCI_HAPUS_DARI_FORM] = 0L
                    hapusTransaksi(id)
                }
            }
        }
    }

    private data class Sumber(
        val transaksi: List<Transaksi>,
        val kategori: List<Kategori>,
        val terawalHari: Long?,
        val terakhirHari: Long?,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<UtamaState> = combine(bulanManual, preferensi.aliran) { manual, pref -> manual to pref }
        .distinctUntilChanged()
        .flatMapLatest { (manual, pref) ->
            val periode = Periode.dariLabel(
                manual ?: Periode.labelUntuk(LocalDate.now(), pref.hariAwalBulan),
                pref.hariAwalBulan,
            )
            combine(
                combine(
                    repo.transaksiPeriode(periode),
                    repo.semuaKategori(),
                    repo.bulanTerawal(),
                    repo.bulanTerakhir(),
                ) { tx, kat, awal, akhir -> Sumber(tx, kat, awal, akhir) },
                undo,
            ) { sumber, undoSaatIni ->
                rakit(periode, sumber, undoSaatIni, pref)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            UtamaState.awal(YearMonth.now(), formatBulan(YearMonth.now())),
        )

    fun bulanSebelumnya() {
        bulanManual.value = state.value.bulan.minusMonths(1)
    }

    fun bulanBerikutnya() {
        bulanManual.value = state.value.bulan.plusMonths(1)
    }

    /**
     * Hapus langsung dari Room, lalu buka jendela urungkan 5 detik. Menghapus
     * lebih dulu berarti menutup aplikasi sebelum 5 detik habis tetap
     * menyelesaikan penghapusan (design-spec §3 / PRD F-4).
     */
    fun hapusTransaksi(id: Long) {
        viewModelScope.launch {
            val tx = repo.transaksi(id) ?: return@launch
            val nama = repo.kategori(tx.kategoriId)?.nama ?: "Lainnya"
            repo.hapus(tx)
            undo.value = HapusTertunda(tx, nama)
            jamUndo?.cancel()
            jamUndo = viewModelScope.launch {
                delay(TAHAN_UNDO_MILLIS)
                undo.value = null
            }
        }
    }

    fun urungkanHapus() {
        val tertunda = undo.value ?: return
        jamUndo?.cancel()
        undo.value = null
        viewModelScope.launch { repo.pulihkan(tertunda.transaksi) }
    }

    fun tandaiInfoDilihat() {
        viewModelScope.launch { preferensi.tandaiInfoLokalDilihat() }
    }

    private fun rakit(
        periode: Periode,
        sumber: Sumber,
        undoSaatIni: HapusTertunda?,
        pref: PreferensiApp,
    ): UtamaState {
        val transaksi = sumber.transaksi
        val kategori = sumber.kategori
        val terawalHari = sumber.terawalHari
        val terakhirHari = sumber.terakhirHari
        val ringkasan = Ringkasan.hitung(transaksi, kategori)
        val katMap = kategori.associateBy { it.id }
        val hariIni = LocalDate.now()
        val ym = periode.label

        val grup = transaksi
            .groupBy { it.tanggal }
            .entries
            .sortedByDescending { it.key }
            .map { (epochDay, daftar) ->
                val tgl = LocalDate.ofEpochDay(epochDay)
                GrupHari(
                    tanggal = tgl,
                    label = labelHari(tgl, hariIni, pref.formatTanggal),
                    subtotalPengeluaran = daftar
                        .filter { it.tipe == TipeTransaksi.PENGELUARAN }
                        .sumOf { it.nominal },
                    transaksi = daftar
                        .sortedByDescending { it.dibuatPada }
                        .map { t ->
                            val k = katMap[t.kategoriId]
                            BarisTransaksi(
                                id = t.id,
                                namaKategori = k?.nama ?: "Lainnya",
                                iconKey = k?.iconKey ?: "lainnya",
                                colorHex = k?.colorHex ?: "#94989E",
                                catatan = t.catatan,
                                nominal = t.nominal,
                                tipe = t.tipe,
                            )
                        },
                )
            }

        val hariAwal = pref.hariAwalBulan
        val bulanTerawal = terawalHari?.let { Periode.labelUntuk(LocalDate.ofEpochDay(it), hariAwal) }
        val bulanTerakhir = terakhirHari?.let { Periode.labelUntuk(LocalDate.ofEpochDay(it), hariAwal) }
        val labelSekarang = Periode.labelUntuk(hariIni, hariAwal)
        // Selalu bisa mundur ke transaksi terlama & maju ke periode berjalan; lebih
        // jauh dari itu hanya kalau memang ada transaksi di sana.
        val batasMaju = maxOf(labelSekarang, bulanTerakhir ?: labelSekarang)

        return UtamaState(
            bulan = ym,
            labelBulan = formatBulan(ym),
            bisaMundur = bulanTerawal != null && bulanTerawal.isBefore(ym),
            bisaMaju = ym.isBefore(batasMaju),
            sisaBulan = ringkasan.selisih,
            totalPemasukan = ringkasan.totalPemasukan,
            totalPengeluaran = ringkasan.totalPengeluaran,
            rincian = ringkasan.rekapPengeluaran,
            hari = grup,
            undo = undoSaatIni,
            tampilkanInfoLokal = !pref.infoLokalSudahDilihat,
            memuat = false,
        )
    }

    companion object {
        const val KUNCI_HAPUS_DARI_FORM = "hapusTransaksiId"

        private const val TAHAN_UNDO_MILLIS = 5_000L

        private val LOKAL = Locale("in", "ID")
        private val FORMAT_BULAN = DateTimeFormatter.ofPattern("MMMM yyyy", LOKAL)

        /** Sesuai contoh Pengaturan §8: PANJANG "25 Agu 2026", RINGKAS "25/08/2026". */
        private val FORMAT_HARI_PANJANG = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", LOKAL)
        private val FORMAT_HARI_RINGKAS = DateTimeFormatter.ofPattern("EEE, dd/MM/yyyy", LOKAL)

        private fun formatBulan(ym: YearMonth): String =
            ym.atDay(1).format(FORMAT_BULAN).replaceFirstChar { it.uppercase() }

        private fun labelHari(tgl: LocalDate, hariIni: LocalDate, formatTanggal: FormatTanggal): String =
            when (tgl) {
                hariIni -> "Hari ini"
                hariIni.minusDays(1) -> "Kemarin"
                else -> {
                    val pola = when (formatTanggal) {
                        FormatTanggal.PANJANG -> FORMAT_HARI_PANJANG
                        FormatTanggal.RINGKAS -> FORMAT_HARI_RINGKAS
                    }
                    tgl.format(pola).replace(".", "").replaceFirstChar { it.uppercase() }
                }
            }
    }
}
