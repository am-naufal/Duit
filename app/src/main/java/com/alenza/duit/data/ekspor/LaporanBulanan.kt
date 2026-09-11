package com.alenza.duit.data.ekspor

import com.alenza.duit.data.Kategori
import com.alenza.duit.data.Periode
import com.alenza.duit.data.Ringkasan
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.data.Transaksi
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Satu baris rekap kategori di sheet "Ringkasan" (PRD F-6). */
data class BarisRekap(
    val kategori: String,
    val tipe: TipeTransaksi,
    val jumlahTransaksi: Int,
    val total: Long,
    /** % terhadap total pengeluaran; 0 untuk kategori pemasukan. */
    val persen: Int,
)

/** Satu baris di sheet "Transaksi" — pemasukan & pengeluaran dipisah kolom (F-6). */
data class BarisTransaksiLaporan(
    val tanggal: LocalDate,
    val tipe: TipeTransaksi,
    val kategori: String,
    val catatan: String,
    val pemasukan: Long,
    val pengeluaran: Long,
    val saldoBerjalan: Long,
)

/** Satu baris di sheet "Harian" — termasuk tanggal tanpa transaksi (F-6). */
data class BarisHarian(
    val tanggal: LocalDate,
    val pemasukan: Long,
    val pengeluaran: Long,
    val selisih: Long,
    val saldoKumulatif: Long,
)

data class LaporanBulanan(
    val bulan: YearMonth,
    /** Rentang tanggal sebenarnya (§8 "hari awal bulan" — bisa bukan tanggal 1). */
    val awal: LocalDate,
    val akhir: LocalDate,
    val judul: String,
    val tanggalEkspor: LocalDate,
    val jumlahTransaksi: Int,
    val totalPemasukan: Long,
    val totalPengeluaran: Long,
    val selisih: Long,
    val rekap: List<BarisRekap>,
    val transaksi: List<BarisTransaksiLaporan>,
    val harian: List<BarisHarian>,
) {
    val kosong: Boolean get() = jumlahTransaksi == 0
}

/**
 * Merakit [LaporanBulanan] dari data mentah. Fungsi murni — tidak menyentuh Room
 * maupun file — supaya bisa diuji di JVM (F-6: total di laporan harus sama persis
 * dengan yang di layar ringkasan).
 */
object RakitLaporan {

    private val BULAN_ID = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("in", "ID"))

    fun dari(
        periode: Periode,
        transaksi: List<Transaksi>,
        kategori: List<Kategori>,
        tanggalEkspor: LocalDate = LocalDate.now(),
    ): LaporanBulanan {
        val bulan = periode.label
        val ringkasan = Ringkasan.hitung(transaksi, kategori)
        val katMap = kategori.associateBy { it.id }

        val kronologis = transaksi.sortedWith(compareBy({ it.tanggal }, { it.dibuatPada }))

        var saldo = 0L
        val barisTransaksi = kronologis.map { t ->
            val masuk = if (t.tipe == TipeTransaksi.PEMASUKAN) t.nominal else 0L
            val keluar = if (t.tipe == TipeTransaksi.PENGELUARAN) t.nominal else 0L
            saldo += masuk - keluar
            BarisTransaksiLaporan(
                tanggal = LocalDate.ofEpochDay(t.tanggal),
                tipe = t.tipe,
                kategori = katMap[t.kategoriId]?.nama ?: "Lainnya",
                catatan = t.catatan.orEmpty(),
                pemasukan = masuk,
                pengeluaran = keluar,
                saldoBerjalan = saldo,
            )
        }

        var kumulatif = 0L
        val jumlahHari = (periode.akhir.toEpochDay() - periode.awal.toEpochDay() + 1).toInt()
        val perHari = (0 until jumlahHari).map { i ->
            val tgl = periode.awal.plusDays(i.toLong())
            val epochDay = tgl.toEpochDay()
            val hariIni = transaksi.filter { it.tanggal == epochDay }
            val masuk = hariIni.filter { it.tipe == TipeTransaksi.PEMASUKAN }.sumOf { it.nominal }
            val keluar = hariIni.filter { it.tipe == TipeTransaksi.PENGELUARAN }.sumOf { it.nominal }
            kumulatif += masuk - keluar
            BarisHarian(tgl, masuk, keluar, masuk - keluar, kumulatif)
        }

        val rekapPengeluaran = ringkasan.rekapPengeluaran.map {
            BarisRekap(it.nama, TipeTransaksi.PENGELUARAN, it.jumlahTransaksi, it.total, it.persen)
        }
        val rekapPemasukan = transaksi
            .filter { it.tipe == TipeTransaksi.PEMASUKAN }
            .groupBy { it.kategoriId }
            .map { (id, daftar) ->
                BarisRekap(
                    kategori = katMap[id]?.nama ?: "Lainnya",
                    tipe = TipeTransaksi.PEMASUKAN,
                    jumlahTransaksi = daftar.size,
                    total = daftar.sumOf { it.nominal },
                    persen = 0,
                )
            }
            .sortedWith(compareByDescending<BarisRekap> { it.total }.thenBy { it.kategori })
        // Kelompok pengeluaran lalu pemasukan, BUKAN digabung-urut lintas tipe —
        // supaya baris rentangnya tetap berurutan per tipe di sheet Excel, jadi
        // formula SUM subtotal (PenulisXlsx) tidak pernah menjumlah nominal
        // masuk dengan nominal keluar jadi satu angka yang tak berarti.
        val rekap = rekapPengeluaran + rekapPemasukan

        val namaBulan = bulan.atDay(1).format(BULAN_ID).replaceFirstChar { it.uppercase() }

        return LaporanBulanan(
            bulan = bulan,
            awal = periode.awal,
            akhir = periode.akhir,
            judul = "Laporan Keuangan — $namaBulan",
            tanggalEkspor = tanggalEkspor,
            jumlahTransaksi = transaksi.size,
            totalPemasukan = ringkasan.totalPemasukan,
            totalPengeluaran = ringkasan.totalPengeluaran,
            selisih = ringkasan.selisih,
            rekap = rekap,
            transaksi = barisTransaksi,
            harian = perHari,
        )
    }
}
