package com.codebrew.whrzat.webservice.pojo.signup

import java.io.File


class SignupSetData(
        var imageFile: File? = null,
        var firstName: String,
        var lastName: String,
        var bio: String,
        var facebookId: String,
        var email: String,
        var contact: String,
        var fromFacebook: Boolean,
        var password: String,
        var image: File?,
        var imageLink:String,
        var code:String,
        var referralCode:String)

