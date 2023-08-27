package com.codebrew.whrzat.ui.settings

import android.util.Log
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.ApiContacts
import com.codebrew.whrzat.webservice.pojo.RewardDetailsData
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SettingsPresenter:SettingsContract.Presenter {
    private  val TAG = "SettingsPresenter"


    private var view:SettingsContract.View?=null

    override fun apiDeleteProfile(userId: String) {

        view?.showLoading()


        RetrofitClient.get().apiDeleteProfile(userId).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.apiDeleteProfileSuccess()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.apiError(it) }
                    }
                }
                view?.dismissLoading()
            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.apiFailure()
            }
        })
    }
    override fun apiLogout() {
        view?.showLoading()
        RetrofitClient.get().apiLogout().enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.apiLogoutSuccess()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.apiError(it) }
                    }

                }

                view?.dismissLoading()
            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.apiFailure()
            }
        })
    }

    override fun apiSyncContacts(contactsObj: ApiContacts) {
        view?.showLoading()
        RetrofitClient.get().apiSynContacts(contactsObj).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.apiSuccessContacts()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.apiError(it) }
                    }

                }

                view?.dismissLoading()
            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.apiFailure()
            }
        })
    }

    override fun apiFeedRadius(userId: String, radius: Any, checked: Boolean) {
        //view?.showLoading()
        Log.e(TAG,"feed on off   " +userId+" "+radius+" "+checked.toString())
        RetrofitClient.get().apiFeedRadius(userId, radius, checked).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.apiSuccessRadius()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.apiError(it) }
                    }

                }

                view?.dismissLoading()
            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.apiFailure()
            }
        })
    }
    override fun apiChangePass(userId: String, oldPass: String, newpass: String, Confirm: String) {
        RetrofitClient.get().apiChangePassword(userId, oldPass, newpass).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.apiSuccessChagnePassword()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.apiError(it) }
                    }

                }

                view?.dismissLoading()
            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.apiFailure()
            }
        })
    }

    override fun apigetRewardsDetails(token: String) {
        view?.showLoading()
        RetrofitClient.get().getRewardDetail("Bearer " + token).enqueue(object : Callback<RewardDetailsData> {
            override fun onResponse(call: Call<RewardDetailsData>, response: Response<RewardDetailsData>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: apigetRewardsDetails ${response.body()}")
                    view?.apiSuccessRewardDetails(response.body()!!)
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.apiError(it) }
                    }

                }

                view?.dismissLoading()
            }


            override fun onFailure(call: Call<RewardDetailsData>, t: Throwable) {
                view?.dismissLoading()
                view?.apiFailure()
            }
        })

    }


    override fun attachView(view: SettingsContract.View) {
        this.view=view
    }

    override fun detachView() {
        view=null
    }
}