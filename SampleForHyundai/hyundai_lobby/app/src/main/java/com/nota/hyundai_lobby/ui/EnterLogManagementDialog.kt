package com.nota.hyundai_lobby.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nota.hyundai_lobby.R
import com.nota.hyundai_lobby.ui.adapter.HistoryAdapter

class EnterLogManagementDialog(activityContext: Context) : Dialog(activityContext) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_management)

        findViewById<Button>(R.id.bt_close).setOnClickListener {
            dismiss()
        }

        val rvList = findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvList.adapter = HistoryAdapter(context)
    }

}