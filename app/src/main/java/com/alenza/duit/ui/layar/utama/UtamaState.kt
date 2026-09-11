package com.alenza.duit.ui.layar.utama

import com.alenza.duit.data.RekapKategori
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.data.Transaksi
import java.time.LocalDate
import java.time.YearMonth

/**
 * State Layar utama, sudah siap tampil — composable tidak menyentuh Room.
 * Sumber kebenaran tetap di Room; ini hasil olahan yang di-hoist dari
 * [UtamaViewModel].
 */
data class UtamaState(
    val bulan: YearMonth,
    /** "Agustus 2026" — sudah diformat Bahasa Indonesia. */
    val labelBulan: String,
    val bisaMundur: Boolean,
    val bisaMaju: Boolean,
    /** Pemasukan − pengeluaran bulan ini. Bisa negatif. */
    val sisaBulan: Long,
    val totalPemasukan: Long,
    val totalPengeluaran: Long,
    /** Urut nominal terbesar; sudah dibulatkan persennya (metode sisa terbesar). */
    val rincian: List<RekapKategori>,
    /** Transaksi dikelompokkan per hari, hari terbaru dulu. */
    val hari: List<GrupHari>,
    /** Penghapusan yang masih bisa diurungkan (design-spec §3), atau null. */
    val undo: HapusTertunda?,
    /**
     * Pesan konfirmasi sederhana ("Transaksi ditambahkan"/"diperbarui") — beda
     * dari [undo], ini tak bisa diurungkan. `null` kalau tak ada yang tampil.
     * Kalau keduanya terisi bersamaan (jarang terjadi), [undo] didahulukan.
     */
    val pesan: String?,
    /** Bilah "Data hanya tersimpan di HP ini" (design-spec §9) belum ditutup. */
    val tampilkanInfoLokal: Boolean,
    /** Data grafik pengeluaran & saldo — harian (periode ini) & bulanan (tren). */
    val grafik: DataGrafik,
    val memuat: Boolean,
) {
    val kosong: Boolean get() = !memuat && hari.isEmpty()

    companion object {
        fun awal(bulan: YearMonth, label: String) = UtamaState(
            bulan = bulan,
            labelBulan = label,
            bisaMundur = false,
            bisaMaju = false,
            sisaBulan = 0L,
            totalPemasukan = 0L,
            totalPengeluaran = 0L,
            rincian = emptyList(),
            hari = emptyList(),
            undo = null,
            pesan = null,
            tampilkanInfoLokal = false,
            grafik = DataGrafik.kosong(),
            memuat = true,
        )
    }
}

/** Satu titik pada grafik batang/garis: label sumbu-X + nilai rupiah. */
data class TitikGrafik(val label: String, val nilai: Long)

/** Rentang waktu yang ditampilkan grafik — dipilih lewat toggle di layar. */
enum class RentangGrafik { HARIAN, BULANAN }

/**
 * Data untuk grafik pengeluaran & grafik saldo tersisa di Layar utama.
 * "Harian" = tiap hari dalam periode yang sedang dilihat (saldo dihitung
 * kumulatif dari awal periode, sama seperti sheet "Harian" di ekspor Excel).
 * "Bulanan" = satu titik per periode untuk beberapa periode terakhir; saldo
 * bulanan BUKAN kumulatif lintas periode — tiap titik berdiri sendiri sebagai
 * pemasukan−pengeluaran periode itu saja, sama seperti arti "Sisa bulan ini".
 */
data class DataGrafik(
    val pengeluaranHarian: List<TitikGrafik>,
    val pengeluaranBulanan: List<TitikGrafik>,
    val saldoHarian: List<TitikGrafik>,
    val saldoBulanan: List<TitikGrafik>,
) {
    companion object {
        fun kosong() = DataGrafik(emptyList(), emptyList(), emptyList(), emptyList())
    }
}

/**
 * Transaksi yang baru dihapus dari Room tapi masih dalam jendela 5 detik untuk
 * diurungkan. [transaksi] disimpan utuh (termasuk id) supaya `pulihkan` bisa
 * mengembalikannya persis seperti semula.
 */
data class HapusTertunda(
    val transaksi: Transaksi,
    val namaKategori: String,
)

data class GrupHari(
    val tanggal: LocalDate,
    /** "Hari ini" / "Kemarin" / "Sen, 25 Agu 2026". */
    val label: String,
    val subtotalPengeluaran: Long,
    val transaksi: List<BarisTransaksi>,
)

data class BarisTransaksi(
    val id: Long,
    val namaKategori: String,
    val iconKey: String,
    val colorHex: String,
    val catatan: String?,
    val nominal: Long,
    val tipe: TipeTransaksi,
)
