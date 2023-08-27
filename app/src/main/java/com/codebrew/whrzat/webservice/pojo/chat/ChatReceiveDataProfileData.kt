package com.codebrew.whrzat.webservice.pojo.chat

data class ChatReceiveDataProfileData(var id: String,
                                      var receiverId: String,
                                      var senderId: String,
                                      var message: String,
                                      var timeStamp: Double) {
}