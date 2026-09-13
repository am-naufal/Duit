package com.alenza.duit.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.alenza.duit.R

/**
 * Kunci ikon & warna kategori disimpan sebagai String di database
 * (Category.iconKey, Category.colorHex) supaya tidak ikut berubah
 * kalau resource id berubah. Peta ini menerjemahkannya ke resource.
 */
object Kategori {

    private val warnaPeta: Map<String, Color> = mapOf(
        "makan"        to Color(0xFFE0785C),
        "transportasi" to Color(0xFF5E8CB5),
        "belanja"      to Color(0xFFB57BA6),
        "tagihan"      to Color(0xFFD9A441),
        "kesehatan"    to Color(0xFF6BA88C),
        "hiburan"      to Color(0xFF8A7CC8),
        "pendidikan"   to Color(0xFF3F9AA6),
        "lainnya"      to Color(0xFF94989E),
        "gaji"         to Color(0xFF4E9E6A),
        "bonus"        to Color(0xFFCF8B5B),
        "hadiah"       to Color(0xFFC76A8A),
        "olahraga"     to Color(0xFF7E9C4A),
    )

    private val ikonPeta: Map<String, Int> = mapOf(
        "makan"        to R.drawable.ic_kat_makan,
        "transportasi" to R.drawable.ic_kat_transportasi,
        "belanja"      to R.drawable.ic_kat_belanja,
        "tagihan"      to R.drawable.ic_kat_tagihan,
        "kesehatan"    to R.drawable.ic_kat_kesehatan,
        "hiburan"      to R.drawable.ic_kat_hiburan,
        "pendidikan"   to R.drawable.ic_kat_pendidikan,
        "lainnya"      to R.drawable.ic_kat_lainnya,
        "gaji"         to R.drawable.ic_kat_gaji,
        "bonus"        to R.drawable.ic_kat_bonus,
        "hadiah"       to R.drawable.ic_kat_hadiah,
        "olahraga"     to R.drawable.ic_kat_olahraga,
    )

    /** Fallback sengaja "lainnya", bukan crash — kategori lama harus tetap tampil. */
    fun warna(key: String): Color = warnaPeta[key] ?: warnaPeta.getValue("lainnya")

    /**
     * Warna dari `Category.colorHex` yang tersimpan di DB (mis. "#E0785C").
     * Ini sumber kebenaran untuk kategori buatan pengguna yang warnanya tidak
     * ada di [warnaPeta]. Format rusak → jatuh ke abu-abu "lainnya".
     */
    fun warnaHex(hex: String): Color = runCatching {
        Color(("FF" + hex.removePrefix("#")).toLong(16))
    }.getOrElse { warnaPeta.getValue("lainnya") }

    @DrawableRes
    fun ikon(key: String): Int = ikonPeta[key] ?: R.drawable.ic_kat_lainnya

    val urutanPengeluaran = listOf(
        "makan", "transportasi", "belanja", "tagihan",
        "kesehatan", "hiburan", "pendidikan", "lainnya",
    )
    val urutanPemasukan = listOf("gaji", "bonus", "hadiah", "lainnya")

    /** Ikon yang bisa dipilih pengguna di Form kategori (design-spec §5). */
    val pilihanIkon: List<String> = listOf(
        "makan", "transportasi", "belanja", "tagihan", "kesehatan", "hiburan",
        "pendidikan", "olahraga", "gaji", "bonus", "hadiah", "lainnya",
    )

    /** Warna yang bisa dipilih pengguna, sebagai hex — disimpan di `Category.colorHex`. */
    val pilihanWarna: List<String> = listOf(
        "#E0785C", "#5E8CB5", "#B57BA6", "#D9A441", "#6BA88C", "#8A7CC8",
        "#3F9AA6", "#7E9C4A", "#4E9E6A", "#CF8B5B", "#C76A8A", "#94989E",
    )
}
