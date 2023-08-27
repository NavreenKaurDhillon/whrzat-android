package com.codebrew.whrzat.ui.LocationGet

import com.codebrew.whrzat.ui.referralCode.ReferralContract
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.LocationResposeData
import com.codebrew.whrzat.webservice.pojo.Referalcodedata
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationsPresenter:LocationContarct.Presenter{
    private var view: LocationContarct.View? = null
    override fun performgetlocation(token: String) {
        view?.showLoading()
        RetrofitClient.get().getLocation("Bearer "+token).enqueue(object : Callback<LocationResposeData> {

            override fun onResponse(call: Call<LocationResposeData>, response: Response<LocationResposeData>) {
                if (response.isSuccessful) {
                    view?.locationSuccess(response.body())
                } else {
                    if (response.code() == 400) {
                        view?.locationFailer("Invalid data list")
                    } else {
                        response.errorBody()?.let {
                            view?.locationError(it)
                        }
                    }
                }
                view?.dismissLoading()

            }


            override fun onFailure(call: Call<LocationResposeData>, t: Throwable) {
                view?.dismissLoading()
                view?.locationFailer(t?.message.toString())
            }


        })
    }

    override fun attachView(view: LocationContarct.View) {
        this.view=view
    }

    override fun detachView() {
        this.view=null
    }

}