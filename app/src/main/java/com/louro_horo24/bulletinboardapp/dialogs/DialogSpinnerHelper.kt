package com.louro_horo24.bulletinboardapp.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView;
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.utils.CityHelper

class DialogSpinnerHelper {

    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView){

        val builder = AlertDialog.Builder(context).setCancelable(true)
        val dialog = builder.create()
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)

        val adapter = RcViewDialogSpinnerAdapter(tvSelection, dialog)

        val rcView = rootView.findViewById<RecyclerView>(R.id.rcSpView)
        val sv = rootView.findViewById<SearchView>(R.id.svSpinner)

        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
        dialog.setView(rootView)


        adapter.updateAdapter(list)
        setSearchViewListener(adapter, list, sv)
        dialog.show()

    }

    //Предполагаемые результаты в поисковой строке по уже написанному
    private fun setSearchViewListener(adapter: RcViewDialogSpinnerAdapter, list: ArrayList<String>, sv: SearchView?){
        sv?.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(newtext: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = CityHelper.filterListData(list, newText)
                adapter.updateAdapter(tempList)
                return true
            }
        })
    }

}