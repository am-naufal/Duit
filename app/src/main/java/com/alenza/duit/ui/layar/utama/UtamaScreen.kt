package com.alenza.duit.ui.layar.utama

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.alenza.duit.R
import com.alenza.duit.data.RekapKategori
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.data.Transaksi
import com.alenza.duit.ui.komponen.GrafikBatang
import com.alenza.duit.ui.komponen.GrafikGaris
import com.alenza.duit.ui.komponen.KartuDuit
import com.alenza.duit.ui.komponen.KartuNotifikasi
import com.alenza.duit.ui.komponen.KartuUrungkan
import com.alenza.duit.ui.komponen.LingkaranIkon
import com.alenza.duit.ui.komponen.SegmentedDua
import com.alenza.duit.ui.komponen.TombolIkon
import com.alenza.duit.ui.theme.DuitTheme
import com.alenza.duit.ui.theme.GayaAngkaKecil
import com.alenza.duit.ui.theme.GayaCatatan
import com.alenza.duit.ui.theme.GayaKolomLabel
import com.alenza.duit.ui.theme.GayaNamaBaris
import com.alenza.duit.ui.theme.GayaNamaRincian
import com.alenza.duit.ui.theme.GayaNominalBaris
import com.alenza.duit.ui.theme.GayaNominalRincian
import com.alenza.duit.ui.theme.Kategori
import com.alenza.duit.ui.theme.KartuAksi
import com.alenza.duit.ui.theme.LayarUtama
import com.alenza.duit.ui.theme.Rupiah
import com.alenza.duit.ui.theme.Spasi
import com.alenza.duit.ui.theme.Sudut
import com.alenza.duit.ui.theme.Ukuran
import com.alenza.duit.ui.theme.Warna
import java.time.LocalDate
import java.time.YearMonth

/** Titik masuk dari navigasi: ambil state dari ViewModel, sisanya stateless. */
@Composable
fun UtamaRoute(
    onTambah: () -> Unit,
    onEkspor: (YearMonth) -> Unit,
    onKelolaKategori: () -> Unit,
    onPengaturan: () -> Unit,
    onTransaksiDiklik: (Long) -> Unit,
    viewModel: UtamaViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    UtamaScreen(
        state = state,
        onBulanSebelumnya = viewModel::bulanSebelumnya,
        onBulanBerikutnya = viewModel::bulanBerikutnya,
        onTambah = onTambah,
        onEkspor = { onEkspor(state.bulan) },
        onKelolaKategori = onKelolaKategori,
        onPengaturan = onPengaturan,
        onTransaksiDiklik = onTransaksiDiklik,
        onHapusTransaksi = viewModel::hapusTransaksi,
        onUrungkanHapus = viewModel::urungkanHapus,
        onInfoDimengerti = viewModel::tandaiInfoDilihat,
    )
}

