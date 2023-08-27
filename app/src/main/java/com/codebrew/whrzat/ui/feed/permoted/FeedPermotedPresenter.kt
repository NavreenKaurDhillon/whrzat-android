package com.codebrew.whrzat.ui.feed.permoted

import com.codebrew.whrzat.webservice.pojo.ApiContacts
import com.codebrew.whrzat.webservice.pojo.feed.FeedData
import okhttp3.ResponseBody

class FeedPermotedPresenter: FeedPermotedContract.Presenter  {

    private var view: FeedPermotedContract.View? = null
    override fun attachView(view: FeedPermotedContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }




}