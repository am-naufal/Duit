package com.alenza.duit.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Kategori transaksi. `iconKey` dan `colorHex` disimpan sebagai String supaya
 * tidak ikut berubah kalau resource id berubah — pemetaan ke resource ada di
 * `com.alenza.duit.ui.theme.Kategori` (CategoryPalette.kt).
 *
 * Nama unik per (nama, tipe): "Lainnya" boleh ada di pengeluaran dan pemasukan
 * sekaligus, tapi tidak boleh ada dua "Makan" di tipe yang sama (PRD §F-2).
 */
@Entity(
    tableName = "kategori",
    indices = [Index(value = ["nama", "tipe"], unique = true)],
)
data class Kategori(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nama: String,
    val tipe: TipeTransaksi,
    val iconKey: String,
    val colorHex: String,
    /** Diarsipkan: hilang dari pilihan input, tapi transaksi lama tetap menampilkannya. */
    val diarsipkan: Boolean = false,
    /** true hanya untuk "Lainnya" — tidak bisa dihapus maupun diarsipkan. */
    val sistem: Boolean = false,
    val urutan: Int,
)
