package com.louro_horo24.bulletinboardapp.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

object CityHelper {

    //Для считывания json файла с городами
    fun getAllCountries(context: Context): ArrayList<String> {

        var tempArray = ArrayList<String>()

        try{

            val inputStream: InputStream = context.assets.open("countriesToCities.json")

            //Узнаем размер необходимого массива для байтов
            val size: Int = inputStream.available()

            //Создаем байтовый массив заданного размера
            val bytesArray = ByteArray(size)

            //Считываем в него данные
            inputStream.read(bytesArray)

            val jsonFile = String(bytesArray)

            //Создаем json-объект для работы с содержимым jsonFile
            val jsonObject = JSONObject(jsonFile)

            //Получаем страны ( объекты )
            val countryNames = jsonObject.names()

            if(countryNames != null){
                for(n in 0 until countryNames.length()){
                    tempArray.add(countryNames.getString(n))
                }
            }

        }catch (e: IOException){

        }

        return tempArray

    }

    fun getAllCities(country: String, context: Context): ArrayList<String> {

        var tempArray = ArrayList<String>()

        try{

            val inputStream: InputStream = context.assets.open("countriesToCities.json")

            val size: Int = inputStream.available()

            val bytesArray = ByteArray(size)

            inputStream.read(bytesArray)

            val jsonFile = String(bytesArray)

            val jsonObject = JSONObject(jsonFile)

            val cityNames = jsonObject.getJSONArray(country)

            for(n in 0 until cityNames.length()){
                tempArray.add(cityNames.getString(n))
            }

        }catch (e: IOException){

        }

        return tempArray

    }




    //Для фильтрации значений в поисковой строке
    fun filterListData(list: ArrayList<String>, searchText: String?) : ArrayList<String>{

        val tempList = ArrayList<String>()
        tempList.clear()

        if(searchText == null) {
            tempList.add("No result")
            return tempList
        }

        for(selection: String in list){
            if(selection.lowercase().startsWith(searchText.lowercase())){
                tempList.add(selection)
            }
        }

        if(tempList.size == 0) tempList.add("No result")
        return tempList

    }



}