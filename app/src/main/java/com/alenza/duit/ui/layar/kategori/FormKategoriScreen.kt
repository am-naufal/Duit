package com.alenza.duit.ui.layar.kategori

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alenza.duit.R
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.ui.komponen.SegmentedTipe
import com.alenza.duit.ui.komponen.TombolIkon
import com.alenza.duit.ui.theme.DuitTheme
import com.alenza.duit.ui.theme.FormKategori
import com.alenza.duit.ui.theme.GayaCatatan
import com.alenza.duit.ui.theme.Garis
import com.alenza.duit.ui.theme.Kategori
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Sudut
import com.alenza.duit.ui.theme.Ukuran
import com.alenza.duit.ui.theme.Warna

/**
 * [onAksiSelesai] beda dari [onSelesai]: dipanggil setelah simpan/arsipkan/hapus
 * sukses, membawa pesan pemberitahuan untuk ditampilkan di Kelola kategori —
 * menutup form tanpa aksi (tombol X) tetap lewat [onSelesai] biasa.
 */
@Composable
fun FormKategoriRoute(
    onSelesai: () -> Unit,
    onAksiSelesai: (String) -> Unit,
    viewModel: FormKategoriViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    FormKategoriScreen(
        state = state,
        onTutup = onSelesai,
        onSimpan = { viewModel.simpan(onAksiSelesai) },
        onArsipkan = { viewModel.arsipkan(onAksiSelesai) },
        onHapus = { viewModel.hapus(onAksiSelesai) },
        onUbahNama = viewModel::ubahNama,
        onPilihTipe = viewModel::pilihTipe,
        onPilihIkon = viewModel::pilihIkon,
        onPilihWarna = viewModel::pilihWarna,
    )
}

