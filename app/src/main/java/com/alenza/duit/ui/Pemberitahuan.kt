package com.alenza.duit.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Pesan pemberitahuan sekali-tampil yang hilang sendiri setelah [durasiMillis] —
 * dipakai tiap ViewModel yang perlu menunjukkan kartu "Transaksi ditambahkan",
 * "Kategori diarsipkan", dsb. (lihat [com.alenza.duit.ui.komponen.KartuNotifikasi]).
 * Dibuat sebagai kelas terpisah supaya logika timer-nya tidak ditulis ulang di
 * tiap ViewModel (Utama, Kelola kategori, Pengaturan).
 */
class Pemberitahuan(
    private val scope: CoroutineScope,
    private val durasiMillis: Long = 2_500L,
) {
    private val _pesan = MutableStateFlow<String?>(null)
    val pesan: StateFlow<String?> = _pesan.asStateFlow()

    private var jam: Job? = null

    fun tampilkan(teks: String) {
        _pesan.value = teks
        jam?.cancel()
        jam = scope.launch {
            delay(durasiMillis)
            _pesan.value = null
        }
    }
}
