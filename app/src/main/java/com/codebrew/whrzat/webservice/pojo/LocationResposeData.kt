package com.codebrew.whrzat.webservice.pojo

class LocationResposeData {

     var data: Array<Data?>?=null
    var message: String? = null

     var statusCode: String? = null


    override fun toString(): String {
        return "ClassPojo [data = $data, message = $message, statusCode = $statusCode]"
    }
    class Data {
        var createdAt: String? = null
        var address: String? = null
        private var __v: String? = null
        var name: String? = null
        var location: Array<String>?=null
        var _id: String? = null


        override fun toString(): String {
            return "ClassPojo [createdAt = $createdAt, address = $address, __v = $__v, name = $name, location = $location, _id = $_id]"
        }
    }


}