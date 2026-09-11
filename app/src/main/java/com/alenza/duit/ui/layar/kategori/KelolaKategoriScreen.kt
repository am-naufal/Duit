package com.alenza.duit.ui.layar.kategori

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alenza.duit.R
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.ui.komponen.KartuDuit
import com.alenza.duit.ui.komponen.KartuNotifikasi
import com.alenza.duit.ui.komponen.LingkaranIkon
import com.alenza.duit.ui.komponen.SegmentedTipe
import com.alenza.duit.ui.komponen.TombolIkon
import com.alenza.duit.ui.theme.DuitTheme
import com.alenza.duit.ui.theme.GayaAngkaKecil
import com.alenza.duit.ui.theme.GayaNamaBaris
import com.alenza.duit.ui.theme.Kategori
import com.alenza.duit.ui.theme.LayarKategori
import com.alenza.duit.ui.theme.KartuAksi
import com.alenza.duit.ui.theme.LayarUtama
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Sudut
import com.alenza.duit.ui.theme.Ukuran
import com.alenza.duit.ui.theme.Warna
import kotlin.math.roundToInt

/** Titik masuk dari navigasi. */
@Composable
fun KelolaKategoriRoute(
    onKembali: () -> Unit,
    onTambah: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: KelolaKategoriViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    KelolaKategoriScreen(
        state = state,
        onKembali = onKembali,
        onTambah = onTambah,
        onEdit = onEdit,
        onPilihTipe = viewModel::pilihTipe,
        onPulihkan = viewModel::pulihkan,
        onUrutanBaru = viewModel::urutkanUlang,
    )
}

