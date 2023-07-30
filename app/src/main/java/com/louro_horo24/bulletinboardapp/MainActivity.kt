package com.louro_horo24.bulletinboardapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.louro_horo24.bulletinboardapp.accountHelper.AccountHelper
import com.louro_horo24.bulletinboardapp.act.DescriptionActivity
import com.louro_horo24.bulletinboardapp.act.EditAdsActivity
import com.louro_horo24.bulletinboardapp.act.FilterActivity
import com.louro_horo24.bulletinboardapp.adapters.AdsRcAdapter
import com.louro_horo24.bulletinboardapp.databinding.ActivityMainBinding
import com.louro_horo24.bulletinboardapp.dialoghelper.DialogConst
import com.louro_horo24.bulletinboardapp.dialoghelper.DialogHelper
import com.louro_horo24.bulletinboardapp.model.Ad
import com.louro_horo24.bulletinboardapp.utils.CircleTransform
import com.louro_horo24.bulletinboardapp.utils.FilterManager
import com.louro_horo24.bulletinboardapp.viewmodel.FirebaseViewModel
import com.squareup.picasso.Picasso

//OnNavigationItemSelectedListener
//NavigationView.OnNavigationItemSelectedListener
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {

    private lateinit var tvAccount: TextView

    private lateinit var imAccount: ImageView

    lateinit var binding: ActivityMainBinding

    private val dialogHelper = DialogHelper(this)

    //Точка входа в Firebase Authentication SDK.
    //С помощью getInstance() получаем экземпляр класса
    val myAuth = Firebase.auth

    val adapter = AdsRcAdapter(this)

    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    lateinit var filterLauncher: ActivityResultLauncher<Intent>

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var clearUpdate: Boolean = true

    private var currentCategory: String? = null

    private var filter: String = "empty"

    private var filterDb: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
        onActivityResultFilter()
    }

    private fun onActivityResult() {

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("MyLog", "Api error: ${e.message}")
            }
        }
    }

    private fun onActivityResultFilter(){
        filterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
                if(it.resultCode == RESULT_OK){
                    filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
                    Log.d("MyLog", "Filter: $filter")
                    Log.d("MyLog", "GetFilter: ${FilterManager.getFilter(filter)}")
                    filterDb = FilterManager.getFilter(filter)
                }else if(it.resultCode == RESULT_CANCELED){
                    filterDb = ""
                    filter = "empty"
                }
        }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(myAuth.currentUser)
    }

    override fun onResume() {
        super.onResume()
        binding.includeID.bNavView.selectedItemId = R.id.id_home
    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this, {
            val list = getAdsByCategory(it)
            if (!clearUpdate) {
                adapter.updateAdapter(list)
            } else {
                adapter.updateWithClearAdapter(list)
            }
            binding.includeID.tvEmpty.visibility =
                if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        })
    }

    //Оставляем только объявления только конкретной категории при необходимости
    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad> {
        val tempList = ArrayList<Ad>()
        tempList.addAll(list)
        if (currentCategory != getString(R.string.def)) {
            tempList.clear()
            list.forEach {
                if (currentCategory == it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

    private fun init() {

        currentCategory = getString(R.string.def)

        setSupportActionBar(binding.includeID.toolbar)

        onActivityResult()

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

        //Получаем доступ к TextView и ImageView в header
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)

        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccountImage)

    }

    private fun bottomMenuOnClick() = with(binding) {
        includeID.bNavView.setOnNavigationItemSelectedListener { item ->
            clearUpdate = true
            when (item.itemId) {
                R.id.id_new_add -> {
                    val i = Intent(this@MainActivity, EditAdsActivity::class.java)
                    startActivity(i)
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    includeID.toolbar.title = getString(R.string.ad_my_ads)
                }
                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavs()
                    includeID.toolbar.title = getString(R.string.ad_my_favourites)
                }
                R.id.id_home -> {
                    currentCategory = getString(R.string.def)
                    firebaseViewModel.loadAllAdsFirstPage(filterDb)
                    includeID.toolbar.title = getString(R.string.def)
                }
            }
            true
        }
    }

    //Инициализируем RecyclerView
    private fun initRecyclerView() {

        binding.apply {

            includeID.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            includeID.rcView.adapter = adapter

        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //Запуск фильтр activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_filter) {
            val i = Intent(this@MainActivity, FilterActivity::class.java).apply {
                putExtra(FilterActivity.FILTER_KEY, filter)
            }
            filterLauncher.launch(i)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true
        when (item.itemId) {

            R.id.id_my_ads -> {
                Toast.makeText(this, "Pressed id_my_ads", Toast.LENGTH_SHORT).show()
                binding.includeID.toolbar.title = getString(R.string.ad_my_ads)
            }

            R.id.id_car -> {
                getAdsFromCat(getString(R.string.ad_car))
                binding.includeID.toolbar.title = getString(R.string.ad_car)
            }

            R.id.id_pc -> {
                getAdsFromCat(getString(R.string.ad_pc))
                binding.includeID.toolbar.title = getString(R.string.ad_pc)
            }

            R.id.id_smart -> {
                getAdsFromCat(getString(R.string.ad_smartphone))
                binding.includeID.toolbar.title = getString(R.string.ad_smartphone)
            }

            R.id.id_dm -> {
                getAdsFromCat(getString(R.string.ad_dm))
                binding.includeID.toolbar.title = getString(R.string.ad_dm)
            }

            R.id.id_realty -> {
                getAdsFromCat(getString(R.string.ad_realty))
                binding.includeID.toolbar.title = getString(R.string.ad_realty)
            }

            R.id.id_animal -> {
                getAdsFromCat(getString(R.string.ad_animal))
                binding.includeID.toolbar.title = getString(R.string.ad_animal)
            }

            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }

            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }

            R.id.id_sign_out -> {
                if (myAuth.currentUser?.isAnonymous == true) {
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

    private fun getAdsFromCat(cat: String) {
        currentCategory = cat
        firebaseViewModel.loadAllAdsFromCat(cat, filterDb)
    }

    fun uiUpdate(user: FirebaseUser?) {

        if (user == null) {
            resources.getString(R.string.not_reg)
            dialogHelper.accHelper.signInAnonymously(object : AccountHelper.onCompleteListener {
                override fun onComplete() {
                    tvAccount.text = resources.getString(R.string.anon_sign)
                    imAccount.setImageResource(R.drawable.ic_account_def)
                }
            })
        } else if (user.isAnonymous) {
            tvAccount.text = resources.getString(R.string.anon_sign)
            imAccount.setImageResource(R.drawable.ic_account_def)
        } else if (!user.isAnonymous) {
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).transform(CircleTransform()).into(imAccount)
        }

    }

    /*
        Пагинация
        addOnScrollListener - слушатель скролла в списке.
        Когда проскролили до конца срабатывает метод
        onScrollStateChanged - когда изменяется состояние при скролле
        newState - cостояние списка
     */
    private fun scrollListener() = with(binding.includeID) {

        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE) {

                    clearUpdate = false

                    val adsList = firebaseViewModel.liveAdsData.value!!

                    if (adsList.isNotEmpty()) {
                        getAdsFromCat(adsList)
                    }

                }
            }
        })
    }

    //Определение категории для подгрузки объявлений в scrollListener
    private fun getAdsFromCat(adsList: ArrayList<Ad>) {
        adsList[0].let {

            if (currentCategory == getString(R.string.def)) {
                firebaseViewModel.loadAllAdsNextPage(it.time, filterDb)
            } else {
                firebaseViewModel.loadAllAdsFromCatNextPage(it.category!!, it.time, filterDb)
            }

        }
    }


    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java)
        i.putExtra(ADS_DESK, ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val ADS_DESK = "ads_desc"
        const val SCROLL_DOWN = 1
    }
}
