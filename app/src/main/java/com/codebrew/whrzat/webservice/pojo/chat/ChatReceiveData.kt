package com.codebrew.whrzat.webservice.pojo.chat

import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData


data class ChatReceiveData(var id: String,
                           var receiverId: UserData,
                           var senderId: ChatSenderDetail,
                           var message: String,
                           var timeStamp: Double)