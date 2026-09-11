package com.alenza.duit.ui.komponen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alenza.duit.R
import com.alenza.duit.ui.theme.AksenGelap
import com.alenza.duit.ui.theme.KartuAksi
import com.alenza.duit.ui.theme.OverlayGelap
import com.alenza.duit.ui.theme.OverlayTeks
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Ukuran

/**
 * Kartu pemberitahuan ringkas di bawah layar — sama gaya visual dengan
 * [KartuUrungkan] (`#22262A`, bar tipis menghitung mundur) tapi tanpa tombol
 * aksi, dipakai untuk konfirmasi sederhana yang tak bisa/perlu diurungkan:
 * "Transaksi ditambahkan", "Kategori diarsipkan", "Tema diperbarui", dst.
 *
 * Kartu ini hanya menampilkan; ViewModel pemanggil ([com.alenza.duit.ui.Pemberitahuan])
 * yang bertanggung jawab menghapus pesannya setelah waktu habis. [key] dipakai
 * untuk me-restart animasi bar setiap kali ada pesan baru.
 */
@Composable
fun KartuNotifikasi(
    pesan: String,
    key: Any,
    modifier: Modifier = Modifier,
    durasiMillis: Int = 2_500,
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
                    .padding(horizontal = Spasi.l, vertical = Spasi.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_centang),
                    contentDescription = null,
                    tint = AksenGelap,
                    modifier = Modifier.size(Ukuran.ikon),
                )
                Spacer(Modifier.width(Spasi.s))
                Text(
                    text = pesan,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OverlayTeks,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
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