@Composable
fun UtamaScreen(
    state: UtamaState,
    onBulanSebelumnya: () -> Unit,
    onBulanBerikutnya: () -> Unit,
    onTambah: () -> Unit,
    onEkspor: () -> Unit,
    onKelolaKategori: () -> Unit,
    onPengaturan: () -> Unit,
    onTransaksiDiklik: (Long) -> Unit,
    onHapusTransaksi: (Long) -> Unit,
    onUrungkanHapus: () -> Unit,
    onInfoDimengerti: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(Modifier.fillMaxSize()) {
            Header(
                labelBulan = state.labelBulan,
                bisaMundur = state.bisaMundur,
                bisaMaju = state.bisaMaju,
                onBulanSebelumnya = onBulanSebelumnya,
                onBulanBerikutnya = onBulanBerikutnya,
                onEkspor = onEkspor,
                onKelolaKategori = onKelolaKategori,
                onPengaturan = onPengaturan,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = Spasi.layar),
            )

            if (state.tampilkanInfoLokal) {
                KartuInfoLokal(
                    onMengerti = onInfoDimengerti,
                    modifier = Modifier.padding(horizontal = Spasi.layar, vertical = Spasi.m),
                )
            }

            if (state.kosong) {
                KondisiKosong(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spasi.layar,
                        end = Spasi.layar,
                        top = Spasi.m,
                        bottom = LayarUtama.gradienTinggi + Spasi.xxl,
                    ),
                ) {
                    item(key = "ringkasan") {
                        KartuRingkasan(
                            sisaBulan = state.sisaBulan,
                            totalPemasukan = state.totalPemasukan,
                            totalPengeluaran = state.totalPengeluaran,
                        )
                    }

                    if (state.rincian.isNotEmpty()) {
                        item(key = "rincian") {
                            Spacer(Modifier.height(Spasi.antarBlok))
                            BlokRincian(state.rincian)
                        }
                    }

                    item(key = "grafik-pengeluaran") {
                        Spacer(Modifier.height(Spasi.antarBlok))
                        BlokGrafikPengeluaran(state.grafik)
                    }

                    item(key = "grafik-saldo") {
                        Spacer(Modifier.height(Spasi.antarBlok))
                        BlokGrafikSaldo(state.grafik)
                    }

                    itemsHari(state.hari, onTransaksiDiklik, onHapusTransaksi)
                }
            }
        }

        // Gradien pudar setinggi 120 dp di atas FAB supaya daftar tidak terpotong keras.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(LayarUtama.gradienTinggi)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                    ),
                ),
        )

        AnimatedVisibility(
            visible = state.undo == null && state.pesan == null,
            modifier = Modifier.align(Alignment.BottomEnd),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            FabTambah(
                onClick = onTambah,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(end = Spasi.layar, bottom = LayarUtama.fabMarginBawah),
            )
        }

        state.undo?.let { undo ->
            val pengeluaran = undo.transaksi.tipe == TipeTransaksi.PENGELUARAN
            val nominal = if (pengeluaran) {
                Rupiah.pengeluaran(undo.transaksi.nominal)
            } else {
                Rupiah.pemasukan(undo.transaksi.nominal)
            }
            KartuUrungkan(
                detail = "${undo.namaKategori} · $nominal",
                onUrungkan = onUrungkanHapus,
                key = undo.transaksi.id,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = Spasi.layar, vertical = KartuAksi.marginBawah)
                    .fillMaxWidth(),
            )
        }

        if (state.undo == null) {
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
}

// ─────────────────────────────── Header ───────────────────────────────

@Composable
private fun Header(
    labelBulan: String,
    bisaMundur: Boolean,
    bisaMaju: Boolean,
    onBulanSebelumnya: () -> Unit,
    onBulanBerikutnya: () -> Unit,
    onEkspor: () -> Unit,
    onKelolaKategori: () -> Unit,
    onPengaturan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(LayarUtama.headerTinggi),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TombolIkon(
            ikon = R.drawable.ic_chevron_left,
            deskripsi = "Bulan sebelumnya",
            onClick = onBulanSebelumnya,
            aktif = bisaMundur,
        )
        Text(
            text = labelBulan,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.width(LayarUtama.labelBulanMin),
        )
        TombolIkon(
            ikon = R.drawable.ic_chevron_right,
            deskripsi = "Bulan berikutnya",
            onClick = onBulanBerikutnya,
            aktif = bisaMaju,
        )

        Spacer(Modifier.weight(1f))

        TombolIkon(
            ikon = R.drawable.ic_ekspor,
            deskripsi = "Ekspor laporan",
            onClick = onEkspor,
        )

        Box {
            var menuTerbuka by remember { mutableStateOf(false) }
            TombolIkon(
                ikon = R.drawable.ic_more_vert,
                deskripsi = "Menu lainnya",
                onClick = { menuTerbuka = true },
            )
            DropdownMenu(
                expanded = menuTerbuka,
                onDismissRequest = { menuTerbuka = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Kelola kategori") },
                    onClick = { menuTerbuka = false; onKelolaKategori() },
                )
                DropdownMenuItem(
                    text = { Text("Pengaturan") },
                    onClick = { menuTerbuka = false; onPengaturan() },
                )
            }
        }
    }
}

