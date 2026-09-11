package com.alenza.duit.ui.layar.pengaturan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alenza.duit.data.FormatTanggal
import com.alenza.duit.data.Preferensi
import com.alenza.duit.data.PreferensiApp
import com.alenza.duit.data.Tema
import com.alenza.duit.ui.Pemberitahuan
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PengaturanState(
    val pref: PreferensiApp,
    val versi: String,
    /** Pesan pemberitahuan sekali-tampil ("Tema diperbarui", dst.), atau null. */
    val pesan: String? = null,
)

class PengaturanViewModel(app: Application) : AndroidViewModel(app) {

    private val preferensi = Preferensi.dari(app)
    private val pemberitahuan = Pemberitahuan(viewModelScope)

    private val versi: String = runCatching {
        app.packageManager.getPackageInfo(app.packageName, 0).versionName
    }.getOrNull() ?: "?"

    val state: StateFlow<PengaturanState> = combine(
        preferensi.aliran,
        pemberitahuan.pesan,
    ) { pref, pesanSaatIni -> PengaturanState(pref, versi, pesanSaatIni) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PengaturanState(PreferensiApp(), versi),
        )

    fun setTema(tema: Tema) {
        viewModelScope.launch { preferensi.setTema(tema) }
        pemberitahuan.tampilkan("Tema diperbarui")
    }

    fun setFormatTanggal(format: FormatTanggal) {
        viewModelScope.launch { preferensi.setFormatTanggal(format) }
        pemberitahuan.tampilkan("Format tanggal diperbarui")
    }

    fun setHariAwalBulan(hari: Int) {
        viewModelScope.launch { preferensi.setHariAwalBulan(hari) }
        pemberitahuan.tampilkan("Hari awal bulan diperbarui")
    }
}
