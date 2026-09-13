package com.duitpribadi.app.ui.splash

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duitpribadi.app.R
import com.duitpribadi.app.ui.theme.DuitColor
import com.duitpribadi.app.ui.theme.DuitPribadiTheme
import kotlinx.coroutines.delay

/**
 * Splash screen "Duit Pribadi".
 *
 * Susunan persis mengikuti mockup:
 *  - background terang dengan blob gradien biru (kiri atas) & magenta (kanan bawah)
 *  - tile logo dompet dengan shadow lembut
 *  - judul dua warna: "Duit" biru + "Pribadi" magenta
 *  - tagline dua baris
 *  - page indicator (pill biru, dot magenta, dot abu)
 *
 * @param onFinished dipanggil setelah animasi + [holdMillis] selesai.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    holdMillis: Long = 1600L,
    onFinished: () -> Unit = {}
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
                stiffness = Spring.StiffnessLow
            )
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

    val isDark = MaterialTheme.colorScheme.background.luminanceIsDark()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BrandBlobs(dark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ---- Logo ----
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(34.dp),
                        ambientColor = DuitColor.Blue,
                        spotColor = DuitColor.Pink
                    )
                    .clip(RoundedCornerShape(34.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_duit_logo),
                    contentDescription = "Logo Duit Pribadi",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(32.dp))

            // ---- Judul dua warna ----
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = DuitColor.BlueDeep)) { append("Duit ") }
                    withStyle(SpanStyle(color = DuitColor.Pink)) { append("Pribadi") }
                },
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offsetY(textOffset.value)
            )

            Spacer(Modifier.height(14.dp))

            // ---- Tagline ----
            Text(
                text = "Kelola Keuanganmu,\nRaih Masa Depanmu",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) DuitColor.TextMutedDark else DuitColor.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offsetY(textOffset.value)
            )
        }

        // ---- Page indicator ----
        PageIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .alpha(textAlpha.value)
        )
    }
}

/** Blob gradien dekoratif di sudut layar. */
@Composable
private fun BrandBlobs(dark: Boolean) {
    val transition = rememberInfiniteTransition(label = "blob")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val alphaFactor = if (dark) 0.55f else 1f

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val d = drift * 18f

        // Blob biru — kiri atas
        val blue = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.78f, 0f)
            cubicTo(
                w * 0.62f, h * 0.09f + d,
                w * 0.46f, h * 0.10f - d,
                w * 0.30f, h * 0.17f + d
            )
            cubicTo(
                w * 0.14f, h * 0.24f,
                w * 0.05f, h * 0.20f,
                0f, h * 0.26f
            )
            close()
        }
        drawPath(
            path = blue,
            brush = Brush.linearGradient(
                colors = listOf(
                    DuitColor.BlueDeep.copy(alpha = alphaFactor),
                    DuitColor.BlueLight.copy(alpha = alphaFactor)
                ),
                start = Offset(0f, 0f),
                end = Offset(w * 0.8f, h * 0.25f)
            )
        )

        // Aksen biru muda tipis di bawah blob
        val blueSoft = Path().apply {
            moveTo(0f, h * 0.02f)
            cubicTo(w * 0.22f, h * 0.16f, w * 0.30f, h * 0.20f, w * 0.52f, h * 0.20f)
            cubicTo(w * 0.30f, h * 0.28f - d, w * 0.16f, h * 0.30f, 0f, h * 0.33f)
            close()
        }
        drawPath(blueSoft, color = DuitColor.BlueSoft.copy(alpha = 0.55f * alphaFactor))

        // Blob magenta — kanan bawah
        val pink = Path().apply {
            moveTo(w, h)
            lineTo(w * 0.02f, h)
            cubicTo(
                w * 0.20f, h * 0.955f - d * 0.5f,
                w * 0.40f, h * 0.945f + d * 0.5f,
                w * 0.56f, h * 0.90f
            )
            cubicTo(
                w * 0.74f, h * 0.855f,
                w * 0.90f, h * 0.875f,
                w, h * 0.845f
            )
            close()
        }
        drawPath(
            path = pink,
            brush = Brush.linearGradient(
                colors = listOf(
                    DuitColor.PinkLight.copy(alpha = alphaFactor),
                    DuitColor.Pink.copy(alpha = alphaFactor)
                ),
                start = Offset(w * 0.2f, h * 0.88f),
                end = Offset(w, h)
            )
        )

        // Aksen pink lembut
        val pinkSoft = Path().apply {
            moveTo(0f, h)
            cubicTo(w * 0.16f, h * 0.945f, w * 0.28f, h * 0.925f + d * 0.5f, w * 0.44f, h * 0.925f)
            cubicTo(w * 0.28f, h * 0.975f, w * 0.14f, h * 0.995f, 0f, h * 0.972f)
            close()
        }
        drawPath(pinkSoft, color = DuitColor.PinkSoft.copy(alpha = 0.7f * alphaFactor))
    }
}

/** Tiga titik indikator: pill biru aktif, dot magenta, dot netral. */
@Composable
private fun PageIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dots")
    val pulse by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(26.dp)
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DuitColor.Blue)
        )
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(DuitColor.Pink.copy(alpha = pulse))
        )
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(DuitColor.PinkSoft)
        )
    }
}

/* ---------- helper ---------- */

private fun Modifier.offsetY(dy: Float): Modifier =
    this.graphicsLayer { translationY = dy }

private fun Color.luminanceIsDark(): Boolean =
    (0.299f * red + 0.587f * green + 0.114f * blue) < 0.5f

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashPreview() {
    DuitPribadiTheme { SplashScreen() }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, uiMode = 0x21)
@Composable
private fun SplashPreviewDark() {
    DuitPribadiTheme(darkTheme = true) { SplashScreen() }
}
