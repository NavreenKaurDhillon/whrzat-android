package com.codebrew.whrzat.ui.login

import com.codebrew.whrzat.webservice.pojo.login.LoginData
import okhttp3.ResponseBody


class LoginContract {

    interface View {
        fun invalidEmail()

        fun emptyEmail()

        fun invalidContact()
        fun emptyPassword()

        fun invalidPassword()

        fun loginSuccess(data: LoginData?)

        fun successForgotPass()

        fun fbNewUser()

        fun loginError(errorMessage: ResponseBody)

        fun errorPasswordApi(errorBody: ResponseBody)


        fun loginFailure()

        fun showLoading()

        fun dismissLoading()

        fun sessionExpired()
    }

    interface Presenter {
        fun performLogin(email: String, password: String)

        fun performFbLogin(id: String)

        fun performForgotPassword(email:String)

        fun attachView(view: View)

        fun detachView()
    }

}
