package com.codebrew.whrzat.webservice.pojo

class Referalcodedata {

    var data: Data? = null

    var message: String? = null

    var statusCode: String? = null
    class Data {

    }
    override fun toString(): String {
        return "ClassPojo [ message = $message, statusCode = $statusCode]"
    }

}