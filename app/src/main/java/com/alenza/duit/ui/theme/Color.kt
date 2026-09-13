package com.alenza.duit.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Nilai warna diambil persis dari papan Design Token.
 * Jangan membulatkan atau "memperbaiki" nilainya — semua pasangan
 * teks/latar di bawah sudah diukur lolos kontras WCAG AA.
 *
 * Rebrand "Duitku" (lihat docs/duitpribadi-splash): `Aksen*` — sebelumnya
 * hijau-teal — sekarang biru, warna identitas brand yang dipakai sebagai
 * `primary` Material di seluruh layar (tombol, FAB, chip terpilih, tautan).
 * `Pemasukan*` dipisah jadi konstanta sendiri memakai nilai hijau lama supaya
 * arti warna hijau/merah pemasukan-pengeluaran tetap seperti sebelumnya —
 * lihat catatan di Theme.kt. `Magenta*`/`UnguGradien` adalah warna sekunder
 * brand, dipakai di splash (blob, judul, page indicator); tidak dipakai
 * sebagai teks kecuali `MagentaTeks` yang sudah diukur lolos kontras AA.
 */

// ── Mode terang ─────────────────────────────────────────────
val LatarTerang        = Color(0xFFF7F6F3)
val KartuTerang        = Color(0xFFFFFFFF)
val TeksUtamaTerang    = Color(0xFF16181B)
val TeksSekunderTerang = Color(0xFF5C6169)
val TeksRedupTerang    = Color(0xFF6B7076)
val GarisTerang        = Color(0xFFEDEBE5)
val TrackTerang        = Color(0xFFF2F0EB)
val AksenTerang        = Color(0xFF1152E4)
val AksenLembutTerang  = Color(0xFFE8EFFF)
val NegatifTerang      = Color(0xFFC0453B)
val NegatifLembut      = Color(0xFFFAEBE9)
val PeringatanTerang   = Color(0xFFD9A441)
val PeringatanLatar    = Color(0xFFFDF6E8)
val PeringatanTeks     = Color(0xFF8A6A21)

// Hijau pemasukan — nilai persis dari `AksenTerang`/`AksenLembutTerang` lama,
// dipisah supaya rebrand warna primer tidak ikut mengubah arti warna pemasukan.
val HijauPemasukanTerang       = Color(0xFF0F7B62)
val HijauPemasukanLembutTerang = Color(0xFFEAF3F0)

// ── Mode gelap ──────────────────────────────────────────────
val LatarGelap        = Color(0xFF101214)
val KartuGelap        = Color(0xFF191C1F)
val TeksUtamaGelap    = Color(0xFFECEDEE)
val TeksSekunderGelap = Color(0xFFA0A5AB)
val TeksRedupGelap    = Color(0xFF8F949B)
val GarisGelap        = Color(0xFF2A2E33)
val TrackGelap        = Color(0xFF262A2E)
val AksenGelap        = Color(0xFF4F8BFF)
val AksenLembutGelap  = Color(0xFF0D1B33)
val PadaAksenGelap    = Color(0xFF081733)   // teks di atas aksen mode gelap
val NegatifGelap      = Color(0xFFE0796C)
val NegatifLembutGlp  = Color(0xFF33211F)

// Hijau pemasukan mode gelap — nilai persis dari `AksenGelap`/`AksenLembutGelap` lama.
val HijauPemasukanGelap       = Color(0xFF34C79E)
val HijauPemasukanLembutGelap = Color(0xFF10312A)

// ── Brand sekunder (splash & aksen dekoratif) ──────────────────
// Dipakai untuk blob & judul splash (SplashScreen.kt). `Magenta`/`MagentaMuda`/
// `MagentaLembut`/`UnguGradien` murni dekoratif (tanpa teks di atasnya) — tidak
// perlu lolos kontras AA. `MagentaTeks` sudah diukur: 4,54:1 di atas `LatarTerang`
// dan 4,91:1 di atas putih, keduanya lolos AA teks normal (≥4,5:1).
val Magenta       = Color(0xFFF5297E)
val MagentaMuda   = Color(0xFFFF5C9E)
val MagentaLembut = Color(0xFFFFC2DA)
val MagentaTeks   = Color(0xFFD81B69)
val UnguGradien   = Color(0xFF8A3BD1)

