package com.louro_horo24.bulletinboardapp.fragments

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.act.EditAdsActivity
import com.louro_horo24.bulletinboardapp.databinding.SelectImageFragItemBinding
import com.louro_horo24.bulletinboardapp.utils.AdapterCallback
import com.louro_horo24.bulletinboardapp.utils.ImageManager
import com.louro_horo24.bulletinboardapp.utils.ImagePicker
import com.louro_horo24.bulletinboardapp.utils.ItemTouchMoveCallback

class SelectImageRvAdapter(val adapterCallback: AdapterCallback): RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(), ItemTouchMoveCallback.ItemTouchAdapter {

    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val viewBinding = SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(viewBinding, parent.context, this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }


    class ImageHolder(private val viewBinding: SelectImageFragItemBinding, val context: Context, val adapter: SelectImageRvAdapter) : RecyclerView.ViewHolder(viewBinding.root){

        //val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        //val image = itemView.findViewById<ImageView>(R.id.imageContent)
        //val imEditImage = itemView.findViewById<ImageButton>(R.id.imEditImage)
        //val imDeleteImage = itemView.findViewById<ImageButton>(R.id.imDelete)
        //val pBar = itemView.findViewById<ProgressBar>(R.id.pBar)
        fun setData(bitMap: Bitmap){

            viewBinding.imEditImage.setOnClickListener{
                ImagePicker.launcher(context as EditAdsActivity, context.launcherSingleSelectImage, 1)
                context.editImagePos = adapterPosition
            }

            viewBinding.imDelete.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                //для сохранения анимации при удалении одного изображения
                for(n in 0 until adapter.mainArray.size){
                    adapter.notifyItemChanged(n)
                }
                adapter.adapterCallback.onItemDelete() //показываем кнопку addImageItem в Toolbar
            }

            viewBinding.tvTitle.text = context.resources.getStringArray(R.array.title_array)[adapterPosition]
            ImageManager.chooseScaleType(viewBinding.imageContent, bitMap)
            viewBinding.imageContent.setImageBitmap(bitMap)
        }
    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean){
        if(needClear == true) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

}