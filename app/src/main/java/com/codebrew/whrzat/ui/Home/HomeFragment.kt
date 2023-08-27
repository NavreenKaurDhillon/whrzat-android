package com.codebrew.whrzat.ui.Home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.tagstrade.adapter.FeedContactAdapter
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.HomeActivity
import com.codebrew.whrzat.databinding.FragmentHomeBinding
import com.codebrew.whrzat.event.*
import com.codebrew.whrzat.ui.SeachExplore.ExploreActivity
import com.codebrew.whrzat.ui.chat.userchat.ChatActivity
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.ui.feed.happening.FeedHappeningAdapter
import com.codebrew.whrzat.ui.map.MapActivity
import com.codebrew.whrzat.ui.otherprofile.ProfileOtherActivity
import com.codebrew.whrzat.ui.settings.FetchContacts
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.ApiContacts
import com.codebrew.whrzat.webservice.pojo.explore.EventListData
import com.codebrew.whrzat.webservice.pojo.explore.Hotspot
import com.codebrew.whrzat.webservice.pojo.explore.HotspotData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import com.codebrew.whrzat.webservice.pojo.feed.FeedData
import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.tabs.TabLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import nz.co.trademe.mapme.annotations.MapAnnotation
import nz.co.trademe.mapme.annotations.OnMapAnnotationClickListener
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class HomeFragment : Fragment(), OnMapReadyCallback,
        View.OnClickListener, HomeContract.View, OnMapAnnotationClickListener,
        ExploreAdapter.OnSpotItemClickListener, SpotAdapter.OnSpotItemClickListener, SwipeRefreshLayout.OnRefreshListener,
        TextView.OnEditorActionListener, TextWatcher, MapsAdapter.OnLoaderDismiss,
  FeedHappeningAdapter.OnHappeningItemClick,FeedContactAdapter.Love,
  FetchContacts.FetchContactsListener  {

    private lateinit var exploreAdapter: ExploreAdapter
    private lateinit var mContext: Context
    private lateinit var mMap: GoogleMap
    private var isMapLoaded = false
    private lateinit var progressDialog: ProgressDialog
    private lateinit var presenter: HomeContract.Presenter
    private lateinit var spotDetailAdapter: SpotAdapter
    private var exploreList = ArrayList<Hotspot>()
    private var spotList = ArrayList<Hotspot>()
    private var mapList = ArrayList<Hotspot>()
    private var mapListItems = ArrayList<Hotspot>()
    private val TAG = "HomeFragment"
    private var exploreListItems = ArrayList<Hotspot>()
    private var skip =0
    private var skipEvent = 0
    private var mapCount = 0
    private lateinit var sendExploreData: SendExploreData
    private var spotRadius =0
    private lateinit var mLinearLayoutManager: GridLayoutManager
    private lateinit var mLinearLayoutManagerEvent: LinearLayoutManager
    private lateinit var mSpotLayoutManagerOnMap: LinearLayoutManager
    private var totalspot = 0
    private var totalspotEvent = 0
    private var pos = 0
    private var isSpotReceive = false
    private var isSpotReceiveEvent = false
    private var isClear = false
    private var isClearEvent = false
    private var isSearch = false
    private var isMapSearchActive = false
    private var count = 0
    private var isCreate = false
    private var oldMapList = ArrayList<Hotspot>()
    private val READ_CONTACTS_PERMISSIONS_REQUEST = 21
    private var isContactSynced = false
    private var isPermissionGranted = false
    val homeActivity = HomeActivity()
    private lateinit var binding: FragmentHomeBinding
    var ii = 0
  private val REQUEST_ID_MULTIPLE_PERMISSIONS = 5

    companion object {
        lateinit var adapter: MapsAdapter
    }

    private var eventList = ArrayList<EventListData.Event>()
    private var happeningList = ArrayList<HappeningListData.ImageDatum>()
    private lateinit var exploreEventAdapter: ExploreEventAdapter
    private lateinit var feedHappeningAdapter: FeedHappeningAdapter
    private lateinit var feedContactAdapter: FeedContactAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        val view = binding.root
        Log.e("OnCreate","OnCreate")

        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                if(event?.getAction() == MotionEvent.ACTION_MOVE){
                GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.tvNoSpot)
//                }
                return true
            }
        })
        return view
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")
        progressDialog = ProgressDialog(mContext)
        presenter = HomePresenter()
        presenter.attachView(this)
        exploreList.clear()
        exploreListItems.clear()
        spotList.clear()
        isSearch = false
        Log.e("OnViewCreated","OnViewCreated")
     //   adapter = MapsAdapter(mContext)
        //adapter.setOnAnnotationClickListener(this)
       /*if(HomeActivity.latitude!=0.0){
           binding.mapExplore.getMapAsync(this)
       }
        setupMap(savedInstanceState)*/
        val userData = Prefs.with(mContext).getObject(Constants.LOGIN_DATA, LoginData::class.java)
        if(userData.radius==0){
            spotRadius = 200
        }else{
            spotRadius = userData.radius
        }
        sendExploreData = sendExploreData("")
        setViewType(getString(R.string.label_map), R.drawable.ic_map, View.VISIBLE, View.GONE, false)
        binding.etSearch.text.clear()

       // radiusCalculate()
      if (ContextCompat.checkSelfPermission(
          requireContext(),
          Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
          requireContext(),
          Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
      ) {
        Log.d(TAG, "onCreate: // already granted ")
        checkAndRequestPermissions()
//        getPermissionToReadUserContacts()
        // Permission is already granted, proceed with your functionality
        // ...
      } else {
        Log.d(TAG, "onCreate: /// denied" )
//        showDialog()
//        getPermissionToReadUserContacts()
      }
        setAdapter()    //list adapter
        setAdapterEvents()
        setContAdapter()
       // setHappeningAdapter()
        setAdapterOnMap()
        clickListeners()
        setUpTabs()

        Log.d("MapApi", "Token+ " + Prefs.with(mContext).getString(Constants.ACCESS_TOKEN, ""))

        binding.rvSpotDetail.visibility = View.GONE
        //searchCursor()
        listSpot()

        binding.etSearch.isFocusable = false
        binding.etSearch.hint = getString(R.string.label_search_hotspot)

        binding.rvSpotDetail.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        })

        binding.btnCreateEvent.setOnClickListener {
          //  startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://whrzat.com/even.html")))
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.whrzat.com/promoteyourevent.html")))
        }
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        setFontType()
    }

  private fun checkAndRequestPermissions(): Boolean {

    val accessFineLocation = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
    val accessCourseLocation = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
    // val accessCallPhone = ContextCompat.checkSelfPermission(this@HomeActivity,Manifest.permission.CALL_PHONE)
    val readContacts = ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_CONTACTS)
    val listPermissionsNeeded = java.util.ArrayList<String>()

    if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (accessCourseLocation != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
//        if(accessCallPhone != PackageManager.PERMISSION_GRANTED){
//            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE)
//        }
    if(readContacts != PackageManager.PERMISSION_GRANTED){
      listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS)
    }

    if (!listPermissionsNeeded.isEmpty()) {

      ActivityCompat.requestPermissions(requireActivity(), listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
      return false
    } else {
      return true
    }

  }


  private fun showDialog()  {

      val builder = android.app.AlertDialog.Builder(requireContext())
      val customView : View= getLayoutInflater().inflate(R.layout.custom_layout, null);
      builder.setView(customView);

      val textViewTitle = customView.findViewById<TextView>(R.id.heading)
      val textViewMessage = customView.findViewById<TextView>(R.id.description)
      textViewTitle.text = "We need permission to your contacts and location"
      textViewMessage.text = "This app relies on read access to your contacts as well as your location access. We require access to this permission to find your friends and suggest people in your hotspot that you may know. We will not store any contact info in our data base if they are not a part of our platform. For further info read our privacy policy.\n"

      val okButton = customView.findViewById<TextView>(R.id.okBT)

      builder.setView(customView)
      // Create the AlertDialog
      val alertDialog: android.app.AlertDialog = builder.create()
      // Set other dialog properties
      alertDialog.setCancelable(false)
      okButton.setOnClickListener {
        getPermissionToReadUserContacts()
        alertDialog.dismiss()
      }
      alertDialog.show()
    }


    private fun setFontType() {
        val face_bold = Typeface.createFromAsset(requireActivity().assets, "fonts/opensans_bold.ttf")
        val face_semi = Typeface.createFromAsset(requireActivity().assets, "fonts/opensans_semibold.ttf")
        binding.tvlogo.setTypeface(face_bold)
        binding.tvType.setTypeface(face_semi)
        binding.tvSearch.setTypeface(face_semi)
        binding.btnCreateEvent.setTypeface(face_bold)
        binding.tvNoSpot.setTypeface(face_semi)
    }

    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.homeView.setBackgroundColor(Color.parseColor("#000000"))
                binding.tabLayout.setBackgroundColor(Color.parseColor("#000000"))
                binding.tabLayout.setSelectedTabIndicatorColor(Color.WHITE)
                binding.tabLayout.setTabTextColors(Color.GRAY,Color.WHITE)
                binding.tvType.setTextColor(Color.WHITE)
                binding.tvSearch.setTextColor(Color.WHITE)
                binding.tvType.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))
                binding.tvSearch.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))
              /*  val success: Boolean = mMap.setMapStyle(MapStyleOptions(resources
                        .getString(R.string.style_json)))
                if (!success) {
                    Log.e("map stayle", "Style parsing failed.")
                }*/
            }
            Configuration.UI_MODE_NIGHT_NO -> {

                binding.homeView.setBackgroundColor(Color.WHITE)
                binding.tabLayout.setBackgroundColor(Color.WHITE)
                binding.tabLayout.setSelectedTabIndicatorColor(Color.BLACK)
                binding.tabLayout.setTabTextColors(Color.parseColor("#80000000"),Color.BLACK)
                binding.tvType.setTextColor(Color.parseColor("#000000"))
                binding.tvSearch.setTextColor(Color.parseColor("#000000"))

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }


    fun setUpTabs() {

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("  Trending  "))
        //binding.tabLayout.addTab(binding.tabLayout.newTab().setText("  Happening  "))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("  Friends  "))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("  Events  "))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                Log.e(TAG, "onTabSelected: Tab Change Position:: ${tab?.position}")
                when (tab?.position) {


                    0 -> {

                        binding.tvNoSpot.visibility = View.GONE
                        binding.rvHome.visibility = View.VISIBLE
                        binding.rvHomePromoted.visibility = View.GONE
                        binding.btnCreateEvent.visibility = View.GONE
                        binding.rvHappeningFeed.visibility=View.GONE
                        binding.rvFeedContact.visibility=View.GONE

                      ii++
                        Log.e(
                            TAG,
                            "onTabSelected: exploreAdapte Item Count ${exploreAdapter.itemCount}"
                        )

                        if (exploreAdapter.itemCount == 0) {
                            binding.tvNoSpot.text = "No hotspot found"
                            binding.tvNoSpot.visibility = View.VISIBLE
                        } else {
                            binding.tvNoSpot.visibility = View.GONE
                        }
                      if (Prefs.with(requireContext()).getInt(Constants.VISITED_HOME,0)==0){
                          if (ii != 0 && ContextCompat.checkSelfPermission(
                              requireContext(),
                              Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                          ) {
                            Log.d(TAG, "onTabSelected: // friends tab not granted")
                            explain("Alert",
                              "Please switch on location services in settings to get nearby hotspots.",
                              "Settings"
                            )
                            ii++
                          }
                        }
                      else{
                        if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                          ) != PackageManager.PERMISSION_GRANTED){
                        explain("Alert",
                          "Please switch on location services in settings to get nearby hotspots.",
                          "Settings"
                        )
                      }}
                        referesh()
                    }
                    3 -> {
                        binding.tvNoSpot.visibility = View.GONE
                        binding.rvHome.visibility = View.GONE
                        binding.rvHomePromoted.visibility = View.GONE
                        binding.btnCreateEvent.visibility = View.GONE
                        binding.rvHappeningFeed.visibility=View.VISIBLE
                        binding.rvFeedContact.visibility=View.GONE
                        if (Prefs.with(mContext).getString(Constants.RADIUS, "").isNotEmpty()) {

                            presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                                    Prefs.with(mContext).getString(Constants.RADIUS, "").toInt(),

                                    Prefs.with(mContext).getString(Constants.LAT, ""),
                                    Prefs.with(mContext).getString(Constants.LNG, ""))
                        } else {
                            presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                                    Constants.SPOT_RADIUS,

                                    Prefs.with(mContext).getString(Constants.LAT, ""),
                                    Prefs.with(mContext).getString(Constants.LNG, ""))
                        }

                       // binding.tvNoSpot.text = "No recent images from nearby"
                        if (feedHappeningAdapter.itemCount == 0) {
                            binding.tvNoSpot.visibility = View.GONE
                        } else {
                            binding.tvNoSpot.visibility = View.GONE
                        }
                        referesh()

                    }
                    1 -> {
                        binding.tvNoSpot.visibility = View.GONE
                        binding.rvHome.visibility = View.GONE
                        binding.rvHomePromoted.visibility = View.GONE
                        binding.btnCreateEvent.visibility = View.GONE
                        binding.rvHappeningFeed.visibility=View.GONE
                        binding.rvFeedContact.visibility=View.VISIBLE
                        if (Prefs.with(mContext).getBoolean(Constants.ISFEEDONBOOL, false)) {
                            binding.tvNoSpot?.visibility = View.GONE
                            binding.swipeToRefresh?.isEnabled = true
                            binding.swipeToRefresh?.isRefreshing = false
                            presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""),false)
                        } else {
                            binding.rvFeedContact?.visibility = View.GONE
                            binding.tvNoSpot?.visibility = View.VISIBLE
                            binding.tvNoSpot?.text = getString(R.string.label_settings_text)
                            binding.swipeToRefresh?.isEnabled = true
                            binding.swipeToRefresh?.isRefreshing = false
                        }
                        referesh()

                      // binding.tvNoSpot.text = "Your contacts haven't been out recently"

                      /*  if (feedContactAdapter.itemCount == 0) {
                            binding.tvNoSpot.visibility = View.GONE

                        } else {

                            binding.tvNoSpot.visibility = View.GONE
                        }*/


                      if (ContextCompat.checkSelfPermission(
                          requireContext(),
                          Manifest.permission.READ_CONTACTS
                        ) != PackageManager.PERMISSION_GRANTED  ) {
                        Log.d(TAG, "onTabSelected: // friends tab not granted")
                        explain("Sync Your Contacts",
                          resources.getString(R.string.contact_permission_description),
                          "Settings"
                        )
                      }
                    }

                    2 -> {
                        binding.tvNoSpot.visibility = View.GONE
                        binding.rvHome.visibility = View.GONE
                        binding.rvHomePromoted.visibility = View.VISIBLE
                        binding.btnCreateEvent.visibility = View.VISIBLE
                        binding.rvHappeningFeed.visibility=View.GONE
                        binding.rvFeedContact.visibility=View.GONE
                        presenter.performEventListApi(sendExploreData, false, spotRadius, skipEvent)

                        if (exploreEventAdapter.itemCount == 0) {
                            binding.tvNoSpot.text = "No event found"
                            binding.tvNoSpot.visibility = View.VISIBLE
                        } else {
                            binding.tvNoSpot.visibility = View.GONE
                        }
                        referesh()
                    }

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNotification(feedApi: FeedApi) {

//        Toast.makeText(context,"FeedApi",Toast.LENGTH_SHORT).show()
        EventBus.getDefault().removeStickyEvent<FeedApi>(FeedApi::class.java)
        if(binding.tabLayout.selectedTabPosition==1){
            if (Prefs.with(mContext).getBoolean(Constants.ISFEEDONBOOL, false)) {
                binding.rvFeedContact?.visibility = View.VISIBLE
                binding.tvNoSpot?.visibility = View.GONE
                binding.swipeToRefresh?.isEnabled = true
                if (feedApi.isContactSync && !isContactSynced) {
                    getPermissionToReadUserContacts()
                } else {
                    presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""), !isFeedApiHit)

                }
            }else {
                binding.tvNoSpot?.visibility = View.VISIBLE
                binding.tvNoSpot?.text = getString(R.string.label_settings_text)
                binding.rvFeedContact?.visibility = View.GONE
                binding.swipeToRefresh?.isEnabled = false

            }
        }
    }

    private fun getPermissionToReadUserContacts() {

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            //  GeneralMethods.showToast(mContext, R.string.label_fetching_contacts)
             //  progressDialog.show()
            FetchContacts(mContext,mContext.contentResolver, this).execute()
            isPermissionGranted = true

        } else {
           showDialog()
            /* ActivityCompat.requestPermissions(activity,
                        String[]{Manifest.permission.READ_CONTACTS},
                        READ_CONTACTS_PERMISSIONS_REQUEST)*/
        }
    }

  private fun explain(title : String, msg: String, positiveText : String) {
    var positiveT = "Yes"
    if (positiveText!=null){
      positiveT = positiveText
    }
    else{
      positiveT ="Settings"
    }
//    val dialog = AlertDialog.Builder(requireContext())
//    dialog.setMessage(msg)
//      dialog.setTitle(title)
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
    val builder = android.app.AlertDialog.Builder(requireContext())
    val customView : View= getLayoutInflater().inflate(R.layout.custom_layout2, null);
    builder.setView(customView);

    val textViewTitle = customView.findViewById<TextView>(R.id.heading2)
    val textViewMessage = customView.findViewById<TextView>(R.id.description2)
    textViewTitle.setText(title)
    textViewMessage.setText(msg)
    val okButton = customView.findViewById<TextView>(R.id.noBT)
    val noButton = customView.findViewById<TextView>(R.id.okBT)
    okButton.setText(positiveT)
    noButton.setText("Cancel")

    builder.setView(customView)
    // Create the AlertDialog
    val alertDialog: android.app.AlertDialog = builder.create()
    // Set other dialog properties
    alertDialog.setCancelable(false)
    okButton.setOnClickListener {
      alertDialog.dismiss()
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


  private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
    android.app.AlertDialog.Builder(requireContext())
      .setMessage(message)
      .setPositiveButton("OK", okListener)
      .setNegativeButton("Cancel", okListener)
      .create()
      .show()
  }

  private fun setAdapterEvents() {
        exploreEventAdapter = ExploreEventAdapter(mContext, eventList)
        mLinearLayoutManagerEvent = LinearLayoutManager(mContext)
       binding.rvHomePromoted.layoutManager = mLinearLayoutManagerEvent
        binding.rvHomePromoted?.adapter = exploreEventAdapter
      //  binding.binding.rvHomePromoted?.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))

    }

    private fun setAdapter() {
        exploreAdapter = ExploreAdapter(mContext)
        mLinearLayoutManager = GridLayoutManager(mContext,2)
        binding.rvHome.layoutManager = mLinearLayoutManager
        binding.rvHome.adapter = exploreAdapter
        exploreAdapter.setonSpotClickListener(this)
       // binding.rvHome.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
    }

    private fun setHappeningAdapter() {
        feedHappeningAdapter = FeedHappeningAdapter(mContext)
        binding.rvHappeningFeed?.layoutManager = LinearLayoutManager(mContext)
        binding.rvHappeningFeed?.adapter = feedHappeningAdapter
      //  binding.rvHappeningFeed?.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
        feedHappeningAdapter.setListener(this)
    }
    private fun setContAdapter() {
        feedContactAdapter = FeedContactAdapter(mContext)
        binding.rvFeedContact?.layoutManager = LinearLayoutManager(mContext)
        binding.rvFeedContact?.adapter = feedContactAdapter
       // binding.rvFeedContact?.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
        feedContactAdapter.setListener(this)
    }


    private fun searchCursor() {
        binding.etSearch.setOnTouchListener { v, event ->
            binding.etSearch.isFocusableInTouchMode = true
            if (isMapLoaded) {
                binding.swipeToRefresh.isEnabled = true
                binding.swipeToRefresh.isRefreshing = true
                isClear = true
                isClearEvent = true
                if (GeneralMethods.isNetworkActive(mContext)) {
                    listSpot()
                } else {
                    GeneralMethods.showToast(mContext, R.string.error_no_connection)
                }
                setViewType(getString(R.string.label_map), R.drawable.ic_map, View.VISIBLE, View.GONE, false)
            }
            false
        }
    }


    override fun onRefresh() {
         referesh()
    }

    private fun referesh() {
        skip = 0
        skipEvent = 0
        mapCount = 0
        exploreList.clear()
        spotList.clear()
        exploreListItems.clear()
        isClear = true
        isClearEvent = true
        isSearch = false
        isSpotReceive = false
        isSpotReceiveEvent = false

      /*  if (binding.tabLayout.selectedTabPosition == 1) {
            binding.swipeToRefresh.isRefreshing = true
            presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                    Constants.SPOT_RADIUS,
                    Prefs.with(mContext).getString(Constants.LAT, ""),
                    Prefs.with(mContext).getString(Constants.LNG, ""))
        }else*/
        if(binding.tabLayout.selectedTabPosition==1){
            binding.swipeToRefresh.isRefreshing = true
            presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""),false)
        }else if(binding.tabLayout.selectedTabPosition==2){
            skipEvent = 0
            presenter.performEventListApi(sendExploreData, false, spotRadius, skipEvent)
        } else{
            binding.swipeToRefresh.isRefreshing = true
            listSpot()

        }
    }

    private fun listSpot() {
//        val sendSearchData = sendExploreData(binding.etSearch.text.toString().trim())
        skip = 0
        skipEvent = 0
        if (GeneralMethods.isNetworkActive(mContext)) {
           // presenter.performEventListApi(sendExploreData, false, spotRadius, skipEvent)
            if (Prefs.with(mContext).getBoolean(Constants.IS_INFINITY, false)) {
                spotRadius = -1
                presenter.performExploreApi(sendExploreData, false, spotRadius, skip)
            } else {
                if (Prefs.with(mContext).getString(Constants.RADIUS, "").isNotEmpty()) {
                    spotRadius = Prefs.with(mContext).getString(Constants.RADIUS, "").toInt()
                    presenter.performExploreApi(sendExploreData, false, spotRadius, skip)
                } else {
                    presenter.performExploreApi(sendExploreData, false, spotRadius, skip)
                }
            }
        } else {
            GeneralMethods.showToast(mContext, R.string.error_no_connection)
        }

        progressDialog.dismiss()

    }

    override fun onMapAnnotationClick(mapAnnotationObject: MapAnnotation): Boolean {
        binding.etSearch.isFocusable = false
        GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.tvNoSpot)
        binding.rvSpotDetail.visibility = View.VISIBLE
        if (mapAnnotationObject.position < oldMapList.size) {
            binding.rvSpotDetail.smoothScrollToPosition(mapAnnotationObject.position)
        }
        return true
    }

    private fun sendExploreData(search: String): SendExploreData {
        return SendExploreData(Prefs.with(mContext).getString(Constants.LAT, ""), Prefs.with(mContext).getString(Constants.LNG, ""),
                "",
                Prefs.with(mContext).getString(Constants.USER_ID, "") as String,
                search)
        /*return SendExploreData("0.0","0.0",
                "",
                Prefs.with(mContext).getString(Constants.USER_ID, "") as String,
                search)*/
    }


    override fun onHotSpotClick(id: String) {
        isSearch = false
        binding.etSearch.text.clear()
        val intent = Intent(mContext, DetailActivity::class.java)
        intent.putExtra(Constants.HOTSPOT_ID, id)
        startActivity(intent)
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }

    private fun setAdapterOnMap() {
        spotDetailAdapter = SpotAdapter(mContext)
        mSpotLayoutManagerOnMap = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        spotDetailAdapter.setonSpotClickListener(this)
        binding.rvSpotDetail.layoutManager = mSpotLayoutManagerOnMap
        binding.rvSpotDetail.adapter = spotDetailAdapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvSpotDetail)
    }

    override fun loading() {
        progressDialog.dismiss()
    }

    private fun setupMap(savedInstanceState: Bundle?) {
        binding.mapExplore.onCreate(savedInstanceState)
        try {
            MapsInitializer.initialize(mContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
        if (p1 == EditorInfo.IME_ACTION_SEARCH) {
            isSearch = true
            isMapSearchActive = false
            if (binding.tvType.text == getString(R.string.label_map)) {
                isClear = true
                isClearEvent = true
                isSpotReceive = true
                mapCount = 0
                isMapSearchActive = false
                val sendSearchData = sendExploreData(binding.etSearch.text.toString().trim())
                if (binding.tabLayout.selectedTabPosition == 0) {
                    presenter.performEventListApi(sendSearchData, true, spotRadius, skipEvent)
                } else {
                    if (Prefs.with(mContext).getString(Constants.RADIUS, "").isNotEmpty()) {
                        val radius = Prefs.with(mContext).getString(Constants.RADIUS, "").toInt()
                        if (binding.etSearch.text.toString().trim().isNotEmpty())
                            presenter.performExploreApi(sendSearchData, true, Constants.SPOT_RADIUS, 0)
                    } else {
                        if (binding.etSearch.text.toString().trim().isNotEmpty())
                            presenter.performExploreApi(sendSearchData, true, Constants.SPOT_RADIUS, 0)

                    }
                }
            } else {
                isMapSearchActive = true
                val sendSearchData = sendExploreData(binding.etSearch.text.toString().trim())
                if (Prefs.with(mContext).getString(Constants.RADIUS, "").isNotEmpty()) {
                    val radius = Prefs.with(mContext).getString(Constants.RADIUS, "").toInt()
                    if (binding.etSearch.text.toString().trim().isNotEmpty()) {
                        presenter.mapViewApi(sendSearchData, true, radius)
                    }
                } else {
                    if (binding.etSearch.text.toString().trim().isNotEmpty()) {
                        presenter.mapViewApi(sendSearchData, true, spotRadius)
                    }
                }
            }
            return true
        }
        return false
    }


    override fun onMapReady(googleMap: GoogleMap) {
      //  adapter.annotations.clear()
      /*  binding.mapExplore.getMapAsync { map ->
            //Attach the adapter to the map view once it's initialized
            adapter.attach(binding.mapExplore, map)
            adapter.setListener(this)

        }*/

      /*  isMapLoaded = true
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL*/
         /* val success: Boolean = googleMap.setMapStyle(MapStyleOptions(resources
                       .getString(R.string.style_json)))
               if (!success) {
                   Log.e("map stayle", "Style parsing failed.")
               }*/

       /* val currentLocation = LatLng(Prefs.with(mContext).getString(Constants.LAT, "").toDouble(),
                Prefs.with(mContext).getString(Constants.LNG, "").toDouble())*/

      /*  var currentLocation=LatLng(HomeActivity.latitude!!, HomeActivity.longitude!!)
        val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(14f).build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        mMap.uiSettings.isMapToolbarEnabled = false

        exploreApi()
        ivCurrent.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        mMap.setOnMapClickListener {
            binding.etSearch.isFocusable = false
            binding.rvSpotDetail.visibility = View.GONE
            GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.etSearch)
            adapter.setOnAnnotationClickListener(this)
        }*/

    }

    private fun exploreApi() {
        if (GeneralMethods.isNetworkActive(mContext)) {
            binding.tvNoSpot.visibility = View.GONE
            radiusCalculate()
            // presenter?.mapViewApi(sendExploreData, true, spotRadius)
        } else {
            GeneralMethods.showToast(mContext, R.string.error_no_connection)
            if (spotList.isEmpty()) {
                binding.tvNoSpot.visibility = View.VISIBLE
                binding.tvNoSpot.text = getString(R.string.error_no_connection)
            }
        }
    }

    override fun afterTextChanged(p0: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(query: CharSequence, p1: Int, p2: Int, p3: Int) {
        if (query.isEmpty()) {
            binding.swipeToRefresh.isEnabled = true

            GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.tvNoSpot)
            if (isSearch) {
                //isSearch = false
                onRefresh()
                isMapSearchActive = false
            } else {
                if (binding.tvType.text == getString(R.string.label_list)) {
                  //  adapter.setOnAnnotationClickListener(this)
                    spotDetailAdapter.addListWithClear(oldMapList)
                   // adapter.addList(oldMapList)
                }

            }

        } else {
            if (query.length > 3) {
                binding.swipeToRefresh.isEnabled = false

                isSearch = true
                isMapSearchActive = false
                if (binding.tvType.text == getString(R.string.label_map)) {
                    isClear = true
                    isClearEvent = true
                    isSpotReceive = true
                    mapCount = 0
                    isMapSearchActive = false
                    val sendSearchData = sendExploreData(binding.etSearch.text.toString().trim())
                    if (binding.tabLayout.selectedTabPosition == 0) {
                        presenter.performEventListApi(sendSearchData, false, spotRadius, skipEvent)
                    } else {
                        if (Prefs.with(mContext).getString(Constants.RADIUS, "").isNotEmpty()) {
                            val radius = Prefs.with(mContext).getString(Constants.RADIUS, "").toInt()
                            if (binding.etSearch.text.toString().trim().isNotEmpty())
                                presenter.performExploreApi(sendSearchData, false, Constants.SPOT_RADIUS, 0)
                        } else {
                            if (binding.etSearch.text.toString().trim().isNotEmpty())
                                presenter.performExploreApi(sendSearchData, false, Constants.SPOT_RADIUS, 0)

                        }
                    }
                }
            }
        }
    }


    private fun clickListeners() {
        binding.tvType.setOnClickListener(this)
        binding.etSearch.setOnEditorActionListener(this)
        binding.swipeToRefresh.setOnRefreshListener(this)
        binding.etSearch.addTextChangedListener(this)
        binding.tvSearch.setOnClickListener(this)

        binding.rvSpotDetail.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView!!, dx, dy)
                val currentPosition = mSpotLayoutManagerOnMap.findFirstVisibleItemPosition()

                // val latLng = LatLngBounds.builder()
                //for(list in oldMapList){
                // latLng.include(LatLng(list.location[1], list.location[0]))
                val currentLocation = LatLng(oldMapList[currentPosition].location[1],
                        oldMapList[currentPosition].location[0])
                //}


                val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(14f).build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        })
        binding.rvHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            mapCount = 1
                            binding.swipeToRefresh.isRefreshing = false
                            //  val loading=loading()

                            if (GeneralMethods.isNetworkActive(mContext)) {
                                if (Prefs.with(mContext).getBoolean(Constants.IS_INFINITY, false)) {
                                    presenter.performExploreApi(sendExploreData, false, -1, skip)

                                } else {
                                    if (Prefs.with(mContext).getString(Constants.RADIUS, "").isEmpty()) {
                                        presenter.performExploreApi(sendExploreData, false, -1, skip)
                                        // EventBus.getDefault().postSticky(RefreshProfileApi(true))
                                    } else {
                                        val radius = Prefs.with(mContext).getString(Constants.RADIUS, "").toInt()
                                        spotRadius = radius
                                        presenter.performExploreApi(sendExploreData, false, radius, skip)
                                    }
                                }
                            } else {
                                GeneralMethods.showToast(mContext, R.string.error_no_connection)
                            }
                        }else{
                            /*val radius = Prefs.with(mContext).getString(Constants.RADIUS, "").toInt()
                            spotRadius = radius*/
                            presenter.performExploreApi(sendExploreData, false, spotRadius, skip)
                        }
                    }
                }
            }
        })
        binding.rvHomePromoted.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                        mapCount = 1
                        binding.swipeToRefresh.isRefreshing = false
                        //  val loading=loading()

                        if (GeneralMethods.isNetworkActive(mContext)) {
//                             presenter.performEventListApi(skipEvent)
                        } else {
                            GeneralMethods.showToast(mContext, R.string.error_no_connection)
                        }
