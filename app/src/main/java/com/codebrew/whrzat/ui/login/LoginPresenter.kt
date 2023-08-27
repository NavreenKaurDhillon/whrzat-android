package com.codebrew.whrzat.ui.login

import android.util.Log
import android.util.Patterns
import com.codebrew.whrzat.activity.SplashActivity
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginPresenter : LoginContract.Presenter {

    private var view: LoginContract.View? = null
    private var type: String? = null

    override fun performLogin(email: String, password: String) {
        if (password.isEmpty()) {
            view?.emptyEmail()
        }else if (password.length < 6) {
            view?.invalidContact()
        }
//        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            view?.invalidEmail()
//        } else if (password.isEmpty()) {
//            view?.emptyPassword()
//        }

        else {
            val map = HashMap<String, String>()
    //            map.put("email", email)
    //            map.put("password", password)
    //            map.put("password", password)
            map.put("contact", password)
            map.put("code", email)
            map.put("timeZone", GeneralMethods.timeZone().toString())
            try {
               // map.put(ApiConstants.DEVICE_TOKEN, FirebaseInstanceId.getInstance().token.toString())
                map.put(ApiConstants.DEVICE_TOKEN, SplashActivity.token.toString())
                Log.d("Log", SplashActivity.token.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            map.put(ApiConstants.DEVICE_TYPE, "ANDROID")
            loginApi(map)
        }
    }

    private fun loginApi(map: HashMap<String, String>) {
        view?.showLoading()
       // Log.e("login params",(map))
        RetrofitClient.get().login(map).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
               // Log.e("login res",(response))
                if (response.isSuccessful) {
                    view?.loginSuccess(response.body()?.data)
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.dismissLoading()
                        view?.sessionExpired()
                    } else {
                        view?.dismissLoading()
                        response.errorBody()?.let { view?.loginError(it) }
                    }
                }
               // view?.dismissLoading()

            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.loginFailure()

            }


        })
    }

    override fun performForgotPassword(email: String) {
        view?.showLoading()
        RetrofitClient.get().apiForgotPassword(email).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.successForgotPass()
                } else {
                    response.errorBody()?.let { view?.errorPasswordApi(it) }
                }

                view?.dismissLoading()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.dismissLoading()
            }
        })
    }


    override fun performFbLogin(id: String) {
    }

    override fun attachView(view: LoginContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }
}
