package com.codebrew.whrzat.ui.otp

import com.codebrew.whrzat.ui.signup.SignupContract
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpPresenter : OtpContract.Presenter {

    private var view: OtpContract.View? = null
    override fun performOtpVerify(otp: String) {
        if (otp.isEmpty()){
            view?.invalidOtp()
        }else {

            val map = HashMap<String, String>()
            map.put("code", otp)

            view?.showLoading()
            RetrofitClient.get().otp(map).enqueue(object : Callback<LoginResponse> {

                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        view?.otpSuccess(response.body()?.data)
                    } else {
                        if (response.code() == 400) {
                            view?.otpWrong()
                        } else {
                            response.errorBody()?.let {
                                view?.otpError(it)
                            }
                        }
                    }
                    view?.dismissLoading()

                }


                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    view?.dismissLoading()
                    view?.otpFailure()

                }


            })
        }
    }

    override fun attachView(view: OtpContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }
}