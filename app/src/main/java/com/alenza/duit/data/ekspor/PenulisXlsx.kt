package com.alenza.duit.data.ekspor

import com.alenza.duit.data.TipeTransaksi
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.OutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Menulis [LaporanBulanan] menjadi file .xlsx lewat fastexcel (writer-only, tanpa
 * jaringan — PRD F-6). Nominal ditulis sebagai angka dengan number-format Rupiah
 * supaya bisa dijumlahkan di Excel; tanggal sebagai tipe tanggal Excel; baris
 * TOTAL memakai formula `SUM` asli.
 */
object PenulisXlsx {

    // Teks literal "Rp" ditulis lewat escape backslash-per-huruf (\R\p), BUKAN
    // "Rp" berkutip. fastexcel 0.18.4 menulis formatCode ini apa adanya ke atribut
    // XML (numFmt formatCode="…") tanpa meng-escape tanda kutip di dalamnya —
    // versi berkutip menghasilkan `formatCode=""Rp"#,##0…"` yang memutus atribut
    // itu sendiri (styles.xml jadi XML tak valid, file .xlsx rusak saat dibuka).
    // Ditemukan lewat verifikasi manual B-5 di docs/keputusan-tertunda.md.
    private const val FORMAT_RUPIAH = "\\R\\p#,##0;[Red]-\\R\\p#,##0"
    private const val FORMAT_TANGGAL = "dd/mm/yyyy"

    private val LOKAL = Locale("in", "ID")
    private val HARI = DateTimeFormatter.ofPattern("EEEE", LOKAL)
    private val TANGGAL_TEKS = DateTimeFormatter.ofPattern("d MMM yyyy", LOKAL)

    /** @param onProgress dipanggil 0f..1f selama menulis sheet Transaksi. */
    fun tulis(laporan: LaporanBulanan, keluaran: OutputStream, onProgress: (Float) -> Unit = {}) {
        Workbook(keluaran, "Duit", "1.0").apply {
            tulisRingkasan(newWorksheet("Ringkasan"), laporan)
            tulisTransaksi(newWorksheet("Transaksi"), laporan, onProgress)
            tulisHarian(newWorksheet("Harian"), laporan)
            finish()
        }
    }

    private fun tulisRingkasan(ws: Worksheet, l: LaporanBulanan) {
        ws.value(0, 0, l.judul)
        ws.style(0, 0).bold().fontSize(14).set()

        ws.value(2, 0, "Tanggal ekspor")
        ws.value(2, 1, l.tanggalEkspor)
        ws.style(2, 1).format(FORMAT_TANGGAL).set()
        ws.value(3, 0, "Jumlah transaksi")
        ws.value(3, 1, l.jumlahTransaksi)
        ws.value(4, 0, "Rentang")
        ws.value(
            4, 1,
            "${l.awal.format(TANGGAL_TEKS)} – ${l.akhir.format(TANGGAL_TEKS)}",
        )

        ws.value(6, 0, "Total Pemasukan")
        uang(ws, 6, 1, l.totalPemasukan)
        ws.value(7, 0, "Total Pengeluaran")
        uang(ws, 7, 1, l.totalPengeluaran)
        ws.value(8, 0, "Selisih")
        uang(ws, 8, 1, l.selisih)
        ws.range(6, 0, 8, 0).style().bold().set()

        val hdr = 10
        val kolom = listOf("Kategori", "Tipe", "Jumlah Transaksi", "Total", "% Pengeluaran")
        kolom.forEachIndexed { c, teks -> ws.value(hdr, c, teks) }
        ws.range(hdr, 0, hdr, kolom.lastIndex).style().bold().set()

        l.rekap.forEachIndexed { i, r ->
            val row = hdr + 1 + i
            ws.value(row, 0, r.kategori)
            ws.value(row, 1, labelTipe(r.tipe))
            ws.value(row, 2, r.jumlahTransaksi)
            uang(ws, row, 3, r.total)
            if (r.tipe == TipeTransaksi.PENGELUARAN) ws.value(row, 4, r.persen / 100.0)
            ws.style(row, 4).format("0%").set()
        }

        val totalRow = hdr + 1 + l.rekap.size
        ws.value(totalRow, 0, "TOTAL")
        if (l.rekap.isNotEmpty()) {
            val awal = hdr + 2
            val akhir = totalRow
            ws.formula(totalRow, 2, "SUM(C$awal:C$akhir)")
            ws.formula(totalRow, 3, "SUM(D$awal:D$akhir)")
        } else {
            ws.value(totalRow, 2, 0)
            uang(ws, totalRow, 3, 0)
        }
        ws.style(totalRow, 3).format(FORMAT_RUPIAH).set()
        ws.range(totalRow, 0, totalRow, 4).style().bold().set()
        ws.setAutoFilter(hdr, 0, totalRow - 1, kolom.lastIndex)

        ws.width(0, 22.0)
        ws.width(1, 14.0)
        ws.width(2, 18.0)
        ws.width(3, 16.0)
        ws.width(4, 14.0)
        ws.freezePane(0, hdr + 1)
    }

