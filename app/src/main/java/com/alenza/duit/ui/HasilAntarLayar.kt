package com.alenza.duit.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Jembatan hasil antar layar untuk kasus yang tak bisa lewat parameter
 * navigasi biasa: form (Tambah/Form kategori) sudah `popBackStack()` sebelum
 * hasilnya sempat diproses layar sebelumnya.
 *
 * Awalnya ini dicoba lewat `NavBackStackEntry.savedStateHandle` (pola resmi
 * Navigation Compose untuk "return a result"), tapi diverifikasi lewat
 * `System.identityHashCode` saat debugging manual di emulator: `SavedStateHandle`
 * yang didapat lewat `previousBackStackEntry.savedStateHandle` BUKAN instance
 * yang sama dengan yang di-inject ke constructor ViewModel tujuan lewat
 * `viewModel()` — bahkan sejak komposisi pertama, bukan cuma setelah navigasi.
 * Akibatnya nilai yang di-`set()` tak pernah "terlihat" oleh `getStateFlow` di
 * ViewModel tujuan; kartu pemberitahuan (dan sebelumnya, forwarding hapus
 * transaksi dari form) tak pernah muncul. Singleton sederhana ini (pola sama
 * seperti [com.alenza.duit.data.Preferensi]/[com.alenza.duit.data.DuitRepository])
 * jadi jalan pintas yang pasti berhasil, tanpa bergantung pada mekanisme itu.
 */
object HasilAntarLayar {

    private val _hapusTransaksiId = MutableStateFlow(0L)
    val hapusTransaksiId: StateFlow<Long> = _hapusTransaksiId.asStateFlow()

    fun kirimHapusTransaksi(id: Long) {
        _hapusTransaksiId.value = id
    }

    fun tandaiHapusTransaksiSelesai() {
        _hapusTransaksiId.value = 0L
    }

    private val _pesanUtama = MutableStateFlow<String?>(null)
    val pesanUtama: StateFlow<String?> = _pesanUtama.asStateFlow()

    fun kirimPesanUtama(pesan: String) {
        _pesanUtama.value = pesan
    }

    fun tandaiPesanUtamaSelesai() {
        _pesanUtama.value = null
    }

    private val _pesanKelolaKategori = MutableStateFlow<String?>(null)
    val pesanKelolaKategori: StateFlow<String?> = _pesanKelolaKategori.asStateFlow()

    fun kirimPesanKelolaKategori(pesan: String) {
        _pesanKelolaKategori.value = pesan
    }

    fun tandaiPesanKelolaKategoriSelesai() {
        _pesanKelolaKategori.value = null
    }
}
