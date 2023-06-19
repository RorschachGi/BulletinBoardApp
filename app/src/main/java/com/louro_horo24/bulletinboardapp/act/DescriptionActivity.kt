package com.louro_horo24.bulletinboardapp.act

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.louro_horo24.bulletinboardapp.MainActivity
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.adapters.ImageAdapter
import com.louro_horo24.bulletinboardapp.databinding.ActivityDescriptionBinding
import com.louro_horo24.bulletinboardapp.model.Ad
import com.louro_horo24.bulletinboardapp.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

class DescriptionActivity : AppCompatActivity() {

    lateinit var binding: ActivityDescriptionBinding

    lateinit var adapter: ImageAdapter

    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        binding.fbTel.setOnClickListener{ call() }

        binding.fbEmail.setOnClickListener{ sendEmail() }
    }

    private fun init(){
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter
        }
        getIntentFromMainAct()
        imageChangeCounter()

    }

    fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
    }


    //Получаем объявление
    private fun getIntentFromMainAct(){
        ad = getSerializable(this, MainActivity.ADS_DESK, Ad::class.java)

        if( ad != null){
            updateUI(ad!!)
        }

    }

    private fun updateUI(ad: Ad){
        ImageManager.fieldImageArray(ad, adapter)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: Ad) = with(binding){
        tvTitle.text = ad.title
        tvDescription.text = ad.description
        tvEmail.text = ad.email
        tvPrice.text = ad.price
        tvTel.text = ad.tel
        tvCountry.text = ad.country
        tvCity.text = ad.city
        tvIndex.text = ad.index
        tvWithSend.text = isWithSend(ad.withSend.toBoolean())

    }

    private fun isWithSend(withSend: Boolean): String{
        val withSendTrue = getString(R.string.with_send_true)
        val withSendFalse = getString(R.string.with_send_false)
        return if(withSend) withSendTrue else withSendFalse
    }


    private fun call(){

        val callUri = "tel:${ad?.tel}"
        val iCall = Intent(Intent.ACTION_DIAL)
        iCall.data = callUri.toUri()
        startActivity(iCall)

    }

    //Intent.createChooser - выбрать с помощью какого приложения открыть
    //Отправка письма продавцу на почту
    private fun sendEmail(){

        val iSendEmail = Intent(Intent.ACTION_SEND)
        iSendEmail.type = "message/rfc822"
        iSendEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            putExtra(Intent.EXTRA_SUBJECT, "Объявление")
            putExtra(Intent.EXTRA_TEXT, "Меня интересует ваше объявление")
        }
        try {
            startActivity(Intent.createChooser(iSendEmail, "Открыть с"))
        }catch (e: ActivityNotFoundException){
            Toast.makeText(this, "Нет подходящих приложений", Toast.LENGTH_LONG).show()
        }

    }

    //Счетчик изображений
    private fun imageChangeCounter(){
        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
                binding.tvImageCounter.text = imageCounter
            }
        })
    }




}