package com.codebrew.whrzat.webservice.pojo.notifications


data class NotificationMain(
    var statusCode: Int,
    var message: String,
    var data: List<NotificationData>)