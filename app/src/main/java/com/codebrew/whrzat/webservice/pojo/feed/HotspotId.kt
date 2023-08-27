package com.codebrew.whrzat.webservice.pojo.feed

import com.codebrew.whrzat.webservice.pojo.PicURL


data class HotspotId(
        var _id: String,
        var area: String,
        var name: String,
        var hotness:String,
        var deleted: Boolean,
     var picture: Picture?=null
)
