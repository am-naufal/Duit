package com.alenza.duit.ui.layar.tambah

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alenza.duit.R
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.ui.komponen.SegmentedTipe
import com.alenza.duit.ui.komponen.TombolIkon
import com.alenza.duit.ui.theme.DuitTheme
import com.alenza.duit.ui.theme.GayaKeypad
import com.alenza.duit.ui.theme.GayaLabelNominal
import com.alenza.duit.ui.theme.Garis
import com.alenza.duit.ui.theme.Kategori
import com.alenza.duit.ui.theme.LayarTambah
import com.alenza.duit.ui.theme.Rupiah
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Sudut
import com.alenza.duit.ui.theme.Ukuran
import com.alenza.duit.ui.theme.Warna
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Titik masuk dari navigasi. Penghapusan tidak dilakukan di sini — form hanya
 * mengirim id-nya lewat [onHapus]; Layar utama yang menghapus + menampilkan
 * kartu "Urungkan" (design-spec §3), jadi hanya ada satu alur hapus.
 *
 * [onSimpanSelesai] beda dari [onSelesai]: dipanggil hanya setelah simpan
 * sukses, membawa pesan pemberitahuan ("Transaksi ditambahkan"/"diperbarui")
 * untuk ditampilkan di Layar utama — menutup form tanpa menyimpan (tombol X)
 * tetap lewat [onSelesai] biasa, tanpa pemberitahuan.
 */
@Composable
fun TambahRoute(
    onSelesai: () -> Unit,
    onSimpanSelesai: (String) -> Unit,
    onHapus: (Long) -> Unit,
    viewModel: TambahViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    TambahScreen(
        state = state,
        onTutup = onSelesai,
        onSimpan = { viewModel.simpan(onSimpanSelesai) },
        onHapus = { state.idUbah?.let(onHapus) },
        onPilihTipe = viewModel::pilihTipe,
        onAngka = viewModel::tekanAngka,
        onRibuan = viewModel::tekanRibuan,
        onHapusAngka = viewModel::hapusAngka,
        onPilihKategori = viewModel::pilihKategori,
        onPilihTanggal = viewModel::pilihTanggal,
        onUbahCatatan = viewModel::ubahCatatan,
    )
}

