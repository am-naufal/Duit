package com.alenza.duit.ui.layar.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.alenza.duit.R
import com.alenza.duit.ui.theme.AksenGelap
import com.alenza.duit.ui.theme.AksenLembutTerang
import com.alenza.duit.ui.theme.AksenTerang
import com.alenza.duit.ui.theme.DuitTheme
import com.alenza.duit.ui.theme.LayarSplash
import com.alenza.duit.ui.theme.Magenta
import com.alenza.duit.ui.theme.MagentaLembut
import com.alenza.duit.ui.theme.MagentaMuda
import com.alenza.duit.ui.theme.MagentaTeks
import com.alenza.duit.ui.theme.Sudut
import com.alenza.duit.ui.theme.Warna
import com.alenza.duit.ui.theme.GayaJudulSplash
import com.alenza.duit.ui.theme.GayaTaglineSplash
import kotlinx.coroutines.delay

/**
 * Splash brand "Duitku" (docs/duitpribadi-splash).
 *
 * Susunan: background terang/gelap dari tema, blob gradien biru (kiri atas) &
 * magenta (kanan bawah), tile logo dompet, judul dua warna "Duit" + "ku",
 * tagline, dan page indicator. Splash sistem (`Theme.Duit.Starting` di
 * themes.xml) menutup jeda sebelum frame Compose pertama; composable ini
 * mengambil alih dengan animasi penuh begitu tampil.
 *
 * @param gelap sama seperti parameter `gelap` pada [DuitTheme] — dioper
 *   eksplisit dari MainActivity, bukan ditebak dari [MaterialTheme], supaya
 *   konsisten dengan preferensi tema pengguna (Tema.TERANG/GELAP override
 *   sistem).
 * @param onFinished dipanggil setelah animasi + [holdMillis] selesai.
 */
@Composable
fun SplashScreen(
    gelap: Boolean,
    modifier: Modifier = Modifier,
    holdMillis: Long = 1600L,
    onFinished: () -> Unit = {},
) {
    val logoScale = remember { Animatable(0.72f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
    LaunchedEffect(Unit) {
        delay(220)
        textAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(220)
        textOffset.animateTo(0f, tween(480, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(holdMillis)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        BrandBlobs(gelap = gelap)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = LayarSplash.spasiKontenAtas),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ---- Logo ----
            Box(
                modifier = Modifier
                    .size(LayarSplash.logoUkuran)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .shadow(
                        elevation = LayarSplash.logoElevasi,
                        shape = Sudut.logo,
                        ambientColor = AksenTerang,
                        spotColor = Magenta,
                    )
                    .clip(Sudut.logo),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo_duitku),
                    contentDescription = "Logo Duitku",
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(Modifier.height(LayarSplash.spasiKontenAtas))

            // ---- Judul dua warna: "Duit" + "ku" ----
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Duit") }
                    withStyle(SpanStyle(color = if (gelap) MagentaMuda else MagentaTeks)) { append("ku") }
                },
                style = GayaJudulSplash,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offsetY(textOffset.value),
            )

            Spacer(Modifier.height(LayarSplash.spasiJudulTagline))

            // ---- Tagline ----
            Text(
                text = "Kelola keuanganmu,\nraih masa depanmu",
                style = GayaTaglineSplash,
                color = Warna.current.teksRedup,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offsetY(textOffset.value),
            )
        }

        // ---- Page indicator ----
        PageIndicator(
            gelap = gelap,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = LayarSplash.indikatorMarginBawah)
                .alpha(textAlpha.value),
        )
    }
}

