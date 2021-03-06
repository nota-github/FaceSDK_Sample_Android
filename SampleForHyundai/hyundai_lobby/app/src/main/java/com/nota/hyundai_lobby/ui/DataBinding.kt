package com.nota.hyundai_lobby.ui

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter

object DataBinding {

    @JvmStatic
    @BindingAdapter("loadBitmapImage")
    fun loadBitmapImage(imageView: ImageView, bitmap: Bitmap?) {
        bitmap?.let {
            imageView.setImageBitmap(bitmap)
        } ?: run {
            imageView.setImageDrawable(null)
        }
    }

    @JvmStatic
    @BindingAdapter("loadBitmapImageRes")
    fun loadBitmapImageRes(imageView: ImageView, res: Int) {
        imageView.setImageResource(res)
    }

}