@Composable
fun TambahScreen(
    state: TambahState,
    onTutup: () -> Unit,
    onSimpan: () -> Unit,
    onHapus: () -> Unit,
    onPilihTipe: (TipeTransaksi) -> Unit,
    onAngka: (Int) -> Unit,
    onRibuan: () -> Unit,
    onHapusAngka: () -> Unit,
    onPilihKategori: (Long) -> Unit,
    onPilihTanggal: (Long) -> Unit,
    onUbahCatatan: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var konfirmasiBuang by rememberSaveable { mutableStateOf(false) }
    var bukaTanggal by rememberSaveable { mutableStateOf(false) }
    var bukaCatatan by rememberSaveable { mutableStateOf(false) }

    fun tutupAman() {
        if (state.kotor) konfirmasiBuang = true else onTutup()
    }

    BackHandler { tutupAman() }

    val warnaTipe =
        if (state.tipe == TipeTransaksi.PENGELUARAN) Warna.current.pengeluaran else Warna.current.pemasukan
    val padLayar = Modifier.padding(horizontal = Spasi.layar)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Layar tak boleh scroll (spec), jadi pada layar sangat pendek jarak
        // antar blok dipadatkan ke token Spasi yang lebih kecil — bukan
        // ditambah scroll — supaya keypad & tombol Simpan tetap utuh terlihat.
        val kompak = maxHeight < LayarTambah.ambangPendek
        val jarakXl = if (kompak) Spasi.l else Spasi.xl
        val jarakL = if (kompak) Spasi.s else Spasi.l

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(bottom = Spasi.l),
        ) {
            AppBar(
                judul = state.judul,
                ubah = state.mode == ModeForm.UBAH,
                onTutup = { tutupAman() },
                onHapus = onHapus,
            )

            Spacer(Modifier.height(jarakL))
            SegmentedTipe(state.tipe, onPilihTipe, padLayar)

            Spacer(Modifier.height(jarakXl))
            NominalBlok(state.nominal, warnaTipe, padLayar.then(Modifier.fillMaxWidth()))

            Spacer(Modifier.height(jarakXl))
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = padLayar,
            )
            Spacer(Modifier.height(Spasi.s))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spasi.s),
                contentPadding = PaddingValues(horizontal = Spasi.layar),
            ) {
                items(state.kategori, key = { it.id }) { chip ->
                    ChipKategori(chip, chip.id == state.kategoriTerpilih) { onPilihKategori(chip.id) }
                }
            }

            Spacer(Modifier.height(jarakL))
            Row(
                modifier = padLayar.then(Modifier.fillMaxWidth()),
                horizontalArrangement = Arrangement.spacedBy(Spasi.m),
            ) {
                FieldPil(
                    ikon = R.drawable.ic_kalender,
                    teks = state.labelTanggal,
                    redup = false,
                    onClick = { bukaTanggal = true },
                    modifier = Modifier.weight(1f),
                )
                FieldPil(
                    ikon = R.drawable.ic_catatan,
                    teks = state.catatan.ifBlank { "Catatan" },
                    redup = state.catatan.isBlank(),
                    onClick = { bukaCatatan = true },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.weight(1f))

            Keypad(
                onAngka = onAngka,
                onRibuan = onRibuan,
                onHapus = onHapusAngka,
                modifier = padLayar.then(Modifier.fillMaxWidth()),
            )

            Spacer(Modifier.height(jarakL))
            Button(
                onClick = onSimpan,
                enabled = state.bisaSimpan,
                shape = RoundedCornerShape(Ukuran.tombolUtamaSudut),
                modifier = padLayar
                    .then(Modifier.fillMaxWidth())
                    .height(Ukuran.tombolUtama),
            ) {
                Text("Simpan", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (konfirmasiBuang) {
        AlertDialog(
            onDismissRequest = { konfirmasiBuang = false },
            title = { Text("Buang perubahan?") },
            text = { Text("Isian yang belum disimpan akan hilang.") },
            confirmButton = {
                TextButton(onClick = { konfirmasiBuang = false; onTutup() }) { Text("Buang") }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiBuang = false }) { Text("Lanjut isi") }
            },
        )
    }

    if (bukaTanggal) {
        DialogTanggal(
            tanggal = state.tanggal,
            onTutup = { bukaTanggal = false },
            onPilih = { onPilihTanggal(it); bukaTanggal = false },
        )
    }

    if (bukaCatatan) {
        DialogCatatan(
            catatan = state.catatan,
            onUbah = onUbahCatatan,
            onTutup = { bukaCatatan = false },
        )
    }
}

// ──────────────────────────── App bar ────────────────────────────

@Composable
private fun AppBar(
    judul: String,
    ubah: Boolean,
    onTutup: () -> Unit,
    onHapus: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Ukuran.appBarTinggi),
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
        if (ubah) {
            TombolIkon(
                ikon = R.drawable.ic_hapus,
                deskripsi = "Hapus transaksi",
                onClick = onHapus,
                tint = Warna.current.pengeluaran,
            )
        }
    }
}

// ────────────────────────── Blok nominal ─────────────────────────

@Composable
private fun NominalBlok(nominal: Long, warna: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("NOMINAL", style = GayaLabelNominal, color = Warna.current.teksRedup)
        Spacer(Modifier.height(Spasi.s))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = Rupiah.format(nominal),
                style = MaterialTheme.typography.displayMedium,
                color = warna,
                maxLines = 1,
            )
            KursorBerkedip(warna)
        }
    }
}

@Composable
private fun KursorBerkedip(warna: Color) {
    val transisi = rememberInfiniteTransition(label = "kursor")
    val alpha by transisi.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
        label = "kursor-alpha",
    )
    Box(
        Modifier
            .padding(start = Spasi.xs)
            .size(width = LayarTambah.kursorLebar, height = LayarTambah.kursorTinggi)
            .background(warna.copy(alpha = alpha), RoundedCornerShape(percent = 50)),
    )
}

