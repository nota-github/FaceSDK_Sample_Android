package com.nota.hyundai_door.ui

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object DataBinding {

    @JvmStatic
    @BindingAdapter("loadBitmapImage")
    fun loadBitmapImage(imageView: ImageView, bitmap: Bitmap?) {
        bitmap?.let {
            Glide.with(imageView).load(bitmap).into(imageView)
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

