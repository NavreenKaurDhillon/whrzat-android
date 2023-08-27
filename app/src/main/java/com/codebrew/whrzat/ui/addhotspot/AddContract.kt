package com.codebrew.whrzat.ui.addhotspot

import com.codebrew.whrzat.webservice.pojo.createHotspot.SupplyHotspotData
import okhttp3.ResponseBody


class AddContract {
    interface View{
        fun emptyHotspotName()
        fun emptyTags()
        fun emptyDescription()
        fun selectImage()
        fun noAddress()
        fun successCreateHotspot()
        fun failureCreateHotspot()
        fun errorCreateHotspot(errorBody: ResponseBody)
        fun sessionExpire()
       // fun showAddress(formatted_address: String?)
        fun dismissLoading()
        fun showLoading()
    }
     interface Presenter{
         fun performCreateHotspot(spotData: SupplyHotspotData)
        // fun findAddress(lat:String,lng:String, apiKey:String)
         fun attachView(view: View)
         fun detachView()
     }
}
