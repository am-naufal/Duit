package com.alenza.duit.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Satu-satunya pintu ke data. UI mengamati `Flow` dari sini; sumber kebenaran
 * tetap Room (CLAUDE.md aturan 6). Tidak menyimpan salinan state.
 */
class DuitRepository(
    private val transaksiDao: TransaksiDao,
    private val kategoriDao: KategoriDao,
    private val jamMillis: () -> Long = System::currentTimeMillis,
) {

    fun kategoriAktif(tipe: TipeTransaksi): Flow<List<Kategori>> = kategoriDao.amatiAktif(tipe)

    fun semuaKategori(): Flow<List<Kategori>> = kategoriDao.amatiSemua()

    fun kategoriArsip(tipe: TipeTransaksi): Flow<List<Kategori>> = kategoriDao.amatiArsip(tipe)

    /** Peta kategoriId → jumlah transaksi. Kategori tanpa transaksi tidak muncul. */
    fun jumlahPerKategori(): Flow<Map<Long, Int>> =
        transaksiDao.amatiJumlahPerKategori().map { baris ->
            baris.associate { it.kategoriId to it.jumlah }
        }

    fun transaksiPeriode(periode: Periode): Flow<List<Transaksi>> =
        transaksiDao.amatiRentang(periode.awal.toEpochDay(), periode.akhir.toEpochDay())

    /** Rentang tanggal bebas (bukan satu [Periode]) — untuk grafik tren beberapa bulan. */
    fun transaksiRentangTanggal(awal: LocalDate, akhir: LocalDate): Flow<List<Transaksi>> =
        transaksiDao.amatiRentang(awal.toEpochDay(), akhir.toEpochDay())

    fun ringkasanPeriode(periode: Periode): Flow<RingkasanBulanan> =
        combine(transaksiPeriode(periode), kategoriDao.amatiSemua()) { tx, kat ->
            Ringkasan.hitung(tx, kat)
        }

    /** Transaksi satu periode, sekali ambil, urut kronologis — untuk ekspor. */
    suspend fun transaksiPeriodeSekali(periode: Periode): List<Transaksi> =
        transaksiDao.ambilRentang(periode.awal.toEpochDay(), periode.akhir.toEpochDay())

    suspend fun semuaKategoriSekali(): List<Kategori> = kategoriDao.ambilSemua()

    fun bulanTerawal(): Flow<Long?> = transaksiDao.amatiTanggalTerawal()

    fun bulanTerakhir(): Flow<Long?> = transaksiDao.amatiTanggalTerakhir()

    suspend fun transaksi(id: Long): Transaksi? = transaksiDao.ambil(id)

    suspend fun simpan(transaksi: Transaksi): Long {
        val now = jamMillis()
        return if (transaksi.id == 0L) {
            transaksiDao.sisip(transaksi.copy(dibuatPada = now, diubahPada = now))
        } else {
            transaksiDao.perbarui(transaksi.copy(diubahPada = now))
            transaksi.id
        }
    }

    /** Menyisipkan kembali transaksi apa adanya (termasuk id) untuk aksi "Urungkan". */
    suspend fun pulihkan(transaksi: Transaksi) {
        transaksiDao.sisip(transaksi)
    }

    suspend fun hapus(transaksi: Transaksi) = transaksiDao.hapus(transaksi)

    suspend fun kategori(id: Long): Kategori? = kategoriDao.ambil(id)

    suspend fun simpanKategori(kategori: Kategori): Long =
        if (kategori.id == 0L) kategoriDao.sisip(kategori)
        else {
            kategoriDao.perbarui(kategori); kategori.id
        }

    /** true kalau nama sudah dipakai kategori lain di tipe yang sama (PRD F-2). */
    suspend fun namaKategoriBentrok(nama: String, tipe: TipeTransaksi, kecualiId: Long): Boolean =
        kategoriDao.adaNamaLain(nama.trim(), tipe, kecualiId) > 0

    suspend fun urutanKategoriBerikutnya(tipe: TipeTransaksi): Int =
        kategoriDao.urutanBerikutnya(tipe)

    /** Simpan urutan baru setelah pengguna menyeret kategori. */
    suspend fun urutkanKategori(kategoriTerurut: List<Kategori>) =
        kategoriDao.perbaruiSemua(
            kategoriTerurut.mapIndexed { i, k -> k.copy(urutan = i) },
        )

    suspend fun arsipkanKategori(kategori: Kategori) =
        kategoriDao.perbarui(kategori.copy(diarsipkan = true))

    suspend fun pulihkanKategori(kategori: Kategori) =
        kategoriDao.perbarui(kategori.copy(diarsipkan = false))

    /** Boleh dihapus hanya kalau belum pernah dipakai dan bukan kategori sistem. */
    suspend fun bolehHapusKategori(kategori: Kategori): Boolean =
        !kategori.sistem && transaksiDao.jumlahPakaiKategori(kategori.id) == 0

    suspend fun hapusKategori(kategori: Kategori) = kategoriDao.hapus(kategori)

    companion object {
        fun dari(context: Context): DuitRepository {
            val db = DuitDatabase.ambil(context)
            return DuitRepository(db.transaksiDao(), db.kategoriDao())
        }
    }
}
