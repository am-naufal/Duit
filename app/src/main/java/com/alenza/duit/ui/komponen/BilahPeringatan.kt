package com.alenza.duit.ui.komponen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.alenza.duit.R
import com.alenza.duit.ui.theme.GayaCatatan
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Sudut
import com.alenza.duit.ui.theme.Ukuran
import com.alenza.duit.ui.theme.Warna

/**
 * Bilah peringatan kuning (design-spec §6 & §8). Dipakai untuk mengingatkan
 * risiko kehilangan data — teksnya tidak boleh dihapus (PRD §10).
 */
@Composable
fun BilahPeringatan(teks: String, modifier: Modifier = Modifier) {
    val warna = Warna.current
    Row(
        modifier = modifier
            .clip(Sudut.peringatan)
            .background(warna.peringatanLatar)
            .padding(Spasi.m),
        horizontalArrangement = Arrangement.spacedBy(Spasi.s),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_peringatan),
            contentDescription = null,
            tint = warna.peringatan,
            modifier = Modifier.size(Ukuran.ikon),
        )
        Text(
            text = teks,
            style = GayaCatatan,
            color = warna.peringatanTeks,
        )
    }
}