//                        }
                    }
                }
            }
        })


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvType -> {
              /*  isSearch = false
                binding.etSearch.text.clear()
                GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.etSearch)
                if (isMapLoaded) {
                    binding.swipeToRefresh.isEnabled = true
                    binding.swipeToRefresh.isRefreshing = true
                    // progressDialog.show()
                    isClear = true
                    isClearEvent = true
                    //referesh()
                    skip = 0
                    exploreList.clear()
                    exploreListItems.clear()
                    spotList.clear()
                    isSearch = false
                    exploreAdapter.clearList()
                    listSpot()

                    setViewType(getString(R.string.label_map), R.drawable.ic_map, View.VISIBLE, View.GONE, false)
                } else {
//                     loadMap()

                    setViewType(getString(R.string.label_list), R.drawable.list_icon, View.GONE, View.VISIBLE, true)
                    binding.etSearch.text.clear()
                    binding.swipeToRefresh.isEnabled = false
                    radiusCalculate()
                }*/
                var intent=Intent(activity,MapActivity::class.java)
                startActivity(intent)

//                EventBus.getDefault().postSticky(MapReferesh(true))
            }
            R.id.tvSearch->{
              var intent = Intent(activity, ExploreActivity::class.java)
              startActivity(intent)

            }

        }
    }

    private fun loadMap() {
        setViewType(getString(R.string.label_list), R.drawable.list_icon, View.GONE, View.VISIBLE, true)
        binding.etSearch.text.clear()
        binding.swipeToRefresh.isEnabled = false
        binding.swipeToRefresh.isEnabled = false
        /*val currentLocation = LatLng(Prefs.with(mContext).getString(Constants.LAT, "").toDouble(),
                            Prefs.with(mContext).getString(Constants.LNG, "").toDouble())
                    val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(18f).build()
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))*/

        /*  if (isMapSearchActive) {
              if (oldMapList.isEmpty()) {
                  if (GeneralMethods.isNetworkActive(mContext)) {
                      presenter.mapViewApi(sendExploreData, true, spotRadius)
                  } else {
                      GeneralMethods.showToast(mContext, R.string.error_no_connection)
                  }
              } else {
                  adapter.addList(oldMapList)
                  spotDetailAdapter.addListWithClear(oldMapList)
              }
          } else {
              adapter.addList(oldMapList)
              spotDetailAdapter.addListWithClear(oldMapList)
          }*/
        radiusCalculate()

    }

    private fun setViewType(type: String, iconType: Int, rVisibility: Int, mapVisibility: Int, isMap: Boolean) {
        binding.etSearch.isFocusable = false
        binding.tvType.text = type
        binding.tvType.setCompoundDrawablesWithIntrinsicBounds(0, iconType, 0, 0)
//        binding.rvHome.visibility = rVisibility
        binding.layoutListItem.visibility = rVisibility
        // binding.rvSpotDetail.visibility = mapVisibility
        binding.mapExplore.visibility = mapVisibility
        binding.ivCurrent.visibility = mapVisibility
        isMapLoaded = isMap
        binding.rvSpotDetail.visibility = View.GONE

        if (type == "Map") {
            if (spotList.isEmpty()) {
                //binding.tvNoSpot.visibility = View.VISIBLE
            } else {
                binding.tvNoSpot.visibility = View.GONE

            }
        } else {
            binding.tvNoSpot.visibility = View.GONE
        }

    }


    override fun sessionExpired() {
        GeneralMethods.tokenExpired(mContext)
    }

    override fun failureExploreApi() {
        // GeneralMethods.showToast(mContext, R.string.error_server_busy)

        if (binding.tabLayout.selectedTabPosition == 0) {

            if (exploreAdapter.itemCount == 0) {
               // binding.tvNoSpot.text = "No hotspot found"
                binding.tvNoSpot.visibility = View.GONE
            } else {
                binding.tvNoSpot.visibility = View.GONE
            }

        } else {
         /*   binding.tvNoSpot.text = "No event found"
            if (exploreEventAdapter.itemCount == 0) {
                binding.tvNoSpot.visibility = View.VISIBLE
            } else {
                binding.tvNoSpot.visibility = View.GONE
            }*/

        }
    }

    override fun errorExploreApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun noSpotAtMapFound() {
        isSearch = false
      //  adapter.clearList()
        spotDetailAdapter.clear()
        exploreAdapter.clearList()

        binding.tvNoSpot.visibility = View.GONE
    }

    override fun errorApi(errorBody: ResponseBody) {
        binding.swipeToRefresh.isRefreshing = false
       /* if(binding.tabLayout.selectedTabPosition==2){
            feedHappeningAdapter.clearList()
            binding.tvNoSpot.text = "No recent images from nearby"
            binding.tvNoSpot.visibility = View.VISIBLE
        }else*/ if(binding.tabLayout.selectedTabPosition==1){
            feedContactAdapter.clearList()
            binding.tvNoSpot.visibility = View.VISIBLE
            Log.e(TAG, "errorApi: Your contacts haven't been out recently", )
            binding.tvNoSpot.text = "Your contacts haven't been out recently"

        }

    }

    override fun successLoveApi() {

    }

    override fun successReport() {
        Toasty.success(mContext, getString(R.string.success_reported), Toast.LENGTH_SHORT, true).show()
    }

    override fun errorReportApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun failureReportApi() {
       // GeneralMethods.showToast(mContext, R.string.error_server_busy)
    }

    override fun failureApi(message: String?) {
        binding.swipeToRefresh.isRefreshing = false
       // feedHappeningAdapter.clearList()
        //binding.tvNoSpot.text = "No recent images from nearby"
        binding.tvNoSpot.text = message
        binding.tvNoSpot.visibility = View.VISIBLE
    }
    private var isFeedApiHit = false
    override fun successFeedApi(data: List<FeedData>) {
        binding.swipeToRefresh.isRefreshing = false
        if (data.isEmpty()) {
            feedContactAdapter.clearList()
            binding.tvNoSpot?.visibility = View.VISIBLE
//            tvFeed?.text = getString(R.string.label_no_feed)
            Log.e(TAG, "successFeedApi: successFeedApi ::: Your contacts haven't been out recently", )
            binding.tvNoSpot?.text = "Your contacts haven't been out recently"
        } else {
            binding.tvNoSpot?.visibility = View.GONE
            feedContactAdapter.addList(data)
        }
        //isFeedApiHit = true
        //binding.swipeToRefresh?.isRefreshing = false
    }

    override fun apiSuccessContacts() {
        isContactSynced = true
        Log.e("contact Synced",""+isContactSynced)
      //  GeneralMethods.showToast(mContext, "Contacts Synced Successfully")
       // presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""), true)
    }

    override fun showLoading() {
        progressDialog?.show()
    }


    override fun mapData(data: HotspotData) {
        spotList.clear()
        spotList.addAll(data.mapData)
        mapList.clear()
        spotDetailAdapter.addListWithClear(data.mapData)
        setHandler()
    }

    override fun noEventFound() {
        eventList.clear()
        exploreEventAdapter.addListWithClear(eventList)
        exploreEventAdapter.notifyDataSetChanged()
        if (exploreEventAdapter.itemCount == 0) {
            binding.tvNoSpot.text = "No event found"
            binding.tvNoSpot.visibility = View.VISIBLE
        } else {

            binding.tvNoSpot.visibility = View.GONE
        }

//        isSpotReceiveEvent=false
//        progressDialog.dismiss()
//        binding.swipeToRefresh.isRefreshing = false
//        eventList.clear()
//        exploreEventAdapter.addList(eventList)
//        exploreEventAdapter.addListWithClear(eventList)
//        if (totalspotEvent > 9) {
//            val count = exploreEventAdapter.itemCount - 1
//            exploreEventAdapter.remove(count)
//            exploreEventAdapter.notifyItemRemoved(count)
//        }

//        GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.tvNoSpot)
    }

    override fun noSpotFound() {
        isSpotReceive = false
        progressDialog.dismiss()
        binding.swipeToRefresh.isRefreshing = false

        if (isSearch) {
            isSearch = false
            exploreList.clear()
            spotList.clear()
         //   adapter.addList(exploreList)
            exploreAdapter.addListWithClear(exploreList)
            if (binding.tvType.text.toString() == getString(R.string.label_list)) {
                binding.tvNoSpot.visibility = View.GONE
            } else {
                if (binding.tabLayout.selectedTabPosition == 0) {
                    binding.tvNoSpot.text = "No hotspots found"
                    binding.tvNoSpot.visibility = View.VISIBLE
                }
            }
        }
        if (exploreAdapter.itemCount == 0) {
            exploreList.clear()
            spotList.clear()
          //  adapter.notifyDataSetChanged()
            exploreAdapter.addList(exploreList)
            if (binding.tvType.text.toString() == getString(R.string.label_list)) {
                binding.tvNoSpot.visibility = View.GONE
            } else {
                if (binding.tabLayout.selectedTabPosition == 0) {
                    binding.tvNoSpot.text = "No hotspots found"
                    binding.tvNoSpot.visibility = View.VISIBLE
                }
//                binding.tvNoSpot.visibility = View.VISIBLE
            }
        }
        if (totalspot > 9) {
            val count = exploreAdapter.itemCount - 1
            exploreAdapter.remove(count)
            exploreAdapter.notifyItemRemoved(count)
        }
        isSearch = true
    }


    override fun exploreList(exploreList: HotspotData) {
        binding.swipeToRefresh.isRefreshing = false
        totalspot = exploreList.count
           skip += 10
        if (isSpotReceive) {
            val count = exploreAdapter.itemCount - 1
            exploreAdapter.remove(count)
            exploreAdapter.notifyItemRemoved(count)
        }

        if (isClear) {
            this.exploreList.clear()

            exploreAdapter.addListWithClear(exploreList.mapData)
            spotDetailAdapter.addListWithClear(exploreList.mapData)
            if (!isSearch) {
                if (totalspot > 9) {
                    exploreAdapter.addLoading()
                }
            }
            isClear = false
        } else {
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

                binding.tvNoSpot.text = "No hotspots found"
                binding.tvNoSpot.visibility = View.VISIBLE
            } else {
                binding.tvNoSpot.visibility = View.GONE
            }
        } else {
         /*   binding.tvNoSpot.text = "No event found"
            if (exploreEventAdapter.itemCount == 0) {
                binding.tvNoSpot.visibility = View.VISIBLE
            } else {
                binding.tvNoSpot.visibility = View.GONE
            }*/

        }
        isSpotReceive = true
    }

    override fun eventListSuccess(data: EventListData.Data) {
//        binding.swipeToRefresh.isRefreshing = false
//        totalspotEvent = data.count!!
//        skipEvent += 10
//        if (isSpotReceiveEvent){
//            val count = exploreEventAdapter.itemCount - 1
//            exploreEventAdapter.remove(count)
//            exploreEventAdapter.notifyItemRemoved(count)
//        }
//        if (isClearEvent) {
//            this.eventList.clear()
//            var list=ArrayList<EventListData.ImageDatum>()
//            list.addAll(data.imageData!!)
//            eventList=list
//            exploreEventAdapter.addListWithClear(list)
//            exploreEventAdapter= FeedPermotedAdapter(mContext,list)
//            isClearEvent = false
//        } else {
//            var list=ArrayList<EventListData.ImageDatum>()
//            list.addAll(data.imageData!!)
//            exploreEventAdapter.addList(list)
//            exploreEventAdapter=FeedPermotedAdapter(mContext,list)
//            //exploreListItems = exploreList.mapData as ArrayList<Hotspot>
////            if (!isSearch) {
////                if (totalspot > 9) {
////                    exploreAdapter.addLoading()
////                }
////            }
//        }

        if (binding.tabLayout.selectedTabPosition == 2) {
            binding.tvNoSpot.visibility = View.GONE
            binding.tvNoSpot.setText("No Event Found")
        }

        eventList.clear()
        eventList.addAll(data.events!!)
        exploreEventAdapter.notifyDataSetChanged()
//        GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.tvNoSpot)

//        isSpotReceiveEvent = true
    }

    override fun successFeedHappeningApi(data: HappeningListData.Data) {
        binding.tvNoSpot.visibility = View.GONE
        binding.swipeToRefresh.isRefreshing = false
        if (data.imageData?.size==0){
            binding.tvNoSpot.text = "No recent images from nearby"
        }else{
            feedHappeningAdapter.addList(data)
        }

    }

    private fun setHandler() {
        mapList.addAll(spotList)
        if (isSearch) {
            isMapSearchActive = true
            isSearch = false
          //  adapter.addList(mapList)
        } else {

            //  setupMap(savedInstanceState)
            oldMapList.clear()
            oldMapList.addAll(mapList)
            isMapSearchActive = false
          //  adapter.addList(oldMapList)
        }

    }


    override fun onPause() {
        super.onPause()
       /* if (binding.mapExplore != null)
            binding.mapExplore.onPause()*/
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: refresh ")

        /*if (binding.mapExplore != null)
            binding.mapExplore.onResume()*/

        binding.etSearch.isFocusable = false

        isSearch = false
        binding.etSearch.text.clear()
        GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.etSearch)
        binding.swipeToRefresh.isEnabled = true
        Log.e("OnResume","OnResume")
       /* ---- ALready commented ---- if (binding.tabLayout.selectedTabPosition == 1) {
            binding.swipeToRefresh.isRefreshing = true
            presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                    Constants.SPOT_RADIUS,
                    Prefs.with(mContext).getString(Constants.LAT, ""),
                    Prefs.with(mContext).getString(Constants.LNG, ""))
        }else   ------Already commented -------- */
        if(binding.tabLayout.selectedTabPosition==1){

            if (Prefs.with(mContext).getBoolean(Constants.ISFEEDONBOOL, false)) {
                binding.tvNoSpot?.visibility = View.GONE
                binding.swipeToRefresh?.isEnabled = true
                binding.swipeToRefresh.isRefreshing = true
                presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""),false)
            } else {
                binding.rvFeedContact?.visibility = View.GONE
                binding.tvNoSpot?.visibility = View.VISIBLE
                binding.tvNoSpot?.text = getString(R.string.label_settings_text)
                binding.swipeToRefresh?.isEnabled = true
                binding.swipeToRefresh?.isRefreshing = false
            }
        }else if(binding.tabLayout.selectedTabPosition==2){
            presenter.performEventListApi(sendExploreData, false, spotRadius, skipEvent)
        }else{
         //  EventBus.getDefault().postSticky(MapReferesh(true))
        }


        //listSpot()
