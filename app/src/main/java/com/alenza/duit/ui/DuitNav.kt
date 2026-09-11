package com.alenza.duit.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alenza.duit.ui.layar.ekspor.EksporRoute
import com.alenza.duit.ui.layar.ekspor.EksporViewModel
import com.alenza.duit.ui.layar.kategori.FormKategoriRoute
import com.alenza.duit.ui.layar.kategori.FormKategoriViewModel
import com.alenza.duit.ui.layar.kategori.KelolaKategoriRoute
import com.alenza.duit.ui.layar.pengaturan.PengaturanRoute
import com.alenza.duit.ui.layar.tambah.TambahRoute
import com.alenza.duit.ui.layar.tambah.TambahViewModel
import com.alenza.duit.ui.layar.utama.UtamaRoute
import com.alenza.duit.ui.layar.utama.UtamaViewModel

/**
 * Peta navigasi seluruh aplikasi. Layar yang belum ada (Ekspor, Pengaturan, Form
 * kategori) sementara memakai lambda kosong — disambungkan saat dibuat.
 */
private object Rute {
    const val UTAMA = "utama"
    const val TAMBAH = "tambah?id={id}"
    const val KELOLA_KATEGORI = "kategori"
    const val FORM_KATEGORI = "kategori/form?id={id}&tipe={tipe}"
    const val PENGATURAN = "pengaturan"
    const val EKSPOR = "ekspor?bulan={bulan}"

    fun tambah(id: Long = 0L): String = "tambah?id=$id"
    fun formKategori(id: Long = 0L, tipe: String = "PENGELUARAN"): String =
        "kategori/form?id=$id&tipe=$tipe"
    fun ekspor(bulan: String): String = "ekspor?bulan=$bulan"
}

@Composable
fun DuitNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Rute.UTAMA) {
        composable(Rute.UTAMA) {
            UtamaRoute(
                onTambah = { nav.navigate(Rute.tambah()) },
                onEkspor = { bulan -> nav.navigate(Rute.ekspor(bulan.toString())) },
                onKelolaKategori = { nav.navigate(Rute.KELOLA_KATEGORI) },
                onPengaturan = { nav.navigate(Rute.PENGATURAN) },
                onTransaksiDiklik = { id -> nav.navigate(Rute.tambah(id)) },
            )
        }
        composable(Rute.KELOLA_KATEGORI) {
            KelolaKategoriRoute(
                onKembali = { nav.popBackStack() },
                onTambah = { nav.navigate(Rute.formKategori()) },
                onEdit = { id -> nav.navigate(Rute.formKategori(id = id)) },
            )
        }
        composable(
            route = Rute.FORM_KATEGORI,
            arguments = listOf(
                navArgument(FormKategoriViewModel.ARG_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument(FormKategoriViewModel.ARG_TIPE) {
                    type = NavType.StringType
                    defaultValue = "PENGELUARAN"
                },
            ),
        ) {
            FormKategoriRoute(onSelesai = { nav.popBackStack() })
        }
        composable(
            route = Rute.TAMBAH,
            arguments = listOf(
                navArgument(TambahViewModel.ARG_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            ),
        ) {
            TambahRoute(
                onSelesai = { nav.popBackStack() },
                onHapus = { id ->
                    // Serahkan penghapusan + "Urungkan" ke Layar utama.
                    nav.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(UtamaViewModel.KUNCI_HAPUS_DARI_FORM, id)
                    nav.popBackStack()
                },
            )
        }
        composable(Rute.PENGATURAN) {
            PengaturanRoute(
                onKembali = { nav.popBackStack() },
                onEkspor = { nav.navigate(Rute.ekspor(EksporViewModel.SEKARANG)) },
                onKelolaKategori = { nav.navigate(Rute.KELOLA_KATEGORI) },
            )
        }
        composable(
            route = Rute.EKSPOR,
            arguments = listOf(
                navArgument(EksporViewModel.ARG_BULAN) {
                    type = NavType.StringType
                    defaultValue = EksporViewModel.SEKARANG
                },
            ),
        ) {
            EksporRoute(onTutup = { nav.popBackStack() })
        }
    }
}
