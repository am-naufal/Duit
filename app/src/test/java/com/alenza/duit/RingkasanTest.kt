package com.alenza.duit

import com.alenza.duit.data.Kategori
import com.alenza.duit.data.Ringkasan
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.data.Transaksi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RingkasanTest {

    private var idBerikut = 1L

    private fun kategori(id: Long, nama: String, tipe: TipeTransaksi) =
        Kategori(id = id, nama = nama, tipe = tipe, iconKey = "lainnya", colorHex = "#94989E", urutan = 0)

    private fun tx(nominal: Long, tipe: TipeTransaksi, kategoriId: Long) =
        Transaksi(
            id = idBerikut++,
            nominal = nominal,
            tipe = tipe,
            kategoriId = kategoriId,
            tanggal = 20_000,
            dibuatPada = 0,
            diubahPada = 0,
        )

    private val makan = kategori(1, "Makan & Minum", TipeTransaksi.PENGELUARAN)
    private val transport = kategori(2, "Transportasi", TipeTransaksi.PENGELUARAN)
    private val belanja = kategori(3, "Belanja", TipeTransaksi.PENGELUARAN)
    private val gaji = kategori(4, "Gaji", TipeTransaksi.PEMASUKAN)
    private val semuaKategori = listOf(makan, transport, belanja, gaji)

    @Test
    fun total_dan_selisih_sama_dengan_penjumlahan_manual() {
        val transaksi = listOf(
            tx(5_000_000, TipeTransaksi.PEMASUKAN, gaji.id),
            tx(50_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(30_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(20_000, TipeTransaksi.PENGELUARAN, transport.id),
        )

        val r = Ringkasan.hitung(transaksi, semuaKategori)

        assertEquals(5_000_000, r.totalPemasukan)
        assertEquals(100_000, r.totalPengeluaran)
        assertEquals(4_900_000, r.selisih)
    }

    @Test
    fun selisih_bisa_negatif() {
        val transaksi = listOf(
            tx(100_000, TipeTransaksi.PEMASUKAN, gaji.id),
            tx(250_000, TipeTransaksi.PENGELUARAN, makan.id),
        )
        assertEquals(-150_000, Ringkasan.hitung(transaksi, semuaKategori).selisih)
    }

    @Test
    fun rekap_urut_dari_nominal_terbesar() {
        val transaksi = listOf(
            tx(20_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(90_000, TipeTransaksi.PENGELUARAN, transport.id),
            tx(50_000, TipeTransaksi.PENGELUARAN, belanja.id),
        )
        val nama = Ringkasan.hitung(transaksi, semuaKategori).rekapPengeluaran.map { it.nama }
        assertEquals(listOf("Transportasi", "Belanja", "Makan & Minum"), nama)
    }

    @Test
    fun persen_sisa_terbesar_berjumlah_seratus_untuk_tiga_bagian_sama() {
        val transaksi = listOf(
            tx(10_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(10_000, TipeTransaksi.PENGELUARAN, transport.id),
            tx(10_000, TipeTransaksi.PENGELUARAN, belanja.id),
        )
        val persen = Ringkasan.hitung(transaksi, semuaKategori).rekapPengeluaran.map { it.persen }
        assertEquals(100, persen.sum())
        assertEquals(setOf(33, 34), persen.toSet())
    }

    @Test
    fun persen_tidak_pernah_melebihi_seratus_dan_selalu_pas() {
        val transaksi = listOf(
            tx(1, TipeTransaksi.PENGELUARAN, makan.id),
            tx(1, TipeTransaksi.PENGELUARAN, transport.id),
            tx(1, TipeTransaksi.PENGELUARAN, belanja.id),
            tx(999_997, TipeTransaksi.PENGELUARAN, gaji.id),
        )
        val persen = Ringkasan.hitung(transaksi, semuaKategori).rekapPengeluaran.map { it.persen }
        assertEquals(100, persen.sum())
        assertTrue(persen.all { it in 0..100 })
    }

    @Test
    fun bulan_kosong_menghasilkan_nol() {
        val r = Ringkasan.hitung(emptyList(), semuaKategori)
        assertEquals(0, r.totalPemasukan)
        assertEquals(0, r.totalPengeluaran)
        assertEquals(0, r.selisih)
        assertTrue(r.rekapPengeluaran.isEmpty())
    }

    @Test
    fun bulan_hanya_pemasukan_tak_punya_rekap_pengeluaran() {
        val transaksi = listOf(tx(3_000_000, TipeTransaksi.PEMASUKAN, gaji.id))
        val r = Ringkasan.hitung(transaksi, semuaKategori)
        assertEquals(3_000_000, r.totalPemasukan)
        assertEquals(0, r.totalPengeluaran)
        assertTrue(r.rekapPengeluaran.isEmpty())
    }

    @Test
    fun jumlah_transaksi_per_kategori_dihitung() {
        val transaksi = listOf(
            tx(10_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(12_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(8_000, TipeTransaksi.PENGELUARAN, makan.id),
            tx(20_000, TipeTransaksi.PENGELUARAN, transport.id),
        )
        val rekap = Ringkasan.hitung(transaksi, semuaKategori).rekapPengeluaran
        assertEquals(3, rekap.first { it.nama == "Makan & Minum" }.jumlahTransaksi)
        assertEquals(30_000, rekap.first { it.nama == "Makan & Minum" }.total)
    }
}
