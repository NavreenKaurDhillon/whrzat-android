package com.codebrew.whrzat.ui.settings

import com.codebrew.whrzat.webservice.pojo.ApiContacts
import com.codebrew.whrzat.webservice.pojo.Referalcodedata
import com.codebrew.whrzat.webservice.pojo.RewardDetailsData
import okhttp3.ResponseBody

class SettingsContract {

    interface View {

        fun apiFeedSuccess()

        fun apiLogoutSuccess()
        fun apiDeleteProfileSuccess()

        fun apiFailure()

        fun apiError(errorBody: ResponseBody)

        fun dismissLoading()

        fun showLoading()

        fun sessionExpire()

        fun apiSuccessContacts()

        fun apiSuccessRadius()

        fun apiSuccessChagnePassword()

         fun apiSuccessRewardDetails(data: RewardDetailsData)
    }

    interface Presenter {

        fun apiDeleteProfile(userId: String)
        fun apiLogout()

        fun apiSyncContacts(contactsObj: ApiContacts)

        fun apiFeedRadius(userId: String, radius: Any, checked: Boolean)

        fun apiChangePass(userId:String,oldPass: String, newpass: String, Confirm: String)

        fun apigetRewardsDetails(token:String)

        fun attachView(view: View)

        fun detachView()

    }
}
