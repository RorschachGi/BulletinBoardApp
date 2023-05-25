package com.louro_horo24.bulletinboardapp.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchMoveCallback(val adapter: ItemTouchAdapter): ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN  //Прописываем движения (вверх и вниз )
        return makeMovementFlags(dragFlag, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        TODO("Not yet implemented")
    }

    //Запускается при долгом нажатии на элемент ( дает доступ к нажатому элементу )
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if(actionState != ItemTouchHelper.ACTION_STATE_IDLE){
            viewHolder?.itemView?.alpha = 0.5f //Делаем элемент более прозрачным
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    //Запускается, когда отпускаем элемент
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.alpha = 1.0f
        adapter.onClear()
        super.clearView(recyclerView, viewHolder)
    }

    //Интерфейс для реализации перемещения элемента
    interface ItemTouchAdapter{
        fun onMove(startPos: Int, targetPos: Int)
        fun onClear()
    }
}