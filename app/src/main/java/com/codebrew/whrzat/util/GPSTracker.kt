package repository

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs

@RequiresApi(Build.VERSION_CODES.M)
class GPSTracker(context: Context) : Service(),LocationListener {

    private var mContext: Context? =null

    // flag for GPS status
    var isGPSEnabled = false

    // flag for network status
    var isNetworkEnabled = false

    // flag for GPS status
    var canGetLocation = false

    var location: Location? = null
    var  latitude:Double?= 0.0
    var longitude:Double?= 0.0


    // The minimum distance to change Updates in meters
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters


    // The minimum time between updates in milliseconds
    private val MIN_TIME_BW_UPDATES = 1000 * 60 * 1 // 1 minute
        .toLong()

    // Declaring a Location Manager
    protected var locationManager: LocationManager? = null
    private val provider_info: String? = null
    // initializer block
    init {
        mContext=context
        getLocation()
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    fun getLocation() {
        try {
            locationManager =
                mContext!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // getting GPS status
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // getting network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) { // no network provider is enabled
            } else {
                canGetLocation = true
                if (ActivityCompat.checkSelfPermission(
                                mContext!!,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                mContext!!,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                ) { // TODO: Consider calling
//    ActivityCompat#requestPermissions
// here to request the missing permissions, and then overriding
//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                          int[] grantResults)
// to handle the case where the user grants the permission. See the documentation
// for ActivityCompat#requestPermissions for more details.
                    return
                }
                if (isNetworkEnabled) {
                    locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                    )
                    Log.d("Network", "Network")
                    if (locationManager != null) {
                        location =
                            locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) {
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        Log.d("GPS Enabled", "GPS Enabled")
                        if (locationManager != null) {
                            location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }else{
                        val provider = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
                        if (provider.contains("gps")) { //if gps is enabled
                            val poke = Intent()
                            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider")
                            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
                            poke.data = Uri.parse("3")
                            sendBroadcast(poke)
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    /**
     * Stop using GPS listener Calling this function will stop using GPS in your
     * app.
     */
    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }

    /**
     * Function to get latitude
     */
    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
        // return latitude
        return this!!.latitude!!
    }

    /**
     * Function to get longitude
     */
    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }
        // return longitude
        return this!!.longitude!!
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    fun canGetLocation(): Boolean {
        return canGetLocation
    }


    /**
     * Function to show settings alert dialog On pressing Settings button will
     * lauch Settings Options
     */
    fun showSettingsAlert() {
      val builder = AlertDialog.Builder(mContext)
      val customView : View = LayoutInflater.from(mContext).inflate(R.layout.custom_layout2, null);
      builder.setView(customView);

      val textViewTitle = customView.findViewById<TextView>(R.id.heading2)
      val textViewMessage = customView.findViewById<TextView>(R.id.description2)
      textViewTitle.setText("GPS settings")
      textViewMessage.setText("GPS is not enabled. Do you want to go to settings menu?")
      val okButton = customView.findViewById<TextView>(R.id.noBT)
      val noButton = customView.findViewById<TextView>(R.id.okBT)
      okButton.setText("Settings")
      noButton.setText("Cancel")

      builder.setView(customView)
      // Create the AlertDialog
      val alertDialog: android.app.AlertDialog = builder.create()
      // Set other dialog properties
      alertDialog.setCancelable(false)
      okButton.setOnClickListener {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        mContext!!.startActivity(intent)
      }
      noButton.setOnClickListener {
        alertDialog.dismiss()
      }
      alertDialog.show()


//        val alertDialog = AlertDialog.Builder(mContext)
//        // Setting DialogHelp Title
//        alertDialog.setTitle("GPS settings")
//        // Setting DialogHelp Message
//        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")
//        // On pressing Settings button
//        alertDialog.setNegativeButton(
//                "Settings"
//        ) { _, _ ->
//            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            mContext!!.startActivity(intent)
//        }
//        // on pressing cancel button
//        alertDialog.setPositiveButton(
//                "Cancel"
//        ) { dialog, which -> dialog.cancel() }
//        // Showing Alert Message
//        alertDialog.show()
    }

    override fun onLocationChanged(location: Location) {
        var bestAccuracy = -1f
        if (location.accuracy != 0.0f && location.accuracy < bestAccuracy || bestAccuracy == -1f) {
            locationManager!!.removeUpdates(this)
        }
        bestAccuracy = location.accuracy
    }



    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

//    override fun onProviderEnabled(provider: String?) {
//    }
//
//    override fun onProviderDisabled(provider: String?) {
//
//    }

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    fun getAccurecy(): Float {
        return location!!.accuracy
    }

}
