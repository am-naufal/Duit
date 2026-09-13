package com.alenza.duit.data

/**
 * Arah sebuah transaksi. Nominal selalu disimpan positif (PRD §7);
 * tipe inilah yang menentukan apakah uang masuk atau keluar.
 */
enum class TipeTransaksi {
    PENGELUARAN,
    PEMASUKAN,
}
