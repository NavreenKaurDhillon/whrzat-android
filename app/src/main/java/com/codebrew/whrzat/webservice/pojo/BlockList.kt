package com.codebrew.whrzat.webservice.pojo

import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData

data class BlockList(
    var statusCode: Int,
    var message: String,
    var data: List<UserData>)