package com.codebrew.whrzat.ui.signup

import android.util.Log
import android.util.Patterns
import com.codebrew.whrzat.activity.SplashActivity
import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import com.codebrew.whrzat.webservice.pojo.signup.SignupSetData
import com.google.gson.Gson
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*


class SignupPresenter : SignupContract.Presenter {
    private val TAG = "SignupPresenter"

    private var view: SignupContract.View? = null

    override fun performSignup(signupData: SignupSetData) {


        if (signupData.firstName.isEmpty()) {
            view?.invalidFirstName()
        }
//        else if (signupData.lastName.isEmpty()) {
//            view?.invalidLastName()
//        }
//        else if (signupData.email.isEmpty()) {
//            view?.invalidEmail()
//        }
//        else if (!Patterns.EMAIL_ADDRESS.matcher(signupData.email).matches()) {
//            view?.invalidEmail()
//        }
//        else if (signupData.password.isEmpty()) {
//            view?.invalidPassword()
//        }
//        else if (signupData.password.length < 6) {
//            view?.errorPasswordLimit()
//        }
        else if (signupData.contact.isEmpty()) {
            view?.emptyContact()
        } else if (signupData.contact.length < 6) {
            view?.invalidContact()
        }
//        else if (signupData.bio.isEmpty()) {
//            view?.errorBio()
//        }
         else if (signupData.imageFile == null) {
            view?.errorImage()
         } else {

            val map = HashMap<String, RequestBody>()

            map.put(ApiConstants.NAME, GeneralMethods.stringToRequestBody(signupData.firstName + " " + signupData.lastName))
            map.put(ApiConstants.EMAIL, GeneralMethods.stringToRequestBody(signupData.email))
            map.put(ApiConstants.CONTACT, GeneralMethods.stringToRequestBody(signupData.contact))
            map.put(ApiConstants.CODE, GeneralMethods.stringToRequestBody(signupData.code))

            try {
                Log.d("Log", SplashActivity.token.toString())
                map.put(ApiConstants.DEVICE_TOKEN, GeneralMethods.stringToRequestBody(SplashActivity.token.toString()))
            } catch (e: Exception) {

            }
            map.put(ApiConstants.DEVICE_TYPE, GeneralMethods.stringToRequestBody("ANDROID"))
            map.put(ApiConstants.TIMEZONE, GeneralMethods.stringToRequestBody(GeneralMethods.timeZone().toString()))

            if (!signupData.fromFacebook) {
                map.put(ApiConstants.PASSWORD, GeneralMethods.stringToRequestBody(signupData.password))
            }

            if (!signupData.bio.isEmpty()) {
                map.put(ApiConstants.BIO, GeneralMethods.stringToRequestBody(signupData.bio))
            }
            /*if (!signupData.referralCode.isEmpty()){
                map.put(ApiConstants.REFERRALCODE, GeneralMethods.stringToRequestBody(signupData.referralCode))

            }*/
            if (!signupData.facebookId.isEmpty()) {
                map.put(ApiConstants.FACEBOOKID, GeneralMethods.stringToRequestBody(signupData.facebookId))
            }

            if (signupData.imageFile != null) {
                map.put((GeneralMethods.imageToRequestBodyKey(ApiConstants.IMAGE, signupData.imageFile.toString())),
                        GeneralMethods.imageToRequestBody((signupData.imageFile as File).absolutePath))
            }
            Log.d(TAG, "performSignup: Params :-- ${(map)}")
            signupApi(map, signupData.fromFacebook)
        }
    }

//    override fun performFbSignup(signupData: SignupSetData) {
//
//        if (signupData.firstName.isEmpty()) {
//            view?.invalidFirstName()
//        }
////        else if (signupData.lastName.isEmpty()) {
////            view?.invalidLastName()
////        }
////        else if (signupData.email.isEmpty()) {
////            view?.invalidEmail()
////        } else if (!Patterns.EMAIL_ADDRESS.matcher(signupData.email).matches()) {
////            view?.invalidEmail()
////        }
//        else if (signupData.contact.isEmpty()) {
//            view?.emptyContact()
//        } else if (signupData.contact.length < 6) {
//            view?.invalidContact()
//        }
//
//        else {
//
//            val map = HashMap<String, RequestBody>()
//
//            map.put(ApiConstants.NAME, GeneralMethods.stringToRequestBody(signupData.firstName + " " + signupData.lastName))
//            map.put(ApiConstants.EMAIL, GeneralMethods.stringToRequestBody(signupData.email))
//            map.put(ApiConstants.CONTACT, GeneralMethods.stringToRequestBody(signupData.contact))
//            try {
//                map.put(ApiConstants.DEVICE_TOKEN, GeneralMethods.stringToRequestBody(SplashActivity.token.toString()))
//            } catch (e: Exception) {
//
//            }
//            map.put(ApiConstants.DEVICE_TYPE, GeneralMethods.stringToRequestBody("ANDROID"))
//            map.put(ApiConstants.TIMEZONE, GeneralMethods.stringToRequestBody(GeneralMethods.timeZone().toString()))
//
//            if (!signupData.bio.isEmpty()) {
//                map.put(ApiConstants.BIO, GeneralMethods.stringToRequestBody(signupData.bio))
//            }
//            if (!signupData.facebookId.isEmpty()) {
//                map.put(ApiConstants.FACEBOOKID, GeneralMethods.stringToRequestBody(signupData.facebookId))
//            }
//
//            if (!signupData.imageLink.isEmpty()) {
//                map.put(ApiConstants.IMAGE, GeneralMethods.stringToRequestBody(signupData.imageLink))
//            }
//
//            signupApi(map, signupData.fromFacebook)
//        }
//    }

    private fun signupApi(map: HashMap<String, RequestBody>, isFromFacebook: Boolean) {
       // Log.e("signupApi params", (map))
        view?.showLoading()
        RetrofitClient.get().signup(map, isFromFacebook).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.signupSuccess(response.body()?.data)
                } else {
                    view?.dismissLoading()
                    if (response.code() == 401) {
                        view?.sessionExpired()
                    } else {
                        response.errorBody()?.let { view?.signupError(it) }
                    }
                }



            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                if (t != null) {
                    Log.d(TAG, "onFailure: ${t.message.toString()}")
                }
                view?.signupFailure()
                view?.dismissLoading()
            }
        })
    }

    override fun attachView(view: SignupContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }
}
