package com.alenza.duit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Transaksi::class, Kategori::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Konversi::class)
abstract class DuitDatabase : RoomDatabase() {

    abstract fun transaksiDao(): TransaksiDao
    abstract fun kategoriDao(): KategoriDao

    companion object {
        private const val NAMA_DB = "duit.db"

        /**
         * Migrasi eksplisit sejak versi 1 (PRD §7). `fallbackToDestructiveMigration`
         * dilarang — pengguna belum punya backup, kehilangan data tidak bisa
         * dipulihkan. Tambahkan `Migration(1, 2)` dst. di sini saat skema berubah.
         */
        val MIGRASI: Array<Migration> = emptyArray()

        @Volatile
        private var instance: DuitDatabase? = null

        fun ambil(context: Context): DuitDatabase =
            instance ?: synchronized(this) {
                instance ?: bangun(context.applicationContext).also { instance = it }
            }

        private fun bangun(context: Context): DuitDatabase =
            Room.databaseBuilder(context, DuitDatabase::class.java, NAMA_DB)
                .addMigrations(*MIGRASI)
                .addCallback(SeederKategori)
                .build()
    }

    /** Isi 12 kategori bawaan sekali, tepat saat database dibuat. */
    private object SeederKategori : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            for (k in KategoriBawaan.daftar) {
                db.execSQL(
                    "INSERT INTO kategori " +
                        "(nama, tipe, iconKey, colorHex, diarsipkan, sistem, urutan) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(
                        k.nama,
                        k.tipe.name,
                        k.iconKey,
                        k.colorHex,
                        if (k.diarsipkan) 1 else 0,
                        if (k.sistem) 1 else 0,
                        k.urutan,
                    ),
                )
            }
        }
    }
}
