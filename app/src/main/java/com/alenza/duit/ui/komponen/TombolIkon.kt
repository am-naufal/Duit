package com.alenza.duit.ui.komponen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.alenza.duit.ui.theme.Ukuran

/**
 * Tombol ikon dengan target sentuh 48×48 dp (PRD §8 / CLAUDE.md aturan 9),
 * termasuk di app bar. Glyph di dalamnya tetap sebesar grid vector (24 dp).
 * Saat nonaktif ikon diredupkan, bukan dihilangkan — supaya panah bulan yang
 * mentok tetap terlihat (design-spec §9).
 */
@Composable
fun TombolIkon(
    @DrawableRes ikon: Int,
    deskripsi: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aktif: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    IconButton(
        onClick = onClick,
        enabled = aktif,
        modifier = modifier.size(Ukuran.sentuhMin),
    ) {
        Icon(
            painter = painterResource(ikon),
            contentDescription = deskripsi,
            tint = if (aktif) tint else tint.copy(alpha = 0.32f),
            modifier = Modifier.size(Ukuran.ikonAppBar),
        )
    }
}
