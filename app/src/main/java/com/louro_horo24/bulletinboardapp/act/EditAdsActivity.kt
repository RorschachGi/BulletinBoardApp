package com.louro_horo24.bulletinboardapp.act

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import com.fxn.utility.PermUtil
import com.louro_horo24.bulletinboardapp.MainActivity
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.adapters.ImageAdapter
import com.louro_horo24.bulletinboardapp.model.Ad
import com.louro_horo24.bulletinboardapp.model.DbManager
import com.louro_horo24.bulletinboardapp.databinding.ActivityEditAdsBinding
import com.louro_horo24.bulletinboardapp.dialogs.DialogSpinnerHelper
import com.louro_horo24.bulletinboardapp.fragments.FragmentCloseInterface
import com.louro_horo24.bulletinboardapp.fragments.ImageListFrag
import com.louro_horo24.bulletinboardapp.utils.CityHelper
import com.louro_horo24.bulletinboardapp.utils.ImagePicker
import java.io.Serializable

class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {

    lateinit var binding: ActivityEditAdsBinding

    private var dialog = DialogSpinnerHelper()

    lateinit var imageAdapter: ImageAdapter

    var chooseImageFrag: ImageListFrag? = null

    private val dbManager = DbManager()

    var editImagePos = 0

    var launcherMultiSelectImage: ActivityResultLauncher<Intent>? = null

    var launcherSingleSelectImage: ActivityResultLauncher<Intent>? = null

    private var isEditState = false

    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
    }

    fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
    }

    private fun checkEditState(){
        isEditState = isEditState()
        if(isEditState()){
            ad = getSerializable(this, MainActivity.ADS_DATA, Ad::class.java)
            if(ad != null){
                fillViews(ad!!)
            }
        }
    }

    //Проверка на то, откуда запустилось EditActivity
    private fun isEditState(): Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(binding){
        tvCountry.text = ad.country
        tvCity.text = ad.city
        editTel.setText(ad.tel)
        editIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        tvCat.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
    }

    //Запрос разрешения на доступ к изображениям, камере
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS ->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //ImagePicker.getImages(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
                }else{
                    Toast.makeText(this, "Approve permissions to open Pis ImagePicker", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun init(){
        imageAdapter = ImageAdapter()
        binding.vpimages.adapter = imageAdapter
        launcherMultiSelectImage = ImagePicker.getLauncherForMultiSelectImages(this)
        launcherSingleSelectImage = ImagePicker.getLauncherForSingleImage(this)
    }

   /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImagePicker.showSelectedImages(resultCode, requestCode, data, this)
    }*/

    //onClicks
    fun onClickSelectCountry(view: View){
        val listCountry = CityHelper.getAllCountries(this)
        dialog.showSpinnerDialog(this, listCountry, binding.tvCountry)
        if(binding.tvCity.text.toString() != getString(R.string.select_city)){
            binding.tvCity.text = getString(R.string.select_city)
        }
    }

    fun onClickSelectCity(view: View){
        val selectedCountry = binding.tvCountry.text.toString()
        if(selectedCountry != getString(R.string.select_country)){
            val listCity = CityHelper.getAllCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, binding.tvCity)
        }else{
            Toast.makeText(this, "No country selected", Toast.LENGTH_LONG).show()
        }

    }

    fun onClickSelectCategory(view: View){

        val listCity = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCity, binding.tvCat)

    }

    fun onClickGetImages(view: View) {
        if(imageAdapter.mainArray.size == 0){
            ImagePicker.launcher(this, launcherMultiSelectImage, 3)
        }else{
            openChooseImageFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }

    }

    fun onClickPublish(view: View){
        val adTemp = fillAd()
        if(isEditState){
            dbManager.publishAd(adTemp.copy(key = ad?.key), onPublishFinish())
        }else{
            dbManager.publishAd(adTemp, onPublishFinish())
        }
    }

    //Дожидаемся загрузки данных перед закрытием EditAdsActivity
    private fun onPublishFinish(): DbManager.FinishWorkListener{
        return object: DbManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }
        }
    }

    //Заполнение data-класса объявления
    private fun fillAd(): Ad{
        val ad: Ad
        binding.apply {
            ad = Ad(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                editTel.text.toString(),
                editIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCat.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                dbManager.db.push().key,
                "0",
                dbManager.auth.uid //генерация ключа
            )
        }
        return ad
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrolViewMain.visibility = View.VISIBLE
        imageAdapter.updateAdapter(list)
        chooseImageFrag = null
    }

    fun openChooseImageFragment(newList : ArrayList<String>?){
        chooseImageFrag = ImageListFrag(this, newList)
        binding.scrolViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }


}