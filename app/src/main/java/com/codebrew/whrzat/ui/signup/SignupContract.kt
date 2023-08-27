package com.codebrew.whrzat.ui.signup

import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.codebrew.whrzat.webservice.pojo.signup.SignupSetData
import okhttp3.ResponseBody


class SignupContract{
    interface View {
        fun invalidUserName()

        fun invalidFirstName()

        fun invalidLastName()

        fun invalidPassword()

        fun invalidContact()

        fun invalidEmail()

        fun signupSuccess(data: LoginData?)

        fun fbNewUser()

        fun signupError(errorMessage: ResponseBody)

        fun signupFailure()

        fun showLoading()

        fun dismissLoading()

        fun sessionExpired()

        fun errorBio()
        fun errorImage()
        fun emptyContact()
        fun errorPasswordLimit()
    }

    interface Presenter {
        fun performSignup(signupData:SignupSetData)

        //fun performFbSignup(signupData:SignupSetData)

        fun attachView(view: View)

        fun detachView()
    }

}
