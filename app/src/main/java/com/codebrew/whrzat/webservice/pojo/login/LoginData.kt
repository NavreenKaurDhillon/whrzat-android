package com.codebrew.whrzat.webservice.pojo.login

import com.codebrew.whrzat.webservice.pojo.PicURL


data class LoginData(
        var _id: String,
        var password: String,
        var profilePicURL: PicURL,
        var registrationDate: Long,
        var radius: Int,
        var notifications: List<String>,
        var blockedBy:List<String>,
        var loves: Int,
        var facebookId: Any,
        var fromFacebook: Boolean,
        var feedsOn: Boolean,
        var rewardStatus: Boolean,
        var bio: String,
        var contact: String,
        var email: String,
        var name: String,
        var referralCode: String,
        //  var __v: Int? = null
        var accessToken: String)

