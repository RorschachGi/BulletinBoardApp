package com.louro_horo24.bulletinboardapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.louro_horo24.bulletinboardapp.MainActivity
import com.louro_horo24.bulletinboardapp.R
import com.louro_horo24.bulletinboardapp.act.DescriptionActivity
import com.louro_horo24.bulletinboardapp.act.EditAdsActivity
import com.louro_horo24.bulletinboardapp.model.Ad
import com.louro_horo24.bulletinboardapp.databinding.AdListItemBinding
import com.squareup.picasso.Picasso

class AdsRcAdapter(val act: MainActivity): RecyclerView.Adapter<AdsRcAdapter.AdHolder>() {

    val adArray = ArrayList<Ad>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act)
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(adArray[position])
    }

    override fun getItemCount(): Int {
        return adArray.size
    }

    class AdHolder(val binding: AdListItemBinding, val act: MainActivity): RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Ad) = with(binding) {

            tvDescription.text = ad.description
            tvPrice.text = ad.price
            tvTitle.text = ad.title
            tvViewCounter.text = ad.viewsCounter
            tvFavCounter.text = ad.favCounter
            Picasso.get().load(ad.mainImage).into(mainImage)


            isFav(ad)
            showEditPanel(isOwner(ad))
            mainOnClick(ad)
        }

        private fun mainOnClick(ad: Ad) = with(binding){
            ibFav.setOnClickListener {
                if(act.myAuth.currentUser?.isAnonymous == false) act.onFavClicked(ad)
            }

            ibEditAd.setOnClickListener(onClickEdit(ad))

            ibDeleteAd.setOnClickListener{
                act.onDeleteItem(ad)
            }

            itemView.setOnClickListener{
                act.onAdViewed(ad)
            }

        }

        private fun isFav(ad: Ad){
            if(ad.isFav){
                binding.ibFav.setImageResource(R.drawable.ic_fav_pressed)
            }else{
                binding.ibFav.setImageResource(R.drawable.ic_fav_normal)
            }
        }

        //слушатель нажатий для кнопки редактирования объявления
        private fun onClickEdit(ad: Ad): OnClickListener{
            return View.OnClickListener {
                val editIntent = Intent(act, EditAdsActivity::class.java)

                //указываем, что открываем EditAdsActivity для редактирования
                editIntent.putExtra(MainActivity.EDIT_STATE, true)

                editIntent.putExtra(MainActivity.ADS_DATA, ad)

                act.startActivity(editIntent)
            }
        }


        private fun isOwner(ad: Ad): Boolean{
            return ad.uid == act.myAuth.uid
        }

        //Показываем/прячем панель редактирования обхявления в зависимости от пользователя
        private fun showEditPanel(isOwner: Boolean){
            if(isOwner){
                binding.editPanel.visibility = View.VISIBLE
            }else{
                binding.editPanel.visibility = View.GONE
            }
        }

    }

    fun updateAdapter(newList: List<Ad>){
        val tempArray = ArrayList<Ad>()
        tempArray.addAll(adArray)
        tempArray.addAll(newList)

        val diffResul = DiffUtil.calculateDiff(DiffUtilHelper(adArray, tempArray))
        diffResul.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(tempArray)
    }

    fun updateWithClearAdapter(newList: List<Ad>){
        val diffResul = DiffUtil.calculateDiff(DiffUtilHelper(adArray, newList))
        diffResul.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(newList)
    }


    interface Listener{
        fun onDeleteItem(ad: Ad)
        fun onAdViewed(ad: Ad)
        fun onFavClicked(ad: Ad)
    }



}