package com.louro_horo24.bulletinboardapp.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
//import com.fxn.pix.Options
//import com.fxn.pix.Pix
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.act.EditAdsActivity
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ak1.pix.models.Options

object ImagePicker {

    const val MAX_IMAGE_COUNT = 3


    private fun getOptions(imageCounter: Int): Options{
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }



    fun getMultiImages(edAct: EditAdsActivity, imageCounter: Int){
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)){ result ->
            when(result.status){
                PixEventCallback.Status.SUCCESS ->{
                    getMultiSelectImages(edAct, result.data)
                    //closePixFragment(edAct)
                }
                else -> {}
            }
        }
    }

    fun addImages(edAct: EditAdsActivity, imageCounter: Int){
        val frag = edAct.chooseImageFrag
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)){ result ->
            when(result.status){
                PixEventCallback.Status.SUCCESS ->{
                    edAct.chooseImageFrag = frag
                    openChooseImageFrag(edAct, frag!!)
                    edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct)
                }
                else -> {}
            }
        }
    }

    fun getSingleImage(edAct: EditAdsActivity){
        val frag = edAct.chooseImageFrag //сохранение предыдущего фрагмента
        edAct.addPixToActivity(R.id.place_holder, getOptions(1)){ result ->
            when(result.status){
                PixEventCallback.Status.SUCCESS ->{
                    edAct.chooseImageFrag = frag
                    openChooseImageFrag(edAct, frag!!)
                    singleImage(edAct, result.data[0])
                }
                else -> {}
            }
        }
    }

    private fun openChooseImageFrag(edAct: EditAdsActivity, frag: Fragment){
        edAct.supportFragmentManager.beginTransaction().replace(R.id.place_holder, frag).commit()
    }

    private fun closePixFragment(edAct: EditAdsActivity){
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach{
            if(it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    /*fun getLauncherForMultiSelectImages(edAct: EditAdsActivity): ActivityResultLauncher<Intent>{

        return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){

                if(result.data != null){

                    val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)

                    if(returnValues?.size!! > 1 && edAct.chooseImageFrag == null){

                        edAct.openChooseImageFragment(returnValues)

                    }else if(returnValues.size == 1 && edAct.chooseImageFrag == null){

                        CoroutineScope(Dispatchers.Main).launch{
                            edAct.binding.pBarLoad.visibility = View.VISIBLE
                            val bitmapArray = ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
                            edAct.binding.pBarLoad.visibility = View.GONE
                            edAct.imageAdapter.updateAdapter(bitmapArray)
                        }

                    }else if(edAct.chooseImageFrag != null){
                        edAct.chooseImageFrag?.updateAdapter(returnValues)
                    }
                }
            }
        }
    }*/

    fun getMultiSelectImages(edAct: EditAdsActivity, uris: List<Uri>) {


        if (uris.size > 1 && edAct.chooseImageFrag == null) {

            edAct.openChooseImageFragment(uris as ArrayList<Uri>)

        } else if (uris.size == 1 && edAct.chooseImageFrag == null) {

            CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.pBarLoad.visibility = View.VISIBLE
                val bitmapArray =
                    ImageManager.imageResize(uris as ArrayList<Uri>, edAct) as ArrayList<Bitmap>
                edAct.binding.pBarLoad.visibility = View.GONE
                edAct.imageAdapter.updateAdapter(bitmapArray)
                closePixFragment(edAct)
            }

        }

    }


    private fun singleImage(edAct: EditAdsActivity, uri: Uri) {
        edAct.chooseImageFrag?.setSingleImage(uri, edAct.editImagePos)
    }

}