@Composable
fun KelolaKategoriScreen(
    state: KelolaKategoriState,
    onKembali: () -> Unit,
    onTambah: () -> Unit,
    onEdit: (Long) -> Unit,
    onPilihTipe: (TipeTransaksi) -> Unit,
    onPulihkan: (Long) -> Unit,
    onUrutanBaru: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val padLayar = Modifier.padding(horizontal = Spasi.layar)

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
            AppBar(onKembali = onKembali, onTambah = onTambah)

            Spacer(Modifier.height(Spasi.s))
            SegmentedTipe(state.tipe, onPilihTipe, padLayar)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spasi.layar,
                    end = Spasi.layar,
                    top = Spasi.l,
                    bottom = Spasi.xxl,
                ),
            ) {
                if (state.aktif.isNotEmpty()) {
                    item(key = "aktif") {
                        KartuDuit(bentuk = Sudut.baris, isi = LayarUtama.kartuBarisIsi) {
                            DaftarAktifBisaDiseret(
                                daftar = state.aktif,
                                onKlik = onEdit,
                                onUrutanBaru = onUrutanBaru,
                            )
                        }
                    }
                }

                if (state.arsip.isNotEmpty()) {
                    item(key = "label-arsip") {
                        Text(
                            text = "DIARSIPKAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = Warna.current.teksRedup,
                            modifier = Modifier.padding(start = Spasi.xs, top = Spasi.xl, bottom = Spasi.s),
                        )
                    }
                    item(key = "arsip") {
                        KartuDuit(bentuk = Sudut.baris, isi = LayarUtama.kartuBarisIsi) {
                            state.arsip.forEach { baris ->
                                BarisArsip(baris, onPulihkan = { onPulihkan(baris.id) })
                            }
                        }
                    }
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
}

// ──────────────────────────────── App bar ────────────────────────────

@Composable
private fun AppBar(onKembali: () -> Unit, onTambah: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Ukuran.appBarTinggi)
            .padding(horizontal = Spasi.layar),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TombolIkon(R.drawable.ic_arrow_back, "Kembali", onClick = onKembali)
        Text(
            text = "Kategori",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .padding(start = Spasi.xs),
        )
        TombolIkon(
            ikon = R.drawable.ic_plus,
            deskripsi = "Kategori baru",
            onClick = onTambah,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

// ──────────────────────────── Seret untuk urutkan ─────────────────────

/**
 * Daftar kategori aktif dengan seret-untuk-mengurutkan lewat handle `ic_geser`
 * (design-spec §4, B-1 di `docs/keputusan-tertunda.md`). Kategori sistem
 * ("Lainnya") tak punya handle dan sengaja dikecualikan dari penyeretan —
 * selalu tetap di posisi paling akhir, sama seperti urutan bawaannya.
 *
 * Deviasi sadar: hanya baris yang sedang diseret yang beranimasi mengikuti
 * jari; baris lain yang tergeser posisinya langsung berpindah tanpa animasi
 * geser. Animasi penuh ala `LazyColumn`'s `Modifier.animateItem()` mengharuskan
 * tiap baris jadi item ter-lazy sendiri-sendiri, yang akan memecah satu
 * bayangan kartu ini jadi berbayang per baris.
 */
@Composable
private fun DaftarAktifBisaDiseret(
    daftar: List<BarisKategori>,
    onKlik: (Long) -> Unit,
    onUrutanBaru: (List<Long>) -> Unit,
) {
    val bisaDiseret = remember(daftar) { daftar.filter { !it.sistem } }
    val tetap = remember(daftar) { daftar.filter { it.sistem } }

    var urutan by remember(bisaDiseret) { mutableStateOf(bisaDiseret) }
    var idDiseret by remember { mutableStateOf<Long?>(null) }
    var offsetDiseretPx by remember { mutableFloatStateOf(0f) }
    var tinggiBarisPx by remember { mutableFloatStateOf(0f) }

    Column {
        urutan.forEach { baris ->
            key(baris.id) {
                val diseret = baris.id == idDiseret
                BarisAktif(
                    baris = baris,
                    onClick = { onKlik(baris.id) },
                    modifier = Modifier
                        .zIndex(if (diseret) 1f else 0f)
                        .graphicsLayer { translationY = if (diseret) offsetDiseretPx else 0f }
                        .onGloballyPositioned { tinggiBarisPx = it.size.height.toFloat() }
                        .then(
                            if (diseret) {
                                Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            } else {
                                Modifier
                            },
                        ),
                    onSeretMulai = {
                        idDiseret = baris.id
                        offsetDiseretPx = 0f
                    },
                    onSeretGeser = { dy ->
                        offsetDiseretPx += dy
                        if (tinggiBarisPx > 0f) {
                            val posisiSekarang = urutan.indexOfFirst { it.id == baris.id }
                            val langkah = (offsetDiseretPx / tinggiBarisPx).roundToInt()
                            if (langkah != 0 && posisiSekarang >= 0) {
                                val tujuan = (posisiSekarang + langkah).coerceIn(0, urutan.lastIndex)
                                if (tujuan != posisiSekarang) {
                                    urutan = urutan.toMutableList()
                                        .apply { add(tujuan, removeAt(posisiSekarang)) }
                                    offsetDiseretPx -= langkah * tinggiBarisPx
                                }
                            }
                        }
                    },
                    onSeretSelesai = {
                        idDiseret = null
                        offsetDiseretPx = 0f
                        if (urutan.map { it.id } != bisaDiseret.map { it.id }) {
                            onUrutanBaru(urutan.map { it.id } + tetap.map { it.id })
                        }
                    },
                )
            }
        }
        tetap.forEach { baris ->
            key(baris.id) {
                BarisAktif(baris = baris, onClick = { onKlik(baris.id) })
            }
        }
    }
}

// ──────────────────────────────── Baris ──────────────────────────────

@Composable
private fun BarisAktif(
    baris: BarisKategori,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSeretMulai: (() -> Unit)? = null,
    onSeretGeser: ((Float) -> Unit)? = null,
    onSeretSelesai: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = LayarUtama.barisTxHorizontal,
                vertical = LayarUtama.barisTxVertikal,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IkonKategori(baris)
        Spacer(Modifier.width(LayarUtama.barisIkonJarak))
        Column(Modifier.weight(1f)) {
            Text(
                text = baris.nama,
                style = GayaNamaBaris,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${baris.jumlahTransaksi} transaksi",
                style = GayaAngkaKecil,
                color = Warna.current.teksRedup,
            )
        }
        Spacer(Modifier.width(Spasi.s))
        if (baris.sistem) {
            ChipBawaan()
        } else {
            Box(
                modifier = Modifier
                    .size(Ukuran.sentuhMin)
                    .pointerInput(baris.id) {
                        detectDragGestures(
                            onDragStart = { onSeretMulai?.invoke() },
                            onDragEnd = { onSeretSelesai?.invoke() },
                            onDragCancel = { onSeretSelesai?.invoke() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onSeretGeser?.invoke(dragAmount.y)
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_geser),
                    contentDescription = "Ubah urutan",
                    tint = Warna.current.teksRedup,
                    modifier = Modifier.size(Ukuran.ikon),
                )
            }
        }
    }
}

@Composable
private fun BarisArsip(baris: BarisKategori, onPulihkan: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = LayarUtama.barisTxHorizontal,
                end = Spasi.xs,
                top = LayarUtama.barisTxVertikal,
                bottom = LayarUtama.barisTxVertikal,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .alpha(0.55f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IkonKategori(baris)
            Spacer(Modifier.width(LayarUtama.barisIkonJarak))
            Column(Modifier.weight(1f)) {
                Text(
                    text = baris.nama,
                    style = GayaNamaBaris,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${baris.jumlahTransaksi} transaksi",
                    style = GayaAngkaKecil,
                    color = Warna.current.teksRedup,
                )
            }
        }
        TextButton(onClick = onPulihkan) {
            Text("Pulihkan", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun IkonKategori(baris: BarisKategori) {
    LingkaranIkon(
        ikon = Kategori.ikon(baris.iconKey),
        latar = Kategori.warnaHex(baris.colorHex),
        ukuran = LayarKategori.ikonBaris,
        sudut = LayarKategori.ikonBarisSudut,
        ikonUkuran = Ukuran.ikon,
    )
}

@Composable
private fun ChipBawaan() {
    Box(
        modifier = Modifier
            .clip(Sudut.pil)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spasi.s, vertical = Spasi.xs),
    ) {
        Text(
            text = "Bawaan",
            style = MaterialTheme.typography.labelSmall,
            color = Warna.current.teksRedup,
        )
    }
}

// ──────────────────────────────── Preview ────────────────────────────

private fun contoh(): KelolaKategoriState = KelolaKategoriState(
    tipe = TipeTransaksi.PENGELUARAN,
    aktif = listOf(
        BarisKategori(1, "Makan & Minum", "makan", "#E0785C", 24, sistem = false),
        BarisKategori(2, "Transportasi", "transportasi", "#5E8CB5", 12, sistem = false),
        BarisKategori(3, "Belanja", "belanja", "#B57BA6", 5, sistem = false),
        BarisKategori(8, "Lainnya", "lainnya", "#94989E", 3, sistem = true),
    ),
    arsip = listOf(
        BarisKategori(20, "Kopi harian", "makan", "#E0785C", 0, sistem = false),
        BarisKategori(21, "Rokok", "lainnya", "#94989E", 18, sistem = false),
    ),
    pesan = null,
    memuat = false,
)

@Composable
private fun PreviewIsi(state: KelolaKategoriState, gelap: Boolean) {
    DuitTheme(gelap = gelap) {
        KelolaKategoriScreen(
            state = state,
            onKembali = {},
            onTambah = {},
            onEdit = {},
            onPilihTipe = {},
            onPulihkan = {},
            onUrutanBaru = {},
        )
    }
}

@Preview(name = "Terang", showBackground = true, heightDp = 720)
@Composable
private fun KelolaKategoriPreviewTerang() = PreviewIsi(contoh(), gelap = false)

@Preview(name = "Gelap", showBackground = true, heightDp = 720)
@Composable
private fun KelolaKategoriPreviewGelap() = PreviewIsi(contoh(), gelap = true)
