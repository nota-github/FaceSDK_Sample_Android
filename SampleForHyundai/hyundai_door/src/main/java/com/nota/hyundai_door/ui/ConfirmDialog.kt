package com.nota.hyundai_door.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.nota.hyundai_door.R

class ConfirmDialog(activityContext: Context,private val bitmap: Bitmap) : Dialog(activityContext) {

    private var isConfirm = false
    private var id = ""
    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_confirm_user)
        findViewById<ImageView>(R.id.iv_confirm).setImageBitmap(bitmap)

        findViewById<Button>(R.id.bt_add).setOnClickListener {
            isConfirm = true
            id = findViewById<EditText>(R.id.et_id).text.toString()
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

    fun getId() = id
    fun getName() = name

}