    private fun tulisTransaksi(ws: Worksheet, l: LaporanBulanan, onProgress: (Float) -> Unit) {
        val kolom = listOf(
            "Tanggal", "Hari", "Tipe", "Kategori", "Catatan",
            "Pemasukan", "Pengeluaran", "Saldo Berjalan",
        )
        kolom.forEachIndexed { c, teks -> ws.value(0, c, teks) }
        ws.range(0, 0, 0, kolom.lastIndex).style().bold().set()

        val total = l.transaksi.size.coerceAtLeast(1)
        l.transaksi.forEachIndexed { i, t ->
            val row = i + 1
            ws.value(row, 0, t.tanggal)
            ws.style(row, 0).format(FORMAT_TANGGAL).set()
            ws.value(row, 1, t.tanggal.format(HARI).replaceFirstChar { it.uppercase() })
            ws.value(row, 2, labelTipe(t.tipe))
            ws.value(row, 3, t.kategori)
            ws.value(row, 4, t.catatan)
            ws.style(row, 4).wrapText(true).set()
            if (t.pemasukan > 0) uang(ws, row, 5, t.pemasukan)
            if (t.pengeluaran > 0) uang(ws, row, 6, t.pengeluaran)
            uang(ws, row, 7, t.saldoBerjalan)
            if (i % 128 == 0) onProgress((i + 1f) / total)
        }
        onProgress(1f)

        ws.setAutoFilter(0, 0, l.transaksi.size, kolom.lastIndex)

        ws.width(0, 12.0)
        ws.width(1, 10.0)
        ws.width(2, 12.0)
        ws.width(3, 20.0)
        ws.width(4, 32.0)
        ws.width(5, 14.0)
        ws.width(6, 14.0)
        ws.width(7, 16.0)
        ws.freezePane(0, 1)
    }

    private fun tulisHarian(ws: Worksheet, l: LaporanBulanan) {
        val kolom = listOf("Tanggal", "Pemasukan", "Pengeluaran", "Selisih", "Saldo Kumulatif")
        kolom.forEachIndexed { c, teks -> ws.value(0, c, teks) }
        ws.range(0, 0, 0, kolom.lastIndex).style().bold().set()

        l.harian.forEachIndexed { i, h ->
            val row = i + 1
            ws.value(row, 0, h.tanggal)
            ws.style(row, 0).format(FORMAT_TANGGAL).set()
            uang(ws, row, 1, h.pemasukan)
            uang(ws, row, 2, h.pengeluaran)
            uang(ws, row, 3, h.selisih)
            uang(ws, row, 4, h.saldoKumulatif)
        }

        ws.setAutoFilter(0, 0, l.harian.size, kolom.lastIndex)

        ws.width(0, 12.0)
        (1..4).forEach { ws.width(it, 16.0) }
        ws.freezePane(0, 1)
    }

    private fun uang(ws: Worksheet, r: Int, c: Int, nilai: Long) {
        ws.value(r, c, nilai)
        ws.style(r, c).format(FORMAT_RUPIAH).set()
    }

    private fun labelTipe(t: TipeTransaksi) =
        if (t == TipeTransaksi.PEMASUKAN) "Pemasukan" else "Pengeluaran"
}
