package com.alenza.duit.ui.layar.ekspor

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alenza.duit.R
import com.alenza.duit.ui.komponen.BilahPeringatan
import com.alenza.duit.ui.theme.AksenGelap
import com.alenza.duit.ui.theme.DuitTheme
import com.alenza.duit.ui.theme.GayaCatatan
import com.alenza.duit.ui.theme.HijauXls
import com.alenza.duit.ui.theme.KartuAksi
import com.alenza.duit.ui.theme.LayarEkspor
import com.alenza.duit.ui.theme.OverlayGelap
import com.alenza.duit.ui.theme.OverlayTeks
import com.alenza.duit.ui.theme.OverlayTeksRedup
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Sudut
import com.alenza.duit.ui.theme.Ukuran
import com.alenza.duit.ui.theme.Warna

private const val MIME_XLSX =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EksporRoute(
    onTutup: () -> Unit,
    viewModel: EksporViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val pemilihLokasi = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(MIME_XLSX),
    ) { uri ->
        // uri null = pengguna batal memilih lokasi → tidak ada file dibuat.
        if (uri != null) viewModel.simpanKe(uri)
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (state.fase == FaseEkspor.MENULIS) viewModel.batal()
            onTutup()
        },
        sheetState = sheetState,
        shape = Sudut.sheet,
    ) {
        EksporIsi(
            state = state,
            onPilihLokasi = { pemilihLokasi.launch(state.namaFile) },
            onBatal = { viewModel.batal() },
            onSelesai = onTutup,
            onBuka = { bukaFile(context, viewModel.uriHasil()) },
            onBagikan = { bagikanFile(context, viewModel.uriHasil()) },
        )
    }
}

@Composable
private fun EksporIsi(
    state: EksporState,
    onPilihLokasi: () -> Unit,
    onBatal: () -> Unit,
    onSelesai: () -> Unit,
    onBuka: () -> Unit,
    onBagikan: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spasi.layar)
            .padding(bottom = Spasi.xxl),
    ) {
        when (state.fase) {
            FaseEkspor.SELESAI -> KartuSelesai(state, onBuka, onBagikan, onSelesai)
            else -> {
                Text(
                    "Ekspor laporan",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = buildString {
                        append(state.labelBulan)
                        state.jumlahTransaksi?.let { append(" · $it transaksi") }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Warna.current.teksRedup,
                )

                Spacer(Modifier.height(Spasi.l))
                KartuNamaFile(state.namaFile)

                Spacer(Modifier.height(Spasi.l))
                IsiLaporan("Ringkasan — total & rekap per kategori")
                IsiLaporan("Transaksi — satu baris per catatan, dengan saldo berjalan")
                IsiLaporan("Harian — pemasukan & pengeluaran tiap tanggal")

                Spacer(Modifier.height(Spasi.l))
                BilahPeringatan(
                    "Ini laporan untuk dibaca, bukan cadangan data — file ini tidak bisa " +
                        "dipulihkan kembali ke aplikasi.",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spasi.l))
                if (state.fase == FaseEkspor.MENULIS) {
                    ProgresEkspor(state.progres)
                    Spacer(Modifier.height(Spasi.m))
                    OutlinedButton(
                        onClick = onBatal,
                        modifier = Modifier.fillMaxWidth().height(LayarEkspor.tombolTinggi),
                    ) { Text("Batal") }
                } else {
                    if (state.fase == FaseEkspor.GAGAL && state.pesanGalat != null) {
                        Text(
                            state.pesanGalat,
                            style = GayaCatatan,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(Spasi.s))
                    }
                    Button(
                        onClick = onPilihLokasi,
                        enabled = state.jumlahTransaksi != null,
                        modifier = Modifier.fillMaxWidth().height(LayarEkspor.tombolTinggi),
                        shape = RoundedCornerShape(Ukuran.tombolUtamaSudut),
                    ) {
                        Text(if (state.fase == FaseEkspor.GAGAL) "Coba lagi" else "Pilih lokasi & simpan")
                    }
                }
            }
        }
    }
}

@Composable
private fun KartuNamaFile(nama: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Sudut.field)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Spasi.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(LayarEkspor.kotakXls)
                .clip(RoundedCornerShape(Spasi.s))
                .background(HijauXls),
            contentAlignment = Alignment.Center,
        ) {
            Text("XLS", color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(Spasi.m))
        Column {
            Text(
                nama,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "3 sheet · kamu pilih lokasi simpannya",
                style = GayaCatatan,
                color = Warna.current.teksRedup,
            )
        }
    }
}

@Composable
private fun IsiLaporan(teks: String) {
    Row(
        modifier = Modifier.padding(vertical = Spasi.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_centang),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Ukuran.ikon),
        )
        Spacer(Modifier.width(Spasi.s))
        Text(
            teks,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ProgresEkspor(progres: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Menyiapkan file…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "${(progres * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(Spasi.s))
        LinearProgressIndicator(
            progress = { progres.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(LayarEkspor.progresTinggi)
                .clip(Sudut.pil),
        )
    }
}

@Composable
private fun KartuSelesai(
    state: EksporState,
    onBuka: () -> Unit,
    onBagikan: () -> Unit,
    onSelesai: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(KartuAksi.sudut),
        color = OverlayGelap,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spasi.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_centang),
                    contentDescription = null,
                    tint = AksenGelap,
                    modifier = Modifier.size(Ukuran.ikon),
                )
                Spacer(Modifier.width(Spasi.s))
                Text(
                    "Laporan ${state.labelBulanPendek} tersimpan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OverlayTeks,
                )
            }
            Text(
                "Di lokasi yang kamu pilih.",
                style = GayaCatatan,
                color = OverlayTeksRedup,
                modifier = Modifier.padding(top = Spasi.xs),
            )
            Spacer(Modifier.height(Spasi.m))
            Row(horizontalArrangement = Arrangement.spacedBy(Spasi.s)) {
                TextButton(onClick = onBuka) { Text("BUKA", color = AksenGelap) }
                TextButton(onClick = onBagikan) { Text("BAGIKAN", color = AksenGelap) }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onSelesai) { Text("TUTUP", color = OverlayTeksRedup) }
            }
        }
    }
}

// ──────────────────────────────── Intent ─────────────────────────────

private fun bukaFile(context: android.content.Context, uri: android.net.Uri?) {
    uri ?: return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, MIME_XLSX)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Buka laporan")) }
}

private fun bagikanFile(context: android.content.Context, uri: android.net.Uri?) {
    uri ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = MIME_XLSX
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Bagikan laporan")) }
}

// ──────────────────────────────── Preview ────────────────────────────

@Composable
private fun PratinjauIsi(state: EksporState, gelap: Boolean) {
    DuitTheme(gelap = gelap) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            EksporIsi(state, {}, {}, {}, {}, {})
        }
    }
}

@Preview(name = "Siap — terang", showBackground = true)
@Composable
private fun EksporPreviewSiap() = PratinjauIsi(
    EksporState.awal(java.time.YearMonth.of(2026, 8), "Agustus 2026", "Laporan-Keuangan-2026-08.xlsx")
        .copy(jumlahTransaksi = 132),
    gelap = false,
)

@Preview(name = "Selesai — gelap", showBackground = true)
@Composable
private fun EksporPreviewSelesai() = PratinjauIsi(
    EksporState.awal(java.time.YearMonth.of(2026, 8), "Agustus 2026", "Laporan-Keuangan-2026-08.xlsx")
        .copy(jumlahTransaksi = 132, fase = FaseEkspor.SELESAI, progres = 1f),
    gelap = true,
)
