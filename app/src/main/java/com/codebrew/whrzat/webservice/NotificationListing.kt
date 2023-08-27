package com.codebrew.whrzat.webservice


data class NotificationListing(var status: Int,
                               var message: String,
                               var data: List<String>)