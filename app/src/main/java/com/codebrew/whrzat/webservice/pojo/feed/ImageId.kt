package com.codebrew.whrzat.webservice.pojo.feed


import com.codebrew.whrzat.webservice.pojo.PicURL

data class ImageId(
    var _id: String,
    var likes: List<String>,
    var likesCount:Int,
    var registrationDate: Long,
    var picture: PicURL,
    var isLoveSelected:Boolean)
