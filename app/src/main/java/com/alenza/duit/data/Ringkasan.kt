package com.alenza.duit.data

/** Rekap pengeluaran satu kategori dalam sebuah bulan. */
data class RekapKategori(
    val kategoriId: Long,
    val nama: String,
    val iconKey: String,
    val colorHex: String,
    val total: Long,
    val jumlahTransaksi: Int,
    /** 0..100, dibulatkan dengan metode sisa terbesar; jumlah semua = 100 persis. */
    val persen: Int,
)

/** Ringkasan bulan berjalan (PRD §F-5). */
data class RingkasanBulanan(
    val totalPemasukan: Long,
    val totalPengeluaran: Long,
    /** Pemasukan − pengeluaran. Bisa negatif. */
    val selisih: Long,
    /** Urut dari nominal terbesar; hanya kategori yang punya pengeluaran bulan ini. */
    val rekapPengeluaran: List<RekapKategori>,
)

object Ringkasan {

    /**
     * Hitung ringkasan dari daftar transaksi satu bulan dan seluruh kategori.
     * Fungsi murni — tidak menyentuh Room — supaya bisa diuji langsung di JVM.
     */
    fun hitung(transaksi: List<Transaksi>, kategori: List<Kategori>): RingkasanBulanan {
        val namaKategori = kategori.associateBy { it.id }

        val totalPemasukan = transaksi
            .filter { it.tipe == TipeTransaksi.PEMASUKAN }
            .sumOf { it.nominal }
        val totalPengeluaran = transaksi
            .filter { it.tipe == TipeTransaksi.PENGELUARAN }
            .sumOf { it.nominal }

        val perKategori = transaksi
            .filter { it.tipe == TipeTransaksi.PENGELUARAN }
            .groupBy { it.kategoriId }
            .map { (kategoriId, daftar) ->
                val kat = namaKategori[kategoriId]
                RekapKategori(
                    kategoriId = kategoriId,
                    nama = kat?.nama ?: "Lainnya",
                    iconKey = kat?.iconKey ?: "lainnya",
                    colorHex = kat?.colorHex ?: "#94989E",
                    total = daftar.sumOf { it.nominal },
                    jumlahTransaksi = daftar.size,
                    persen = 0,
                )
            }
            .sortedWith(compareByDescending<RekapKategori> { it.total }.thenBy { it.nama })

        return RingkasanBulanan(
            totalPemasukan = totalPemasukan,
            totalPengeluaran = totalPengeluaran,
            selisih = totalPemasukan - totalPengeluaran,
            rekapPengeluaran = bagiPersen(perKategori, totalPengeluaran),
        )
    }

    /**
     * Metode sisa terbesar (largest remainder): tiap nilai dibulatkan ke bawah
     * dulu, lalu selisih menuju 100 dibagikan ke baris dengan sisa pecahan
     * terbesar. Hasilnya selalu berjumlah 100 persis dan tidak pernah > 100
     * (design-spec §1).
     */
    private fun bagiPersen(rekap: List<RekapKategori>, total: Long): List<RekapKategori> {
        if (rekap.isEmpty() || total <= 0L) {
            return rekap.map { it.copy(persen = 0) }
        }

        val bawah = rekap.map { ((it.total * 100L) / total).toInt() }
        val sisa = rekap.mapIndexed { i, r -> i to (r.total * 100L) % total }
        var kurang = 100 - bawah.sum()

        val tambahan = IntArray(rekap.size)
        for ((i, _) in sisa.sortedWith(
            compareByDescending<Pair<Int, Long>> { it.second }
                .thenByDescending { rekap[it.first].total }
                .thenBy { rekap[it.first].kategoriId },
        )) {
            if (kurang <= 0) break
            tambahan[i] = 1
            kurang--
        }

        return rekap.mapIndexed { i, r -> r.copy(persen = bawah[i] + tambahan[i]) }
    }
}
