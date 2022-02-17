package com.nota.hyundai_door.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nota.hyundai_door.R
import com.nota.hyundai_door.data.RegistrationRepository
import com.nota.hyundai_door.data.User
import com.nota.nota_sdk.task.vision.FacialProcess

/**
 *
 */
class UserAdapter(private var itemList: ArrayList<User>, private val deleteUserCallback:(position: Int)->Unit) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    fun replaceData(itemList: ArrayList<User>){
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_user, parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvId.text = itemList[position].id
        holder.tvName.text = itemList[position].name

        holder.btDelete.setOnClickListener {
            deleteUserCallback(position)
        }
    }

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvId: TextView = view.findViewById(R.id.tv_id)
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val btDelete: Button = view.findViewById(R.id.bt_delete)
    }

}