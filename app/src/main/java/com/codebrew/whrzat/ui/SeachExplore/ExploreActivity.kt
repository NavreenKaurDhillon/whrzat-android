package com.codebrew.whrzat.ui.SeachExplore

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivityExploreBinding
import com.codebrew.whrzat.ui.Home.*
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.explore.EventListData
import com.codebrew.whrzat.webservice.pojo.explore.Hotspot
import com.codebrew.whrzat.webservice.pojo.explore.HotspotData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.google.android.material.tabs.TabLayout

import okhttp3.ResponseBody

class ExploreActivity : AppCompatActivity(),ExploreContracts.View,SwipeRefreshLayout.OnRefreshListener, TextWatcher,ExploreAdapter.OnSpotItemClickListener{
    private lateinit var progressDialog: ProgressDialog
    private lateinit var presenter: ExploreContracts.Presenter
    private lateinit var exploreAdapter: ExploreAdapter
    private lateinit var exploreEventAdapter: ExploreEventAdapter
    private var eventList = ArrayList<EventListData.Event>()
    private var exploreList = ArrayList<Hotspot>()
    private var exploreListItems = ArrayList<Hotspot>()
    private var totalspot = 0
    private val TAG = "ExploreActivity"
    private var totalspotEvent = 0
    private var isClear = false
    private var isClearEvent = false
    private var isSearch = false
    private var isSpotReceive = false
    private var isSpotReceiveEvent = false
    private var skip = 0
    private var skipEvent = 0
    private lateinit var binding: ActivityExploreBinding
    private lateinit var sendExploreData: SendExploreData
    private var spotRadius = 200

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_explore)
//        setContentView(R.layout.activity_explore)
        Log.d(TAG, "onCreate: StartActivity")
        progressDialog = ProgressDialog(this)
        presenter = ExplorePresenter()
        presenter.attachView(this)
        exploreList.clear()
        exploreListItems.clear()
        val userData = Prefs.with(this).getObject(Constants.LOGIN_DATA, LoginData::class.java)

      if (ContextCompat.checkSelfPermission(
         this,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        explain("Alert","Please switch on location services in settings to get nearby hotspots.","Settings")

      }
        if(userData.radius==0){
            spotRadius = 200
        }else{
            spotRadius = userData.radius
        }
        sendExploreData = sendExploreData("")

        binding.swipeRefresh.isEnabled = false
       // radiusCalculate()
        listSpot()
        clickListeners()

        setAdapter()    //list adapter
        setAdapterEvents()
        setUpTabs()


        binding.imgExpoback.setOnClickListener {
            onBackPressed()
        }

        binding.btnCreateEvent.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://whrzat.com/even.html")))
        }
        val window = window
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()


    }

  private fun explain(title : String,msg: String, positiveText : String) {
    var positiveT = "Yes"
    if (positiveText!=null){
      positiveT = positiveText
    }
//    val dialog = androidx.appcompat.app.AlertDialog.Builder(this@HomeActivity)
//    dialog.setMessage(msg)
//      dialog.setTitle("          "+title)
//      .setPositiveButton(positiveT) { paramDialogInterface, paramInt ->
//        //  permissionsclass.requestPermission(type,code);
//        startActivity(
//          Intent(
//            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//            Uri.parse("package:com.codebrew.whrzat")
//          )
//        )
//      }
//      .setNegativeButton("Cancel") { paramDialogInterface, paramInt ->
//        // Toast.makeText(this@HomeActivity, "Required", Toast.LENGTH_LONG).show()
//      }
//    dialog.show()

    //custom
    val builder = AlertDialog.Builder(this)
    val customView : View= getLayoutInflater().inflate(R.layout.custom_layout2, null);
    builder.setView(customView);

    val textViewTitle = customView.findViewById<TextView>(R.id.heading2)
    val textViewMessage = customView.findViewById<TextView>(R.id.description2)
    textViewTitle.setText(title)
    textViewMessage.setText(msg)
    val okButton = customView.findViewById<TextView>(R.id.okBT)
    val noButton = customView.findViewById<TextView>(R.id.noBT)
    okButton.setText(positiveT)
    noButton.setText("Cancel")

    builder.setView(customView)
    // Create the AlertDialog
    val alertDialog: android.app.AlertDialog = builder.create()
    // Set other dialog properties
    alertDialog.setCancelable(false)
    okButton.setOnClickListener {
      startActivity(
        Intent(
          Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
          Uri.parse("package:com.codebrew.whrzat")
        )
      )
    }
    noButton.setOnClickListener {
      alertDialog.dismiss()
    }
    alertDialog.show()
  }



  private fun clickListeners() {

        binding.etExpoSearch.addTextChangedListener(this)

        binding.rvTrending.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView!!, dx, dy)
                val totalItems = mLinearLayoutManager.itemCount
                val firstVisible = mLinearLayoutManager.findFirstVisibleItemPosition()
                val lastItems = mLinearLayoutManager.findLastCompletelyVisibleItemPosition()

                if (totalItems == lastItems + 1) {
                    if (isSpotReceive && skip != 0) {
                        if (!isSearch) {
                            if (totalspot != exploreAdapter.itemCount)
                                isClear = false
                            binding.swipeRefresh.isRefreshing = false
                            //  val loading=loading()

                            if (GeneralMethods.isNetworkActive(this@ExploreActivity)) {
                                if (Prefs.with(this@ExploreActivity).getBoolean(Constants.IS_INFINITY, false)) {
                                    presenter.performExploreApi(sendExploreData, false, -1, skip)

                                } else {
                                    if (Prefs.with(this@ExploreActivity).getString(Constants.RADIUS, "").isEmpty()) {
                                        presenter.performExploreApi(sendExploreData, false, -1, skip)
                                        // EventBus.getDefault().postSticky(RefreshProfileApi(true))
                                    } else {
                                        val radius = Prefs.with(this@ExploreActivity).getString(Constants.RADIUS, "").toInt()
                                        spotRadius = radius
                                        presenter.performExploreApi(sendExploreData, false, radius, skip)
                                    }
                                }
                            } else {
                                GeneralMethods.showToast(this@ExploreActivity, R.string.error_no_connection)
                            }
                        }
                    }
                }
            }
        })
        binding.rvEvent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItems = mLinearLayoutManagerEvent.itemCount
                val firstVisible = mLinearLayoutManagerEvent.findFirstVisibleItemPosition()
                val lastItems = mLinearLayoutManagerEvent.findLastCompletelyVisibleItemPosition()


                if (totalItems == lastItems + 1) {
                    if (isSpotReceiveEvent && skipEvent != 0) {
//                        if (!isSearch) {
                        if (totalspotEvent != exploreEventAdapter.itemCount)
                            isClearEvent = false
                        binding.swipeRefresh.isRefreshing = false
                        //  val loading=loading()

                        if (GeneralMethods.isNetworkActive(this@ExploreActivity)) {
//                             presenter.performEventListApi(skipEvent)
                        } else {
                            GeneralMethods.showToast(this@ExploreActivity, R.string.error_no_connection)
                        }
//                        }
                    }
                }
            }
        })
    }

    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.llExplore.setBackgroundColor(Color.parseColor("#000000"))
               binding. tabLayout.setBackgroundColor(Color.parseColor("#000000"))
               binding. tabLayout.setSelectedTabIndicatorColor(Color.WHITE)
               binding. tabLayout.setTabTextColors(Color.GRAY, Color.WHITE)
                binding.imgExpoback.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
               binding. txtExpTitle.setTextColor(Color.WHITE)
                binding.etExpoSearch.setHintTextColor(Color.parseColor("#000000"))
                binding.etExpoSearch.setTextColor(Color.parseColor("#000000"))
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor= ContextCompat.getColor(this, R.color.black)

            }
            Configuration.UI_MODE_NIGHT_NO -> {

              binding.  llExplore.setBackgroundColor(Color.WHITE)
              binding.  tabLayout.setBackgroundColor(Color.WHITE)
               binding. tabLayout.setSelectedTabIndicatorColor(Color.BLACK)
               binding. tabLayout.setTabTextColors(Color.parseColor("#80000000"), Color.BLACK)
                binding.imgExpoback.setImageResource(R.drawable.ic_baseline_arrow_back_24)
               binding. txtExpTitle.setTextColor(Color.parseColor("#000000"))
              /*  binding.etExpoSearch.setHintTextColor(Color.parseColor("#3d4143"))*/
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
               // window.navigationBarColor=ContextCompat.getColor(this, R.color.white)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }

    private fun setUpTabs() {
       binding. tabLayout.addTab(binding.tabLayout.newTab().setText("Trending"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Events"))
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {

                    0 -> {
                        binding.rvTrending.visibility = View.VISIBLE
                        binding.rvEvent.visibility = View.GONE
                        binding.btnCreateEvent.visibility=View.GONE
                        binding.tvNoEvent.text = "No hotspots found"

                        if (exploreAdapter.itemCount == 0) {
                            binding.tvNoEvent.visibility = View.VISIBLE
                        } else {

                            binding.tvNoEvent.visibility = View.GONE
                        }
                    }
                    1 -> {

                        binding.rvTrending.visibility = View.GONE
                        binding.rvEvent.visibility = View.VISIBLE
                        binding.tvNoEvent.text = "No event found"
                        binding.btnCreateEvent.visibility=View.GONE
                        presenter.performEventListApi(sendExploreData, false, spotRadius, skipEvent)
                        if (exploreEventAdapter.itemCount == 0) {
                            binding.tvNoEvent.visibility = View.VISIBLE
                        } else {
                            binding.tvNoEvent.visibility = View.GONE
                        }
                    }

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }
    private lateinit var mLinearLayoutManager: GridLayoutManager
    private lateinit var mLinearLayoutManagerEvent: LinearLayoutManager
    private fun setAdapter() {
        exploreAdapter = ExploreAdapter(this)
        mLinearLayoutManager = GridLayoutManager(this, 2)
        binding.rvTrending.layoutManager = mLinearLayoutManager
        binding.rvTrending.adapter = exploreAdapter
        exploreAdapter.setonSpotClickListener(this)
        // rvHome.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
    }
    private fun setAdapterEvents() {
        exploreEventAdapter = ExploreEventAdapter(this, eventList)
        mLinearLayoutManagerEvent = LinearLayoutManager(this)
        binding.rvEvent.layoutManager = mLinearLayoutManagerEvent
        binding.rvEvent.adapter = exploreEventAdapter
      //  binding.rvEvent?.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

    }

    override fun onResume() {
        super.onResume()
        isSearch = false
    /*    binding.etExpoSearch.text.clear()
        GeneralMethods.hideSoftKeyboard(this, binding.etExpoSearch)*/
    }
    override fun onRefresh() {
        val userData = Prefs.with(this).getObject(Constants.LOGIN_DATA, LoginData::class.java)
        spotRadius = userData.radius
        sendExploreData = sendExploreData("")
        referesh()
    }
    private fun referesh() {
        skip = 0
        skipEvent = 0
        exploreList.clear()
        exploreListItems.clear()
        isClear = true
        isClearEvent = true
        isSearch = false
        isSpotReceive = false
        isSpotReceiveEvent = false
        binding.swipeRefresh.isRefreshing = true
        listSpot()
    }

    private fun listSpot() {
        skip = 0
        skipEvent = 0

        if (GeneralMethods.isNetworkActive(this)) {
             presenter.performEventListApi(sendExploreData, false, spotRadius, skipEvent)

            if (Prefs.with(this).getBoolean(Constants.IS_INFINITY, false)) {
               // spotRadius = -1
                presenter.performExploreApi(sendExploreData, false, spotRadius, skip)
            } else {
                if (Prefs.with(this).getString(Constants.RADIUS, "").isNotEmpty()) {
                    presenter.performExploreApi(sendExploreData, false, spotRadius, skip)
                } else {
                    presenter.performExploreApi(sendExploreData, false, spotRadius, skip)
                }
            }
        } else {
            GeneralMethods.showToast(this, R.string.error_no_connection)
        }

        progressDialog.dismiss()
    }

    private fun radiusCalculate() {
        /*  if (mMap != null) {
              adapter.annotations.removeAll(adapter.annotations)
          }*/
        binding.swipeRefresh.isEnabled = false
        if (Prefs.with(this).getBoolean(Constants.IS_INFINITY, false)) {
            presenter.mapViewApi(sendExploreData, true, -1)

        } else {
            if (Prefs.with(this).getString(Constants.RADIUS, "").isEmpty()) {
                presenter.mapViewApi(sendExploreData, true, spotRadius)
                // EventBus.getDefault().postSticky(RefreshProfileApi(true))
            } else {
                spotRadius = Prefs.with(this).getString(Constants.RADIUS, "").toInt()
                presenter.mapViewApi(sendExploreData, true, spotRadius)
            }
        }
    }

    private fun sendExploreData(search: String): SendExploreData {
        return SendExploreData(Prefs.with(this).getString(Constants.LAT, ""),
                Prefs.with(this).getString(Constants.LNG, ""),
                "",
                Prefs.with(this).getString(Constants.USER_ID, "") as String,
                search)
    }


    override fun exploreList(exploreList: HotspotData) {
        binding.swipeRefresh.isRefreshing = false
        totalspot = exploreList.count
//        tvNoSpot.visibility = View.GONE
        skip += 10
        if (isSpotReceive) {
            val count = exploreAdapter.itemCount - 1
            exploreAdapter.remove(count)
            exploreAdapter.notifyItemRemoved(count)
        }

        if (isSearch) {
           // this.exploreList.clear()
             exploreAdapter.clearList()
             exploreAdapter.addListWithClear(exploreList.mapData)
           // spotDetailAdapter.addListWithClear(exploreList.mapData)
            if (!isSearch) {
                if (totalspot > 9) {
                    exploreAdapter.addLoading()
                }
            }
            isClear = false
        } else {
            //exploreAdapter.clearList()
            exploreAdapter.addList(exploreList.mapData)
            exploreListItems = exploreList.mapData as ArrayList<Hotspot>
            if (!isSearch) {
                if (totalspot > 9) {
                    exploreAdapter.addLoading()
                }
            }
        }
        if (binding.tabLayout.selectedTabPosition == 0) {
            if (exploreAdapter.itemCount == 0) {

                binding.tvNoEvent.text = "No hotspots found"
                binding.tvNoEvent.visibility = View.VISIBLE
            } else {
                binding.tvNoEvent.visibility = View.GONE
            }
        } else {
           /* binding.tvNoEvent.text = "No event found"
            if (exploreEventAdapter.itemCount == 0) {
                binding.tvNoEvent.visibility = View.VISIBLE
            } else {
                binding.tvNoEvent.visibility = View.GONE
            }*/

        }
        isSpotReceive = true
    }

    override fun eventListSuccess(data: EventListData.Data) {
        if (binding.tabLayout.selectedTabPosition == 0) {
            binding.tvNoEvent.visibility = View.GONE
        }
        eventList.clear()
        exploreEventAdapter.clearList()
        eventList.addAll(data.events!!)
        exploreEventAdapter.notifyDataSetChanged()
    }

    override fun showLoading() {
        progressDialog?.show()
    }

    override fun dismissLoading() {
        progressDialog?.dismiss()
    }

    override fun sessionExpired() {
        GeneralMethods.tokenExpired(this)
    }

    override fun failureExploreApi() {
        if (binding.tabLayout.selectedTabPosition == 0) {

            if (exploreAdapter.itemCount == 0) {
                binding.tvNoEvent.text = "No hotspots found"
                binding.tvNoEvent.visibility = View.VISIBLE
            } else {
                binding.tvNoEvent.visibility = View.GONE
            }

        } else {
         /*   binding.tvNoEvent.text = "No event found"
            if (exploreEventAdapter.itemCount == 0) {
                binding.tvNoEvent.visibility = View.VISIBLE
            } else {
                binding.tvNoEvent.visibility = View.GONE
            }*/

        }
    }

    override fun errorExploreApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(this, errorBody)
    }

    override fun noSpotFound() {
        isSpotReceive = false
        progressDialog.dismiss()
        binding.swipeRefresh.isRefreshing = false

        if (isSearch) {
            isSearch = false
            exploreList.clear()
           // spotList.clear()
            exploreAdapter.addListWithClear(exploreList)
            if (binding.tabLayout.selectedTabPosition == 0) {
                binding.tvNoEvent.text = "No hotspots found"
                binding.tvNoEvent.visibility = View.VISIBLE
            }
        }
        if (exploreAdapter.itemCount == 0) {
            exploreList.clear()
           // spotList.clear()
            exploreAdapter.addList(exploreList)
            if (binding.tabLayout.selectedTabPosition == 0) {
                binding.tvNoEvent.text = "No hotspots found"
                binding.tvNoEvent.visibility = View.VISIBLE
            }
        }
        if (totalspot > 9) {
            val count = exploreAdapter.itemCount - 1
            exploreAdapter.remove(count)
            exploreAdapter.notifyItemRemoved(count)
        }
        isSearch = true
    }

    override fun noEventFound() {
        eventList.clear()
        exploreEventAdapter.addListWithClear(eventList)
        exploreEventAdapter.notifyDataSetChanged()
        if (exploreEventAdapter.itemCount == 0) {
            if (binding.tabLayout.selectedTabPosition == 1) {
                binding.tvNoEvent.text = "No event found"
                binding.tvNoEvent.visibility = View.VISIBLE
            }
        } else {

            binding.tvNoEvent.visibility = View.GONE
        }
    }

    override fun errorApi(errorBody: ResponseBody) {

    }

    override fun failureApi() {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        exploreList.clear()
        exploreListItems.clear()
    }

    override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
          var str=binding.etExpoSearch.text.toString().trim()
       if (str.length==0){
           binding.swipeRefresh.isEnabled = false
         //  GeneralMethods.hideSoftKeyboard(this, binding.tvNoEvent)
           //presenter.mapViewApi(sendExploreData, true, spotRadius)
          // presenter.performExploreApi(sendExploreData, false,spotRadius, 0)

            exploreAdapter.clearList()
            onRefresh()
       }else{
           if (str.length >2) {
               binding.swipeRefresh.isEnabled = false
               isSearch = true
               isClear = true
               isClearEvent = true
               isSpotReceive = true
                exploreAdapter.notifyDataSetChanged()
               val sendSearchData = sendExploreData(str)
               spotRadius=-1;
               if (binding.tabLayout.selectedTabPosition == 0) {
                   presenter.performExploreApi(sendSearchData, false,spotRadius, 0)
               } else {
                   presenter.performEventListApi(sendSearchData, false, spotRadius, skipEvent)
               }

           }else{
               binding.swipeRefresh.isEnabled = false
              // onRefresh()
               //presenter.mapViewApi(sendExploreData, true, spotRadius)
             //  presenter.performExploreApi(sendExploreData, false,spotRadius, 0)
           }

       }

    }

    override fun afterTextChanged(s: Editable?) {


    }

    override fun onHotSpotClick(id: String) {
        isSearch = false
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(Constants.HOTSPOT_ID, id)
        startActivity(intent)
    }


}
