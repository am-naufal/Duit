package com.alenza.duit.ui.komponen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Sudut

/**
 * Kartu dasar seluruh aplikasi: permukaan putih/gelap dengan bayangan halus.
 * design-spec: `Surface(tonalElevation = 0.dp, shadowElevation = 1.dp)` —
 * jangan pakai elevation Material default, terlalu berat untuk desain ini.
 */
@Composable
fun KartuDuit(
    modifier: Modifier = Modifier,
    bentuk: Shape = Sudut.kartu,
    isi: Dp = Spasi.xl,
    konten: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = bentuk,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Column(Modifier.padding(isi), content = konten)
    }
}
