package com.codebrew.whrzat.ui.LocationGet

import com.codebrew.whrzat.ui.referralCode.ReferralContract
import com.codebrew.whrzat.webservice.pojo.LocationResposeData
import com.codebrew.whrzat.webservice.pojo.Referalcodedata
import okhttp3.ResponseBody

class LocationContarct {
    interface View{

        fun locationSuccess(data: LocationResposeData?)
        fun locationError(errorMessage: ResponseBody)
        fun locationFailer(message:String)
        fun showLoading()

        fun dismissLoading()
    }

    interface Presenter {
        fun performgetlocation(token:String)

//        fun performFbSignup(signupData: SignupSetData)

        fun attachView(view:View)

        fun detachView()

    }
}