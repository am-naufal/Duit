package com.alenza.duit.ui.layar.kategori

import com.alenza.duit.data.TipeTransaksi

/**
 * State layar Kelola kategori (design-spec §4). Di-hoist dari
 * [KelolaKategoriViewModel]; composable tidak menyentuh Room.
 */
data class KelolaKategoriState(
    val tipe: TipeTransaksi,
    /** Kategori aktif untuk [tipe], urut sesuai `urutan`. */
    val aktif: List<BarisKategori>,
    /** Kategori yang diarsipkan untuk [tipe]. */
    val arsip: List<BarisKategori>,
    /** Pesan pemberitahuan sekali-tampil ("Kategori diarsipkan", dst.), atau null. */
    val pesan: String?,
    val memuat: Boolean,
) {
    companion object {
        fun awal(): KelolaKategoriState = KelolaKategoriState(
            tipe = TipeTransaksi.PENGELUARAN,
            aktif = emptyList(),
            arsip = emptyList(),
            pesan = null,
            memuat = true,
        )
    }
}

data class BarisKategori(
    val id: Long,
    val nama: String,
    val iconKey: String,
    val colorHex: String,
    val jumlahTransaksi: Int,
    /** "Lainnya" — tidak bisa dihapus/diarsipkan, tidak bisa diurutkan. */
    val sistem: Boolean,
)