// ──────────────────────────── Chip kategori ──────────────────────

/**
 * Chip digambar setinggi `Ukuran.chipTinggi` (38 dp, design-spec) tapi dibungkus
 * area sentuh 48 dp — CLAUDE.md aturan 9.
 */
@Composable
private fun ChipKategori(chip: KategoriChip, terpilih: Boolean, onClick: () -> Unit) {
    val bentuk = Sudut.pil
    Box(
        modifier = Modifier
            .height(Ukuran.sentuhMin)
            .clip(bentuk)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(Ukuran.chipTinggi)
                .clip(bentuk)
                .background(
                    if (terpilih) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                )
                .border(
                    width = if (terpilih) Garis.tebal else Garis.tipis,
                    color = if (terpilih) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    shape = bentuk,
                )
                .padding(horizontal = Spasi.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spasi.s),
        ) {
            Box(
                Modifier
                    .size(LayarTambah.titikKategori)
                    .clip(CircleShape)
                    .background(Kategori.warnaHex(chip.colorHex)),
            )
            Text(
                text = if (chip.arsip) "${chip.nama} (arsip)" else chip.nama,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (terpilih) FontWeight.SemiBold else FontWeight.Medium,
                color = if (terpilih) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

// ───────────────────────── Field tanggal / catatan ───────────────

/**
 * Field digambar setinggi `Ukuran.fieldTinggi` (46 dp, design-spec) di dalam
 * area sentuh 48 dp — CLAUDE.md aturan 9.
 */
@Composable
private fun FieldPil(
    @DrawableRes ikon: Int,
    teks: String,
    redup: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(Ukuran.sentuhMin)
            .clip(Sudut.field)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Ukuran.fieldTinggi)
                .clip(Sudut.field)
                .border(Garis.tipis, MaterialTheme.colorScheme.outline, Sudut.field)
                .padding(horizontal = Spasi.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spasi.s),
        ) {
            Icon(
                painter = painterResource(ikon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Ukuran.ikon),
            )
            Text(
                text = teks,
                style = MaterialTheme.typography.bodyMedium,
                color = if (redup) Warna.current.teksRedup else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ──────────────────────────────── Keypad ─────────────────────────

@Composable
private fun Keypad(
    onAngka: (Int) -> Unit,
    onRibuan: () -> Unit,
    onHapus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spasi.s)) {
        listOf(1..3, 4..6, 7..9).forEach { baris ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spasi.s)) {
                baris.forEach { d ->
                    TombolKeypad(Modifier.weight(1f), onClick = { onAngka(d) }) {
                        Text(
                            "$d",
                            style = GayaKeypad,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spasi.s)) {
            TombolKeypad(Modifier.weight(1f), onClick = onRibuan) {
                Text("000", style = GayaKeypad, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TombolKeypad(Modifier.weight(1f), onClick = { onAngka(0) }) {
                Text("0", style = GayaKeypad, color = MaterialTheme.colorScheme.onSurface)
            }
            TombolKeypad(Modifier.weight(1f), onClick = onHapus) {
                Icon(
                    painter = painterResource(R.drawable.ic_hapus_mundur),
                    contentDescription = "Hapus satu angka",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Ukuran.ikonAppBar),
                )
            }
        }
    }
}

@Composable
private fun TombolKeypad(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isi: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .height(Ukuran.tombolKeypad)
            .clip(Sudut.field)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { isi() }
}

// ──────────────────────────────── Dialog ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTanggal(
    tanggal: LocalDate,
    onTutup: () -> Unit,
    onPilih: (Long) -> Unit,
) {
    val dapatDipilih = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().year >= 2000

            override fun isSelectableYear(year: Int): Boolean = year >= 2000
        }
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = tanggal.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = dapatDipilih,
    )
    DatePickerDialog(
        onDismissRequest = onTutup,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onPilih(
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toEpochDay(),
                        )
                    }
                },
            ) { Text("Pilih") }
        },
        dismissButton = { TextButton(onClick = onTutup) { Text("Batal") } },
    ) {
        DatePicker(state = pickerState)
    }
}

