package com.codebrew.whrzat.webservice.pojo.explore

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class FavoriteListData {
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
        @SerializedName("listFavorite")
        @Expose
        var listFavorite: List<ListFavorite>? = null
    }

    class ListFavorite {
        @SerializedName("_id")
        @Expose
        var id: String? = null
        @SerializedName("area")
        @Expose
        var area: String? = null
        @SerializedName("createdBy")
        @Expose
        var createdBy: String? = null
        @SerializedName("description")
        @Expose
        var description: String? = null
        @SerializedName("registrationDate")
        @Expose
        var registrationDate: Int? = null
        @SerializedName("dummyCountExpiry")
        @Expose
        var dummyCountExpiry: String? = null

        @SerializedName("dummyCount")
        @Expose
        var dummyCount: Int? = null
        @SerializedName("allCheckedIn")
        @Expose
        var allCheckedIn: List<String>? = null
        @SerializedName("favourites")
        @Expose
        var favourites: List<String>? = null
        @SerializedName("checkedIn")
        @Expose
        var checkedIn: List<Any>? = null
        @SerializedName("eventName")
        @Expose
        var eventName: List<Any>? = null
        @SerializedName("events")
        @Expose
        var events: List<Any>? = null
        @SerializedName("hotness")
        @Expose
        var hotness: String? = null
        @SerializedName("blocked")
        @Expose
        var blocked: Boolean? = null
        @SerializedName("deleted")
        @Expose
        var deleted: Boolean? = null
        @SerializedName("tags")
        @Expose
        var tags: List<String>? = null
        @SerializedName("addedBy")
        @Expose
        var addedBy: String? = null
        @SerializedName("picture")
        @Expose
        var picture: Picture? = null
        @SerializedName("name")
        @Expose
        var name: String? = null
        @SerializedName("isFavouriteColor")
        @Expose
        var isFavouriteColor: String? = null
        @SerializedName("isFavouriteCount")
        @Expose
        var isFavouriteCount: Int? = null
    }
    class Picture {
        @SerializedName("original")
        @Expose
        var original: String? = null
        @SerializedName("thumbnail")
        @Expose
        var thumbnail: String? = null
    }
}