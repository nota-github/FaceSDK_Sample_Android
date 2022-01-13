package com.nota.hyundai_lobby.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nota.hyundai_lobby.R
import com.nota.hyundai_lobby.data.User
import com.nota.hyundai_lobby.http.HttpRepository

@SuppressLint("NotifyDataSetChanged")
class UserAdapter : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val itemList = ArrayList<User>()

    init {
        HttpRepository.getInstance().getUsers {
            itemList.clear()
            itemList.addAll(it)
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_user, parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = itemList[position].name
        holder.tvIndex.text = itemList[position].id
        holder.tvDong.text = itemList[position].dong + "동"
        holder.tvHo.text = itemList[position].ho + "호"

        holder.btRemove.setOnClickListener {
            HttpRepository.getInstance().removeUser(itemList[position])
            itemList.removeAt(position)
            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvName: TextView = view.findViewById(R.id.et_name)
        val tvDong: TextView = view.findViewById(R.id.tv_dong)
        val tvHo: TextView = view.findViewById(R.id.tv_ho)
        val tvIndex: TextView = view.findViewById(R.id.tv_index)
        val btRemove: Button = view.findViewById(R.id.bt_remove)
    }
}
