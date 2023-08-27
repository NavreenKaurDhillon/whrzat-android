package com.codebrew.whrzat.webservice

import com.google.gson.annotations.SerializedName


class ResponseError{
    @SerializedName("statusCode")
    val statusCode: String? = null
    @SerializedName("message")
     val message: String? = null
    @SerializedName("error")
    val error: Error? = null


}