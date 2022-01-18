package com.nota.hyundai_lobby.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nota.hyundai_lobby.R
import com.nota.hyundai_lobby.data.Intruder
import com.nota.hyundai_lobby.http.GlideApp
import com.nota.hyundai_lobby.http.HttpRepository
import com.nota.hyundai_lobby.http.ManagementApiClient
import java.util.*

@SuppressLint("NotifyDataSetChanged", "HardwareIds")
class IntruderAdapter(context: Context) : RecyclerView.Adapter<IntruderAdapter.ViewHolder>() {

    private val itemList = ArrayList<Intruder>()

    init {
        val androidId: String = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        HttpRepository.getInstance().getIntruder(androidId){
            itemList.clear()
            itemList.addAll(it)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_intruder, parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTimestamp.text = itemList[position].timestamp
        GlideApp.with(holder.ivIntruder).load(ManagementApiClient.baseUrl+"ht/api/intruder_img?img_key=" + itemList[position].img_key).into(holder.ivIntruder)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvTimestamp: TextView = view.findViewById(R.id.tv_timestamp)
        val btDetail: Button = view.findViewById(R.id.bt_detail)
        val layoutDetail: ConstraintLayout = view.findViewById(R.id.layout_detail)
        val ivIntruder: ImageView = view.findViewById(R.id.iv_intruder)
    }
}
