package com.codebrew.whrzat.ui.otp

import com.codebrew.whrzat.ui.signup.SignupContract
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.codebrew.whrzat.webservice.pojo.signup.SignupSetData
import okhttp3.ResponseBody

class OtpContract {
    interface View{

        fun otpSuccess(data: LoginData?)
        fun otpWrong()
        fun invalidOtp()
        fun otpFailure()
        fun otpError(errorMessage: ResponseBody)

        fun showLoading()

        fun dismissLoading()
    }

    interface Presenter {
        fun performOtpVerify(otp:String)

//        fun performFbSignup(signupData: SignupSetData)

        fun attachView(view: OtpContract.View)

        fun detachView()

    }
}