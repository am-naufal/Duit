package com.alenza.duit.ui.komponen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.alenza.duit.ui.theme.Ukuran

/**
 * Lingkaran (persegi membulat) berisi ikon — dipakai di baris transaksi, blok
 * rincian kategori, kartu ringkasan, dan nanti di layar kategori. Satu tempat
 * supaya ukuran & pembulatannya konsisten.
 */
@Composable
fun LingkaranIkon(
    @DrawableRes ikon: Int,
    latar: Color,
    modifier: Modifier = Modifier,
    ukuran: Dp = Ukuran.ikonBaris,
    sudut: Dp = Ukuran.ikonBarisSudut,
    ikonUkuran: Dp = Ukuran.ikon,
    tint: Color = Color.White,
    deskripsi: String? = null,
) {
    Box(
        modifier = modifier
            .size(ukuran)
            .background(latar, RoundedCornerShape(sudut)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(ikon),
            contentDescription = deskripsi,
            tint = tint,
            modifier = Modifier.size(ikonUkuran),
        )
    }
}
