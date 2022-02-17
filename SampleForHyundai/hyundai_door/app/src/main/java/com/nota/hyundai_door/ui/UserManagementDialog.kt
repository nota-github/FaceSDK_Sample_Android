package com.nota.hyundai_door.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nota.hyundai_door.R
import com.nota.hyundai_door.data.RegistrationRepository
import com.nota.hyundai_door.ui.adapter.UserAdapter

class UserManagementDialog(activityContext: Context) : Dialog(activityContext) {

    lateinit var userAdapter : UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_user)
        val rv = findViewById<RecyclerView>(R.id.rv_member)
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        userAdapter = UserAdapter(RegistrationRepository.getInstance().getUserList()) {
            RegistrationRepository.getInstance().deleteUserIndex(it)
            userAdapter.replaceData(RegistrationRepository.getInstance().getUserList())
        }

        rv.adapter = userAdapter
    }
}