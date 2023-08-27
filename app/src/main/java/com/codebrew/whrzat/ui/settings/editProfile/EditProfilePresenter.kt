package com.codebrew.whrzat.ui.settings.editProfile

import com.codebrew.whrzat.util.ApiConstants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.EditData
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class EditProfilePresenter : EditProfileContractor.Presenter {

    private var view: EditProfileContractor.View? = null
    override fun apiEdiProfile(editData: EditData) {


        when {
            editData.name.isEmpty() -> view?.errorName()
            editData.contact.isEmpty() -> view?.errorContact()
            editData.contact.length<6 ->view?.errorContact()
//            editData.bio.isEmpty() -> view?.errorBio()
            else -> editProfileApi(editData)
        }

    }

    private fun editProfileApi(editData: EditData) {
        val map = HashMap<String, RequestBody>()
        if (editData.image != null) {
            map.put((GeneralMethods.imageToRequestBodyKey(ApiConstants.IMAGE, editData.image.toString())),
                    GeneralMethods.imageToRequestBody((editData.image as File).absolutePath))
        }
        map.put(ApiConstants.NAME,GeneralMethods.stringToRequestBody(editData.name))
        map.put(ApiConstants.CONTACT,GeneralMethods.stringToRequestBody(editData.contact))
        map.put(ApiConstants.BIO,GeneralMethods.stringToRequestBody(editData.bio))
        map.put(ApiConstants.USER_ID,GeneralMethods.stringToRequestBody(editData.userId))


        view?.showLoading()
        RetrofitClient.get().apiEditProfile(map).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { view?.apiEditSuccess(it) }
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
                view?.showLoading()
                view?.dismissLoading()

            }
        })
    }


    override fun attachView(view: EditProfileContractor.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}