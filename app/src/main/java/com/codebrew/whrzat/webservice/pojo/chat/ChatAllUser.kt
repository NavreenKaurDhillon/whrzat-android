package com.codebrew.whrzat.webservice.pojo.chat

import com.codebrew.whrzat.webservice.pojo.feed.CreatedBy

data class ChatAllUser(var message: ChatReceiveData,
                       var pro: CreatedBy,
                       var unread:Int)
