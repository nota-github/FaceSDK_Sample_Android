package com.nota.hyundai_lobby.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nota.hyundai_lobby.R
import com.nota.hyundai_lobby.ui.adapter.HistoryAdapter
import com.nota.hyundai_lobby.ui.adapter.IntruderAdapter

class IntruderLogManagementDialog(activityContext: Context) : Dialog(activityContext) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_management)

        findViewById<Button>(R.id.bt_close).setOnClickListener {
            dismiss()
        }

        val rvList = findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = GridLayoutManager(context,  2, GridLayoutManager.VERTICAL, false)
        rvList.adapter = IntruderAdapter(context)
    }

}