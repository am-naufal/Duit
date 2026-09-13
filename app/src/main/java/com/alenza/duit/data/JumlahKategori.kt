package com.alenza.duit.data

/**
 * Hasil proyeksi query `COUNT(*)` transaksi per kategori — bukan entity, hanya
 * bentuk baris yang dikembalikan [TransaksiDao.amatiJumlahPerKategori].
 */
data class JumlahKategori(
    val kategoriId: Long,
    val jumlah: Int,
)