//        exploreAdapter.clearList()
//        onRefresh()

//        if (binding.tvType.text == getString(R.string.label_map)) {
//
//            binding.swipeToRefresh.isEnabled = true
//            binding.swipeToRefresh.isRefreshing = true
//            // progressDialog.show()
//            isClear = true
//            isClearEvent= true
//            //referesh()
//            skip = 0
//            skipEvent = 0
//            exploreList.clear()
//            exploreListItems.clear()
//            eventList.clear()
//            spotList.clear()
//            isSearch = false
//            exploreAdapter.clearList()
//            exploreEventAdapter.clearList()
//            listSpot()
//            setViewType(getString(R.string.label_map), R.drawable.map_icon, View.VISIBLE, View.GONE, false)
//        } else {
//            loadMap()
//        }

    }

    override fun onDestroy() {
        super.onDestroy()
       /* if (binding.mapExplore != null)
            binding.mapExplore.onDestroy()*/

        presenter.detachView()

    }

    override fun onLowMemory() {
        super.onLowMemory()
      /*  if (binding.mapExplore != null)
            binding.mapExplore.onLowMemory()*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNotification(refreshExploreApi: RefreshExploreApi) {

        EventBus.getDefault().postSticky(RefreshProfileApi(true))
        EventBus.getDefault().removeStickyEvent<RefreshExploreApi>(RefreshExploreApi::class.java)
        if (refreshExploreApi.isExploreApi) {
            binding.etSearch.isFocusable = false
          //  progressDialog.show()
            /* oldMapList.clear()
             mapList.clear()*/
            exploreList.clear()
            exploreListItems.clear()
            spotList.clear()
            isSearch = false
            /*adapter.clearList()
            adapter.notifyDataSetChanged()
            adapter.setOnAnnotationClickListener(this)*/


            // setViewType(getString(R.string.label_list), R.drawable.list_icon, View.GONE, View.VISIBLE, true)
            if (isMapSearchActive) {
                binding.etSearch.text.clear()
            }

            if (GeneralMethods.isNetworkActive(mContext)) {
                if (binding.tvType.text == getString(R.string.label_list)) {
                    oldMapList.clear()
                    mapList.clear()
                    adapter.clearList()
                    adapter.notifyDataSetChanged()
                    adapter.setOnAnnotationClickListener(this)
                    radiusCalculate()
                    binding.swipeToRefresh.isEnabled = false
                } else {
                    exploreAdapter.clearList()
                    binding.swipeToRefresh.isEnabled = false
                 //   referesh()
                }
            } else {
                GeneralMethods.showToast(mContext, R.string.error_no_connection)
            }

        }
    }


    private fun radiusCalculate() {
      /*  if (mMap != null) {
            adapter.annotations.removeAll(adapter.annotations)
        }*/
        binding.swipeToRefresh.isEnabled = false
        if (Prefs.with(mContext).getBoolean(Constants.IS_INFINITY, false)) {
            spotRadius = -1
            presenter.mapViewApi(sendExploreData, true, -1)

        } else {
            if (Prefs.with(mContext).getString(Constants.RADIUS, "").isEmpty()) {
                presenter.mapViewApi(sendExploreData, true, spotRadius)
                // EventBus.getDefault().postSticky(RefreshProfileApi(true))
            } else {
                spotRadius = Prefs.with(mContext).getString(Constants.RADIUS, "").toInt()
                presenter.mapViewApi(sendExploreData, true, spotRadius)
            }
        }


    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefershMap(refreshExploreApi: MapReferesh) {
        Log.e("CallMap", "Call")
        binding.etSearch.isFocusable = false
        EventBus.getDefault().removeStickyEvent<MapReferesh>(MapReferesh::class.java)
//        if (refreshExploreApi.isMapRefersh) {
//            setViewType(getString(R.string.label_list), R.drawable.list_icon, View.GONE, View.VISIBLE, true)
//            radiusCalculate()
//        }
        isSearch = false
        binding.etSearch.text.clear()
        GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.etSearch)

        if (binding.tvType.text == getString(R.string.label_map)) {

           // binding.swipeToRefresh.isEnabled = true
          //  binding.swipeToRefresh.isRefreshing = true
            // progressDialog.show()
            isClear = true
            isClearEvent = true
            //referesh()
            skip = 0
            exploreList.clear()
            exploreListItems.clear()
            spotList.clear()
            isSearch = false
            exploreAdapter.clearList()
            listSpot()
            setViewType(getString(R.string.label_map), R.drawable.ic_map, View.VISIBLE, View.GONE, false)
        } else {
           // loadMap()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

    }

    override fun onReportClick(id: String) {
        presenter.apiReport(id, Prefs.with(mContext).getString(Constants.USER_ID, ""))
    }

    override fun onLocationClick(data: HappeningListData.ImageDatum) {


        //locationFrament.show(activity?.supportFragmentManager, locationFrament.tag)
    }

    override fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int) {
        EventBus.getDefault().postSticky(LoveApi(true))
        presenter.love(Prefs.with(mContext).getString(Constants.USER_ID, ""), imageId, isLike)
    }

    override fun report(id: String) {
        presenter.apiReport(id, Prefs.with(mContext).getString(Constants.USER_ID, ""))
    }

    override fun spotDetail(id: String) {
        val intent1=Intent(mContext, DetailActivity::class.java)
        intent1.putExtra(Constants.HOTSPOT_ID, id)
        startActivity(intent1)
    }

    override fun viewProfile(otherUserid: String, ivProfilePic: ImageView, tvUserName: TextView, original: String, name: String) {
        val intent = Intent(mContext, ProfileOtherActivity::class.java)
        intent.putExtra(Constants.OTHER_USER_ID, otherUserid)
        intent.putExtra(ProfileOtherActivity.PIC_URL, original)
        intent.putExtra(ProfileOtherActivity.USERNAME, name)
        val p1 = Pair.create(ivProfilePic as View, "pic")
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as FragmentActivity, p1)
        startActivity(intent, options.toBundle())
    }

    override fun onChatClick(id: String, ivProfilePic: ImageView, original: String) {
        val intent = Intent(mContext, ChatActivity::class.java)
        intent.putExtra(Constants.OTHER_USER_ID, id)
        intent.putExtra(Constants.PIC, original)
        val p1 = Pair.create(ivProfilePic as View, "pic")
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as FragmentActivity, p1)
        startActivity(intent, options.toBundle())
    }

    override fun onContactsReceived(contacts: List<String>) {
        isContactSynced = true
       // progressDialog.dismiss()
        val contactsObj = ApiContacts(Prefs.with(mContext).getString(Constants.USER_ID, ""), contacts)

        presenter.apiSyncContacts(contactsObj)
    }

    override fun onErrorFetchingContacts() {
        //progressDialog.dismiss()
        GeneralMethods.showToast(mContext, "error")
    }





}
