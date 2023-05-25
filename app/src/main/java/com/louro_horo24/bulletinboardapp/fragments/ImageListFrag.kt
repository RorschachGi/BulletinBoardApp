package com.louro_horo24.bulletinboardapp.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.act.EditAdsActivity
import com.louro_horo24.bulletinboardapp.databinding.ListImageFragBinding
import com.louro_horo24.bulletinboardapp.dialoghelper.ProgressDialog
import com.louro_horo24.bulletinboardapp.utils.AdapterCallback
import com.louro_horo24.bulletinboardapp.utils.ImageManager
import com.louro_horo24.bulletinboardapp.utils.ImagePicker
import com.louro_horo24.bulletinboardapp.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFrag(private val fragCloseInterface: FragmentCloseInterface, private val newList: ArrayList<String>?) : Fragment(), AdapterCallback {

    lateinit var binding: ListImageFragBinding
    val adapter = SelectImageRvAdapter(this)
    val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var job: Job? = null
    private var addImageItem: MenuItem? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ListImageFragBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar()

        //функции drag & drop и swipe-to-dismiss в RecyclerView
        touchHelper.attachToRecyclerView(binding.rcViewSelectImage)

        //activity в фрагменте уже есть, поскольку фрагмент встроен в нее
        binding.rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
        binding.rcViewSelectImage.adapter = adapter

        if(newList != null){
            resizeSelectedImages(newList, true)
        }

    }

    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }

    override fun onDetach() {
        super.onDetach()
        fragCloseInterface.onFragClose(adapter.mainArray)
        job?.cancel() // остановка Корутины при закрытии фрагмента
    }

    private fun resizeSelectedImages(newList: ArrayList<String>, needClear: Boolean){
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity as Activity)
            val bitmapList = ImageManager.imageResize(newList)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if(adapter.mainArray.size > 2) addImageItem?.isVisible = false
        }
    }

    //Работа с toolbar
    private fun setUpToolbar(){

        binding.tb.inflateMenu(R.menu.menu_choose_image)

        val deleteItem = binding.tb.menu.findItem(R.id.id_delete_image)
        addImageItem = binding.tb.menu.findItem(R.id.id_add_image)

        //выход из фрагмента
        binding.tb.setNavigationOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }

        //удаление изображений с экрана редактирования
        deleteItem.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            addImageItem?.isVisible = true
            true
        }

        //добавление изображений с экрана редактирования
        addImageItem?.setOnMenuItemClickListener {
            val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePicker.launcher(activity as EditAdsActivity, (activity as EditAdsActivity).launcherMultiSelectImage, imageCount)
            true
        }
    }

    fun updateAdapter(newList: ArrayList<String>){
        resizeSelectedImages(newList, false)
    }

    //Замена одного изображения другим
    fun setSingleImage(uri: String, pos: Int){
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(listOf(uri))
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }

    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>){
        adapter.updateAdapter(bitmapList, true)
    }




}