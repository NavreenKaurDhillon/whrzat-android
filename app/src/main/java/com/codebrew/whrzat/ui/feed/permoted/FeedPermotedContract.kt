package com.codebrew.whrzat.ui.feed.permoted

import okhttp3.ResponseBody

class FeedPermotedContract {
    interface View{
        fun errorApi(errorBody: ResponseBody)

        fun failureApi()

        fun sessionExpire()

        fun dismissLoading()

        fun showLoading()
    }
    interface Presenter{


        fun attachView(view: FeedPermotedContract.View)



        fun detachView()
    }
}