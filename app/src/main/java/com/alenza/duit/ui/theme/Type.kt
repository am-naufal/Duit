package com.alenza.duit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.alenza.duit.R

/*
 * Plus Jakarta Sans, di-bundle sebagai resource — bukan Downloadable Fonts.
 * Alasannya: aplikasi ini sengaja tanpa jaringan, jadi font pun sebaiknya
 * tidak bergantung pada layanan lain. Ukurannya ~400 KB untuk 4 bobot.
 *
 * Berkas diambil dari docs/Plus_Jakarta_Sans/static/ (SIL Open Font License):
 *   PlusJakartaSans-Regular.ttf  → res/font/plus_jakarta_sans_regular.ttf  (400)
 *   PlusJakartaSans-Medium.ttf   → res/font/plus_jakarta_sans_medium.ttf   (500)
 *   PlusJakartaSans-SemiBold.ttf → res/font/plus_jakarta_sans_semibold.ttf (600)
 *   PlusJakartaSans-Bold.ttf     → res/font/plus_jakarta_sans_bold.ttf     (700)
 *
 * OFL.txt ada di app/src/main/assets/ — lisensinya mewajibkan.
 */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

/** Gaya dasar untuk angka rupiah — tabular figures supaya kolom tidak bergoyang. */
val Angka = TextStyle(
    fontFamily = PlusJakartaSans,
    fontFeatureSettings = "tnum",
)

private fun gaya(
    size: Int,
    weight: FontWeight,
    lineHeight: Int,
    tracking: Float = 0f,
    tabular: Boolean = false,
) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = size.sp,
    fontWeight = weight,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
    fontFeatureSettings = if (tabular) "tnum" else null,
)

val TipografiDuit = Typography(
    // Nominal saat input — 40 / 700 / -3,5%
    displayMedium = gaya(40, FontWeight.Bold, 46, -0.035f, tabular = true),
    // Saldo di kartu ringkasan — 34 / 700 / -3%
    displaySmall  = gaya(34, FontWeight.Bold, 40, -0.030f, tabular = true),
    // Judul bulan di header — 17 / 600
    titleLarge    = gaya(17, FontWeight.SemiBold, 24, -0.010f),
    // Judul layar di app bar — 16 / 600
    titleMedium   = gaya(16, FontWeight.SemiBold, 22, -0.010f),
    // Baris utama, nama kategori — 15 / 600
    bodyLarge     = gaya(15, FontWeight.SemiBold, 20),
    // Baris pendukung, catatan — 13 / 400
    bodyMedium    = gaya(13, FontWeight.Normal, 18),
    // Label di dalam kartu — 13 / 500
    labelLarge    = gaya(13, FontWeight.Medium, 18),
    // Label grup di Pengaturan — 12 / 700 / +8%
    labelSmall    = gaya(12, FontWeight.Bold, 16, 0.080f),
)

/*
 * Gaya di luar slot Typography Material — dipakai untuk ukuran yang disebut
 * eksplisit di docs/design-spec.md tapi tidak punya slot sendiri. Nama, bukan
 * angka mentah di composable (CLAUDE.md). Nilai 14,5/12,5 sp dari spec
 * dibulatkan ke 15/13 karena `gaya()` memakai bilangan bulat.
 */
val GayaNamaBaris      = gaya(15, FontWeight.SemiBold, 20)                 // nama kategori, baris transaksi
val GayaNamaRincian    = gaya(14, FontWeight.Medium, 18)                  // nama, blok "per kategori"
val GayaNominalBaris   = gaya(15, FontWeight.SemiBold, 20, tabular = true)
val GayaNominalRincian = gaya(14, FontWeight.SemiBold, 18, tabular = true)
val GayaAngkaKecil     = gaya(12, FontWeight.Normal, 16, tabular = true)   // persen, subtotal hari
val GayaCatatan        = gaya(13, FontWeight.Normal, 18)                  // catatan transaksi
val GayaKolomLabel     = gaya(12, FontWeight.Medium, 16)                  // "Masuk" / "Keluar" di kartu ringkasan
val GayaLabelNominal   = gaya(12, FontWeight.SemiBold, 16, 0.02f)         // label "NOMINAL" di form transaksi
val GayaKeypad         = gaya(23, FontWeight.Medium, 28)                  // angka di keypad milik aplikasi

/*
 * Splash (docs/duitpribadi-splash). Referensi memakai FontWeight.ExtraBold
 * (800) untuk judul, tapi Plus Jakarta Sans di proyek ini cuma di-bundle
 * 4 bobot (400/500/600/700) — dipakai Bold (700) supaya tidak jatuh ke bold
 * sintetis.
 */
val GayaJudulSplash   = gaya(38, FontWeight.Bold, 44, -0.020f)
val GayaTaglineSplash = gaya(15, FontWeight.Medium, 24)