@Composable
fun FormKategoriScreen(
    state: FormKategoriState,
    onTutup: () -> Unit,
    onSimpan: () -> Unit,
    onArsipkan: () -> Unit,
    onHapus: () -> Unit,
    onUbahNama: (String) -> Unit,
    onPilihTipe: (TipeTransaksi) -> Unit,
    onPilihIkon: (String) -> Unit,
    onPilihWarna: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var konfirmasiBuang by rememberSaveable { mutableStateOf(false) }
    var konfirmasiHapus by rememberSaveable { mutableStateOf(false) }

    fun tutupAman() {
        if (state.kotor) konfirmasiBuang = true else onTutup()
    }
    BackHandler { tutupAman() }

    val padLayar = Modifier.padding(horizontal = Spasi.layar)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        AppBar(
            judul = state.judul,
            bisaSimpan = state.bisaSimpan,
            onTutup = { tutupAman() },
            onSimpan = onSimpan,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = Spasi.xxl),
        ) {
            Spacer(Modifier.height(Spasi.m))
            Pratinjau(state, Modifier.fillMaxWidth())

            Spacer(Modifier.height(Spasi.xl))
            FieldNama(state.nama, state.galat, onUbahNama, padLayar.then(Modifier.fillMaxWidth()))

            Spacer(Modifier.height(Spasi.l))
            SegmentedTipe(
                terpilih = state.tipe,
                onPilih = { if (!state.sistem) onPilihTipe(it) },
                modifier = padLayar,
            )

            Spacer(Modifier.height(Spasi.xl))
            Label("Ikon", padLayar)
            Spacer(Modifier.height(Spasi.s))
            GridPilihan(
                item = state.pilihanIkon,
                modifier = padLayar.then(Modifier.fillMaxWidth()),
            ) { key ->
                TileIkon(key, terpilih = key == state.iconKey) { onPilihIkon(key) }
            }

            Spacer(Modifier.height(Spasi.xl))
            Label("Warna", padLayar)
            Spacer(Modifier.height(Spasi.s))
            GridPilihan(
                item = state.pilihanWarna,
                modifier = padLayar.then(Modifier.fillMaxWidth()),
            ) { hex ->
                PetakWarna(hex, terpilih = hex == state.colorHex) { onPilihWarna(hex) }
            }

            if (state.mode == ModeKategori.UBAH) {
                Spacer(Modifier.height(Spasi.xl))
                if (state.sistem) {
                    Text(
                        text = "Kategori bawaan — tidak bisa dihapus maupun diarsipkan.",
                        style = GayaCatatan,
                        color = Warna.current.teksRedup,
                        textAlign = TextAlign.Center,
                        modifier = padLayar.then(Modifier.fillMaxWidth()),
                    )
                } else {
                    TextButton(
                        onClick = {
                            if (state.sudahDipakai) onArsipkan() else konfirmasiHapus = true
                        },
                        modifier = padLayar.then(Modifier.fillMaxWidth()),
                    ) {
                        Text(
                            text = if (state.sudahDipakai) "Arsipkan kategori" else "Hapus kategori",
                            color = Warna.current.pengeluaran,
                        )
                    }
                    if (state.sudahDipakai) {
                        Text(
                            text = "Sudah dipakai transaksi, jadi hanya bisa diarsipkan — riwayat tetap utuh.",
                            style = GayaCatatan,
                            color = Warna.current.teksRedup,
                            textAlign = TextAlign.Center,
                            modifier = padLayar
                                .then(Modifier.fillMaxWidth())
                                .padding(top = Spasi.xs),
                        )
                    }
                }
            }
        }
    }

    if (konfirmasiBuang) {
        AlertDialog(
            onDismissRequest = { konfirmasiBuang = false },
            title = { Text("Buang perubahan?") },
            text = { Text("Perubahan yang belum disimpan akan hilang.") },
            confirmButton = {
                TextButton(onClick = { konfirmasiBuang = false; onTutup() }) { Text("Buang") }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiBuang = false }) { Text("Lanjut isi") }
            },
        )
    }

    if (konfirmasiHapus) {
        AlertDialog(
            onDismissRequest = { konfirmasiHapus = false },
            title = { Text("Hapus kategori ini?") },
            text = { Text("Kategori \"${state.nama}\" belum pernah dipakai, jadi akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = { konfirmasiHapus = false; onHapus() }) {
                    Text("Hapus", color = Warna.current.pengeluaran)
                }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiHapus = false }) { Text("Batal") }
            },
        )
    }
}

// ──────────────────────────────── App bar ────────────────────────────

@Composable
private fun AppBar(
    judul: String,
    bisaSimpan: Boolean,
    onTutup: () -> Unit,
    onSimpan: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Ukuran.appBarTinggi)
            .padding(horizontal = Spasi.layar),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TombolIkon(R.drawable.ic_close, "Tutup", onClick = onTutup)
        Text(
            text = judul,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .padding(start = Spasi.xs),
        )
        Button(
            onClick = onSimpan,
            enabled = bisaSimpan,
            shape = Sudut.pil,
        ) {
            Text("Simpan")
        }
    }
}

// ──────────────────────────────── Pratinjau ──────────────────────────

@Composable
private fun Pratinjau(state: FormKategoriState, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(FormKategori.pratinjauKotak)
                .clip(RoundedCornerShape(FormKategori.pratinjauSudut))
                .background(Kategori.warnaHex(state.colorHex)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Kategori.ikon(state.iconKey)),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(FormKategori.pratinjauIkon),
            )
        }
        Spacer(Modifier.height(Spasi.s))
        Text(
            text = state.nama.ifBlank { "Nama kategori" },
            style = MaterialTheme.typography.bodyLarge,
            color = if (state.nama.isBlank()) Warna.current.teksRedup else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Pratinjau",
            style = GayaCatatan,
            color = Warna.current.teksRedup,
        )
    }
}

// ──────────────────────────────── Field nama ─────────────────────────

