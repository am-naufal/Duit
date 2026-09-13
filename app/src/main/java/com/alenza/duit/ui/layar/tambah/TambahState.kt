package com.alenza.duit.ui.layar.tambah

import com.alenza.duit.data.TipeTransaksi
import java.time.LocalDate

enum class ModeForm { BARU, UBAH }

/** Kategori yang tampil sebagai chip di form — bagian yang perlu composable saja. */
data class KategoriChip(
    val id: Long,
    val nama: String,
    val colorHex: String,
    /**
     * True hanya untuk kategori yang sudah diarsipkan tapi tetap disisipkan ke
     * daftar karena masih jadi kategori terpilih transaksi yang sedang diubah —
     * tanpa ini kategori terpilih jadi tak kelihatan sama sekali di form.
     */
    val arsip: Boolean = false,
)

/**
 * State form Tambah / Ubah transaksi (design-spec §2). Di-hoist dari
 * [TambahViewModel]; composable tidak menyentuh Room. Isian bertahan lewat
 * `SavedStateHandle` di ViewModel, jadi state ini boleh dibangun ulang kapan pun.
 */
data class TambahState(
    val mode: ModeForm,
    /** Id transaksi yang sedang diubah, atau null saat membuat baru. */
    val idUbah: Long?,
    val tipe: TipeTransaksi,
    /** Rupiah penuh, selalu ≥ 0. Diformat lewat `Rupiah` saat ditampilkan. */
    val nominal: Long,
    val kategoriTerpilih: Long?,
    val kategori: List<KategoriChip>,
    val tanggal: LocalDate,
    /** "Hari ini" atau "28 Agu". */
    val labelTanggal: String,
    val catatan: String,
    /** Ada isian/perubahan yang belum disimpan — untuk konfirmasi "Buang perubahan?". */
    val kotor: Boolean,
    val memuat: Boolean,
) {
    val bisaSimpan: Boolean get() = nominal > 0L && kategoriTerpilih != null

    val judul: String get() = if (mode == ModeForm.UBAH) "Ubah transaksi" else "Transaksi baru"

    companion object {
        fun awal(): TambahState = TambahState(
            mode = ModeForm.BARU,
            idUbah = null,
            tipe = TipeTransaksi.PENGELUARAN,
            nominal = 0L,
            kategoriTerpilih = null,
            kategori = emptyList(),
            tanggal = LocalDate.now(),
            labelTanggal = "Hari ini",
            catatan = "",
            kotor = false,
            memuat = true,
        )
    }
}
