package com.louro_horo24.bulletinboardapp.adapters

import androidx.recyclerview.widget.DiffUtil
import com.louro_horo24.bulletinboardapp.model.Ad

class DiffUtilHelper(val oldList: List<Ad>, val newList: List<Ad>): DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    //Сравниваем уникальное значение элемента
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].key == newList[newItemPosition].key
    }

    //Сравниваем элементы полностью
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}