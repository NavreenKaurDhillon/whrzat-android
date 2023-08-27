package com.codebrew.whrzat.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.HomeActivity
import com.codebrew.whrzat.databinding.ActivityMapBinding
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.ui.Home.MapsAdapter
import com.codebrew.whrzat.ui.Home.SpotAdapter
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs

import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.explore.Hotspot
import com.codebrew.whrzat.webservice.pojo.explore.HotspotData
import com.codebrew.whrzat.webservice.pojo.explore.SendExploreData
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

import nz.co.trademe.mapme.annotations.MapAnnotation
import nz.co.trademe.mapme.annotations.OnInfoWindowClickListener
import nz.co.trademe.mapme.annotations.OnMapAnnotationClickListener
import okhttp3.ResponseBody

class MapActivity : AppCompatActivity(), OnMapReadyCallback ,MapContract.View, MapsAdapter.OnLoaderDismiss, OnMapAnnotationClickListener,
        SpotAdapter.OnSpotItemClickListener {
    private lateinit var mContext: Context
    private lateinit var mMap: GoogleMap
    private  val TAG = "MapActivity"
    private var spotList = ArrayList<Hotspot>()
    private var mapList = ArrayList<Hotspot>()
    private lateinit var presenter: MapContract.Presenter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mspotDetailAdapter: SpotAdapter
    private var oldMapList = ArrayList<Hotspot>()
    private var spotRadius = 0
    private lateinit var binding: ActivityMapBinding
    private lateinit var sendExploreData: SendExploreData
    private lateinit var mSpotLayoutManagerOnMap: LinearLayoutManager
    companion object {
        lateinit var adapter: MapsAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_map)
//        setContentView(R.layout.activity_map)
        Log.d(TAG, "onCreate: StartActivity")
        progressDialog = ProgressDialog(this)
        presenter = MapPresenter()
        presenter.attachView(this)
        if(HomeActivity.latitude!=0.0){
           binding. mapView.getMapAsync(this)
      }
       setupMap(savedInstanceState)
        adapter = MapsAdapter(this)
        adapter.setOnAnnotationClickListener(this)
        val userData = Prefs.with(this).getObject(Constants.LOGIN_DATA, LoginData::class.java)
        spotRadius = userData.radius
        sendExploreData = sendExploreData("")
        setAdapterOnMap()
       binding. rvSpotDetailmap.visibility=View.INVISIBLE


       binding. imgMapback.setOnClickListener {
            onBackPressed()
        }

        binding.rvSpotDetailmap.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

        val window = window
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()

    }

    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.llmap.setBackgroundColor(Color.parseColor("#000000"))
                binding.imgMapback.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                // etExpoSearch.setTextColor(Color.parseColor("#000000"))
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor= ContextCompat.getColor(this, R.color.black)

            }
            Configuration.UI_MODE_NIGHT_NO -> {

                binding.llmap.setBackgroundColor(Color.WHITE)
                binding.imgMapback.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                /*  etExpoSearch.setHintTextColor(Color.parseColor("#3d4143"))*/
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
                //window.navigationBarColor= ContextCompat.getColor(this, R.color.white)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }

    private fun setupMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        try {
            MapsInitializer.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAdapterOnMap() {
        mspotDetailAdapter = SpotAdapter(this)
        mSpotLayoutManagerOnMap = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mspotDetailAdapter.setonSpotClickListener(this)
       binding. rvSpotDetailmap.layoutManager = mSpotLayoutManagerOnMap
       binding. rvSpotDetailmap.adapter = mspotDetailAdapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvSpotDetailmap)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        adapter.annotations.clear()
        binding.mapView.getMapAsync { map ->
              //Attach the adapter to the map view once it's initialized
              adapter.attach(binding.mapView, map)
              adapter.setListener(this)

          }
          mMap = googleMap
          mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        /* val success: Boolean = googleMap.setMapStyle(MapStyleOptions(resources
                      .getString(R.string.style_json)))
              if (!success) {
                  Log.e("map stayle", "Style parsing failed.")
              }*/

        /* val currentLocation = LatLng(Prefs.with(mContext).getString(Constants.LAT, "").toDouble(),
                 Prefs.with(mContext).getString(Constants.LNG, "").toDouble())*/

          var currentLocation=LatLng(HomeActivity.latitude!!, HomeActivity.longitude!!)

        val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(14f).build()
          mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
          mMap.uiSettings.isMapToolbarEnabled = false

          exploreApi()
       binding. ivCurrentmap.setOnClickListener {
              mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
          }

          mMap.setOnMapClickListener {
             binding. rvSpotDetailmap.visibility=View.GONE
              adapter.setOnAnnotationClickListener(this)
          }


        /*mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                intent = new Intent(this, ShowImagesActivity.class);
                intent.putExtra(ITEM_POSITION, Adapterposition);
            }
        }*/
    }

    private fun sendExploreData(search: String): SendExploreData {
        return SendExploreData(Prefs.with(this).getString(Constants.LAT, ""),
                Prefs.with(this).getString(Constants.LNG, ""),
                "",
                Prefs.with(this).getString(Constants.USER_ID, "") as String,
                search)
    }

    private fun exploreApi() {
        if (GeneralMethods.isNetworkActive(this)) {
//            tvNoSpot.visibility = View.GONE
            radiusCalculate()
            // presenter?.mapViewApi(sendExploreData, true, spotRadius)
        } else {
            GeneralMethods.showToast(this, R.string.error_no_connection)
            if (spotList.isEmpty()) {
                GeneralMethods.showToast(this,"no spot found")
            }
        }
    }

    private fun radiusCalculate() {
        //Log.e("data",(sendExploreData))
        if (Prefs.with(this).getBoolean(Constants.IS_INFINITY, false)) {
            spotRadius = -1
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

    override fun mapData(data: HotspotData) {
        //Log.e("mapdata==",(data.mapData))
        spotList.clear()
        spotList.addAll(data.mapData)
        mapList.clear()
        mspotDetailAdapter.addListWithClear(data.mapData)
        setHandler()
    }
    private fun setHandler() {
        mapList.addAll(spotList)
        oldMapList.clear()
        oldMapList.addAll(mapList)
        adapter.addList(oldMapList)

    }

    override fun onPause() {
        super.onPause()
         if (binding.mapView != null)
             binding.mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (binding.mapView != null)
            binding.mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
         if (binding.mapView != null)
             binding.mapView.onDestroy()
        presenter.detachView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
          if (binding.mapView != null)
              binding.mapView.onLowMemory()
    }


    override fun noSpotAtMapFound() {

    }

    override fun sessionExpired() {
        GeneralMethods.tokenExpired(this)
    }

    override fun failureExploreApi() {

    }

    override fun errorExploreApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(this, errorBody)
    }

    override fun noSpotFound() {

    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }

    override fun loading() {
        progressDialog.dismiss()
    }

    override fun onMapAnnotationClick(mapAnnotationObject: MapAnnotation): Boolean {
        //GeneralMethods.hideSoftKeyboard(this, tvNoSpot)
        binding.rvSpotDetailmap.visibility = View.VISIBLE
        binding.ivCurrentmap.visibility=View.VISIBLE
        if (mapAnnotationObject.position < oldMapList.size) {
            binding.rvSpotDetailmap.smoothScrollToPosition(mapAnnotationObject.position)
        }
       /* val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(Constants.HOTSPOT_ID, oldMapList[mapAnnotationObject.position]._id)
        startActivity(intent)*/
        return true
    }

    override fun onHotSpotClick(id: String) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(Constants.HOTSPOT_ID, id)
        startActivity(intent)
    }
}