package com.alenza.duit.ui.layar.kategori

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.alenza.duit.data.DuitRepository
import com.alenza.duit.data.Kategori
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.ui.theme.Kategori as Palet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Form tambah/ubah kategori. Isian di `SavedStateHandle`; validasi nama (kosong /
 * duplikat per tipe, PRD F-2) dijalankan saat menyimpan.
 */
class FormKategoriViewModel(
    app: Application,
    private val simpanan: SavedStateHandle,
) : AndroidViewModel(app) {

    private val repo = DuitRepository.dari(app)
    private val idEdit: Long = simpanan.get<Long>(ARG_ID) ?: 0L
    private var asli: Kategori? = null

    private val nama = simpanan.getStateFlow(K_NAMA, "")
    private val tipe = simpanan.getStateFlow(K_TIPE, tipeAwal())
    private val iconKey = simpanan.getStateFlow(K_IKON, Palet.pilihanIkon.first())
    private val colorHex = simpanan.getStateFlow(K_WARNA, Palet.pilihanWarna.first())
    private val galat = MutableStateFlow<String?>(null)
    private val sudahDipakai = MutableStateFlow(false)

    init {
        if (idEdit != 0L) {
            viewModelScope.launch {
                val k = repo.kategori(idEdit) ?: return@launch
                asli = k
                sudahDipakai.value = !repo.bolehHapusKategori(k)
                if (simpanan.get<Boolean>(K_TERMUAT) != true) {
                    simpanan[K_NAMA] = k.nama
                    simpanan[K_TIPE] = k.tipe.name
                    simpanan[K_IKON] = k.iconKey
                    simpanan[K_WARNA] = k.colorHex
                }
                simpanan[K_TERMUAT] = true
            }
        }
    }

    val state: StateFlow<FormKategoriState> = combine(
        combine(nama, tipe, iconKey, colorHex) { n, t, ik, w -> Empat(n, t, ik, w) },
        galat,
        sudahDipakai,
    ) { isian, pesan, dipakai ->
        val k = asli
        FormKategoriState(
            mode = if (idEdit == 0L) ModeKategori.BARU else ModeKategori.UBAH,
            idUbah = idEdit.takeIf { it != 0L },
            nama = isian.nama,
            tipe = TipeTransaksi.valueOf(isian.tipe),
            iconKey = isian.iconKey,
            colorHex = isian.colorHex,
            pilihanIkon = Palet.pilihanIkon,
            pilihanWarna = Palet.pilihanWarna,
            sistem = k?.sistem == true,
            sudahDipakai = dipakai,
            galat = pesan,
            kotor = if (k == null) {
                isian.nama.isNotBlank()
            } else {
                isian.nama != k.nama ||
                    isian.tipe != k.tipe.name ||
                    isian.iconKey != k.iconKey ||
                    isian.colorHex != k.colorHex
            },
            memuat = false,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FormKategoriState.awal(Palet.pilihanIkon, Palet.pilihanWarna),
    )

    fun ubahNama(teks: String) {
        simpanan[K_NAMA] = teks.take(BATAS_NAMA)
        if (galat.value != null) galat.value = null
    }

    fun pilihTipe(t: TipeTransaksi) {
        if (asli?.sistem == true) return
        simpanan[K_TIPE] = t.name
    }

    fun pilihIkon(key: String) { simpanan[K_IKON] = key }

    fun pilihWarna(hex: String) { simpanan[K_WARNA] = hex }

    /** [onSelesai] menerima pesan pemberitahuan ("Kategori ditambahkan"/"diperbarui"). */
    fun simpan(onSelesai: (String) -> Unit) {
        val s = state.value
        val namaBersih = s.nama.trim()
        if (namaBersih.isEmpty()) {
            galat.value = "Nama kategori tidak boleh kosong"
            return
        }
        val pesan = if (idEdit == 0L) "Kategori ditambahkan" else "Kategori diperbarui"
        viewModelScope.launch {
            if (repo.namaKategoriBentrok(namaBersih, s.tipe, idEdit)) {
                galat.value = "Sudah ada kategori \"$namaBersih\" di ${labelTipe(s.tipe)}"
                return@launch
            }
            val dasar = asli
            val kategori = if (dasar == null) {
                Kategori(
                    nama = namaBersih,
                    tipe = s.tipe,
                    iconKey = s.iconKey,
                    colorHex = s.colorHex,
                    urutan = repo.urutanKategoriBerikutnya(s.tipe),
                )
            } else {
                dasar.copy(
                    nama = namaBersih,
                    tipe = if (dasar.sistem) dasar.tipe else s.tipe,
                    iconKey = s.iconKey,
                    colorHex = s.colorHex,
                )
            }
            repo.simpanKategori(kategori)
            onSelesai(pesan)
        }
    }

    /** Kategori yang sudah dipakai transaksi (PRD F-2) — hilang dari pilihan, riwayat tetap. */
    fun arsipkan(onSelesai: (String) -> Unit) {
        val k = asli ?: return
        viewModelScope.launch {
            repo.arsipkanKategori(k)
            onSelesai("Kategori diarsipkan")
        }
    }

    /** Hanya untuk kategori yang belum pernah dipakai (PRD F-2). */
    fun hapus(onSelesai: (String) -> Unit) {
        val k = asli ?: return
        viewModelScope.launch {
            if (repo.bolehHapusKategori(k)) {
                repo.hapusKategori(k)
                onSelesai("Kategori dihapus")
            }
        }
    }

    private fun tipeAwal(): String =
        simpanan.get<String>(ARG_TIPE) ?: TipeTransaksi.PENGELUARAN.name

    private data class Empat(val nama: String, val tipe: String, val iconKey: String, val colorHex: String)

    companion object {
        const val ARG_ID = "id"
        const val ARG_TIPE = "tipe"

        private const val K_NAMA = "form_kat_nama"
        private const val K_TIPE = "form_kat_tipe"
        private const val K_IKON = "form_kat_ikon"
        private const val K_WARNA = "form_kat_warna"
        private const val K_TERMUAT = "form_kat_termuat"

        private const val BATAS_NAMA = 30

        private fun labelTipe(t: TipeTransaksi) =
            if (t == TipeTransaksi.PENGELUARAN) "Pengeluaran" else "Pemasukan"
    }
}
