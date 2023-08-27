package com.codebrew.whrzat.webservice.pojo.notifications


import com.codebrew.whrzat.webservice.pojo.feed.CreatedBy
import com.codebrew.whrzat.webservice.pojo.feed.HotspotId
import com.codebrew.whrzat.webservice.pojo.feed.ImageId

data class NotificationData(
        var id: String,
        var imageId: ImageId,
        var userId: CreatedBy,
        var likedBy: CreatedBy,
        var hotspotId: HotspotId,
        var registrationDate: Long,
        var event: String,
        var hotness: String,
        var blocked: Boolean,
        var deleted: Boolean,
        var v: Int)
