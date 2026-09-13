package com.alenza.duit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Catatan sadar: PRD §8 menyebut Dynamic Color (Material You) di Android 12+,
 * tapi desainnya berpijak pada satu aksen tetap (#1152E4 / #4F8BFF, brand
 * "Duitku") dan palet kategori dengan kroma setara. Dynamic Color akan
 * menimpa keduanya.
 *
 * Default di sini: Dynamic Color MATI. Kalau nanti dinyalakan, batasi hanya ke
 * warna latar/permukaan dan biarkan aksen + palet kategori tetap dari file ini —
 * jangan biarkan wallpaper mengubah arti warna merah/hijau pemasukan-pengeluaran.
 */
@Composable
fun DuitTheme(
    gelap: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val skema = if (gelap) SkemaGelap else SkemaTerang
    val tambahan = if (gelap) WarnaTambahanGelap else WarnaTambahanTerang

    CompositionLocalProvider(LocalWarna provides tambahan) {
        MaterialTheme(
            colorScheme = skema,
            typography = TipografiDuit,
            content = content,
        )
    }
}

/** Pintasan: `Warna.pengeluaran` di dalam composable. */
object Warna {
    val current
        @Composable get() = LocalWarna.current
}
