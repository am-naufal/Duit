package com.alenza.duit

import com.alenza.duit.ui.theme.Rupiah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RupiahTest {

    @Test
    fun format_ribuan_pakai_titik() {
        assertEquals("Rp 45.000", Rupiah.format(45_000))
        assertEquals("Rp 8.500.000", Rupiah.format(8_500_000))
    }

    @Test
    fun format_nol() {
        assertEquals("Rp 0", Rupiah.format(0))
    }

    @Test
    fun pengeluaran_pakai_minus_sign_bukan_hyphen() {
        val hasil = Rupiah.pengeluaran(15_000)
        assertEquals("−Rp 15.000", hasil)
        assertTrue("harus memakai U+2212", hasil.startsWith('−'))
        assertTrue("tidak boleh memakai hyphen-minus", !hasil.startsWith('-'))
    }

    @Test
    fun pemasukan_pakai_plus() {
        assertEquals("+Rp 8.500.000", Rupiah.pemasukan(8_500_000))
    }

    @Test
    fun angka_tanpa_prefix_rp() {
        assertEquals("1.250.000", Rupiah.angka(1_250_000))
        assertEquals("0", Rupiah.angka(0))
    }
}
