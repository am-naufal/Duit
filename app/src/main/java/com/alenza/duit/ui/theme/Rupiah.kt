package com.alenza.duit.ui.theme

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Format rupiah tunggal untuk seluruh aplikasi.
 * Nominal disimpan sebagai Long dalam rupiah penuh (PRD §7) — tanpa desimal,
 * tanpa Double, supaya tidak ada galat pembulatan.
 */
object Rupiah {

    private val simbol = DecimalFormatSymbols(Locale("in", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val format = DecimalFormat("#,###", simbol)

    /** 45000 → "Rp 45.000" */
    fun format(nominal: Long): String = "Rp " + format.format(nominal)

    /** 45000 → "−Rp 45.000" (minus sign U+2212, bukan hyphen). */
    fun pengeluaran(nominal: Long): String = "−" + format(nominal)

    /** 8500000 → "+Rp 8.500.000" */
    fun pemasukan(nominal: Long): String = "+" + format(nominal)

    /** Tanpa prefix "Rp", untuk field input. */
    fun angka(nominal: Long): String = format.format(nominal)
}