/** Blob gradien dekoratif di sudut layar. Murni dekoratif — tanpa teks di atasnya. */
@Composable
private fun BrandBlobs(gelap: Boolean) {
    val transition = rememberInfiniteTransition(label = "blob")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )
    val alphaFactor = if (gelap) 0.55f else 1f

    // Stop gradien biru: sengaja ditukar posisi antar tema (bukan dua warna
    // baru) supaya blob tetap terang & jadi aksen — bukan cuma nada gelap
    // di atas latar gelap. AksenLembutTerang dipakai murni dekoratif di sini.
    val biruDalam = if (gelap) AksenGelap else AksenTerang
    val biruMuda = if (gelap) AksenLembutTerang else AksenGelap

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val d = drift * 18f

        // Blob biru — kiri atas
        val biru = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.78f, 0f)
            cubicTo(
                w * 0.62f, h * 0.09f + d,
                w * 0.46f, h * 0.10f - d,
                w * 0.30f, h * 0.17f + d,
            )
            cubicTo(
                w * 0.14f, h * 0.24f,
                w * 0.05f, h * 0.20f,
                0f, h * 0.26f,
            )
            close()
        }
        drawPath(
            path = biru,
            brush = Brush.linearGradient(
                colors = listOf(
                    biruDalam.copy(alpha = alphaFactor),
                    biruMuda.copy(alpha = alphaFactor),
                ),
                start = Offset(0f, 0f),
                end = Offset(w * 0.8f, h * 0.25f),
            ),
        )

        // Aksen biru muda tipis di bawah blob
        val biruSoft = Path().apply {
            moveTo(0f, h * 0.02f)
            cubicTo(w * 0.22f, h * 0.16f, w * 0.30f, h * 0.20f, w * 0.52f, h * 0.20f)
            cubicTo(w * 0.30f, h * 0.28f - d, w * 0.16f, h * 0.30f, 0f, h * 0.33f)
            close()
        }
        drawPath(biruSoft, color = biruMuda.copy(alpha = 0.55f * alphaFactor))

        // Blob magenta — kanan bawah
        val magenta = Path().apply {
            moveTo(w, h)
            lineTo(w * 0.02f, h)
            cubicTo(
                w * 0.20f, h * 0.955f - d * 0.5f,
                w * 0.40f, h * 0.945f + d * 0.5f,
                w * 0.56f, h * 0.90f,
            )
            cubicTo(
                w * 0.74f, h * 0.855f,
                w * 0.90f, h * 0.875f,
                w, h * 0.845f,
            )
            close()
        }
        drawPath(
            path = magenta,
            brush = Brush.linearGradient(
                colors = listOf(
                    MagentaMuda.copy(alpha = alphaFactor),
                    Magenta.copy(alpha = alphaFactor),
                ),
                start = Offset(w * 0.2f, h * 0.88f),
                end = Offset(w, h),
            ),
        )

        // Aksen magenta lembut
        val magentaSoft = Path().apply {
            moveTo(0f, h)
            cubicTo(w * 0.16f, h * 0.945f, w * 0.28f, h * 0.925f + d * 0.5f, w * 0.44f, h * 0.925f)
            cubicTo(w * 0.28f, h * 0.975f, w * 0.14f, h * 0.995f, 0f, h * 0.972f)
            close()
        }
        drawPath(magentaSoft, color = MagentaLembut.copy(alpha = 0.7f * alphaFactor))
    }
}

/** Tiga titik indikator: pil biru aktif, dot magenta, dot netral. */
@Composable
private fun PageIndicator(gelap: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dots")
    val pulse by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val dotAktif = if (gelap) MagentaMuda else Magenta

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LayarSplash.indikatorJarak),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(LayarSplash.indikatorPilLebar)
                .height(LayarSplash.indikatorPilTinggi)
                .clip(Sudut.pil)
                .background(MaterialTheme.colorScheme.primary),
        )
        Box(
            Modifier
                .size(LayarSplash.indikatorDot)
                .clip(CircleShape)
                .background(dotAktif.copy(alpha = pulse)),
        )
        Box(
            Modifier
                .size(LayarSplash.indikatorDot)
                .clip(CircleShape)
                .background(MagentaLembut),
        )
    }
}

/* ---------- helper ---------- */

private fun Modifier.offsetY(dy: Float): Modifier =
    this.graphicsLayer { translationY = dy }

@Preview(name = "Terang", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashPreviewTerang() {
    DuitTheme(gelap = false) { SplashScreen(gelap = false) }
}

@Preview(name = "Gelap", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashPreviewGelap() {
    DuitTheme(gelap = true) { SplashScreen(gelap = true) }
}
