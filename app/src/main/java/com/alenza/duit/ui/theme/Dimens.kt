package com.alenza.duit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Skala spasi. Jangan pakai angka di luar daftar ini tanpa alasan. */
object Spasi {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    /** Padding kiri-kanan semua layar. */
    val layar = 20.dp
    /** Jarak antar blok (kartu ringkasan → kategori → daftar). */
    val antarBlok = 18.dp
}

/** Tebal garis / border. */
object Garis {
    val tipis = 1.dp
    val tebal = 1.5.dp
}

object Sudut {
    val chip = RoundedCornerShape(12.dp)
    val field = RoundedCornerShape(16.dp)
    val baris = RoundedCornerShape(22.dp)
    val kartu = RoundedCornerShape(24.dp)
    val sheet = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    val pil = RoundedCornerShape(percent = 50)
    val peringatan = RoundedCornerShape(14.dp)
    /** Tile logo brand di splash (docs/duitpribadi-splash). */
    val logo = RoundedCornerShape(34.dp)
}

object Ukuran {
    /** Target sentuh minimum — PRD §8. Jangan turunkan. */
    val sentuhMin = 48.dp

    val tombolIkon = 40.dp
    val ikon = 20.dp

    val fab = 60.dp
    val fabIkon = 26.dp
    val fabSudut = 22.dp

    val tombolUtama = 54.dp
    val tombolUtamaSudut = 18.dp

    /*
     * Tinggi visual dari design-spec §2. Area sentuh komponennya tetap dinaikkan
     * ke `sentuhMin` (48 dp) — CLAUDE.md aturan 9. Segmented control memakai
     * `sentuhMin` langsung sebagai tinggi track.
     */
    val chipTinggi = 38.dp
    val fieldTinggi = 46.dp

    /** Lingkaran ikon di baris transaksi / kategori. */
    val ikonBaris = 36.dp
    val ikonBarisSudut = 13.dp
    val ikonKategori = 26.dp
    val ikonKategoriSudut = 9.dp

    val tombolKeypad = 56.dp
    val barProporsi = 5.dp

    /** Tinggi app bar / header custom (Layar utama & form). */
    val appBarTinggi = 48.dp
    /** Glyph ikon di app bar / header — cocok dengan grid 24 vector drawable. */
    val ikonAppBar = 24.dp
    /** Ikon di dalam lingkaran kecil (26 dp) blok rincian kategori. */
    val ikonDalamKategori = 14.dp
    /** Lingkaran ikon di kartu ringkasan. */
    val ikonRingkasan = 30.dp
    val ikonRingkasanSudut = 11.dp
}

/** Nilai spesifik Layar utama (design-spec §1). Ambil dari sini, jangan tulis mentah. */
object LayarUtama {
    val headerTinggi = 48.dp
    val labelBulanMin = 112.dp

    val ringkasanPemisahAtas = 18.dp
    val ringkasanPemisahBawah = 16.dp

    val rincianIsi = 16.dp
    val rincianAntarBaris = 15.dp
    val rincianJarakBar = 6.dp
    val persenLebar = 32.dp

    val kartuBarisIsi = 5.dp
    val barisTxVertikal = 11.dp
    val barisTxHorizontal = 12.dp
    val barisIkonJarak = 12.dp

    val fabMarginBawah = 34.dp
    val gradienTinggi = 120.dp

    val ikonKosong = 64.dp
}

/** Grafik pengeluaran & saldo di Layar utama (fitur alert/grafik, bukan design-spec asli). */
object LayarUtamaGrafik {
    val tinggiBatang = 130.dp
    val tinggiGaris = 130.dp
    val jarakBar = 3.dp
    val barSudut = 3.dp
    val titikRadius = 3.dp
    val garisTebal = 2.dp
    val tinggiLabelSumbu = 18.dp
    /** Tinggi toggle Harian/Bulanan — sama seperti target sentuh minimum. */
    val toggleTinggi = Ukuran.sentuhMin
}

/** Kartu aksi melayang + geser-untuk-hapus (design-spec §3). */
object KartuAksi {
    val sudut = 18.dp
    val progresTinggi = 3.dp
    val marginBawah = 16.dp
    /** Lebar panel merah yang muncul saat baris digeser ke kiri. */
    val panelGeserHapus = 96.dp
}

/** Nilai spesifik layar Kelola kategori (design-spec §4). */
object LayarKategori {
    val ikonBaris = 34.dp
    val ikonBarisSudut = 12.dp
}

/** Nilai spesifik Form kategori (design-spec §5). */
object FormKategori {
    val pratinjauKotak = 76.dp
    val pratinjauSudut = 28.dp
    val pratinjauIkon = 34.dp
    val namaFieldTinggi = 52.dp
    val ikonTile = 52.dp
    val ikonTileSudut = 16.dp
    val warnaLingkaran = 44.dp
    val warnaCincin = 3.dp
    val gridKolom = 6
}

/** Nilai spesifik Pengaturan (design-spec §8). */
object LayarPengaturan {
    val ikonKotak = 32.dp
    val ikonKotakSudut = 11.dp
    val pemisahMulaiDari = 59.dp
}

/** Nilai spesifik bottom sheet Ekspor (design-spec §6 & §7). */
object LayarEkspor {
    val handleLebar = 38.dp
    val handleTinggi = 4.dp
    val kotakXls = 36.dp
    val progresTinggi = 6.dp
    val tombolTinggi = 50.dp
    val peringatanSudut = 14.dp
}

/** Nilai spesifik Splash — brand "Duitku" (docs/duitpribadi-splash). */
object LayarSplash {
    val logoUkuran = 136.dp
    val logoElevasi = 28.dp

    /** Jarak logo → judul dan padding kiri-kanan layar — sama seperti [Spasi.xxxl]. */
    val spasiKontenAtas get() = Spasi.xxxl
    val spasiJudulTagline = 14.dp

    val indikatorPilLebar = 26.dp
    val indikatorPilTinggi = 7.dp
    val indikatorDot = 7.dp
    /** Jarak antar elemen page indicator — sama seperti [Spasi.s]. */
    val indikatorJarak get() = Spasi.s
    val indikatorMarginBawah = 64.dp
}

/** Nilai spesifik layar Tambah / Ubah transaksi (design-spec §2). */
object LayarTambah {
    /** Kursor berkedip setelah nominal — batang 2,5 × 34 dp. */
    val kursorLebar = 2.5.dp
    val kursorTinggi = 34.dp
    /** Titik warna kategori di dalam chip. */
    val titikKategori = 9.dp

    /**
     * Ambang tinggi konten — di bawahnya layar dianggap "pendek" (edge case:
     * layar tak boleh scroll, jadi jarak antar blok dipadatkan ke token [Spasi]
     * yang lebih kecil supaya keypad & tombol Simpan tetap utuh tanpa terpotong).
     */
    val ambangPendek = 620.dp
}
