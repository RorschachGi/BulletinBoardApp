package com.louro_horo24.bulletinboardapp.act

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.tasks.OnCompleteListener
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
import com.louro_horo24.bulletinboardapp.utils.ImageManager
import com.louro_horo24.bulletinboardapp.utils.ImagePicker
import java.io.ByteArrayOutputStream
import java.io.Serializable

class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {

    lateinit var binding: ActivityEditAdsBinding

    private var dialog = DialogSpinnerHelper()

    lateinit var imageAdapter: ImageAdapter

    var chooseImageFrag: ImageListFrag? = null

    private val dbManager = DbManager()

    var editImagePos = 0

    private var imageIndex = 0

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
        updateImageCounter(0)
        ImageManager.fieldImageArray(ad, imageAdapter)
    }

    //Запрос разрешения на доступ к изображениям, камере
    /*override fun onRequestPermissionsResult(
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
    }*/

    private fun init(){
        imageAdapter = ImageAdapter()
        binding.vpimages.adapter = imageAdapter
        imageChangeCounter()
        //launcherMultiSelectImage = ImagePicker.getLauncherForMultiSelectImages(this)
        //launcherSingleSelectImage = ImagePicker.getLauncherForSingleImage(this)
    }


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
            ImagePicker.getMultiImages(this, 3)
        }else{
            openChooseImageFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }

    }

    fun onClickPublish(view: View) {
        ad = fillAd()
        uploadImages()
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
        val adTemp: Ad
        binding.apply {
            adTemp = Ad(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                editTel.text.toString(),
                editIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCat.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                editEmail.text.toString(),
                ad?.mainImage ?:"empty",
                ad?.image2 ?:"empty",
                ad?.image3 ?: "empty",
                ad?.key ?: dbManager.db.push().key,
                "0",
                dbManager.auth.uid, //генерация ключа
                ad?.time ?: System.currentTimeMillis().toString()
            )
        }
        return adTemp
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrolViewMain.visibility = View.VISIBLE
        imageAdapter.updateAdapter(list)
        chooseImageFrag = null
        updateImageCounter(binding.vpimages.currentItem)
    }

    fun openChooseImageFragment(newList : ArrayList<Uri>?){
        chooseImageFrag = ImageListFrag(this)
        if(newList != null){
            chooseImageFrag?.resizeSelectedImages(newList, true, this)
        }
        binding.scrolViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }

    private fun uploadImages() {
        if (imageIndex == 3) {
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
        val oldUrl = getUrlFromAd()
        if (imageAdapter.mainArray.size > imageIndex) {
            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
            if(oldUrl.startsWith("http")){
                updateImage(byteArray, oldUrl){
                    nextImage(it.result.toString())
                }
            }else{
                uploadImage(byteArray) {
                    //dbManager.publishAd(ad!!, onPublishFinish())
                    nextImage(it.result.toString())
                }
            }
        } else {
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }

    //Счетчик изображений
    private fun nextImage(uri: String){
        setImageUriToAd(uri)
        imageIndex++
        uploadImages()
    }

    //Заполнение полей под изображения в классе Ad
    private fun setImageUriToAd(uri: String){
        when(imageIndex){
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(image2 = uri)
            2 -> ad = ad?.copy(image3 = uri)
        }
    }

    private fun getUrlFromAd(): String{
        return listOf(ad?.mainImage!!, ad?.image2!!, ad?.image3!!)[imageIndex]
    }

    //FireBase принимает изображения по байтам. Готовим изображение, возвращаем ByteArray
    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray{
        val outStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)

        return outStream.toByteArray()
    }

    //Загрузка одного изображения
    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>){

        val imStorageRef = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")

        val upTask = imStorageRef.putBytes(byteArray)

        upTask.continueWithTask{
            task -> imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)

    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>){
        dbManager.dbStorage.storage
            .getReferenceFromUrl(oldUrl)
            .delete().addOnCompleteListener(listener)
    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>) {
        val imStorageRef = dbManager.dbStorage.storage.getReferenceFromUrl(url)
        val upTask = imStorageRef.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }



    private fun imageChangeCounter(){
        binding.vpimages.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    private fun updateImageCounter(counter: Int){
        var index = 1
        val itemCount = binding.vpimages.adapter?.itemCount
        if(itemCount == 0) index = 0
        val imageCounter = "${counter + index}/${itemCount}"
        binding.tvImageCounter.text = imageCounter
    }

}