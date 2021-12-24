package com.nota.hyundai_door.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nota.hyundai_door.R
import com.nota.nota_sdk.task.vision.FacialProcess

/**
 * 시각적 디버깅을 위한 RecyclerView Adapter
 */
class DebugImageAdapter : RecyclerView.Adapter<DebugImageAdapter.ViewHolder>() {

    data class Item(val bitmap: Bitmap, val text: String)

    private val itemList = ArrayList<Item>()

    @SuppressLint("NotifyDataSetChanged")
    fun add(image: Item){
        itemList.add(image)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun replace(items: ArrayList<Item>){
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear(){
        itemList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_debug_image, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.ivDebug).load(itemList[position].bitmap).into(holder.ivDebug)
        holder.tvDebug.text = itemList[position].text
    }

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val ivDebug: ImageView = view.findViewById(R.id.iv_debug)
        val tvDebug: TextView = view.findViewById(R.id.tv_debug)

    }

    companion object {
        fun List<FacialProcess.FaceDetectResult>.toItemList() : ArrayList<Item>{
            val itemList = ArrayList<Item>()
            this.forEach { data ->
                itemList.add(Item(data.detectedFaceBitmap,data.faceQuality.toString()))
            }
            return itemList
        }
    }

}