// ────────────────────────── Kartu ringkasan ──────────────────────────

@Composable
private fun KartuRingkasan(
    sisaBulan: Long,
    totalPemasukan: Long,
    totalPengeluaran: Long,
) {
    val warna = Warna.current
    KartuDuit(bentuk = Sudut.kartu, isi = Spasi.xl) {
        Text(
            text = "Sisa bulan ini",
            style = MaterialTheme.typography.labelLarge,
            color = warna.teksRedup,
        )
        Spacer(Modifier.height(Spasi.xs))
        Text(
            text = rupiahBertanda(sisaBulan),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(LayarUtama.ringkasanPemisahAtas))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(LayarUtama.ringkasanPemisahBawah))

        Row(horizontalArrangement = Arrangement.spacedBy(Spasi.m)) {
            KolomRingkas(
                ikon = R.drawable.ic_masuk,
                latar = warna.pemasukanLembut,
                warnaTeks = warna.pemasukan,
                label = "Masuk",
                nominal = totalPemasukan,
                modifier = Modifier.weight(1f),
            )
            KolomRingkas(
                ikon = R.drawable.ic_keluar,
                latar = warna.pengeluaranLembut,
                warnaTeks = warna.pengeluaran,
                label = "Keluar",
                nominal = totalPengeluaran,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun KolomRingkas(
    @DrawableRes ikon: Int,
    latar: Color,
    warnaTeks: Color,
    label: String,
    nominal: Long,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        LingkaranIkon(
            ikon = ikon,
            latar = latar,
            ukuran = Ukuran.ikonRingkasan,
            sudut = Ukuran.ikonRingkasanSudut,
            ikonUkuran = Ukuran.ikon,
            tint = warnaTeks,
        )
        Spacer(Modifier.width(Spasi.s))
        Column {
            Text(label, style = GayaKolomLabel, color = Warna.current.teksRedup)
            Text(Rupiah.format(nominal), style = GayaNominalBaris, color = warnaTeks)
        }
    }
}

// ─────────────────────── Blok "per kategori" ────────────────────────

@Composable
private fun BlokRincian(rincian: List<RekapKategori>) {
    var terlipat by rememberSaveable { mutableStateOf(false) }
    val putaran by animateFloatAsState(if (terlipat) -90f else 0f, label = "putaran chevron")

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Pengeluaran per kategori",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TombolIkon(
                ikon = R.drawable.ic_chevron_down,
                deskripsi = if (terlipat) "Buka rincian" else "Tutup rincian",
                onClick = { terlipat = !terlipat },
                modifier = Modifier.rotate(putaran),
            )
        }
        Spacer(Modifier.height(Spasi.s))

        AnimatedVisibility(visible = !terlipat) {
            KartuDuit(bentuk = Sudut.baris, isi = LayarUtama.rincianIsi) {
                val tampil = rincian.take(3)
                tampil.forEachIndexed { i, r ->
                    if (i > 0) Spacer(Modifier.height(LayarUtama.rincianAntarBaris))
                    BarisRincian(r)
                }
                if (rincian.size > 3) {
                    Spacer(Modifier.height(LayarUtama.rincianAntarBaris))
                    val sisa = rincian.drop(3)
                    Text(
                        text = "+${sisa.size} kategori lain · ${Rupiah.format(sisa.sumOf { it.total })}",
                        style = GayaAngkaKecil,
                        color = Warna.current.teksRedup,
                    )
                }
            }
        }
    }
}

