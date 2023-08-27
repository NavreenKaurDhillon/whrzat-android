package com.codebrew.whrzat.webservice.pojo.otherprofile

import com.codebrew.whrzat.webservice.pojo.PicURL
import com.codebrew.whrzat.webservice.pojo.feed.ImageId
import com.google.gson.annotations.SerializedName

data class UserData(
        var _id: String,
        var password: Any?,
        var profilePicURL: PicURL,
        var registrationDate: Long,
        var notifications: List<String>,
        var blockedBy: List<Any>,
        var feeds: List<Any>,
        var loves: Int,
        var radius: Int,
        var facebookId: String,
        var feedsOn: Boolean,
        var fromFacebook: Boolean,
        var bio: String,
        var contact: String,
        var email: String,
        var name: String,
        var __v: Int,
        var accessToken: String,
        var images: List<ImageData>


)