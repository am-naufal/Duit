package com.alenza.duit.ui.layar.kategori

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.alenza.duit.data.DuitRepository
import com.alenza.duit.data.Kategori
import com.alenza.duit.data.TipeTransaksi
import com.alenza.duit.ui.HasilAntarLayar
import com.alenza.duit.ui.Pemberitahuan
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Daftar kategori aktif + arsip untuk tipe yang dipilih. Tipe disimpan di
 * `SavedStateHandle` supaya bertahan saat rotasi.
 */
class KelolaKategoriViewModel(
    app: Application,
    private val simpanan: SavedStateHandle,
) : AndroidViewModel(app) {

    private val repo = DuitRepository.dari(app)
    private val pemberitahuan = Pemberitahuan(viewModelScope)

    private val tipeStr = simpanan.getStateFlow(K_TIPE, TipeTransaksi.PENGELUARAN.name)

    init {
        // Pesan dari FormKategoriScreen (tambah/ubah/arsipkan/hapus kategori) —
        // lewat HasilAntarLayar (lihat catatan di sana untuk alasannya, bukan
        // lagi NavBackStackEntry.savedStateHandle).
        viewModelScope.launch {
            HasilAntarLayar.pesanKelolaKategori.collect { pesan ->
                if (pesan != null) {
                    HasilAntarLayar.tandaiPesanKelolaKategoriSelesai()
                    pemberitahuan.tampilkan(pesan)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<KelolaKategoriState> = tipeStr
        .flatMapLatest { nama ->
            val tipe = TipeTransaksi.valueOf(nama)
            combine(
                repo.kategoriAktif(tipe),
                repo.kategoriArsip(tipe),
                repo.jumlahPerKategori(),
                pemberitahuan.pesan,
            ) { aktif, arsip, jumlah, pesanSaatIni ->
                KelolaKategoriState(
                    tipe = tipe,
                    aktif = aktif.map { it.keBaris(jumlah[it.id] ?: 0) },
                    arsip = arsip.map { it.keBaris(jumlah[it.id] ?: 0) },
                    pesan = pesanSaatIni,
                    memuat = false,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KelolaKategoriState.awal())

    fun pilihTipe(tipe: TipeTransaksi) {
        simpanan[K_TIPE] = tipe.name
    }

    fun pulihkan(id: Long) {
        viewModelScope.launch {
            repo.kategori(id)?.let {
                repo.pulihkanKategori(it)
                pemberitahuan.tampilkan("Kategori dipulihkan")
            }
        }
    }

    /**
     * Dipanggil setelah pengguna selesai menyeret kategori ke posisi baru
     * (design-spec §4, `ic_geser`). [idBaru] adalah id kategori aktif untuk
     * [tipeStr] saat ini, sudah urut sesuai posisi akhir seretan.
     */
    fun urutkanUlang(idBaru: List<Long>) {
        viewModelScope.launch {
            val kategori = idBaru.mapNotNull { repo.kategori(it) }
            repo.urutkanKategori(kategori)
            pemberitahuan.tampilkan("Urutan kategori disimpan")
        }
    }

    private fun Kategori.keBaris(jumlah: Int) = BarisKategori(
        id = id,
        nama = nama,
        iconKey = iconKey,
        colorHex = colorHex,
        jumlahTransaksi = jumlah,
        sistem = sistem,
    )

    companion object {
        private const val K_TIPE = "kelola_kategori_tipe"
    }
}
