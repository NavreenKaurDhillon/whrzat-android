package com.codebrew.whrzat.webservice.pojo.HotspotDetail

import com.codebrew.whrzat.webservice.pojo.PicURL


class ImagesFeed(
        var _id: String,
        var createdBy: CreatedBy? = null,
        var addedBy: String? = null,
        var hotspotId: String? = null,
        var blocked: Boolean,
        var likes: List<Any>? = null,
        var likesCount: Int? = null,
        var registrationDate: Long? = null,
        var isLiked: Boolean,
        var picture: PicURL? = null)
// var __v: Int)
