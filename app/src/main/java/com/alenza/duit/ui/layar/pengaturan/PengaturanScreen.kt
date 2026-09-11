package com.alenza.duit.ui.layar.pengaturan

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alenza.duit.R
import com.alenza.duit.data.FormatTanggal
import com.alenza.duit.data.PreferensiApp
import com.alenza.duit.data.Tema
import com.alenza.duit.ui.komponen.BilahPeringatan
import com.alenza.duit.ui.komponen.KartuNotifikasi
import com.alenza.duit.ui.komponen.TombolIkon
import com.alenza.duit.ui.theme.DuitTheme
import com.alenza.duit.ui.theme.LayarPengaturan
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Sudut
import com.alenza.duit.ui.theme.KartuAksi
import com.alenza.duit.ui.theme.Ukuran
import com.alenza.duit.ui.theme.Warna

@Composable
fun PengaturanRoute(
    onKembali: () -> Unit,
    onEkspor: () -> Unit,
    onKelolaKategori: () -> Unit,
    viewModel: PengaturanViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    PengaturanScreen(
        state = state,
        onKembali = onKembali,
        onEkspor = onEkspor,
        onKelolaKategori = onKelolaKategori,
        onSetTema = viewModel::setTema,
        onSetFormat = viewModel::setFormatTanggal,
        onSetHariAwal = viewModel::setHariAwalBulan,
    )
}

@Composable
fun PengaturanScreen(
    state: PengaturanState,
    onKembali: () -> Unit,
    onEkspor: () -> Unit,
    onKelolaKategori: () -> Unit,
    onSetTema: (Tema) -> Unit,
    onSetFormat: (FormatTanggal) -> Unit,
    onSetHariAwal: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialog by remember { mutableStateOf(DialogPengaturan.TIDAK_ADA) }
    val pref = state.pref

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Ukuran.appBarTinggi)
                .padding(horizontal = Spasi.layar),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TombolIkon(R.drawable.ic_arrow_back, "Kembali", onClick = onKembali)
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = Spasi.xs),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spasi.layar)
                .padding(bottom = Spasi.xxl),
        ) {
            LabelGrup("TAMPILAN")
            Grup {
                Baris(R.drawable.ic_info, "Tema", labelTema(pref.tema)) { dialog = DialogPengaturan.TEMA }
                Pemisah()
                Baris(R.drawable.ic_kalender, "Format tanggal", pref.formatTanggal.contoh) {
                    dialog = DialogPengaturan.FORMAT
                }
                Pemisah()
                Baris(R.drawable.ic_kalender, "Hari awal bulan", "Tanggal ${pref.hariAwalBulan}") {
                    dialog = DialogPengaturan.HARI_AWAL
                }
            }

            Spacer(Modifier.height(Spasi.xl))
            LabelGrup("DATA")
            Grup {
                Baris(R.drawable.ic_ekspor, "Ekspor laporan bulan ini", null, onClick = onEkspor)
                Pemisah()
                Baris(R.drawable.ic_catatan, "Kelola kategori", null, onClick = onKelolaKategori)
            }
            Spacer(Modifier.height(Spasi.m))
            BilahPeringatan(
                teks = "Belum ada cadangan data. Kalau HP hilang atau aplikasi dihapus, " +
                    "semua catatan ikut hilang — ekspor Excel adalah laporan, bukan cadangan.",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spasi.xl))
            LabelGrup("TENTANG")
            Grup {
                Baris(R.drawable.ic_gembok, "Tanpa izin internet", "Terverifikasi", chevron = false)
                Pemisah()
                Baris(R.drawable.ic_info, "Versi", state.versi, chevron = false)
            }
        }
    }

        state.pesan?.let { pesan ->
            KartuNotifikasi(
                pesan = pesan,
                key = pesan,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = Spasi.layar, vertical = KartuAksi.marginBawah)
                    .fillMaxWidth(),
            )
        }
    }

    when (dialog) {
        DialogPengaturan.TEMA -> DialogPilihan(
            judul = "Tema",
            opsi = Tema.entries,
            terpilih = pref.tema,
            label = { labelTema(it) },
            onPilih = { onSetTema(it); dialog = DialogPengaturan.TIDAK_ADA },
            onTutup = { dialog = DialogPengaturan.TIDAK_ADA },
        )
        DialogPengaturan.FORMAT -> DialogPilihan(
            judul = "Format tanggal",
            opsi = FormatTanggal.entries,
            terpilih = pref.formatTanggal,
            label = { it.contoh },
            onPilih = { onSetFormat(it); dialog = DialogPengaturan.TIDAK_ADA },
            onTutup = { dialog = DialogPengaturan.TIDAK_ADA },
        )
        DialogPengaturan.HARI_AWAL -> DialogHariAwal(
            nilai = pref.hariAwalBulan,
            onPilih = { onSetHariAwal(it); dialog = DialogPengaturan.TIDAK_ADA },
            onTutup = { dialog = DialogPengaturan.TIDAK_ADA },
        )
        DialogPengaturan.TIDAK_ADA -> Unit
    }
}

