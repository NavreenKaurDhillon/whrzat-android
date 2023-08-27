package com.codebrew.whrzat.webservice.pojo.explore

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class EventListData {
    @SerializedName("statusCode")
    @Expose
    var statusCode: Int? = null
    @SerializedName("message")
    @Expose
    var message: String? = null
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data {
        @SerializedName("events")
        @Expose
        var events: List<Event>? = null
        @SerializedName("count")
        @Expose
        var count: Int? = null
    }

    class Event {
        @SerializedName("_id")
        @Expose
        var id: String? = null
        @SerializedName("refund")
        @Expose
        var refund: String? = null
        @SerializedName("description")
        @Expose
        var description: String? = null
        @SerializedName("location")
        @Expose
        var location: List<Double>? = null
        @SerializedName("locationName")
        @Expose
        var locationName: String? = null
        @SerializedName("registrationDate")
        @Expose
        var registrationDate: String? = null
        @SerializedName("deleted")
        @Expose
        var deleted: Boolean? = null
        @SerializedName("picture")
        @Expose
        var picture: Picture? = null
        @SerializedName("isCreatedFromWebsite")
        @Expose
        var isCreatedFromWebsite: Boolean? = null
        @SerializedName("addedBy")
        @Expose
        var addedBy: String? = null
        @SerializedName("tags")
        @Expose
        var tags: List<String>? = null
        @SerializedName("endDate")
        @Expose
        var endDate: String? = null
        @SerializedName("startDate")
        @Expose
        var startDate: String? = null
        @SerializedName("name")
        @Expose
        var name: String? = null
        @SerializedName("email")
        @Expose
        var email: String? = null
        @SerializedName("organizerName")
        @Expose
        var organizerName: String? = null
        @SerializedName("__v")
        @Expose
        var v: Int? = null
        @SerializedName("timeEnd")
        @Expose
        var timeEnd: String? = null
        @SerializedName("timeStart")
        @Expose
        var timeStart: String? = null
    }
    class Picture {
        @SerializedName("thumbnail")
        @Expose
        var thumbnail: String? = null
        @SerializedName("original")
        @Expose
        var original: String? = null
    }
}