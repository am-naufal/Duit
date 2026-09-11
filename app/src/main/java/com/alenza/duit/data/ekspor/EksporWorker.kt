package com.alenza.duit.data.ekspor

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.alenza.duit.data.DuitRepository
import com.alenza.duit.data.Periode
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.YearMonth

/**
 * Menulis ekspor Excel lewat WorkManager, bukan coroutine biasa di ViewModel
 * (PRD F-6: ekspor di atas 5.000 baris harus selamat kalau aplikasi ditutup
 * paksa di tengah proses). Dipicu dari `EksporViewModel` hanya untuk laporan
 * besar — laporan normal tetap lewat jalur langsung yang sudah ada, karena
 * instan dan tak perlu bertahan dari kematian proses.
 *
 * Menerima rentang [Periode] + tujuan [Uri] lewat `inputData` dan merakit ulang
 * [LaporanBulanan] dari Room sendiri (bukan lewat objek yang sudah dihitung di
 * ViewModel) — worker ini harus bisa jalan sendiri walau proses yang
 * menjadwalkannya sudah mati.
 */
class EksporWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uri = inputData.getString(KEY_URI)?.let(Uri::parse)
            ?: return Result.failure(workDataOf(KEY_ERROR to "Data ekspor tidak lengkap"))
        val label = inputData.getString(KEY_LABEL)?.let(YearMonth::parse)
            ?: return Result.failure(workDataOf(KEY_ERROR to "Data ekspor tidak lengkap"))
        val awalEpoch = inputData.getLong(KEY_AWAL, -1L)
        val akhirEpoch = inputData.getLong(KEY_AKHIR, -1L)
        if (awalEpoch < 0 || akhirEpoch < 0) {
            return Result.failure(workDataOf(KEY_ERROR to "Data ekspor tidak lengkap"))
        }
        val periode = Periode(label, LocalDate.ofEpochDay(awalEpoch), LocalDate.ofEpochDay(akhirEpoch))

        val repo = DuitRepository.dari(applicationContext)
        val laporan = RakitLaporan.dari(periode, repo.transaksiPeriodeSekali(periode), repo.semuaKategoriSekali())

        return try {
            val keluaran = applicationContext.contentResolver.openOutputStream(uri)
                ?: return Result.failure(workDataOf(KEY_ERROR to "Tidak bisa membuka lokasi yang dipilih"))
            keluaran.use { os ->
                PenulisXlsx.tulis(laporan, os) { p -> setProgressAsync(workDataOf(KEY_PROGRES to p)) }
            }
            Result.success()
        } catch (batal: CancellationException) {
            hapusFileSetengah(uri)
            throw batal
        } catch (e: Exception) {
            hapusFileSetengah(uri)
            Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Ekspor gagal")))
        }
    }

    private fun hapusFileSetengah(uri: Uri) {
        runCatching { applicationContext.contentResolver.delete(uri, null, null) }
    }

    companion object {
        const val KEY_URI = "uri"
        const val KEY_LABEL = "label"
        const val KEY_AWAL = "awal"
        const val KEY_AKHIR = "akhir"
        const val KEY_PROGRES = "progres"
        const val KEY_ERROR = "error"

        fun data(uri: Uri, periode: Periode): Data = workDataOf(
            KEY_URI to uri.toString(),
            KEY_LABEL to periode.label.toString(),
            KEY_AWAL to periode.awal.toEpochDay(),
            KEY_AKHIR to periode.akhir.toEpochDay(),
        )
    }
}