@Composable
private fun DialogCatatan(
    catatan: String,
    onUbah: (String) -> Unit,
    onTutup: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text("Catatan") },
        text = {
            OutlinedTextField(
                value = catatan,
                onValueChange = onUbah,
                placeholder = { Text("Opsional, maksimal 140 karakter") },
                supportingText = { Text("${catatan.length}/140") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = onTutup) { Text("Selesai") } },
    )
}

// ──────────────────────────────── Preview ────────────────────────

private fun contoh(): TambahState = TambahState(
    mode = ModeForm.BARU,
    idUbah = null,
    tipe = TipeTransaksi.PENGELUARAN,
    nominal = 45_000L,
    kategoriTerpilih = 1L,
    kategori = listOf(
        KategoriChip(1, "Makan & Minum", "#E0785C"),
        KategoriChip(2, "Transportasi", "#5E8CB5"),
        KategoriChip(3, "Belanja", "#B57BA6"),
        KategoriChip(4, "Tagihan", "#D9A441"),
        KategoriChip(5, "Hiburan", "#8A7CC8"),
    ),
    tanggal = LocalDate.now(),
    labelTanggal = "Hari ini",
    catatan = "",
    kotor = true,
    memuat = false,
)

private val AKSI_KOSONG: TambahScreenAksi = TambahScreenAksi()

private data class TambahScreenAksi(
    val tutup: () -> Unit = {},
    val simpan: () -> Unit = {},
    val hapus: () -> Unit = {},
    val tipe: (TipeTransaksi) -> Unit = {},
    val angka: (Int) -> Unit = {},
    val ribuan: () -> Unit = {},
    val hapusAngka: () -> Unit = {},
    val kategori: (Long) -> Unit = {},
    val tanggal: (Long) -> Unit = {},
    val catatan: (String) -> Unit = {},
)

@Composable
private fun PreviewIsi(state: TambahState, gelap: Boolean) {
    DuitTheme(gelap = gelap) {
        TambahScreen(
            state = state,
            onTutup = AKSI_KOSONG.tutup,
            onSimpan = AKSI_KOSONG.simpan,
            onHapus = AKSI_KOSONG.hapus,
            onPilihTipe = AKSI_KOSONG.tipe,
            onAngka = AKSI_KOSONG.angka,
            onRibuan = AKSI_KOSONG.ribuan,
            onHapusAngka = AKSI_KOSONG.hapusAngka,
            onPilihKategori = AKSI_KOSONG.kategori,
            onPilihTanggal = AKSI_KOSONG.tanggal,
            onUbahCatatan = AKSI_KOSONG.catatan,
        )
    }
}

@Preview(name = "Terang", showBackground = true, heightDp = 800)
@Composable
private fun TambahPreviewTerang() = PreviewIsi(contoh(), gelap = false)

@Preview(name = "Gelap", showBackground = true, heightDp = 800)
@Composable
private fun TambahPreviewGelap() = PreviewIsi(contoh(), gelap = true)

@Preview(name = "Pemasukan + ubah", showBackground = true, heightDp = 800)
@Composable
private fun TambahPreviewPemasukan() = PreviewIsi(
    contoh().copy(
        mode = ModeForm.UBAH,
        idUbah = 42L,
        tipe = TipeTransaksi.PEMASUKAN,
        nominal = 8_500_000L,
        kategoriTerpilih = 9L,
        kategori = listOf(
            KategoriChip(9, "Gaji", "#4E9E6A"),
            KategoriChip(10, "Bonus", "#CF8B5B"),
            KategoriChip(11, "Hadiah", "#C76A8A"),
        ),
        labelTanggal = "27 Agu",
        catatan = "Gaji Agustus",
    ),
    gelap = false,
)
