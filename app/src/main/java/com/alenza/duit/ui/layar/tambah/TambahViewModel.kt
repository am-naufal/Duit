package com.alenza.duit.ui.layar.tambah

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.alenza.duit.data.DuitRepository
import com.alenza.duit.data.FormatTanggal
import com.alenza.duit.data.Kategori
import com.alenza.duit.data.Preferensi
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.data.Transaksi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Menyimpan isian form di `SavedStateHandle` supaya bertahan saat rotasi maupun
 * proses dimatikan (design-spec §2 / PRD F-1). Sumber kebenaran tetap Room; VM
 * tidak menyimpan salinan daftar kategori.
 */
class TambahViewModel(
    app: Application,
    private val simpanan: SavedStateHandle,
) : AndroidViewModel(app) {

    private val repo = DuitRepository.dari(app)
    private val preferensi = Preferensi.dari(app)
    private val idEdit: Long = simpanan.get<Long>(ARG_ID) ?: 0L

    /** Transaksi asli saat mode ubah — untuk membandingkan "kotor" & menjaga `dibuatPada`. */
    private var asli: Transaksi? = null

    private val tipeStr = simpanan.getStateFlow(K_TIPE, TipeTransaksi.PENGELUARAN.name)
    private val nominal = simpanan.getStateFlow(K_NOMINAL, 0L)
    private val kategoriId = simpanan.getStateFlow(K_KATEGORI, 0L)
    private val tanggalEpoch = simpanan.getStateFlow(K_TANGGAL, LocalDate.now().toEpochDay())
    private val catatan = simpanan.getStateFlow(K_CATATAN, "")

    init {
        if (idEdit != 0L) {
            viewModelScope.launch {
                val t = repo.transaksi(idEdit)
                asli = t
                if (t != null && simpanan.get<Boolean>(K_TERMUAT) != true) {
                    simpanan[K_TIPE] = t.tipe.name
                    simpanan[K_NOMINAL] = t.nominal
                    simpanan[K_KATEGORI] = t.kategoriId
                    simpanan[K_TANGGAL] = t.tanggal
                    simpanan[K_CATATAN] = t.catatan.orEmpty()
                }
                simpanan[K_TERMUAT] = true
            }
        }
    }

    private data class Isian(
        val tipe: TipeTransaksi,
        val nominal: Long,
        val kategoriId: Long,
        val tanggalEpoch: Long,
        val catatan: String,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TambahState> = combine(
        tipeStr, nominal, kategoriId, tanggalEpoch, catatan,
    ) { t, n, k, tgl, c ->
        Isian(TipeTransaksi.valueOf(t), n, k, tgl, c)
    }.flatMapLatest { isian ->
        combine(repo.kategoriAktif(isian.tipe), preferensi.aliran) { kats, pref ->
            bangun(isian, sisipkanArsipTerpilih(isian, kats), pref.formatTanggal)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TambahState.awal())

    /**
     * Kategori terpilih bisa saja sudah diarsipkan sejak transaksi ini dibuat —
     * `kategoriAktif` tak akan memuatnya lagi. Tanpa ini chip terpilih hilang
     * dari tampilan meski `kategoriTerpilih` tetap terisi (edge case).
     */
    private suspend fun sisipkanArsipTerpilih(isian: Isian, kats: List<Kategori>): List<Kategori> {
        if (isian.kategoriId == 0L || kats.any { it.id == isian.kategoriId }) return kats
        val terpilih = repo.kategori(isian.kategoriId) ?: return kats
        return kats + terpilih
    }

    fun pilihTipe(t: TipeTransaksi) {
        if (t.name == tipeStr.value) return
        simpanan[K_TIPE] = t.name
        // Kategori lama milik tipe sebelumnya — batalkan pilihan.
        simpanan[K_KATEGORI] = 0L
    }

    fun tekanAngka(digit: Int) {
        val baru = nominal.value * 10 + digit
        if (baru in 0..BATAS_NOMINAL) simpanan[K_NOMINAL] = baru
    }

    fun tekanRibuan() {
        val baru = nominal.value * 1_000
        if (baru in 0..BATAS_NOMINAL) simpanan[K_NOMINAL] = baru
    }

    fun hapusAngka() {
        simpanan[K_NOMINAL] = nominal.value / 10
    }

    fun pilihKategori(id: Long) {
        simpanan[K_KATEGORI] = id
    }

    fun pilihTanggal(epochDay: Long) {
        simpanan[K_TANGGAL] = epochDay
    }

    fun ubahCatatan(teks: String) {
        simpanan[K_CATATAN] = teks.take(BATAS_CATATAN)
    }

    /** [onSelesai] menerima pesan pemberitahuan ("Transaksi ditambahkan"/"diperbarui"). */
    fun simpan(onSelesai: (String) -> Unit) {
        val s = state.value
        val kategori = s.kategoriTerpilih ?: return
        if (s.nominal <= 0L) return
        val dasar = asli ?: KOSONG
        val pesan = if (idEdit == 0L) "Transaksi ditambahkan" else "Transaksi diperbarui"
        viewModelScope.launch {
            repo.simpan(
                dasar.copy(
                    id = idEdit,
                    nominal = s.nominal,
                    tipe = s.tipe,
                    kategoriId = kategori,
                    tanggal = s.tanggal.toEpochDay(),
                    catatan = s.catatan.trim().ifBlank { null },
                ),
            )
            onSelesai(pesan)
        }
    }

    private fun bangun(isian: Isian, kats: List<Kategori>, formatTanggal: FormatTanggal): TambahState {
        val tgl = LocalDate.ofEpochDay(isian.tanggalEpoch)
        val a = asli
        val kotor = if (idEdit == 0L) {
            isian.nominal > 0L ||
                isian.kategoriId != 0L ||
                isian.catatan.isNotBlank() ||
                isian.tipe != TipeTransaksi.PENGELUARAN ||
                isian.tanggalEpoch != LocalDate.now().toEpochDay()
        } else {
            a != null && (
                isian.nominal != a.nominal ||
                    isian.tipe != a.tipe ||
                    isian.kategoriId != a.kategoriId ||
                    isian.tanggalEpoch != a.tanggal ||
                    isian.catatan.trim().ifBlank { null } != a.catatan
                )
        }

        return TambahState(
            mode = if (idEdit == 0L) ModeForm.BARU else ModeForm.UBAH,
            idUbah = idEdit.takeIf { it != 0L },
            tipe = isian.tipe,
            nominal = isian.nominal,
            kategoriTerpilih = isian.kategoriId.takeIf { it != 0L },
            kategori = kats.map { KategoriChip(it.id, it.nama, it.colorHex, arsip = it.diarsipkan) },
            tanggal = tgl,
            labelTanggal = labelTanggal(tgl, formatTanggal),
            catatan = isian.catatan,
            kotor = kotor,
            memuat = false,
        )
    }

    companion object {
        const val ARG_ID = "id"

        private const val K_TIPE = "form_tipe"
        private const val K_NOMINAL = "form_nominal"
        private const val K_KATEGORI = "form_kategori"
        private const val K_TANGGAL = "form_tanggal"
        private const val K_CATATAN = "form_catatan"
        private const val K_TERMUAT = "form_termuat"

        /** 999.999.999.999 — jauh di atas kebutuhan nyata, aman dari overflow Long. */
        private const val BATAS_NOMINAL = 999_999_999_999L
        private const val BATAS_CATATAN = 140

        private val KOSONG = Transaksi(
            id = 0L,
            nominal = 0L,
            tipe = TipeTransaksi.PENGELUARAN,
            kategoriId = 0L,
            tanggal = 0L,
            catatan = null,
            dibuatPada = 0L,
            diubahPada = 0L,
        )

        private val LOKAL = Locale("in", "ID")

        /**
         * Versi ringkas (tanpa tahun) dari preferensi Pengaturan §8 — field pil
         * di form ini sengaja sesingkat contoh design-spec ("28 Agu"), jadi
         * [FormatTanggal] hanya menukar gaya bulan-nama vs angka/garis-miring,
         * bukan menambah tahun.
         */
        private val FORMAT_PENDEK_PANJANG = DateTimeFormatter.ofPattern("d MMM", LOKAL)
        private val FORMAT_PENDEK_RINGKAS = DateTimeFormatter.ofPattern("dd/MM", LOKAL)

        private fun labelTanggal(tgl: LocalDate, formatTanggal: FormatTanggal): String =
            if (tgl == LocalDate.now()) {
                "Hari ini"
            } else {
                val pola = when (formatTanggal) {
                    FormatTanggal.PANJANG -> FORMAT_PENDEK_PANJANG
                    FormatTanggal.RINGKAS -> FORMAT_PENDEK_RINGKAS
                }
                tgl.format(pola).replace(".", "")
            }
    }
}
