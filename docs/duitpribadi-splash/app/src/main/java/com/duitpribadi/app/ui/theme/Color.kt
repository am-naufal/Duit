package com.duitpribadi.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Palet "Duit Pribadi" — diambil dari mockup (gradien biru → magenta).
 */
object DuitColor {
    // Biru
    val BlueDeep   = Color(0xFF1152E4)
    val Blue       = Color(0xFF2563FF)
    val BlueLight  = Color(0xFF4F8BFF)
    val BlueSoft   = Color(0xFFBBD3FF)

    // Magenta / pink
    val Pink       = Color(0xFFF5297E)
    val PinkLight  = Color(0xFFFF5C9E)
    val PinkSoft   = Color(0xFFFFC2DA)

    // Ungu (titik tengah gradien)
    val Purple     = Color(0xFF8A3BD1)

    // Netral
    val Background = Color(0xFFF7F9FE)
    val Surface    = Color(0xFFFFFFFF)
    val TextMuted  = Color(0xFF6B7280)
    val Divider    = Color(0xFFE6EAF2)

    // Dark mode
    val BackgroundDark = Color(0xFF0E1116)
    val SurfaceDark    = Color(0xFF161A22)
    val TextMutedDark  = Color(0xFF9CA3AF)
}

/** Gradien utama: biru (kiri-atas) → magenta (kanan-bawah). */
val BrandGradient = Brush.linearGradient(
    colors = listOf(DuitColor.BlueDeep, DuitColor.Blue, DuitColor.Pink),
    start = androidx.compose.ui.geometry.Offset(0f, 0f),
    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

/** Gradien untuk teks judul "Duit Pribadi". */
val TitleGradient = Brush.linearGradient(
    colors = listOf(DuitColor.BlueDeep, DuitColor.Pink)
)