@Composable
private fun FieldNama(
    nama: String,
    galat: String?,
    onUbah: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var fokus by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(FormKategori.namaFieldTinggi)
                .clip(Sudut.field)
                .border(
                    width = if (fokus) Garis.tebal else Garis.tipis,
                    color = when {
                        galat != null -> MaterialTheme.colorScheme.error
                        fokus -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    },
                    shape = Sudut.field,
                )
                .padding(horizontal = Spasi.m),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = nama,
                onValueChange = onUbah,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (nama.isEmpty()) {
                        Text(
                            "Nama kategori",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Warna.current.teksRedup,
                        )
                    }
                    inner()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { fokus = it.isFocused },
            )
        }
        if (galat != null) {
            Text(
                text = galat,
                style = GayaCatatan,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Spasi.xs, start = Spasi.xs),
            )
        }
    }
}

// ──────────────────────────────── Grid ───────────────────────────────

@Composable
private fun Label(teks: String, modifier: Modifier = Modifier) {
    Text(
        text = teks,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun <T> GridPilihan(
    item: List<T>,
    modifier: Modifier = Modifier,
    sel: @Composable (T) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spasi.s)) {
        item.chunked(FormKategori.gridKolom).forEach { baris ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spasi.s)) {
                baris.forEach { nilai ->
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        sel(nilai)
                    }
                }
                repeat(FormKategori.gridKolom - baris.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TileIkon(key: String, terpilih: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(FormKategori.ikonTile)
            .clip(RoundedCornerShape(FormKategori.ikonTileSudut))
            .background(
                if (terpilih) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            )
            .border(
                width = if (terpilih) Garis.tebal else 0.dp,
                color = if (terpilih) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(FormKategori.ikonTileSudut),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Kategori.ikon(key)),
            contentDescription = key,
            tint = if (terpilih) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(Ukuran.ikon),
        )
    }
}

@Composable
private fun PetakWarna(hex: String, terpilih: Boolean, onClick: () -> Unit) {
    val warna = Kategori.warnaHex(hex)
    Box(
        modifier = Modifier
            .size(FormKategori.warnaLingkaran)
            .clickable(onClick = onClick)
            .then(
                if (terpilih) {
                    Modifier.border(Garis.tebal, warna, CircleShape).padding(FormKategori.warnaCincin)
                } else {
                    Modifier
                },
            )
            .clip(CircleShape)
            .background(warna),
        contentAlignment = Alignment.Center,
    ) {
        if (terpilih) {
            Icon(
                painter = painterResource(R.drawable.ic_centang),
                contentDescription = "Terpilih",
                tint = Color.White,
                modifier = Modifier.size(Ukuran.ikon),
            )
        }
    }
}

// ──────────────────────────────── Preview ────────────────────────────

private fun contoh(): FormKategoriState = FormKategoriState(
    mode = ModeKategori.BARU,
    idUbah = null,
    nama = "Langganan",
    tipe = TipeTransaksi.PENGELUARAN,
    iconKey = "hiburan",
    colorHex = "#8A7CC8",
    pilihanIkon = Kategori.pilihanIkon,
    pilihanWarna = Kategori.pilihanWarna,
    sistem = false,
    sudahDipakai = false,
    galat = null,
    kotor = true,
    memuat = false,
)

@Composable
private fun PreviewIsi(state: FormKategoriState, gelap: Boolean) {
    DuitTheme(gelap = gelap) {
        FormKategoriScreen(state, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Terang", showBackground = true, heightDp = 900)
@Composable
private fun FormKategoriPreviewTerang() = PreviewIsi(contoh(), gelap = false)

@Preview(name = "Gelap", showBackground = true, heightDp = 900)
@Composable
private fun FormKategoriPreviewGelap() = PreviewIsi(contoh(), gelap = true)

@Preview(name = "Ubah — sudah dipakai", showBackground = true, heightDp = 900)
@Composable
private fun FormKategoriPreviewUbah() = PreviewIsi(
    contoh().copy(mode = ModeKategori.UBAH, idUbah = 3L, nama = "Belanja", sudahDipakai = true),
    gelap = false,
)
