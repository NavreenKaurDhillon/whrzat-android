package com.codebrew.whrzat.ui.referralCode

import com.codebrew.whrzat.ui.otp.OtpContract
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.Referalcodedata
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReferralPresenter:ReferralContract.Presenter {
    private var view: ReferralContract.View? = null

    override fun performreferral(token:String,referralcode: String) {
        if (referralcode.isEmpty()){
            view?.invalidreferral()
        }else {
            val map = HashMap<String, String>()
            map.put(ApiConstants.REFERRALCODE, referralcode)
            view?.showLoading()
            RetrofitClient.get().signupWithReferralCode("Bearer "+token,map).enqueue(object : Callback<Referalcodedata> {

                override fun onResponse(call: Call<Referalcodedata>, response: Response<Referalcodedata>) {
                    if (response.isSuccessful) {
                        view?.referralSuccess(response.body())
                    } else {
                        if (response.code() == 400) {
                            view?.referralFailer("Invalid Referral Code")
                        } else {
                            response.errorBody()?.let {
                                view?.referralError(it)
                            }
                        }
                    }
                    view?.dismissLoading()

                }


                override fun onFailure(call: Call<Referalcodedata>, t: Throwable) {
                    view?.dismissLoading()
                    view?.referralFailer(t?.message.toString())
                }


            })
        }
    }


    override fun attachView(view: ReferralContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

}