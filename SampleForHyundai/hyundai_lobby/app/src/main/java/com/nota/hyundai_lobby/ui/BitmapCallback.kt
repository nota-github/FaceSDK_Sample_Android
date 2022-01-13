package com.nota.hyundai_lobby.ui

import android.graphics.Bitmap

interface BitmapCallback {
    fun toBitmap(bitmap: Bitmap, orientation: Int)
}