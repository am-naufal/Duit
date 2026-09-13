package com.alenza.duit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alenza.duit.data.Preferensi
import com.alenza.duit.data.Tema
import com.alenza.duit.ui.DuitNav
import com.alenza.duit.ui.layar.splash.SplashScreen
import com.alenza.duit.ui.theme.DuitTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Wajib sebelum super.onCreate()/setContent() — lihat SplashScreen.kt
        // & Theme.Duit.Starting di themes.xml (docs/duitpribadi-splash).
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferensi = Preferensi.dari(this)
        // Baca sekali secara sinkron sebelum frame pertama supaya tema pilihan
        // pengguna (bukan hasil tebakan `Tema.SISTEM`) langsung benar — tanpa ini
        // ada kilatan singkat sebelum DataStore selesai dibaca secara async.
        val prefAwal = runBlocking { preferensi.aliran.first() }
        setContent {
            val pref by preferensi.aliran.collectAsState(initial = prefAwal)
            val gelap = when (pref.tema) {
                Tema.SISTEM -> isSystemInDarkTheme()
                Tema.TERANG -> false
                Tema.GELAP -> true
            }
            var tampilkanSplash by remember { mutableStateOf(true) }

            DuitTheme(gelap = gelap) {
                Box(Modifier.fillMaxSize()) {
                    // Splash Compose (animasi brand) mengambil alih dari splash
                    // sistem begitu frame pertama tampil, lalu serah-terima ke
                    // DuitNav dengan fade + scale halus.
                    if (tampilkanSplash) {
                        SplashScreen(gelap = gelap, onFinished = { tampilkanSplash = false })
                    }
                    AnimatedVisibility(
                        visible = !tampilkanSplash,
                        enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.98f, animationSpec = tween(400)),
                    ) {
                        DuitNav()
                    }
                }
            }
        }
    }
}
