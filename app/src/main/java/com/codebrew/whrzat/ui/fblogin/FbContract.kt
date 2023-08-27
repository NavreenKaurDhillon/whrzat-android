package com.codebrew.whrzat.ui.fblogin

import com.codebrew.whrzat.webservice.pojo.login.LoginData
import okhttp3.ResponseBody


class FbContract {
    interface View {
        fun successFb()

        fun signupScreen()

        fun homeScreen(data: LoginData?)

        fun errorFbLogin(errorBody: ResponseBody)

        fun failureFbLogin()

        fun dismissLoading()

        fun showLoading()
        fun sessionExpire()

    }

    interface Presenter {
        fun performLoginFb(id: String)

        fun attachView(view: View)

        fun detachView()
        fun  loginFb(id: String)
    }
}