@Composable
private fun BarisRincian(r: RekapKategori) {
    val warnaKategori = Kategori.warnaHex(r.colorHex)
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LingkaranIkon(
                ikon = Kategori.ikon(r.iconKey),
                latar = warnaKategori,
                ukuran = Ukuran.ikonKategori,
                sudut = Ukuran.ikonKategoriSudut,
                ikonUkuran = Ukuran.ikonDalamKategori,
            )
            Spacer(Modifier.width(Spasi.m))
            Text(
                text = r.nama,
                style = GayaNamaRincian,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = Rupiah.format(r.total),
                style = GayaNominalRincian,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(Spasi.s))
            Text(
                text = "${r.persen}%",
                style = GayaAngkaKecil,
                color = Warna.current.teksRedup,
                textAlign = TextAlign.End,
                modifier = Modifier.width(LayarUtama.persenLebar),
            )
        }
        Spacer(Modifier.height(LayarUtama.rincianJarakBar))
        BarProporsi(fraksi = r.persen / 100f, warna = warnaKategori)
    }
}

@Composable
private fun BarProporsi(fraksi: Float, warna: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(Ukuran.barProporsi)
            .background(Warna.current.track, CircleShape),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraksi.coerceIn(0f, 1f))
                .height(Ukuran.barProporsi)
                .background(warna, CircleShape),
        )
    }
}

// ──────────────────────── Grafik pengeluaran & saldo ────────────────────
// Fitur alert/grafik — di luar tujuh layar design-spec asli. Toggle
// Harian/Bulanan disimpan lokal (bukan di UtamaState) karena keduanya sudah
// dihitung sekali di ViewModel; berpindah cuma memilih data yang mana yang
// ditampilkan, tak perlu query ulang.

