package com.nota.hyundai_lobby.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.nota.hyundai_lobby.R

class ConfirmDialog(activityContext: Context,private val bitmap: Bitmap) : Dialog(activityContext) {

    private var isConfirm = false
    private var dong = ""
    private var ho = ""
    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_confirm_user)
        findViewById<ImageView>(R.id.iv_confirm).setImageBitmap(bitmap)

        findViewById<Button>(R.id.bt_add).setOnClickListener {
            isConfirm = true
            dong = findViewById<EditText>(R.id.et_dong).text.toString()
            ho = findViewById<EditText>(R.id.et_ho).text.toString()
            name = findViewById<EditText>(R.id.et_name).text.toString()
            dismiss()
        }

        findViewById<Button>(R.id.bt_cancel).setOnClickListener {
            isConfirm = false
            dismiss()
        }
    }

    fun isConfirm() : Boolean{
        return isConfirm
    }

    fun getDong() = dong
    fun getHo() = ho
    fun getName() = name

}