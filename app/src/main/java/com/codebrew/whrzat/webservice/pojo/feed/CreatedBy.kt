package com.codebrew.whrzat.webservice.pojo.feed
import com.codebrew.whrzat.webservice.pojo.PicURL
import com.google.gson.annotations.SerializedName

data class CreatedBy(
        var _id: String,
        var profilePicURL: PicURL?=null,
        var name: String
)
