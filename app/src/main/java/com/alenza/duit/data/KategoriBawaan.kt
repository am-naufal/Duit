package com.alenza.duit.data

/**
 * 12 kategori bawaan (PRD §F-2). Diisi sekali saat database dibuat pertama kali —
 * lihat [DuitDatabase]. Warna diambil dari palet di
 * `com.alenza.duit.ui.theme.Kategori`; `iconKey` cocok dengan kunci di sana.
 *
 * "Lainnya" ada di kedua tipe dan bertanda `sistem = true`: tidak bisa dihapus
 * maupun diarsipkan, jadi tujuan fallback selalu tersedia.
 */
object KategoriBawaan {

    val daftar: List<Kategori> = buildList {
        var urut = 0
        fun tambah(nama: String, tipe: TipeTransaksi, key: String, warna: String, sistem: Boolean = false) {
            add(
                Kategori(
                    nama = nama,
                    tipe = tipe,
                    iconKey = key,
                    colorHex = warna,
                    sistem = sistem,
                    urutan = urut++,
                ),
            )
        }

        tambah("Makan & Minum", TipeTransaksi.PENGELUARAN, "makan", "#E0785C")
        tambah("Transportasi", TipeTransaksi.PENGELUARAN, "transportasi", "#5E8CB5")
        tambah("Belanja", TipeTransaksi.PENGELUARAN, "belanja", "#B57BA6")
        tambah("Tagihan", TipeTransaksi.PENGELUARAN, "tagihan", "#D9A441")
        tambah("Kesehatan", TipeTransaksi.PENGELUARAN, "kesehatan", "#6BA88C")
        tambah("Hiburan", TipeTransaksi.PENGELUARAN, "hiburan", "#8A7CC8")
        tambah("Pendidikan", TipeTransaksi.PENGELUARAN, "pendidikan", "#3F9AA6")
        tambah("Lainnya", TipeTransaksi.PENGELUARAN, "lainnya", "#94989E", sistem = true)

        urut = 0
        tambah("Gaji", TipeTransaksi.PEMASUKAN, "gaji", "#4E9E6A")
        tambah("Bonus", TipeTransaksi.PEMASUKAN, "bonus", "#CF8B5B")
        tambah("Hadiah", TipeTransaksi.PEMASUKAN, "hadiah", "#C76A8A")
        tambah("Lainnya", TipeTransaksi.PEMASUKAN, "lainnya", "#94989E", sistem = true)
    }
}
