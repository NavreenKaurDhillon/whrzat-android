package com.codebrew.whrzat.webservice.pojo.chat


data class ChatUser(var statusCode: Int,
                    var message: String,
                    var data:List<ChatAllUser>)