@Composable
private fun BlokGrafikPengeluaran(grafik: DataGrafik) {
    var rentang by rememberSaveable { mutableStateOf(RentangGrafik.HARIAN) }
    KartuDuit(bentuk = Sudut.kartu, isi = Spasi.xl) {
        Text(
            text = "Grafik pengeluaran",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spasi.m))
        ToggleRentangGrafik(rentang, onPilih = { rentang = it })
        Spacer(Modifier.height(Spasi.l))
        val data = if (rentang == RentangGrafik.HARIAN) grafik.pengeluaranHarian else grafik.pengeluaranBulanan
        GrafikBatang(
            data = data,
            warna = Warna.current.pengeluaran,
            labelSetiap = if (rentang == RentangGrafik.HARIAN) 5 else 1,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BlokGrafikSaldo(grafik: DataGrafik) {
    var rentang by rememberSaveable { mutableStateOf(RentangGrafik.HARIAN) }
    KartuDuit(bentuk = Sudut.kartu, isi = Spasi.xl) {
        Text(
            text = "Grafik saldo tersisa",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spasi.m))
        ToggleRentangGrafik(rentang, onPilih = { rentang = it })
        Spacer(Modifier.height(Spasi.l))
        val data = if (rentang == RentangGrafik.HARIAN) grafik.saldoHarian else grafik.saldoBulanan
        GrafikGaris(
            data = data,
            labelSetiap = if (rentang == RentangGrafik.HARIAN) 5 else 1,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ToggleRentangGrafik(terpilih: RentangGrafik, onPilih: (RentangGrafik) -> Unit) {
    SegmentedDua(
        opsiSatu = "Harian",
        opsiDua = "Bulanan",
        satuAktif = terpilih == RentangGrafik.HARIAN,
        onPilihSatu = { onPilih(RentangGrafik.HARIAN) },
        onPilihDua = { onPilih(RentangGrafik.BULANAN) },
    )
}

// ──────────────────────── Daftar transaksi ─────────────────────────

private fun LazyListScope.itemsHari(
    hari: List<GrupHari>,
    onTransaksiDiklik: (Long) -> Unit,
    onHapusTransaksi: (Long) -> Unit,
) {
    hari.forEach { grup ->
        item(key = "hari-${grup.tanggal}") {
            Spacer(Modifier.height(Spasi.antarBlok))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spasi.xs, vertical = Spasi.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = grup.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (grup.subtotalPengeluaran > 0L) {
                    Text(
                        text = Rupiah.format(grup.subtotalPengeluaran),
                        style = GayaAngkaKecil,
                        color = Warna.current.teksRedup,
                    )
                }
            }
        }
        item(key = "kartu-${grup.tanggal}") {
            KartuDuit(bentuk = Sudut.baris, isi = LayarUtama.kartuBarisIsi) {
                grup.transaksi.forEach { t ->
                    // Kunci per-id: tanpa ini state geser bisa "menempel" ke baris
                    // lain saat satu baris terhapus (komposisi berdasar posisi).
                    key(t.id) {
                        BarisTransaksiGeser(
                            t = t,
                            onClick = { onTransaksiDiklik(t.id) },
                            onHapus = { onHapusTransaksi(t.id) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Baris transaksi + geser ke kiri untuk menghapus (design-spec §3). Setelah
 * dilepas melewati ambang, [onHapus] dipanggil; baris lalu hilang sendiri karena
 * daftar mengamati Flow Room.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarisTransaksiGeser(t: BarisTransaksi, onClick: () -> Unit, onHapus: () -> Unit) {
    val geser = rememberSwipeToDismissBoxState()

    LaunchedEffect(geser.currentValue) {
        if (geser.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onHapus()
        }
    }

    SwipeToDismissBox(
        state = geser,
        enableDismissFromStartToEnd = false,
        backgroundContent = { PanelHapus() },
    ) {
        Box(Modifier.background(MaterialTheme.colorScheme.surface)) {
            BarisTransaksiItem(t, onClick)
        }
    }
}

@Composable
private fun PanelHapus() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Warna.current.pengeluaran),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            modifier = Modifier.width(KartuAksi.panelGeserHapus),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hapus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(Ukuran.ikon),
            )
            Spacer(Modifier.height(Spasi.xs))
            Text("Hapus", style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

@Composable
private fun BarisTransaksiItem(t: BarisTransaksi, onClick: () -> Unit) {
    val warna = Warna.current
    val pengeluaran = t.tipe == TipeTransaksi.PENGELUARAN
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent, Sudut.field)
            .clickable(onClick = onClick)
            .padding(
                horizontal = LayarUtama.barisTxHorizontal,
                vertical = LayarUtama.barisTxVertikal,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LingkaranIkon(
            ikon = Kategori.ikon(t.iconKey),
            latar = Kategori.warnaHex(t.colorHex),
            ukuran = Ukuran.ikonBaris,
            sudut = Ukuran.ikonBarisSudut,
            ikonUkuran = Ukuran.ikon,
        )
        Spacer(Modifier.width(LayarUtama.barisIkonJarak))
        Column(Modifier.weight(1f)) {
            Text(
                text = t.namaKategori,
                style = GayaNamaBaris,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!t.catatan.isNullOrBlank()) {
                Text(
                    text = t.catatan,
                    style = GayaCatatan,
                    color = warna.teksRedup,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(Spasi.s))
        Text(
            text = if (pengeluaran) Rupiah.pengeluaran(t.nominal) else Rupiah.pemasukan(t.nominal),
            style = GayaNominalBaris,
            color = if (pengeluaran) warna.pengeluaran else warna.pemasukan,
        )
    }
}

// ──────────────────────────── FAB & kosong ─────────────────────────

@Composable
private fun FabTambah(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Ukuran.fabSudut),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 3.dp,
        modifier = modifier.size(Ukuran.fab),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = "Tambah transaksi",
                modifier = Modifier.size(Ukuran.fabIkon),
            )
        }
    }
}

@Composable
private fun KartuInfoLokal(onMengerti: () -> Unit, modifier: Modifier = Modifier) {
    val warna = Warna.current
    KartuDuit(modifier = modifier, bentuk = Sudut.kartu, isi = Spasi.l) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LingkaranIkon(
                ikon = R.drawable.ic_gembok,
                latar = warna.pemasukanLembut,
                tint = warna.pemasukan,
                ukuran = Ukuran.ikonRingkasan,
                sudut = Ukuran.ikonRingkasanSudut,
            )
            Spacer(Modifier.width(Spasi.m))
            Text(
                text = "Data hanya tersimpan di HP ini",
                style = GayaNamaBaris,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(Spasi.s))
        Text(
            text = "Tidak ada akun, tidak ada cloud. Kalau HP hilang atau aplikasi dihapus, " +
                "catatan ikut hilang. Ekspor Excel berkala kalau butuh arsip.",
            style = GayaCatatan,
            color = warna.teksRedup,
        )
        Spacer(Modifier.height(Spasi.s))
        TextButton(onClick = onMengerti, modifier = Modifier.align(Alignment.End)) {
            Text("Mengerti")
        }
    }
}

@Composable
private fun KondisiKosong(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = Spasi.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_catatan),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(LayarUtama.ikonKosong),
        )
        Spacer(Modifier.height(Spasi.l))
        Text(
            text = "Belum ada catatan bulan ini",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spasi.s))
        Text(
            text = "Catat pengeluaran pertamamu — cukup nominal dan kategori, di bawah 10 detik.",
            style = MaterialTheme.typography.bodyMedium,
            color = Warna.current.teksRedup,
            textAlign = TextAlign.Center,
        )
    }
}

/** "Rp 3.450.000" atau "−Rp 120.000" untuk nilai yang bisa negatif. */
private fun rupiahBertanda(nominal: Long): String =
    if (nominal < 0L) "−" + Rupiah.format(-nominal) else Rupiah.format(nominal)

// ──────────────────────────────── Preview ──────────────────────────────

private fun contohState(): UtamaState = UtamaState(
    bulan = YearMonth.of(2026, 8),
    labelBulan = "Agustus 2026",
    bisaMundur = true,
    bisaMaju = false,
    sisaBulan = 3_450_000L,
    totalPemasukan = 8_500_000L,
    totalPengeluaran = 5_050_000L,
    rincian = listOf(
        RekapKategori(1, "Makan & Minum", "makan", "#E0785C", 1_850_000L, 24, 37),
        RekapKategori(2, "Transportasi", "transportasi", "#5E8CB5", 920_000L, 12, 18),
        RekapKategori(3, "Belanja", "belanja", "#B57BA6", 780_000L, 5, 15),
        RekapKategori(4, "Tagihan", "tagihan", "#D9A441", 700_000L, 3, 14),
        RekapKategori(5, "Hiburan", "hiburan", "#8A7CC8", 800_000L, 6, 16),
    ),
    hari = listOf(
        GrupHari(
            tanggal = LocalDate.of(2026, 8, 28),
            label = "Hari ini",
            subtotalPengeluaran = 92_000L,
            transaksi = listOf(
                BarisTransaksi(1, "Makan & Minum", "makan", "#E0785C", "Makan siang di kantor", 35_000L, TipeTransaksi.PENGELUARAN),
                BarisTransaksi(2, "Transportasi", "transportasi", "#5E8CB5", "Ojek pulang", 22_000L, TipeTransaksi.PENGELUARAN),
                BarisTransaksi(3, "Makan & Minum", "makan", "#E0785C", "Kopi sore", 35_000L, TipeTransaksi.PENGELUARAN),
            ),
        ),
        GrupHari(
            tanggal = LocalDate.of(2026, 8, 27),
            label = "Kemarin",
            subtotalPengeluaran = 0L,
            transaksi = listOf(
                BarisTransaksi(4, "Gaji", "gaji", "#4E9E6A", "Gaji Agustus", 8_500_000L, TipeTransaksi.PEMASUKAN),
            ),
        ),
        GrupHari(
            tanggal = LocalDate.of(2026, 8, 25),
            label = "Sen, 25 Agu 2026",
            subtotalPengeluaran = 265_000L,
            transaksi = listOf(
                BarisTransaksi(5, "Belanja", "belanja", "#B57BA6", null, 265_000L, TipeTransaksi.PENGELUARAN),
            ),
        ),
    ),
    undo = null,
    pesan = null,
    tampilkanInfoLokal = false,
    grafik = DataGrafik(
        pengeluaranHarian = listOf(
            45_000L, 0L, 92_000L, 60_000L, 0L, 150_000L, 35_000L, 0L, 80_000L, 22_000L,
            0L, 65_000L, 110_000L, 0L, 40_000L, 55_000L, 0L, 200_000L, 30_000L, 0L,
            75_000L, 0L, 95_000L, 60_000L, 0L, 265_000L, 0L, 92_000L,
        ).mapIndexed { i, nilai -> TitikGrafik((i + 1).toString(), nilai) },
        pengeluaranBulanan = listOf(
            "Mar" to 4_200_000L, "Apr" to 3_800_000L, "Mei" to 5_100_000L,
            "Jun" to 4_650_000L, "Jul" to 5_800_000L, "Agu" to 5_050_000L,
        ).map { (label, nilai) -> TitikGrafik(label, nilai) },
        saldoHarian = run {
            var kumulatif = 0L
            listOf(
                8_500_000L, -45_000L, -92_000L, -60_000L, 0L, -150_000L, -35_000L, 0L, -80_000L, -22_000L,
                0L, -65_000L, -110_000L, 0L, -40_000L, -55_000L, 0L, -200_000L, -30_000L, 0L,
                -75_000L, 0L, -95_000L, -60_000L, 0L, -265_000L, 0L, -92_000L,
            ).mapIndexed { i, delta ->
                kumulatif += delta
                TitikGrafik((i + 1).toString(), kumulatif)
            }
        },
        saldoBulanan = listOf(
            "Mar" to 1_200_000L, "Apr" to (-350_000L), "Mei" to 2_100_000L,
            "Jun" to 900_000L, "Jul" to (-600_000L), "Agu" to 3_450_000L,
        ).map { (label, nilai) -> TitikGrafik(label, nilai) },
    ),
    memuat = false,
)

private val CONTOH_UNDO = HapusTertunda(
    transaksi = Transaksi(
        id = 42,
        nominal = 35_000L,
        tipe = TipeTransaksi.PENGELUARAN,
        kategoriId = 1,
        tanggal = 0,
        catatan = "Kopi sore",
        dibuatPada = 0,
        diubahPada = 0,
    ),
    namaKategori = "Makan & Minum",
)

@Composable
private fun UtamaPreviewIsi(state: UtamaState, gelap: Boolean) {
    DuitTheme(gelap = gelap) {
        UtamaScreen(
            state = state,
            onBulanSebelumnya = {},
            onBulanBerikutnya = {},
            onTambah = {},
            onEkspor = {},
            onKelolaKategori = {},
            onPengaturan = {},
            onTransaksiDiklik = {},
            onHapusTransaksi = {},
            onUrungkanHapus = {},
            onInfoDimengerti = {},
        )
    }
}

@Preview(name = "Terang", showBackground = true, heightDp = 900)
@Composable
private fun UtamaPreviewTerang() = UtamaPreviewIsi(contohState(), gelap = false)

@Preview(name = "Gelap", showBackground = true, heightDp = 900)
@Composable
private fun UtamaPreviewGelap() = UtamaPreviewIsi(contohState(), gelap = true)

@Preview(name = "Kosong — terang", showBackground = true, heightDp = 900)
@Composable
private fun UtamaPreviewKosong() =
    UtamaPreviewIsi(contohState().copy(rincian = emptyList(), hari = emptyList()), gelap = false)

@Preview(name = "Urungkan hapus — gelap", showBackground = true, heightDp = 900)
@Composable
private fun UtamaPreviewUndo() = UtamaPreviewIsi(contohState().copy(undo = CONTOH_UNDO), gelap = true)
