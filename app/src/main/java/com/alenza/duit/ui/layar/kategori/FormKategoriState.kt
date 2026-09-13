package com.alenza.duit.ui.layar.kategori

import com.alenza.duit.data.TipeTransaksi

enum class ModeKategori { BARU, UBAH }

/**
 * State Form kategori (design-spec §5). Di-hoist dari [FormKategoriViewModel];
 * isian bertahan lewat `SavedStateHandle`.
 */
data class FormKategoriState(
    val mode: ModeKategori,
    val idUbah: Long?,
    val nama: String,
    val tipe: TipeTransaksi,
    val iconKey: String,
    val colorHex: String,
    val pilihanIkon: List<String>,
    val pilihanWarna: List<String>,
    /** "Lainnya" — tipe terkunci, tak bisa diarsipkan/dihapus. */
    val sistem: Boolean,
    /** Sudah dipakai transaksi → hanya bisa diarsipkan, tak bisa dihapus (PRD F-2). */
    val sudahDipakai: Boolean,
    /** Pesan validasi (nama kosong / nama duplikat), atau null. */
    val galat: String?,
    val kotor: Boolean,
    val memuat: Boolean,
) {
    val bisaSimpan: Boolean get() = nama.isNotBlank()

    val judul: String get() = if (mode == ModeKategori.UBAH) "Ubah kategori" else "Kategori baru"

    companion object {
        fun awal(
            pilihanIkon: List<String>,
            pilihanWarna: List<String>,
        ): FormKategoriState = FormKategoriState(
            mode = ModeKategori.BARU,
            idUbah = null,
            nama = "",
            tipe = TipeTransaksi.PENGELUARAN,
            iconKey = pilihanIkon.first(),
            colorHex = pilihanWarna.first(),
            pilihanIkon = pilihanIkon,
            pilihanWarna = pilihanWarna,
            sistem = false,
            sudahDipakai = false,
            galat = null,
            kotor = false,
            memuat = true,
        )
    }
}
