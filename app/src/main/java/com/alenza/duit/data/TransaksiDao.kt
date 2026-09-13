package com.alenza.duit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaksiDao {

    @Insert
    suspend fun sisip(transaksi: Transaksi): Long

    @Update
    suspend fun perbarui(transaksi: Transaksi)

    @Delete
    suspend fun hapus(transaksi: Transaksi)

    @Query("SELECT * FROM transaksi WHERE id = :id")
    suspend fun ambil(id: Long): Transaksi?

    /**
     * Semua transaksi dalam rentang epoch day [dariHari]..[sampaiHari] (inklusif),
     * terbaru dulu. Dipakai untuk daftar dan ringkasan bulan berjalan.
     */
    @Query(
        "SELECT * FROM transaksi WHERE tanggal BETWEEN :dariHari AND :sampaiHari " +
            "ORDER BY tanggal DESC, dibuatPada DESC",
    )
    fun amatiRentang(dariHari: Long, sampaiHari: Long): Flow<List<Transaksi>>

    /** Ambil sekali, urut kronologis — untuk ekspor Excel (PRD F-6). */
    @Query(
        "SELECT * FROM transaksi WHERE tanggal BETWEEN :dariHari AND :sampaiHari " +
            "ORDER BY tanggal ASC, dibuatPada ASC",
    )
    suspend fun ambilRentang(dariHari: Long, sampaiHari: Long): List<Transaksi>

    @Query("SELECT COUNT(*) FROM transaksi WHERE kategoriId = :kategoriId")
    suspend fun jumlahPakaiKategori(kategoriId: Long): Int

    /** Jumlah transaksi per kategori — untuk daftar "N transaksi" di Kelola kategori. */
    @Query("SELECT kategoriId, COUNT(*) AS jumlah FROM transaksi GROUP BY kategoriId")
    fun amatiJumlahPerKategori(): Flow<List<JumlahKategori>>

    /** Epoch day transaksi paling awal — untuk membatasi navigasi bulan mundur. */
    @Query("SELECT MIN(tanggal) FROM transaksi")
    fun amatiTanggalTerawal(): Flow<Long?>

    /**
     * Epoch day transaksi paling akhir — F-1 mengizinkan tanggal masa depan, jadi
     * navigasi bulan maju harus bisa menjangkau bulan transaksi terjauh.
     */
    @Query("SELECT MAX(tanggal) FROM transaksi")
    fun amatiTanggalTerakhir(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM transaksi")
    suspend fun jumlah(): Int
}
