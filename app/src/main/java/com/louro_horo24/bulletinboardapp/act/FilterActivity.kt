package com.louro_horo24.bulletinboardapp.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.databinding.ActivityFilterBinding
import com.louro_horo24.bulletinboardapp.dialogs.DialogSpinnerHelper
import com.louro_horo24.bulletinboardapp.utils.CityHelper

class FilterActivity : AppCompatActivity() {

    lateinit var binding: ActivityFilterBinding

    private var dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBarSettings()
        onClickSelectCountry()
        onClickSelectCity()
        onClickDone()
        getFilter()
        onClickClear()
    }

//    private fun init(){
//        actionBarSettings()
//        onClickSelectCountry()
//        onClickSelectCity()
//        onClickDone()
//        getFilter()
//        onClickClear()
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    //Активация стрелки "Назад" в Action Bar
    fun actionBarSettings(){
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    private fun getFilter() = with(binding){
        val filter = intent.getStringExtra(FILTER_KEY)
        if(filter != null && filter != "empty"){
            val filterArray = filter.split("_")
            if(filterArray[0] != "empty") tvCountry.text = filterArray[0]
            if(filterArray[1] != "empty") tvCity.text = filterArray[1]
            if(filterArray[2] != "empty") editIndex.setText(filterArray[2])
            checkBoxWithSend.isChecked = filterArray[3].toBoolean()

        }
    }

    //onClicks
    private fun onClickSelectCountry() = with(binding){
        tvCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountry, tvCountry)
            if(tvCity.text.toString() != getString(R.string.select_city)){
                tvCity.text = getString(R.string.select_city)
            }
        }
    }

    private fun onClickSelectCity() = with(binding){
        tvCity.setOnClickListener {
            val selectedCountry = tvCountry.text.toString()
            if(selectedCountry != getString(R.string.select_country)){
                val listCity = CityHelper.getAllCities(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCity, tvCity)
            }else{
                Toast.makeText(this@FilterActivity, "No country selected", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onClickDone() = with(binding){
       btDone.setOnClickListener{
            val i  = Intent().apply {
                putExtra(FILTER_KEY, createFilter())
            }
           setResult(RESULT_OK, i)
           finish()
       }
    }

    private fun onClickClear() = with(binding){
        btClear.setOnClickListener{
            tvCountry.text = getString(R.string.select_country)
            tvCity.text = getString(R.string.select_city)
            editIndex.setText("")
            checkBoxWithSend.isChecked = false
            setResult(RESULT_CANCELED)
        }
    }

    private fun createFilter(): String = with(binding){
        val sBuilder = StringBuilder()
        val arrayTempFilter = listOf(tvCountry.text,
            tvCity.text,
            editIndex.text,
            checkBoxWithSend.isChecked.toString())

        for((i, s) in arrayTempFilter.withIndex()){
            if(s != getString(R.string.select_country) && s != getString(R.string.select_city) && s.isNotEmpty()){
                sBuilder.append(s)
                if(i != arrayTempFilter.size - 1) sBuilder.append("_")
            }else{
                sBuilder.append("empty")
                if(i != arrayTempFilter.size - 1) sBuilder.append("_")
            }
        }
        return sBuilder.toString()
    }

    companion object{
        const val FILTER_KEY = "filter_key"
    }
}