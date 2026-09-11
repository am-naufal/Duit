package com.alenza.duit.data

import java.time.LocalDate
import java.time.YearMonth

/**
 * Rentang tanggal satu "bulan berjalan". Sesuai Pengaturan §8, bulan berjalan
 * bisa mulai bukan tanggal 1 kalau pengguna mengatur `hariAwalBulan` (1..28) —
 * berguna kalau gajian bukan di awal bulan.
 *
 * [label] adalah bulan-kalender tempat periode ini *mulai* dan dipakai untuk
 * penamaan ("Agustus 2026") serta navigasi maju/mundur; [awal]..[akhir] adalah
 * rentang tanggal sebenarnya yang dipakai untuk query data.
 *
 * Contoh: `hariAwalBulan = 25`, `label = Agustus 2026` → [awal] 25 Agu 2026,
 * [akhir] 24 Sep 2026. Kalau `hariAwalBulan = 1` (default), periode ini sama
 * persis dengan bulan kalender biasa.
 */
data class Periode(val label: YearMonth, val awal: LocalDate, val akhir: LocalDate) {

    companion object {
        /** Bangun periode yang mulai tanggal [hariAwalBulan] pada bulan [label]. */
        fun dariLabel(label: YearMonth, hariAwalBulan: Int): Periode {
            val awal = label.atDay(hariAwalBulan)
            val akhir = label.plusMonths(1).atDay(hariAwalBulan).minusDays(1)
            return Periode(label, awal, akhir)
        }

        /** [label] periode yang memuat [tanggal], dengan aturan hari awal bulan ini. */
        fun labelUntuk(tanggal: LocalDate, hariAwalBulan: Int): YearMonth =
            if (tanggal.dayOfMonth >= hariAwalBulan) {
                YearMonth.from(tanggal)
            } else {
                YearMonth.from(tanggal).minusMonths(1)
            }

        /** Periode "sekarang" — yang memuat hari ini. */
        fun sekarang(hariAwalBulan: Int): Periode =
            dariLabel(labelUntuk(LocalDate.now(), hariAwalBulan), hariAwalBulan)
    }
}
