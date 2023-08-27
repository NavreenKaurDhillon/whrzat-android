package com.codebrew.whrzat.ui.addhotspot

import android.util.Log
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.FetchData
import com.codebrew.whrzat.webservice.pojo.createHotspot.SupplyHotspotData
import com.codebrew.whrzat.webservice.pojo.geocoder.GeocoderResponse
import com.google.gson.Gson
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPresenter : AddContract.Presenter {


    private var view: AddContract.View? = null

    override fun performCreateHotspot(spotData: SupplyHotspotData) {

        if (spotData.image== null) {
            view?.selectImage()
        } else if (spotData.hotspotName.isEmpty()) {
            view?.emptyHotspotName()
        }/* else if (spotData.tags.isEmpty()) {
            view?.emptyTags()
        } */else if (spotData.description.isEmpty()) {
            view?.emptyDescription()
        } else {
            val map = HashMap<String, RequestBody>()
            map.put((GeneralMethods.imageToRequestBodyKey(ApiConstants.IMAGE, spotData.image.toString())),
                    GeneralMethods.imageToRequestBody(spotData.image.absolutePath))
            map.put(ApiConstants.NAME, GeneralMethods.stringToRequestBody(spotData.hotspotName))
            map.put(ApiConstants.DESCRIPTION, GeneralMethods.stringToRequestBody(spotData.description))
            map.put(ApiConstants.TAGS, GeneralMethods.stringToRequestBody(spotData.tags))
            map.put(ApiConstants.LATITUDE, GeneralMethods.stringToRequestBody(spotData.lat))
            map.put(ApiConstants.LONGITUDE, GeneralMethods.stringToRequestBody(spotData.lng))
            map.put(ApiConstants.AREA, GeneralMethods.stringToRequestBody(spotData.area))
            map.put(ApiConstants.CREATED_BY, GeneralMethods.stringToRequestBody(spotData.createdBy))


            createHotSpotApi(map)
        }
    }

    private fun createHotSpotApi(map: HashMap<String, RequestBody>) {
        view?.showLoading()
        RetrofitClient.get().createHotspot(map).enqueue(object : Callback<FetchData> {
            override fun onResponse(call: Call<FetchData>, response: Response<FetchData>) {
                if (response.isSuccessful) {
                    view?.successCreateHotspot()
                } else {
                    if (response.code() == 401) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorCreateHotspot(it) }
                    }

                }
                view?.dismissLoading()
            }

            override fun onFailure(call: Call<FetchData>, t: Throwable) {
                view?.failureCreateHotspot()
                view?.dismissLoading()
            }
        })
    }

//    override fun findAddress(lat: String, lng: String, apiKey:String) {
//        val latlng = "$lat,$lng"
//       RetrofitClient.get().geoCoderApi(latlng, apiKey).enqueue(object : Callback<GeocoderResponse> {
//
//           override fun onResponse(call: Call<GeocoderResponse>?, response: Response<GeocoderResponse>) {
//               if (response.isSuccessful) {
//                   if (response.body()?.results != null) {
//                       try {
//                           Log.e("findAddress", " ----------- geoCoderApi() " + (response))
//                           view?.showAddress(response.body()?.results?.get(0)?.formatted_address)
//                       } catch (e: IndexOutOfBoundsException) {
//                           e.printStackTrace()
//                       }
//                   }
//
//               } else {
//
//               }
//           }
//
//           override fun onFailure(call: Call<GeocoderResponse>?, t: Throwable?) {
//               Log.e("failure", t!!.message.toString())
//           }
//
//
//       })
//    }

    override fun attachView(view: AddContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

}
