package com.louro_horo24.bulletinboardapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File

object ImageManager {

    const val MAX_IMAGE_SIZE = 1000
    const val WIDTH = 0
    const val HEIGHT = 1

    //Получаем ширину и высоту изображения
    fun getImageSize(uri: String): List<Int>{

        val options = BitmapFactory.Options().apply{
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(uri, options)

        return if(imageRotation(uri) == 90){
            listOf(options.outHeight, options.outWidth)
        }else{
            listOf(options.outWidth, options.outHeight)
        }

    }

    //Получаем ориентацию экрана ( верт/горизонт)
    private fun imageRotation(uri: String): Int{

        val rotation: Int

        //Получаем файл по ссылке, чтобы считать данные об ориентации
        val imageFile = File(uri)

        val exif = ExifInterface(imageFile.absolutePath)

        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        rotation = if(orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270){
            90
        }else{
            0
        }

        return rotation
    }

    //Изменяем размеры изображения при необходимости
    //Сжатие изображений трудоемкий процесс, используем Coroutine
    suspend fun imageResize(uris: List<String>): List<Bitmap> = withContext(Dispatchers.IO){

        //для хранения ширины и высоты изображений
        val tempList = ArrayList<List<Int>>()

        val bitmapList = ArrayList<Bitmap>()

        for(n in uris.indices){

            val size = getImageSize(uris[n])

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
            bitmapList.add(Picasso.get().load(File(uris[i])).resize(tempList[i][WIDTH], tempList[i][HEIGHT]).get())
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

}