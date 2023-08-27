package com.codebrew.whrzat.ui.hotspotlocation

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivityHotspotLocationBinding
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class HotspotLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val TAG = "HotspotLocationActivity"
    private lateinit var binding: ActivityHotspotLocationBinding

    private var hotspotId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_hotspot_location)
      //  setContentView(R.layout.activity_hotspot_location)
        Log.d(TAG, "onCreate: StartActivity")
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapExplore) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.tvBack.setOnClickListener {
            finish()
        }
        if (intent.getStringExtra(Constants.HOTSPOT_ID) != null) {
            hotspotId = intent.getStringExtra(Constants.HOTSPOT_ID)!!
        } else {
            GeneralMethods.showToast(this, "Unable to get hotspot id")
        }
        binding.spotView.setOnClickListener {
            Log.d("hjotSpotId",hotspotId)
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(Constants.HOTSPOT_ID, hotspotId)
            startActivity(intent)
        }

        setData()

    }

    fun setData() {
        val req = RequestOptions()
        req.transform(CircleCrop())
                .placeholder(R.drawable.circular_placeholder_products)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

        Glide.with(this).load(intent.getStringExtra(Constants.PIC))
                .apply(req)
                .into(binding.ivSpotPic)

        binding.tvSpotTitle.text = intent.getStringExtra(Constants.SPOT_NAME)
        binding.tvSpotSubTitle.text = intent.getStringExtra(Constants.SPOT_DESCRIPTION)

        when (intent.getStringExtra(Constants.COLOR)) {
            Constants.BLUE -> {
                setPopularityType(this.getString(R.string.label_chill),
                        ContextCompat.getColor(this, R.color.blueChill),
                        R.drawable.flame_blue_listing_icon, binding.tvPopularityType)
            }
            Constants.RED -> {
                setPopularityType(this.getString(R.string.label_very_popular),
                        ContextCompat.getColor(this, R.color.redVeryPopular),
                        R.drawable.flame_red_listing_icon, binding.tvPopularityType)
            }
            Constants.YELLOW -> {
                setPopularityType(this.getString(R.string.label_Just_in),
                        ContextCompat.getColor(this, R.color.yellow),
                        R.drawable.flame_yellow_listing_icon, binding.tvPopularityType)
            }
            else -> {
                setPopularityType(this.getString(R.string.label_popular)
                        , ContextCompat.getColor(this, R.color.orangePopular),
                        R.drawable.flame_orange_listing_icon, binding.tvPopularityType)
            }
        }


    }

    private fun setPopularityType(popularityText: String, textColor: Int, popularity_icon: Int, itemView: TextView) {
        itemView.apply {
            text = popularityText
            setTextColor(textColor)
            setCompoundDrawablesWithIntrinsicBounds(popularity_icon, 0, 0, 0)
        }


    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//        if (intent!=null) {
//            Log.d("Locaiton",intent.getStringExtra(Constants.LAT)+intent.getStringExtra(Constants.LNG))
//            val currentLocation = LatLng(intent.getStringExtra(Constants.LAT).toDouble(),
//                    intent.getStringExtra(Constants.LNG).toDouble())
//            mMap.addMarker(MarkerOptions().position(currentLocation)
//                    .title(intent.getStringExtra(Constants.SPOT_NAME)))
//            val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(14f).build()
//            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//        }
//        mMap.uiSettings.isMapToolbarEnabled = false

        val currentLocation = LatLng(Prefs.with(this).getString(Constants.LAT, "").toDouble(),
                Prefs.with(this).getString(Constants.LNG, "").toDouble())
        val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(14f).build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        mMap.uiSettings.isMapToolbarEnabled = false

    }

}