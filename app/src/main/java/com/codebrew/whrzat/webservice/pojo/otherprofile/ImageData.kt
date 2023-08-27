package com.codebrew.whrzat.webservice.pojo.otherprofile

import com.codebrew.whrzat.webservice.pojo.feed.CreatedBy
import com.codebrew.whrzat.webservice.pojo.feed.HotspotId
import com.codebrew.whrzat.webservice.pojo.feed.ImageId

data class ImageData(var _id: String,
                     var imageId: ImageId,
                     var createdBy: CreatedBy,
                     var hotspotId: HotspotId,
                     var expiry: Long,
                     var registrationDate: Long,
                     var blocked: Boolean,
                     var deleted: Boolean,
                     var type: String,
                     var isLiked: Boolean,
                     var isCheckin: Boolean)
