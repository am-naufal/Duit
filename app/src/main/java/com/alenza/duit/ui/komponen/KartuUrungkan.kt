package com.alenza.duit.ui.komponen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alenza.duit.ui.theme.AksenGelap
import com.alenza.duit.ui.theme.GayaCatatan
import com.alenza.duit.ui.theme.KartuAksi
import com.alenza.duit.ui.theme.OverlayGelap
import com.alenza.duit.ui.theme.OverlayTeks
import com.alenza.duit.ui.theme.OverlayTeksRedup
import com.alenza.duit.ui.theme.Spasi

/**
 * Kartu "Transaksi dihapus" di bawah layar (design-spec §3) — bukan Snackbar
 * Material bawaan. Latar selalu gelap (`#22262A`), bar tipis di bawahnya
 * menghitung mundur jendela urungkan.
 *
 * Kartu ini hanya menampilkan; siapa pun yang memakainya bertanggung jawab
 * menutupnya saat waktu habis. `[key]` dipakai untuk me-restart animasi bar
 * setiap kali ada penghapusan baru.
 */
@Composable
fun KartuUrungkan(
    detail: String,
    onUrungkan: () -> Unit,
    key: Any,
    modifier: Modifier = Modifier,
    durasiMillis: Int = 5_000,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(KartuAksi.sudut),
        color = OverlayGelap,
        shadowElevation = 6.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spasi.l, end = Spasi.s, top = Spasi.m, bottom = Spasi.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Transaksi dihapus",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OverlayTeks,
                    )
                    Text(
                        text = detail,
                        style = GayaCatatan,
                        color = OverlayTeksRedup,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    onClick = onUrungkan,
                    colors = ButtonDefaults.textButtonColors(contentColor = AksenGelap),
                ) {
                    Text("URUNGKAN", fontWeight = FontWeight.SemiBold)
                }
            }

            val sisa = remember(key) { Animatable(1f) }
            LaunchedEffect(key) {
                sisa.snapTo(1f)
                sisa.animateTo(0f, tween(durasiMillis, easing = LinearEasing))
            }
            Box(
                Modifier
                    .fillMaxWidth(sisa.value.coerceIn(0f, 1f))
                    .height(KartuAksi.progresTinggi)
                    .background(AksenGelap.copy(alpha = 0.55f)),
            )
        }
    }
}
