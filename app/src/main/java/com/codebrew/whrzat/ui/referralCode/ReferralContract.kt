package com.codebrew.whrzat.ui.referralCode

import com.codebrew.whrzat.ui.otp.OtpContract
import com.codebrew.whrzat.webservice.pojo.Referalcodedata
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import okhttp3.ResponseBody

class ReferralContract {
    interface View{

        fun referralSuccess(data: Referalcodedata?)
        fun referralError(errorMessage: ResponseBody)
        fun referralFailer(message:String)
        fun invalidreferral()
        fun showLoading()

        fun dismissLoading()
    }

    interface Presenter {
        fun performreferral(token:String,referralcode:String)

//        fun performFbSignup(signupData: SignupSetData)

        fun attachView(view: ReferralContract.View)

        fun detachView()

    }
}