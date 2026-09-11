package com.alenza.duit.ui.komponen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.alenza.duit.ui.layar.utama.TitikGrafik
import com.alenza.duit.ui.theme.LayarUtamaGrafik
import com.alenza.duit.ui.theme.Warna

/**
 * Grafik batang sederhana — dipakai untuk tren pengeluaran (fitur alert/grafik
 * di Layar utama, di luar design-spec asli). Semua nilai diasumsikan ≥ 0.
 * [labelSetiap] mengendalikan berapa titik yang label sumbu-X-nya ditampilkan
 * (mis. 5 supaya tak berjejal saat data harian sebulan penuh).
 */
@Composable
fun GrafikBatang(
    data: List<TitikGrafik>,
    warna: Color,
    modifier: Modifier = Modifier,
    labelSetiap: Int = 1,
) {
    Column(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(LayarUtamaGrafik.tinggiBatang),
        ) {
            if (data.isEmpty()) return@Canvas
            val maks = data.maxOf { it.nilai }.coerceAtLeast(1L).toFloat()
            val lebarSlot = size.width / data.size
            val jarakPx = LayarUtamaGrafik.jarakBar.toPx()
            val lebarBar = (lebarSlot - jarakPx).coerceAtLeast(1f)
            val sudutPx = LayarUtamaGrafik.barSudut.toPx()
            data.forEachIndexed { i, titik ->
                val tinggiBar = (titik.nilai / maks) * size.height
                val x = i * lebarSlot + (lebarSlot - lebarBar) / 2f
                drawRoundRect(
                    color = warna,
                    topLeft = Offset(x, size.height - tinggiBar),
                    size = Size(lebarBar, tinggiBar.coerceAtLeast(2f)),
                    cornerRadius = CornerRadius(sudutPx, sudutPx),
                )
            }
        }
        LabelSumbuX(data, labelSetiap)
    }
}

/**
 * Grafik garis untuk tren saldo — bisa negatif. Garis penghubung netral
 * (`onSurfaceVariant`), titik diwarnai hijau (pemasukan) kalau nilainya ≥ 0
 * dan merah (pengeluaran) kalau < 0. Garis nol putus-putus muncul hanya kalau
 * datanya benar-benar melintasi nol, supaya tak berisik saat semua titik
 * bertanda sama.
 */
@Composable
fun GrafikGaris(
    data: List<TitikGrafik>,
    modifier: Modifier = Modifier,
    labelSetiap: Int = 1,
) {
    val warnaPositif = Warna.current.pemasukan
    val warnaNegatif = Warna.current.pengeluaran
    val warnaGaris = MaterialTheme.colorScheme.onSurfaceVariant
    val warnaBaseline = MaterialTheme.colorScheme.outlineVariant

    Column(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(LayarUtamaGrafik.tinggiGaris),
        ) {
            if (data.isEmpty()) return@Canvas
            val nilaiMaks = data.maxOf { it.nilai }
            val nilaiMin = data.minOf { it.nilai }
            val rentang = (nilaiMaks - nilaiMin).coerceAtLeast(1L).toFloat()
            val jarakX = if (data.size > 1) size.width / (data.size - 1) else 0f
            val radiusPx = LayarUtamaGrafik.titikRadius.toPx()
            val tebalPx = LayarUtamaGrafik.garisTebal.toPx()

            fun yUntuk(nilai: Long): Float {
                val proporsi = (nilai - nilaiMin) / rentang
                return size.height - proporsi * size.height
            }

            if (nilaiMin < 0L && nilaiMaks > 0L) {
                val yNol = yUntuk(0L)
                drawLine(
                    color = warnaBaseline,
                    start = Offset(0f, yNol),
                    end = Offset(size.width, yNol),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                )
            }

            val titik = data.mapIndexed { i, t -> Offset(i * jarakX, yUntuk(t.nilai)) }
            for (i in 0 until titik.size - 1) {
                drawLine(
                    color = warnaGaris,
                    start = titik[i],
                    end = titik[i + 1],
                    strokeWidth = tebalPx,
                    cap = StrokeCap.Round,
                )
            }
            titik.forEachIndexed { i, offset ->
                val warnaTitik = if (data[i].nilai < 0L) warnaNegatif else warnaPositif
                drawCircle(color = warnaTitik, radius = radiusPx, center = offset)
            }
        }
        LabelSumbuX(data, labelSetiap)
    }
}

@Composable
private fun LabelSumbuX(data: List<TitikGrafik>, labelSetiap: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(LayarUtamaGrafik.tinggiLabelSumbu),
    ) {
        data.forEachIndexed { i, titik ->
            // Label pertama/terakhir rata ke tepi luar (bukan tengah slotnya)
            // supaya angka dua digit tak terpotong kartu — slot di kedua ujung
            // tak punya tetangga kosong untuk "meminjam" ruang seperti di tengah.
            val perataan = when (i) {
                0 -> Alignment.CenterStart
                data.lastIndex -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            Box(Modifier.weight(1f), contentAlignment = perataan) {
                if (i % labelSetiap == 0 || i == data.lastIndex) {
                    // softWrap=false + maxLines=1: slotnya sempit (bisa sampai
                    // 1/30 lebar layar untuk grafik harian sebulan penuh) —
                    // tanpa ini angka dua digit ("16", "21", dst.) melipat ke
                    // baris kedua yang lolos dari tinggi kotak berlabel dan
                    // jadi tak kelihatan (cuma digit pertama yang tampil).
                    Text(
                        text = titik.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Warna.current.teksRedup,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