private enum class DialogPengaturan { TIDAK_ADA, TEMA, FORMAT, HARI_AWAL }

private fun labelTema(tema: Tema): String = when (tema) {
    Tema.SISTEM -> "Ikuti sistem"
    Tema.TERANG -> "Terang"
    Tema.GELAP -> "Gelap"
}

// ──────────────────────────────── Bagian ─────────────────────────────

@Composable
private fun LabelGrup(teks: String) {
    Text(
        text = teks,
        style = MaterialTheme.typography.labelSmall,
        color = Warna.current.teksRedup,
        modifier = Modifier.padding(start = Spasi.xs, bottom = Spasi.s),
    )
}

@Composable
private fun Grup(konten: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    androidx.compose.material3.Surface(
        shape = Sudut.baris,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(content = konten)
    }
}

@Composable
private fun Pemisah() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(start = LayarPengaturan.pemisahMulaiDari),
    )
}

@Composable
private fun Baris(
    @DrawableRes ikon: Int,
    judul: String,
    nilai: String?,
    chevron: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Spasi.m, vertical = Spasi.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(LayarPengaturan.ikonKotak)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(LayarPengaturan.ikonKotakSudut))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(ikon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Ukuran.ikon),
            )
        }
        Spacer(Modifier.width(Spasi.m))
        Text(
            text = judul,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (nilai != null) {
            Text(
                text = nilai,
                style = MaterialTheme.typography.bodyMedium,
                color = Warna.current.teksRedup,
            )
        }
        if (chevron) {
            Spacer(Modifier.width(Spasi.xs))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Warna.current.teksRedup,
                modifier = Modifier.size(Ukuran.ikon),
            )
        }
    }
}

// ──────────────────────────────── Dialog ─────────────────────────────

@Composable
private fun <T> DialogPilihan(
    judul: String,
    opsi: List<T>,
    terpilih: T,
    label: (T) -> String,
    onPilih: (T) -> Unit,
    onTutup: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text(judul) },
        text = {
            Column {
                opsi.forEach { o ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = o == terpilih, onClick = { onPilih(o) })
                            .padding(vertical = Spasi.s),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = o == terpilih, onClick = { onPilih(o) })
                        Spacer(Modifier.width(Spasi.s))
                        Text(label(o), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onTutup) { Text("Tutup") } },
    )
}

@Composable
private fun DialogHariAwal(nilai: Int, onPilih: (Int) -> Unit, onTutup: () -> Unit) {
    var sementara by remember(nilai) { mutableIntStateOf(nilai) }
    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text("Hari awal bulan") },
        text = {
            Column {
                Text(
                    "Berguna kalau gajian tidak di tanggal 1. \"Bulan berjalan\" dihitung " +
                        "mulai tanggal ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Warna.current.teksRedup,
                )
                Spacer(Modifier.height(Spasi.l))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { if (sementara > 1) sementara-- }) { Text("−") }
                    Text(
                        "Tanggal $sementara",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    TextButton(onClick = { if (sementara < 28) sementara++ }) { Text("+") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onPilih(sementara) }) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onTutup) { Text("Batal") } },
    )
}

// ──────────────────────────────── Preview ────────────────────────────

@Composable
private fun PreviewIsi(gelap: Boolean) {
    DuitTheme(gelap = gelap) {
        PengaturanScreen(
            state = PengaturanState(PreferensiApp(), "1.0"),
            onKembali = {}, onEkspor = {}, onKelolaKategori = {},
            onSetTema = {}, onSetFormat = {}, onSetHariAwal = {},
        )
    }
}

@Preview(name = "Terang", showBackground = true, heightDp = 820)
@Composable
private fun PengaturanPreviewTerang() = PreviewIsi(gelap = false)

@Preview(name = "Gelap", showBackground = true, heightDp = 820)
@Composable
private fun PengaturanPreviewGelap() = PreviewIsi(gelap = true)