/**
 * Warna yang tidak punya slot di Material 3 ColorScheme.
 * Diakses lewat [LocalWarna] — lihat Theme.kt.
 */
@Immutable
data class WarnaTambahan(
    val teksRedup: Color,
    val track: Color,
    val pemasukan: Color,
    val pemasukanLembut: Color,
    val pengeluaran: Color,
    val pengeluaranLembut: Color,
    val peringatan: Color,
    val peringatanLatar: Color,
    val peringatanTeks: Color,
)

val WarnaTambahanTerang = WarnaTambahan(
    teksRedup = TeksRedupTerang,
    track = TrackTerang,
    pemasukan = HijauPemasukanTerang,
    pemasukanLembut = HijauPemasukanLembutTerang,
    pengeluaran = NegatifTerang,
    pengeluaranLembut = NegatifLembut,
    peringatan = PeringatanTerang,
    peringatanLatar = PeringatanLatar,
    peringatanTeks = PeringatanTeks,
)

val WarnaTambahanGelap = WarnaTambahan(
    teksRedup = TeksRedupGelap,
    track = TrackGelap,
    pemasukan = HijauPemasukanGelap,
    pemasukanLembut = HijauPemasukanLembutGelap,
    pengeluaran = NegatifGelap,
    pengeluaranLembut = NegatifLembutGlp,
    peringatan = PeringatanTerang,
    peringatanLatar = Color(0xFF2A2314),
    peringatanTeks = Color(0xFFE6C888),
)

/*
 * Kartu aksi melayang (urungkan hapus di §3, ekspor selesai di §7). Selalu gelap
 * di kedua mode — seperti Snackbar inverse-surface. Nilai persis dari design-spec.
 */
val OverlayGelap     = Color(0xFF22262A)
val OverlayTeks      = Color(0xFFECEDEE)
val OverlayTeksRedup = Color(0xFF9AA0A6)

/** Kotak "XLS" di kartu nama file ekspor (design-spec §6). */
val HijauXls = Color(0xFF1E7B4E)

val LocalWarna = staticCompositionLocalOf { WarnaTambahanTerang }

internal val SkemaTerang = lightColorScheme(
    primary = AksenTerang,
    onPrimary = Color.White,
    primaryContainer = AksenLembutTerang,
    onPrimaryContainer = AksenTerang,
    background = LatarTerang,
    onBackground = TeksUtamaTerang,
    surface = KartuTerang,
    onSurface = TeksUtamaTerang,
    surfaceVariant = GarisTerang,
    onSurfaceVariant = TeksSekunderTerang,
    outline = GarisTerang,
    outlineVariant = TrackTerang,
    error = NegatifTerang,
    onError = Color.White,
    errorContainer = NegatifLembut,
    onErrorContainer = NegatifTerang,
)

internal val SkemaGelap = darkColorScheme(
    primary = AksenGelap,
    onPrimary = PadaAksenGelap,
    primaryContainer = AksenLembutGelap,
    onPrimaryContainer = AksenGelap,
    background = LatarGelap,
    onBackground = TeksUtamaGelap,
    surface = KartuGelap,
    onSurface = TeksUtamaGelap,
    surfaceVariant = GarisGelap,
    onSurfaceVariant = TeksSekunderGelap,
    outline = GarisGelap,
    outlineVariant = TrackGelap,
    error = NegatifGelap,
    onError = PadaAksenGelap,
    errorContainer = NegatifLembutGlp,
    onErrorContainer = NegatifGelap,
)
