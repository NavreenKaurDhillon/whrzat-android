package com.codebrew.whrzat.ui.Home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.codebrew.whrzat.R
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.ui.map.MapActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.webservice.pojo.explore.Hotspot
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.makeramen.roundedimageview.RoundedImageView
import nz.co.trademe.mapme.annotations.AnnotationFactory
import nz.co.trademe.mapme.annotations.MapAnnotation
import nz.co.trademe.mapme.annotations.MarkerAnnotation
import nz.co.trademe.mapme.googlemaps.GoogleMapMeAdapter


class MapsAdapter(context: MapActivity) : GoogleMapMeAdapter(context) {

    private var countIndex = 0
    private var count = 0
    private val markersList = ArrayList<Hotspot>()
    private lateinit var loading: OnLoaderDismiss
    override fun getItemCount(): Int = markersList.size

    override fun onBindAnnotation(annotation: MapAnnotation, position: Int, payload: Any?) {
        val item = this.markersList[position]
        if (annotation is MarkerAnnotation) {
            try {
//                annotation.removeFromMap(this.map!!,context)
                count = markersList.size
                if (annotation!=null) {
                    downloadImages(item, annotation, position)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun setListener(loading: OnLoaderDismiss) {
        this.loading = loading
    }


    @SuppressLint("CheckResult")
    private fun downloadImages(item: Hotspot, annotation: MarkerAnnotation, pos: Int) {
        val mDefaultBackground = context.resources.getDrawable(R.drawable.circular_placeholder_products)
        val reqs = RequestOptions()
        reqs.transform(RoundedCorners(context.resources.getDimensionPixelOffset(R.dimen.dp_6)))
                .error(mDefaultBackground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)


        try {

            Glide.with(context)
                    .asBitmap()
                    .apply(reqs)
                    .load(item.picture.thumbnail)
                    .into(object : CustomTarget<Bitmap>(60, 60) {


                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            setMarker(resource, pos, annotation, item)
                            if (pos == markersList.size - 1) {
                                loading.loading()
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                    })

        } catch (e: Exception) {

        }

    }


    private fun setMarker(bmImg: Bitmap?, countIndex: Int, annotation: MarkerAnnotation, item: Hotspot) {
        val customMarkerView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.custom_marker, null)
        customMarkerView.findViewById<RoundedImageView>(R.id.ivPopularity).setImageBitmap(bmImg)


        if (countIndex < count) {
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

            try {
                if (annotation!=null) {

                    annotation.icon = getMarkerBitmapFromView(customMarkerView)
                }
            } catch (e: Exception) {

            }

        }

    }

    override fun onCreateAnnotation(factory: AnnotationFactory<GoogleMap>, position: Int, annotationType: Int): MapAnnotation {
        val item = this.markersList[position]

        if (map!=null) {
            if (position == markersList.size - 1) {
                val latLng = LatLngBounds.builder()
                for (hostSpot in markersList) {
                    latLng.include(LatLng(hostSpot.location[1], hostSpot.location[0]))
                }
                // map?.isMyLocationEnabled = true
                val currentLocation = LatLng(Prefs.with(context).getString(Constants.LAT, "").toDouble(),
                        Prefs.with(context).getString(Constants.LNG, "").toDouble())
                val cameraPosition = CameraPosition.Builder().target(currentLocation).zoom(14f).build()
                map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                map?.addMarker(MarkerOptions()
                        .zIndex(1.0f)
                        .position(currentLocation)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location)))
                //  map?.moveCamera(CameraUpdateFactory.newLatLngBounds(latLng.build(), 0))
            }

        }

        return factory.createMarker(nz.co.trademe.mapme.LatLng(item.location[1], item.location[0]), null, "")
    }


    private fun getMarkerBitmapFromView(customMarkerView: View): Bitmap {
        customMarkerView.layoutParams = (RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT))
        customMarkerView.measure(customMarkerView.measuredWidth, customMarkerView.measuredHeight)
        customMarkerView.layout(0, 0, customMarkerView.measuredWidth, customMarkerView.measuredHeight)
        customMarkerView.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(customMarkerView.measuredWidth, customMarkerView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        customMarkerView.draw(canvas)
      /*  canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable = customMarkerView.background
        drawable?.draw(canvas)
        customMarkerView.draw(canvas)*/
        return returnedBitmap
    }

    fun addList(exploreList: ArrayList<Hotspot>) {
        annotations.clear()
        markersList.clear()
        markersList.addAll(exploreList)
        countIndex = 0

        notifyDataSetChanged()

    }

    fun clearList() {
        markersList.clear()
        notifyDataSetChanged()
    }

    interface OnLoaderDismiss {
        fun loading()
    }


}
