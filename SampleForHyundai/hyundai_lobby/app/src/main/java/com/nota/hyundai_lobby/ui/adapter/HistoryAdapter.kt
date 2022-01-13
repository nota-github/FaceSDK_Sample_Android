package com.nota.hyundai_lobby.ui.adapter

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nota.hyundai_lobby.R
import com.nota.hyundai_lobby.data.EnterLog
import com.nota.hyundai_lobby.http.HttpRepository
import java.util.*

@SuppressLint("NotifyDataSetChanged", "HardwareIds")
class HistoryAdapter(context: Context) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val itemList = ArrayList<EnterLog>()

    init {
        val androidId: String = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        HttpRepository.getInstance().getUserEnterHistory(androidId){
            itemList.clear()
            itemList.addAll(it)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_history, parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = itemList[position].name
        holder.tvIndex.text = itemList[position].id
        holder.tvTimestamp.text = itemList[position].timestamp
        holder.tvDong.text = itemList[position].dong + "동"
        holder.tvHo.text = itemList[position].ho + "호"

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvName: TextView = view.findViewById(R.id.et_name)
        val tvIndex: TextView = view.findViewById(R.id.tv_index)
        val tvTimestamp: TextView = view.findViewById(R.id.tv_timestamp)
        val tvDong: TextView = view.findViewById(R.id.tv_dong)
        val tvHo: TextView = view.findViewById(R.id.tv_ho)
    }
}
