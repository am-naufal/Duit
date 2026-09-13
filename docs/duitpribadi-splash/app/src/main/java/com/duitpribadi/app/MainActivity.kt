package com.duitpribadi.app

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.duitpribadi.app.ui.splash.SplashScreen
import com.duitpribadi.app.ui.theme.DuitPribadiTheme

class MainActivity : ComponentActivity() {

    /** Ganti jadi state dari ViewModel (mis. load DB Room) kalau sudah ada. */
    private var appReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // WAJIB dipanggil sebelum setContent()
        val splash = installSplashScreen()

        // Tahan splash sistem sampai data awal siap (maks. beberapa ratus ms).
        splash.setKeepOnScreenCondition { !appReady }

        // Exit animation: ikon sistem menyusut & fade, lalu diserahkan ke Compose.
        splash.setOnExitAnimationListener { provider ->
            val fade = ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f).apply {
                duration = 250L
                interpolator = AnticipateInterpolator()
            }
            val shrink = ObjectAnimator.ofFloat(provider.view, View.SCALE_X, 1f, 0.85f).apply {
                duration = 250L
            }
            fade.doOnEnd { provider.remove() }
            shrink.start()
            fade.start()
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TODO: ganti dengan inisialisasi asli (buka database, baca preferensi, dll)
        appReady = true

        setContent {
            DuitPribadiTheme {
                var showSplash by remember { mutableStateOf(true) }

                AnimatedVisibility(
                    visible = showSplash,
                    exit = fadeOut(androidx.compose.animation.core.tween(350))
                ) {
                    SplashScreen(
                        holdMillis = 1600L,
                        onFinished = { showSplash = false }
                    )
                }

                AnimatedVisibility(
                    visible = !showSplash,
                    enter = fadeIn(androidx.compose.animation.core.tween(400)) +
                        scaleIn(initialScale = 0.98f)
                ) {
                    // TODO: ganti dengan NavHost / HomeScreen milikmu
                    HomePlaceholder()
                }
            }
        }
    }
}
