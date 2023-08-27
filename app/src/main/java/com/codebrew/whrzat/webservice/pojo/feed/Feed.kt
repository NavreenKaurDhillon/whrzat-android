package com.codebrew.whrzat.webservice.pojo.feed

data class Feed(
    var statusCode: Int,
    var message: String,
    var data: List<FeedData>
)
