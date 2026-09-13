package com.alenza.duit.ui.layar.ekspor

import java.time.YearMonth

enum class FaseEkspor { SIAP, MENULIS, SELESAI, GAGAL }

data class EksporState(
    val bulan: YearMonth,
    /** "Agustus 2026". */
    val labelBulan: String,
    /** "Laporan-Keuangan-2026-08.xlsx". */
    val namaFile: String,
    /** null selama data masih dimuat. */
    val jumlahTransaksi: Int?,
    val kosong: Boolean,
    val fase: FaseEkspor,
    /** 0f..1f, hanya relevan saat [fase] == MENULIS. */
    val progres: Float,
    val pesanGalat: String?,
) {
    val labelBulanPendek: String get() = labelBulan.substringBefore(' ')

    companion object {
        fun awal(bulan: YearMonth, label: String, namaFile: String) = EksporState(
            bulan = bulan,
            labelBulan = label,
            namaFile = namaFile,
            jumlahTransaksi = null,
            kosong = false,
            fase = FaseEkspor.SIAP,
            progres = 0f,
            pesanGalat = null,
        )
    }
}
