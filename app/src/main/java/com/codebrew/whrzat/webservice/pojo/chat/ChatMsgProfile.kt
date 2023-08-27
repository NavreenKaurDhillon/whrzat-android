package com.codebrew.whrzat.webservice.pojo.chat

import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData


class ChatMsgProfile(var arr: List<ChatReceiveDataProfileData>,
                     var profile: UserData,
                     var blocked:Boolean,
                     var blockedBy:Boolean)