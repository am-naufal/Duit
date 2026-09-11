package com.alenza.duit.ui.komponen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Sudut
import com.alenza.duit.ui.theme.Ukuran

/**
 * Pemilih Pengeluaran / Pemasukan (design-spec §2 & §5) — dibangun di atas
 * [SegmentedDua] generik.
 *
 * design-spec menggambar track 40 dp, tapi CLAUDE.md aturan 9 (target sentuh
 * minimum 48 dp) menang: track dinaikkan ke 48 dp. Kontrol pil selebar layar
 * jadi selisih 8 dp ini praktis tak terlihat.
 */
@Composable
fun SegmentedTipe(
    terpilih: TipeTransaksi,
    onPilih: (TipeTransaksi) -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedDua(
        opsiSatu = "Pengeluaran",
        opsiDua = "Pemasukan",
        satuAktif = terpilih == TipeTransaksi.PENGELUARAN,
        onPilihSatu = { onPilih(TipeTransaksi.PENGELUARAN) },
        onPilihDua = { onPilih(TipeTransaksi.PEMASUKAN) },
        modifier = modifier,
    )
}

/**
 * Toggle dua-pilihan generik — track `outlineVariant` radius penuh, segmen
 * aktif berlatar `surface` dengan bayangan 1 dp. Dipakai [SegmentedTipe] dan
 * toggle Harian/Bulanan grafik Layar utama, supaya gaya visualnya satu tempat.
 */
@Composable
fun SegmentedDua(
    opsiSatu: String,
    opsiDua: String,
    satuAktif: Boolean,
    onPilihSatu: () -> Unit,
    onPilihDua: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Ukuran.sentuhMin)
            .background(MaterialTheme.colorScheme.outlineVariant, Sudut.pil)
            .padding(Spasi.xs),
        horizontalArrangement = Arrangement.spacedBy(Spasi.xs),
    ) {
        Segmen(opsiSatu, satuAktif, onPilihSatu)
        Segmen(opsiDua, !satuAktif, onPilihDua)
    }
}

@Composable
private fun RowScope.Segmen(teks: String, aktif: Boolean, onClick: () -> Unit) {
    val isi = @Composable {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = teks,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (aktif) FontWeight.SemiBold else FontWeight.Medium,
                color = if (aktif) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }

    if (aktif) {
        Surface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = Sudut.pil,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
        ) { isi() }
    } else {
        Box(Modifier.weight(1f).fillMaxHeight().clip(Sudut.pil)) { isi() }
    }
}
