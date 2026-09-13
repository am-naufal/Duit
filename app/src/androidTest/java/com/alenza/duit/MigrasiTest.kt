package com.alenza.duit

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alenza.duit.data.DuitDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Kerangka tes migrasi Room (PRD §12 — disiapkan lebih dulu meski versi 2 belum
 * ada). Skema versi 1 diekspor otomatis ke `app/schemas/` saat build karena
 * `room.schemaLocation` sudah diset di `build.gradle.kts`.
 *
 * Saat menambah versi 2: tambahkan `Migration(1, 2)` ke `DuitDatabase.MIGRASI`
 * lalu isi `runMigrationsAndValidate(DB, 2, true, *DuitDatabase.MIGRASI)` di sini.
 *
 * Butuh perangkat/emulator: `./gradlew connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class MigrasiTest {

    private val db = "migrasi-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        DuitDatabase::class.java,
    )

    @Test
    fun skemaVersi1Terbuka() {
        helper.createDatabase(db, 1).close()
    }
}
