package com.nota.hyundai_door.ui

import android.graphics.Bitmap

interface BitmapCallback {
    fun toBitmap(bitmap: Bitmap, orientation: Int)
}