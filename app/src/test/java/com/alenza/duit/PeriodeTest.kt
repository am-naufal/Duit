package com.alenza.duit

import com.alenza.duit.data.Periode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/** Kunci perilaku "hari awal bulan" (Pengaturan §8) — B-3 di keputusan-tertunda.md. */
class PeriodeTest {

    @Test
    fun hari_awal_1_sama_dengan_bulan_kalender_biasa() {
        val p = Periode.dariLabel(YearMonth.of(2026, 8), hariAwalBulan = 1)
        assertEquals(LocalDate.of(2026, 8, 1), p.awal)
        assertEquals(LocalDate.of(2026, 8, 31), p.akhir)
    }

    @Test
    fun hari_awal_25_menghasilkan_rentang_lintas_bulan_kalender() {
        val p = Periode.dariLabel(YearMonth.of(2026, 8), hariAwalBulan = 25)
        assertEquals(LocalDate.of(2026, 8, 25), p.awal)
        assertEquals(LocalDate.of(2026, 9, 24), p.akhir)
    }

    @Test
    fun tanggal_sebelum_hari_awal_termasuk_label_bulan_sebelumnya() {
        // Gajian tanggal 25: 10 September masih bagian "bulan berjalan" Agustus.
        val label = Periode.labelUntuk(LocalDate.of(2026, 9, 10), hariAwalBulan = 25)
        assertEquals(YearMonth.of(2026, 8), label)
    }

    @Test
    fun tanggal_pada_atau_setelah_hari_awal_termasuk_label_bulan_ini() {
        val label = Periode.labelUntuk(LocalDate.of(2026, 9, 25), hariAwalBulan = 25)
        assertEquals(YearMonth.of(2026, 9), label)
    }

    @Test
    fun labelUntuk_dan_dariLabel_saling_konsisten() {
        val hariAwalBulan = 25
        val tanggal = LocalDate.of(2026, 9, 10)
        val periode = Periode.dariLabel(Periode.labelUntuk(tanggal, hariAwalBulan), hariAwalBulan)
        assertTrue(!tanggal.isBefore(periode.awal) && !tanggal.isAfter(periode.akhir))
    }
}
