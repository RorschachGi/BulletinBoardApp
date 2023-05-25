package com.louro_horo24.bulletinboardapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.louro_horo24.bulletinboardapp.R

class ImageAdapter: RecyclerView.Adapter<ImageAdapter.ImageHolder>() {

    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false)
        return ImageHolder(view)
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    fun updateAdapter(newList: List<Bitmap>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }

    class ImageHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun setData(bitmap: Bitmap){
            val imItem = itemView.findViewById<ImageView>(R.id.imItem)
            imItem.setImageBitmap(bitmap)
        }
    }



}