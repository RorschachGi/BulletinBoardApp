package com.louro_horo24.bulletinboardapp.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.louro_horo24.bulletinboardapp.adapters.ImageAdapter
import com.louro_horo24.bulletinboardapp.model.Ad
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.File
import java.io.InputStream

object ImageManager {

    const val MAX_IMAGE_SIZE = 1000
    const val WIDTH = 0
    const val HEIGHT = 1

    //Получаем ширину и высоту изображения
    /*fun getImageSize(uri: String): List<Int>{


        val options = BitmapFactory.Options().apply{
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(uri, options)

        return if(imageRotation(uri) == 90){
            listOf(options.outHeight, options.outWidth)
        }else{
            listOf(options.outWidth, options.outHeight)
        }

    }*/

    fun getImageSize(uri: Uri, act: Activity): List<Int> {

        val inStream = act.contentResolver.openInputStream(uri)

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeStream(inStream, null, options)

        return listOf(options.outWidth, options.outHeight)


    }


    suspend fun imageResize(uris: List<Uri>, act: Activity): List<Bitmap> = withContext(Dispatchers.IO){

        //для хранения ширины и высоты изображений
        val tempList = ArrayList<List<Int>>()

        val bitmapList = ArrayList<Bitmap>()

        for(n in uris.indices){

            val size = getImageSize(uris[n], act)

            //Вычисляем пропорцию. Делим ширину на высоту
            val imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat()

            //В зависимости от ориентации
            //Если ширина больше высоты
            if(imageRatio > 1){

                if(size[WIDTH] > MAX_IMAGE_SIZE ){
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt() ))
                } else{
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }

                //Если высота больше ширины
            }else{

                if(size[HEIGHT] > MAX_IMAGE_SIZE ){
                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE ))
                } else{
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
            }

        }

        //Сжатие изображений
        for(i in uris.indices){
            kotlin.runCatching {
                bitmapList.add(Picasso.get().load((uris[i])).resize(tempList[i][WIDTH], tempList[i][HEIGHT]).get())
            }
        }


        return@withContext bitmapList

    }

    //Обработка изображения в зависимости от ориентации
    fun chooseScaleType(im: ImageView, bitmap: Bitmap){
        if(bitmap.width > bitmap.height){
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        }else{
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    //Из String в Bitmap
    private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO){

        val bitmapList = ArrayList<Bitmap>()

        for(i in uris.indices){
            kotlin.runCatching {
                bitmapList.add(Picasso.get().load(uris[i]).get())
            }
        }

        return@withContext bitmapList
    }

    fun fieldImageArray(ad: Ad, adapter: ImageAdapter){
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        CoroutineScope(Dispatchers.Main).launch {
            val bitmapList = getBitmapFromUris(listUris)
            adapter.updateAdapter(bitmapList as ArrayList<Bitmap>)
        }
    }

}