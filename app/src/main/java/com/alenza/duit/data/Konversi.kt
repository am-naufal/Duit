package com.alenza.duit.data

import androidx.room.TypeConverter

/** Room tidak tahu cara menyimpan enum; simpan sebagai nama String yang stabil. */
class Konversi {

    @TypeConverter
    fun tipeKeString(tipe: TipeTransaksi): String = tipe.name

    @TypeConverter
    fun stringKeTipe(nilai: String): TipeTransaksi = TipeTransaksi.valueOf(nilai)
}
