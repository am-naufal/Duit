package com.alenza.duit

import com.alenza.duit.data.Kategori
import com.alenza.duit.data.Periode
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.data.Transaksi
import com.alenza.duit.data.ekspor.PenulisXlsx
import com.alenza.duit.data.ekspor.RakitLaporan
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileOutputStream
import java.nio.file.Files
import java.time.YearMonth
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Mengunci validitas STRUKTUR file .xlsx, bukan cuma angkanya (itu tugas
 * [LaporanTest]). Tes angka lain tak pernah membuka file yang sungguhan
 * ditulis [PenulisXlsx] — celah inilah yang membuat bug nyata lolos dari
 * `assembleDebug`/`testDebugUnitTest`/`lintDebug` sampai ditemukan lewat
 * verifikasi manual B-5 (docs/keputusan-tertunda.md): `FORMAT_RUPIAH` yang
 * memakai teks literal berkutip (`"Rp"`) membuat fastexcel 0.18.4 menulis
 * `formatCode=""Rp"#,##0…"` ke `xl/styles.xml` — tanda kutip di dalamnya tak
 * di-escape, memutus atribut XML itu sendiri dan membuat seluruh file .xlsx
 * gagal dibuka ("file rusak") di Excel/Sheets/LibreOffice manapun.
 */
class PenulisXlsxTest {

    private fun kategori(id: Long, nama: String, tipe: TipeTransaksi) =
        Kategori(id = id, nama = nama, tipe = tipe, iconKey = "lainnya", colorHex = "#94989E", urutan = 0)

    /** Setiap entry XML di dalam .xlsx harus well-formed — memutus atribut/tag = file rusak. */
    @Test
    fun setiap_bagian_xml_di_dalam_xlsx_well_formed() {
        val gaji = kategori(1, "Gaji", TipeTransaksi.PEMASUKAN)
        val makan = kategori(2, "Makan & Minum", TipeTransaksi.PENGELUARAN)
        val transaksi = listOf(
            Transaksi(
                id = 1, nominal = 5_000_000, tipe = TipeTransaksi.PEMASUKAN, kategoriId = gaji.id,
                tanggal = YearMonth.of(2026, 9).atDay(1).toEpochDay(), dibuatPada = 1, diubahPada = 1,
            ),
            Transaksi(
                id = 2, nominal = 50_000, tipe = TipeTransaksi.PENGELUARAN, kategoriId = makan.id,
                tanggal = YearMonth.of(2026, 9).atDay(2).toEpochDay(), dibuatPada = 2, diubahPada = 2,
                // Koma, kutip ganda, ampersand, sudut siku, apostrof, emoji (termasuk surrogate
                // pair) — celah escaping yang biasa memutus XML/format code kalau tak ditangani.
                catatan = """Beli, "diskon" 25% <bumbu> & teman's shop 😀""",
            ),
        )
        val periode = Periode.dariLabel(YearMonth.of(2026, 9), hariAwalBulan = 1)
        val laporan = RakitLaporan.dari(periode, transaksi, listOf(gaji, makan))

        // File sungguhan di disk, dibaca lewat ZipFile (akses direktori-pusat) —
        // sama seperti cara openpyxl/kebanyakan pembaca .xlsx sungguhan membuka
        // zip, beda dari ZipInputStream yang murni sekuensial lewat header lokal.
        val berkas = Files.createTempFile("penulis-xlsx-test", ".xlsx").toFile()
        berkas.deleteOnExit()
        FileOutputStream(berkas).use { PenulisXlsx.tulis(laporan, it) }

        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        var jumlahXmlDiperiksa = 0
        ZipFile(berkas).use { zip ->
            zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.endsWith(".xml") }
                .forEach { entry ->
                    zip.getInputStream(entry).use { isi ->
                        // parse() melempar SAXParseException kalau XML-nya tak well-formed —
                        // itulah yang menangkap bug formatCode berkutip di atas.
                        builder.parse(isi)
                    }
                    jumlahXmlDiperiksa++
                }
        }
        assertTrue("Tak ada entry .xml yang diperiksa — tes ini salah setup", jumlahXmlDiperiksa > 0)
    }
}
