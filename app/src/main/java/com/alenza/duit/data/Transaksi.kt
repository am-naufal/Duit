package com.alenza.duit.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Satu catatan uang masuk atau keluar.
 *
 * - `nominal` selalu positif, satuan rupiah penuh sebagai `Long` (PRD §7) —
 *   tidak pernah `Double`, supaya tidak ada galat pembulatan.
 * - `tanggal` disimpan sebagai epoch day (`Long`); konversi tampilan pakai
 *   `java.time`.
 * - `ON DELETE RESTRICT`: kategori yang masih dipakai tidak bisa dihapus dari DB,
 *   hanya diarsipkan (PRD §F-2).
 */
@Entity(
    tableName = "transaksi",
    foreignKeys = [
        ForeignKey(
            entity = Kategori::class,
            parentColumns = ["id"],
            childColumns = ["kategoriId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("tanggal"), Index("kategoriId")],
)
data class Transaksi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nominal: Long,
    val tipe: TipeTransaksi,
    val kategoriId: Long,
    /** Epoch day (hari sejak 1970-01-01), zona waktu perangkat. */
    val tanggal: Long,
    /** Bebas, maksimal 140 karakter, opsional. */
    val catatan: String? = null,
    val dibuatPada: Long,
    val diubahPada: Long,
)
