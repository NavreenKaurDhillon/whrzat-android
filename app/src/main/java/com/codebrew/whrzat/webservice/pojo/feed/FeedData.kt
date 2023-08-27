package com.codebrew.whrzat.webservice.pojo.feed


data class FeedData(
    var _id: String,
    var imageId: ImageId,
    var createdBy: CreatedBy,
    var hotspotId: HotspotId,
    var registrationDate: Long,
    var isLiked:Boolean,
    var type: String
)
