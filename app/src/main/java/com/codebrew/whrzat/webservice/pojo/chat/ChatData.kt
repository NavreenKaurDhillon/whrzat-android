package com.codebrew.whrzat.webservice.pojo.chat


data class ChatData(
        var statusCode: Int,
        var message: String,
        var data: ChatMsgProfile)