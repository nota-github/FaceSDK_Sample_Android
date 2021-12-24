package com.nota.hyundai_lobby.http

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

object HttpUtil {

    internal fun convertBitmapToString(arg: Bitmap, qualityRate: Int): String {
        val baos = ByteArrayOutputStream()
        arg.compress(Bitmap.CompressFormat.JPEG, qualityRate, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

}