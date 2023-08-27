package com.codebrew.whrzat.ui.Home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.webservice.pojo.explore.Hotspot
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.makeramen.roundedimageview.RoundedImageView
import nz.co.trademe.mapme.annotations.AnnotationFactory
import nz.co.trademe.mapme.annotations.MapAnnotation
import nz.co.trademe.mapme.annotations.MarkerAnnotation
import nz.co.trademe.mapme.googlemaps.GoogleMapMeAdapter


class MapAdapter(context: Context) : GoogleMapMeAdapter(context) {


   private var markersList = ArrayList<Hotspot>()

    override fun getItemCount(): Int = markersList.size

    override fun onBindAnnotation(annotation: MapAnnotation, position: Int, payload: Any?) {
        if (position < markersList.size) {
            val item = this.markersList[position]
            if (annotation is MarkerAnnotation) {
                val customMarkerView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.custom_marker, null)
                customMarkerView.findViewById<RoundedImageView>(R.id.ivPopularity).setImageResource(R.drawable.flame_red_icon)
                try {
                 /*   val bmImg = Ion.with(context)
                            .load(item.picture?.thumbnail).asBitmap().get()*/
                   // customMarkerView.ivPopularity.setImageBitmap(bmImg)
                    when (item.hotness) {
                        Constants.BLUE -> {
                            customMarkerView.findViewById<ImageView>(R.id.ivFire).setImageResource(R.drawable.flame_blue_icon)
                        }
                        Constants.RED -> {
                            customMarkerView.findViewById<ImageView>(R.id.ivFire).setImageResource(R.drawable.flame_red_icon)
                        }
                        Constants.YELLOW -> {
                            customMarkerView.findViewById<ImageView>(R.id.ivFire).setImageResource(R.drawable.flame_yellow_icon)
                        }
                        else -> {
                            customMarkerView.findViewById<ImageView>(R.id.ivFire).setImageResource(R.drawable.flame_orange_icon)
                        }

                    }
                    annotation.icon = getMarkerBitmapFromView(customMarkerView)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                //annotation.setTitle("")
            }
        }
    }

    override fun onCreateAnnotation(factory: AnnotationFactory<GoogleMap>, position: Int, annotationType: Int): MapAnnotation {
        val item = this.markersList[position]
        if (position == markersList.size - 1) {
            val latLng = LatLngBounds.builder()
            for (hostSpot in markersList) {
                latLng.include(LatLng(hostSpot.location[1], hostSpot.location[0]))
            }
            val currentLocation = LatLng(Prefs.with(context).getString(Constants.LAT, "").toDouble(),
                    Prefs.with(context).getString(Constants.LNG, "").toDouble())

            /*     map?.addMarker(MarkerOptions()
                         .position(currentLocation)
                         .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location)))*/

        }
        return factory.createMarker(nz.co.trademe.mapme.LatLng(item.location[1], item.location[0]), null, "")
    }


    private fun getMarkerBitmapFromView(customMarkerView: View): Bitmap {
        customMarkerView.layoutParams = (RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT))
        customMarkerView.measure(customMarkerView.measuredWidth, customMarkerView.measuredHeight)
        customMarkerView.layout(0, 0, customMarkerView.measuredWidth, customMarkerView.measuredHeight)
        customMarkerView.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(customMarkerView.measuredWidth, customMarkerView.measuredHeight,
                Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        customMarkerView.draw(canvas)
        return returnedBitmap
    }


    fun addList(exploreList: ArrayList<Hotspot>) {
        markersList.clear()
        markersList.addAll(exploreList)
        notifyDataSetChanged()
    }


    /*fun addListWithClear(exploreList: ArrayList<Hotspot>) {
      markersList.clear()
      markersList.addAll(exploreList)
      notifyDataSetChanged()
    }*/

    fun clearList() {
       markersList.clear()
        notifyDataSetChanged()
    }

}


