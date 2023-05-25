package com.louro_horo24.bulletinboardapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.louro_horo24.bulletinboardapp.model.Ad
import com.louro_horo24.bulletinboardapp.model.DbManager

class FirebaseViewModel: ViewModel() {

    private val dbManager = DbManager()

    //LiveData следит за изменениями и когда view доступно, обновляет его
    val liveAdsData = MutableLiveData<ArrayList<Ad>>()

    //Передача данных в liveAdsData ( списка объявлений )
    fun loadAllAds(){
        dbManager.getAllAds(object: DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }

        })
    }

    fun loadMyAds(){
        dbManager.getMyAds(object : DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyFavs(){
        dbManager.getMyFavs(object: DbManager.ReadDataCallback{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }

        })
    }

    //Удаление из базы данных и RcView
    fun deleteItem(ad: Ad){
        dbManager.deleteAd(ad, object: DbManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList)
            }
        })
    }

    fun adViewed(ad: Ad){
        dbManager.adViewed(ad)
    }

    fun onFavClick(ad: Ad){
        dbManager.onFavClick(ad, object: DbManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsData.value
                val pos = updatedList?.indexOf(ad)
                if(pos != -1){
                    pos?.let{
                        val favCounter = if(ad.isFav) ad.favCounter.toInt() - 1 else ad.favCounter.toInt() + 1
                        updatedList[pos] = updatedList[pos].copy(isFav = !ad.isFav, favCounter = favCounter.toString())
                    }
                }
                liveAdsData.postValue(updatedList)
            }

        })
    }


}