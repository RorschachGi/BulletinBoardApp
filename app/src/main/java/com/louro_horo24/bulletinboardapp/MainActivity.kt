package com.louro_horo24.bulletinboardapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.louro_horo24.bulletinboardapp.accountHelper.AccountHelper
import com.louro_horo24.bulletinboardapp.act.EditAdsActivity
import com.louro_horo24.bulletinboardapp.adapters.AdsRcAdapter
import com.louro_horo24.bulletinboardapp.databinding.ActivityMainBinding
import com.louro_horo24.bulletinboardapp.dialoghelper.DialogConst
import com.louro_horo24.bulletinboardapp.dialoghelper.DialogHelper
import com.louro_horo24.bulletinboardapp.dialoghelper.GoogleAccConst
import com.louro_horo24.bulletinboardapp.model.Ad
import com.louro_horo24.bulletinboardapp.viewmodel.FirebaseViewModel

//OnNavigationItemSelectedListener
//NavigationView.OnNavigationItemSelectedListener
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {

    private lateinit var tvAccount: TextView

    lateinit var binding: ActivityMainBinding

    private val dialogHelper = DialogHelper(this)

    //Точка входа в Firebase Authentication SDK.
    //С помощью getInstance() получаем экземпляр класса
    val myAuth = Firebase.auth

    val adapter = AdsRcAdapter(this)

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadAllAds()
        bottomMenuOnClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == GoogleAccConst.GOOGLE_SIGN_IN_REQUEST_CODE){

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account != null){
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            }catch (e: ApiException){
                Log.d("MyLog", "Api error: ${e.message}")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //currentUser - если не зарегестрирован вернет null, иначе вернет user
    override fun onStart() {
        super.onStart()
        uiUpdate(myAuth.currentUser)
    }

    override fun onResume() {
        super.onResume()
        binding.includeID.bNavView.selectedItemId = R.id.id_home
    }

    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this, {
            adapter.updateAdapter(it)
            binding.includeID.tvEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    private fun init() {

        setSupportActionBar(binding.includeID.toolbar)

        //Создание кнопки в Toolbar для открытия NavigationView
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.includeID.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        //navView будет передавать события ( нажатия ) в этот же класс
        binding.navView.setNavigationItemSelectedListener(this)

        //Получаем доступ к TextView в header
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)


        /*binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.id_my_ads -> {
                    Toast.makeText(this, "Presed id_my_ads", Toast.LENGTH_SHORT).show()
                }

                R.id.id_car -> {
                    Toast.makeText(this, "Presed id_car", Toast.LENGTH_SHORT).show()
                }

                R.id.id_pc -> {
                    Toast.makeText(this, "Presed id_pc", Toast.LENGTH_SHORT).show()
                }

                R.id.id_smart -> {
                    Toast.makeText(this, "Presed id_smart", Toast.LENGTH_SHORT).show()
                }

                R.id.id_dm -> {
                    Toast.makeText(this, "Presed id_dm", Toast.LENGTH_SHORT).show()
                }

                R.id.id_realty -> {
                    Toast.makeText(this, "Presed id_realty", Toast.LENGTH_SHORT).show()
                }

                R.id.id_animal -> {
                    Toast.makeText(this, "Presed id_animal", Toast.LENGTH_SHORT).show()
                }

                R.id.id_sign_up -> {
                    Toast.makeText(this, "Presed id_sign_up", Toast.LENGTH_SHORT).show()
                }

                R.id.id_sign_in -> {
                    Toast.makeText(this, "Presed id_sign_in", Toast.LENGTH_SHORT).show()
                }

                R.id.id_sign_out -> {
                    Toast.makeText(this, "Presed id_sign_out", Toast.LENGTH_SHORT).show()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }*/
    }

    private fun bottomMenuOnClick() = with(binding){
        includeID.bNavView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.id_new_add -> {
                    val i = Intent(this@MainActivity, EditAdsActivity::class.java)
                    startActivity(i)
                }
                R.id.id_my_ads ->{
                    firebaseViewModel.loadMyAds()
                    includeID.toolbar.title = getString(R.string.ad_my_ads)
                }
                R.id.id_favs ->{
                    firebaseViewModel.loadMyFavs()
                    includeID.toolbar.title = getString(R.string.ad_my_favourites)
                }
                R.id.id_home ->{
                    firebaseViewModel.loadAllAds()
                    includeID.toolbar.title = getString(R.string.def)
                }
            }
            true
        }
    }

    //Инициализируем RecyclerView
    private fun initRecyclerView(){

        binding.apply {

            includeID.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            includeID.rcView.adapter = adapter

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.id_my_ads -> {
                Toast.makeText(this, "Pressed id_my_ads", Toast.LENGTH_SHORT).show()
            }

            R.id.id_car -> {
                Toast.makeText(this, "Pressed id_car", Toast.LENGTH_SHORT).show()
            }

            R.id.id_pc -> {
                Toast.makeText(this, "Pressed id_pc", Toast.LENGTH_SHORT).show()
            }

            R.id.id_smart -> {
                Toast.makeText(this, "Pressed id_smart", Toast.LENGTH_SHORT).show()
            }

            R.id.id_dm -> {
                Toast.makeText(this, "Pressed id_dm", Toast.LENGTH_SHORT).show()
            }

            R.id.id_realty -> {
                Toast.makeText(this, "Pressed id_realty", Toast.LENGTH_SHORT).show()
            }

            R.id.id_animal -> {
                Toast.makeText(this, "Pressed id_animal", Toast.LENGTH_SHORT).show()
            }

            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }

            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }

            R.id.id_sign_out -> {
                if(myAuth.currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }

                uiUpdate(null)

                //signOut() - для выхода из аккаунта
                myAuth.signOut()
                dialogHelper.accHelper.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?){

        if(user == null){
            resources.getString(R.string.not_reg)
            dialogHelper.accHelper.signInAnonymously(object : AccountHelper.onCompleteListener{
                override fun onComplete() {
                    tvAccount.text = resources.getString(R.string.anon_sign)
                }
            })
        }else if(user.isAnonymous){
            tvAccount.text = resources.getString(R.string.anon_sign)
        }else if(!user.isAnonymous){
            tvAccount.text = user.email
        }

    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }

    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }
}
