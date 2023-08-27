package com.codebrew.whrzat.ui.settings.editProfile

import com.codebrew.whrzat.webservice.pojo.EditData
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import okhttp3.ResponseBody

class EditProfileContractor {

    interface View{

        fun apiEditSuccess(data: LoginData)

        fun apiFailure()

        fun apiError(errorBody: ResponseBody)

        fun showLoading()

        fun dismissLoading()
        fun sessionExpire()
        fun errorName()
        fun errorContact()
        fun errorBio()
    }

    interface Presenter{

        fun apiEdiProfile(editData: EditData)

        fun attachView(view:View)

        fun detachView()
    }
}