package com.alenza.duit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class Tema { SISTEM, TERANG, GELAP }

enum class FormatTanggal(val contoh: String) {
    PANJANG("25 Agu 2026"),
    RINGKAS("25/08/2026"),
}

/** Semua preferensi aplikasi dalam satu bentuk siap pakai. */
data class PreferensiApp(
    val tema: Tema = Tema.SISTEM,
    val formatTanggal: FormatTanggal = FormatTanggal.PANJANG,
    /** Tanggal 1..28 sebagai awal "bulan berjalan". Default 1 = bulan kalender. */
    val hariAwalBulan: Int = 1,
    /** Bilah "Data hanya tersimpan di HP ini" (design-spec §9) sudah ditutup. */
    val infoLokalSudahDilihat: Boolean = false,
)

private val Context.dataStorePreferensi: DataStore<Preferences> by
    preferencesDataStore(name = "preferensi")

/**
 * Pembungkus DataStore. Tidak menyimpan salinan state — UI mengamati [aliran].
 */
class Preferensi private constructor(private val ds: DataStore<Preferences>) {

    val aliran: Flow<PreferensiApp> = ds.data.map { p ->
        PreferensiApp(
            tema = runCatching { Tema.valueOf(p[K_TEMA] ?: "") }.getOrDefault(Tema.SISTEM),
            formatTanggal = runCatching { FormatTanggal.valueOf(p[K_FORMAT] ?: "") }
                .getOrDefault(FormatTanggal.PANJANG),
            hariAwalBulan = (p[K_HARI_AWAL] ?: 1).coerceIn(1, 28),
            infoLokalSudahDilihat = p[K_INFO] ?: false,
        )
    }

    suspend fun setTema(tema: Tema) = ds.edit { it[K_TEMA] = tema.name }

    suspend fun setFormatTanggal(format: FormatTanggal) = ds.edit { it[K_FORMAT] = format.name }

    suspend fun setHariAwalBulan(hari: Int) = ds.edit { it[K_HARI_AWAL] = hari.coerceIn(1, 28) }

    suspend fun tandaiInfoLokalDilihat() = ds.edit { it[K_INFO] = true }

    companion object {
        private val K_TEMA = stringPreferencesKey("tema")
        private val K_FORMAT = stringPreferencesKey("format_tanggal")
        private val K_HARI_AWAL = intPreferencesKey("hari_awal_bulan")
        private val K_INFO = booleanPreferencesKey("info_lokal_dilihat")

        @Volatile
        private var instance: Preferensi? = null

        fun dari(context: Context): Preferensi = instance ?: synchronized(this) {
            instance ?: Preferensi(context.applicationContext.dataStorePreferensi).also { instance = it }
        }
    }
}
