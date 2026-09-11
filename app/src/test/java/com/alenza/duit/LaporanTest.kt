package com.alenza.duit

import com.alenza.duit.data.Kategori
import com.alenza.duit.data.Periode
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.data.Transaksi
import com.alenza.duit.data.ekspor.RakitLaporan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class LaporanTest {

    private val bulan = YearMonth.of(2026, 8)
    private val periode = Periode.dariLabel(bulan, hariAwalBulan = 1)
    private var idBerikut = 1L

    private val makan = Kategori(1, "Makan & Minum", TipeTransaksi.PENGELUARAN, "makan", "#E0785C", urutan = 0)
    private val gaji = Kategori(2, "Gaji", TipeTransaksi.PEMASUKAN, "gaji", "#4E9E6A", urutan = 0)
    private val kategori = listOf(makan, gaji)

    private fun tx(hari: Int, nominal: Long, tipe: TipeTransaksi, kategoriId: Long) = Transaksi(
        id = idBerikut++,
        nominal = nominal,
        tipe = tipe,
        kategoriId = kategoriId,
        tanggal = bulan.atDay(hari).toEpochDay(),
        dibuatPada = idBerikut,
        diubahPada = idBerikut,
    )

    @Test
    fun total_laporan_sama_dengan_penjumlahan_manual() {
        val transaksi = listOf(
            tx(1, 5_000_000, TipeTransaksi.PEMASUKAN, gaji.id),
            tx(2, 50_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(3, 30_000, TipeTransaksi.PENGELUARAN, makan.id),
        )
        val l = RakitLaporan.dari(periode, transaksi, kategori, LocalDate.of(2026, 9, 1))

        assertEquals(5_000_000, l.totalPemasukan)
        assertEquals(80_000, l.totalPengeluaran)
        assertEquals(4_920_000, l.selisih)
        assertEquals(3, l.jumlahTransaksi)
    }

    @Test
    fun saldo_berjalan_baris_terakhir_sama_dengan_selisih() {
        val transaksi = listOf(
            tx(1, 3_000_000, TipeTransaksi.PEMASUKAN, gaji.id),
            tx(5, 250_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(9, 120_000, TipeTransaksi.PENGELUARAN, makan.id),
        )
        val l = RakitLaporan.dari(periode, transaksi, kategori)

        assertEquals(l.selisih, l.transaksi.last().saldoBerjalan)
        assertEquals(l.selisih, l.harian.last().saldoKumulatif)
    }

    @Test
    fun sheet_harian_punya_satu_baris_per_tanggal_bulan() {
        val l = RakitLaporan.dari(periode, emptyList(), kategori)
        assertEquals(31, l.harian.size)
        assertTrue(l.kosong)
        assertTrue(l.harian.all { it.pemasukan == 0L && it.pengeluaran == 0L })
    }

    @Test
    fun transaksi_diurut_kronologis_naik() {
        val transaksi = listOf(
            tx(20, 10_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(3, 20_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(11, 15_000, TipeTransaksi.PENGELUARAN, makan.id),
        )
        val tanggal = RakitLaporan.dari(periode, transaksi, kategori).transaksi.map { it.tanggal.dayOfMonth }
        assertEquals(listOf(3, 11, 20), tanggal)
    }
}
