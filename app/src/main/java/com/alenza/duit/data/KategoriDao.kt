package com.alenza.duit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KategoriDao {

    @Insert
    suspend fun sisip(kategori: Kategori): Long

    @Insert
    suspend fun sisipSemua(kategori: List<Kategori>)

    @Update
    suspend fun perbarui(kategori: Kategori)

    /** Hanya untuk kategori yang belum pernah dipakai (PRD §F-2). */
    @Delete
    suspend fun hapus(kategori: Kategori)

    @Query("SELECT * FROM kategori WHERE id = :id")
    suspend fun ambil(id: Long): Kategori?

    @Query("SELECT * FROM kategori ORDER BY urutan ASC, nama ASC")
    fun amatiSemua(): Flow<List<Kategori>>

    @Query("SELECT * FROM kategori ORDER BY urutan ASC, nama ASC")
    suspend fun ambilSemua(): List<Kategori>

    @Query(
        "SELECT * FROM kategori WHERE tipe = :tipe AND diarsipkan = 0 " +
            "ORDER BY urutan ASC, nama ASC",
    )
    fun amatiAktif(tipe: TipeTransaksi): Flow<List<Kategori>>

    @Query(
        "SELECT * FROM kategori WHERE tipe = :tipe AND diarsipkan = 1 " +
            "ORDER BY urutan ASC, nama ASC",
    )
    fun amatiArsip(tipe: TipeTransaksi): Flow<List<Kategori>>

    @Query("SELECT COUNT(*) FROM kategori")
    suspend fun jumlah(): Int

    /** Nama unik per (nama, tipe) — PRD F-2. `kecuali` = id yang sedang diedit. */
    @Query(
        "SELECT COUNT(*) FROM kategori WHERE nama = :nama AND tipe = :tipe AND id != :kecuali",
    )
    suspend fun adaNamaLain(nama: String, tipe: TipeTransaksi, kecuali: Long): Int

    /** Nilai `urutan` untuk kategori baru — ditaruh paling belakang. */
    @Query("SELECT COALESCE(MAX(urutan), -1) + 1 FROM kategori WHERE tipe = :tipe")
    suspend fun urutanBerikutnya(tipe: TipeTransaksi): Int

    @Update
    suspend fun perbaruiSemua(kategori: List<Kategori>)
}
