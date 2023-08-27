package com.codebrew.whrzat.ui.fblogin

import android.util.Log
import com.codebrew.whrzat.activity.SplashActivity
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FbPresenter : FbContract.Presenter {


    private var view: FbContract.View? = null



    override fun performLoginFb(id: String) {
        if (!id.isEmpty()) {
            view?.showLoading()
            val map = HashMap<String, String>()
            map.put(ApiConstants.FB_ID, id)
            map.put(ApiConstants.TIMEZONE, GeneralMethods.timeZone().toString())
            map.put(ApiConstants.DEVICE_TYPE,"ANDROID")
            try {
               // map.put(ApiConstants.DEVICE_TOKEN,FirebaseInstanceId.getInstance().token.toString())
                map.put(ApiConstants.DEVICE_TOKEN,SplashActivity.token.toString())
                Log.d("Log",SplashActivity.token.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            RetrofitClient.get().fbLogin(map).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                            view?.successFb()
                    } else {
                        when {
                                response.code() == ApiConstants.UNAUTHORIZED_ACCESS -> view?.sessionExpire()
                            response.code()==400 -> view?.signupScreen()
                            else -> response.errorBody()?.let {
                                view?.errorFbLogin(it)
                            }
                        }
                    }
                    view?.dismissLoading()
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    view?.dismissLoading()
                    view?.showLoading()
                }
            })
        }
    }


    override fun loginFb(id: String) {
        view?.showLoading()
        val map = HashMap<String, String>()
        map.put(ApiConstants.FB_ID, id)
        map.put(ApiConstants.TIMEZONE, GeneralMethods.timeZone().toString())
        try {
            //map.put(ApiConstants.DEVICE_TOKEN,FirebaseInstanceId.getInstance().token.toString())
            map.put(ApiConstants.DEVICE_TOKEN,SplashActivity.token.toString())
            Log.d("Log",SplashActivity.token.toString())
        } catch (e: Exception) {
        e.printStackTrace()
        }
        RetrofitClient.get().login(map).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.homeScreen(response.body()?.data)
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let {
                            view?.errorFbLogin(it)
                        }
                    }
                }


                view?.dismissLoading()

            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()

            }

        })
    }

    override fun attachView(view: FbContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}