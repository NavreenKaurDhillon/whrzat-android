package com.codebrew.whrzat.webservice.pojo.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class HappeningListData {
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
        @SerializedName("imageData")
        @Expose
        var imageData: List<ImageDatum>? = null
        @SerializedName("count")
        @Expose
        var count: Int? = null
    }

    class ImageDatum {
        @SerializedName("_id")
        @Expose
        var id: String? = null
        @SerializedName("createdBy")
        @Expose
        var createdBy: CreatedBy? = null
        @SerializedName("hotspotId")
        @Expose
        var hotspotId: HotspotId? = null
        @SerializedName("expiry")
        @Expose
        var expiry: String? = null
        @SerializedName("registrationDate")
        @Expose
        var registrationDate: String? = null
        @SerializedName("isLikedCount")
        @Expose
        var isLikedCount: Int? = null
        @SerializedName("isLiked")
        @Expose
        var isLiked: Boolean? = null
        @SerializedName("picture")
        @Expose
        var picture: Picture_? = null
    }
    class HotspotId {
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
        @SerializedName("location")
        @Expose
        var location: List<Double>? = null
        @SerializedName("registrationDate")
        @Expose
        var registrationDate: Int? = null
        @SerializedName("dummyCountExpiry")
        @Expose
        var dummyCountExpiry: Int? = null
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
        var checkedIn: List<String>? = null
        @SerializedName("eventName")
        @Expose
        var eventName: List<String>? = null
        @SerializedName("events")
        @Expose
        var events: List<String>? = null
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
        @SerializedName("__v")
        @Expose
        var v: Int? = null
    }
    class Picture {
        @SerializedName("thumbnail")
        @Expose
        var thumbnail: String? = null
        @SerializedName("original")
        @Expose
        var original: String? = null
    }
    class CreatedBy {
        @SerializedName("_id")
        @Expose
        var id: String? = null
        @SerializedName("password")
        @Expose
        var password: String? = null
        @SerializedName("timeZone")
        @Expose
        var timeZone: Int? = null
        @SerializedName("profilePicURL")
        @Expose
        var profilePicURL: ProfilePicURL? = null
        @SerializedName("registrationDate")
        @Expose
        var registrationDate: String? = null
        @SerializedName("allFavouritesOfUsers")
        @Expose
        var allFavouritesOfUsers: List<String>? = null
        @SerializedName("maxFavourites")
        @Expose
        var maxFavourites: Int? = null
        @SerializedName("notifications")
        @Expose
        var notifications: List<String>? = null
        @SerializedName("blockedContacts")
        @Expose
        var blockedContacts: List<Any>? = null
        @SerializedName("blockedBy")
        @Expose
        var blockedBy: List<Any>? = null
        @SerializedName("activeChats")
        @Expose
        var activeChats: List<String>? = null
        @SerializedName("feeds")
        @Expose
        var feeds: List<String>? = null
        @SerializedName("loves")
        @Expose
        var loves: Int? = null
        @SerializedName("radius")
        @Expose
        var radius: Int? = null
        @SerializedName("facebookId")
        @Expose
        var facebookId: Any? = null
        @SerializedName("feedsOn")
        @Expose
        var feedsOn: Boolean? = null
        @SerializedName("isContactVerified")
        @Expose
        var isContactVerified: Boolean? = null
        @SerializedName("isAdminCreated")
        @Expose
        var isAdminCreated: Boolean? = null
        @SerializedName("fromFacebook")
        @Expose
        var fromFacebook: Boolean? = null
        @SerializedName("deviceId")
        @Expose
        var deviceId: String? = null
        @SerializedName("deviceType")
        @Expose
        var deviceType: String? = null
        @SerializedName("bio")
        @Expose
        var bio: String? = null
        @SerializedName("blocked")
        @Expose
        var blocked: Boolean? = null
        @SerializedName("deleted")
        @Expose
        var deleted: Boolean? = null
        @SerializedName("code")
        @Expose
        var code: Any? = null
        @SerializedName("contact")
        @Expose
        var contact: String? = null
        @SerializedName("email")
        @Expose
        var email: String? = null
        @SerializedName("name")
        @Expose
        var name: String? = null
        @SerializedName("__v")
        @Expose
        var v: Int? = null
        @SerializedName("accessToken")
        @Expose
        var accessToken: String? = null
        @SerializedName("location")
        @Expose
        var location: List<Double>? = null
    }
    class Picture_ {
        @SerializedName("thumbnail")
        @Expose
        var thumbnail: String? = null
        @SerializedName("original")
        @Expose
        var original: String? = null
    }

    class ProfilePicURL {
        @SerializedName("thumbnail")
        @Expose
        var thumbnail: String? = null
        @SerializedName("original")
        @Expose
        var original: String? = null
    }
}