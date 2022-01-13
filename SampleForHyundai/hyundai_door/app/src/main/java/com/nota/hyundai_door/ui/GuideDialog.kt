package com.nota.hyundai_door.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.nota.hyundai_door.R

class GuideDialog(activityContext: Context) : Dialog(activityContext) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_guide)

        findViewById<Button>(R.id.bt_ok).setOnClickListener {
            dismiss()
        }

    }

}