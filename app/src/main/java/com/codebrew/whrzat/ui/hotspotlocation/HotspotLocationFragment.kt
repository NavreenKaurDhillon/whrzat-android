package com.codebrew.whrzat.ui.hotspotlocation

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivityHotspotLocationBinding
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng


class HotspotLocationFragment: BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var mContext: Context
    private  val TAG = "HotspotLocationFragment"
    private lateinit var binding: ActivityHotspotLocationBinding
    private lateinit var mMap: GoogleMap
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityHotspotLocationBinding.inflate(inflater,container,false)
        val view = binding.root
        val mapFragment =
                childFragmentManager.findFragmentById(R.id.mapExplore) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment

        mapFragment!!.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")
        binding.tvBack.setOnClickListener {
            dismiss()
        }
//        mapExplore.getMapAsync(this)

    }


    override fun onMapReady(googleMap: GoogleMap) {

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.setMyLocationEnabled(true);
        mMap = googleMap!!
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val currentLocation = LatLng(Prefs.with(mContext).getString(Constants.LAT, "").toDouble(),
                Prefs.with(mContext).getString(Constants.LNG, "").toDouble())
        val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(14f).build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        mMap.uiSettings.isMapToolbarEnabled = false
    }
}