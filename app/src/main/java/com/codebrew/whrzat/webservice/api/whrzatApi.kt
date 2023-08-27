package com.codebrew.whrzat.webservice.api

import com.codebrew.whrzat.webservice.NotificationListing
import com.codebrew.whrzat.webservice.pojo.*
import com.codebrew.whrzat.webservice.pojo.HotspotDetail.DetailData
import com.codebrew.whrzat.webservice.pojo.allevents.EventMain
import com.codebrew.whrzat.webservice.pojo.chat.ChatData
import com.codebrew.whrzat.webservice.pojo.chat.ChatUser
import com.codebrew.whrzat.webservice.pojo.explore.EventListData
import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData
import com.codebrew.whrzat.webservice.pojo.explore.ExploreData
import com.codebrew.whrzat.webservice.pojo.explore.FavoriteListData
import com.codebrew.whrzat.webservice.pojo.feed.Feed
import com.codebrew.whrzat.webservice.pojo.geocoder.GeocoderResponse
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import com.codebrew.whrzat.webservice.pojo.notifications.NotificationMain
import com.codebrew.whrzat.webservice.pojo.otherprofile.ProfileData
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface whrzatApi {

    @FormUrlEncoded()
    @POST("api/customer/customerLogIn")
    fun login(@FieldMap params: Map<String, String>): Call<LoginResponse>

    @FormUrlEncoded()
    @POST("api/customer/verifyOtp")
    fun otp(@FieldMap params: Map<String, String>): Call<LoginResponse>

   /* @FormUrlEncoded()
    @POST("api/customer/facebookIdCheck")
    fun fbLogin(@Field("facebookId") id: String): Call<LoginResponse>*/
     @FormUrlEncoded()
     @POST("api/customer/facebookIdCheck")
     fun fbLogin(@FieldMap params: Map<String, String>): Call<LoginResponse>

   /* {
        "facebookId": "3537276939731932",
        "deviceId": "cCNpKBs-QrScJp23pcZ4cB:APA91bHoORm0w94Rnag8EjsfIm_kVz1OCh1HjVvfWrxQ8Fo6QveFXGo9lzrfvad-oT7AzKltK1ylv1tGkRVV4AroDciX6D6ZhDB5Ubi1RB1PefpYLvmtl8O3ExHtkwUM9PgcICWgLHAd",
        "deviceType": "ANDROID",
        "timeZone": "19800"
    }*/

    @Multipart
    @POST("api/customer/customerSignUp")
    fun signup(@PartMap params: Map<String, @JvmSuppressWildcards RequestBody>, @Part("fromFacebook") fb: Boolean): Call<LoginResponse>


    @Multipart
    @POST("/api/customer/createHotspot")
    fun createHotspot(@PartMap params: Map<String, @JvmSuppressWildcards RequestBody>): Call<FetchData>

    @GET("https://maps.googleapis.com/maps/api/geocode/json")
    fun geoCoderApi(@Query("latlng") latLng: String, @Query("key") key: String): Call<GeocoderResponse>

    // latlng="+latlong+"sensor=true&key=AIzaSyDU3Ngeye4rHP_uvgXjt2jhGzpIhU6URXg")

    @FormUrlEncoded
    @POST("/api/customer/hotspotMapView")
    fun apiMapView(@FieldMap params: Map<String, String>, @Field("radius") radius: Int,
                   @Field("limit") limit: Int,@Field("skip") skip: Int): Call<ExploreData>

    @FormUrlEncoded
    @POST("/api/customer/hotspotDetails")
    fun apiHotspotDetail(@Field("hotspotId") id: String, @Field("userId") userId: String): Call<DetailData>

    @FormUrlEncoded
    @POST("/api/customer/isCheckIn")
    fun apiIsCheckIn(@Field("hotspotId") id: String, @Field("userId") userId: String): Call<DetailData>

    @FormUrlEncoded
    @POST("/api/customer/checkIn")
    fun apiCheckIn(@Field("hotspotId") id: String, @Field("userId") userId: String): Call<DetailData>

    @FormUrlEncoded
    @POST("/api/customer/checkOut")
    fun apiCheckOut(@Field("hotspotId") id: String, @Field("userId") userId: String): Call<DetailData>

    @Multipart
    @POST("/api/customer/addImages")
    fun apiAddImages(@PartMap params: Map<String, @JvmSuppressWildcards RequestBody>): Call<DetailData>

    @FormUrlEncoded
    @POST("/api/customer/createEvent")
    fun apiCreateEvent(@FieldMap params: Map<String, String>): Call<DetailData>

    @FormUrlEncoded
    @POST("/api/customer/getEvents")
    fun apiGetEvents(@Field("hotspotId") id: String, @Field("userId") userId: String): Call<EventMain>

    @FormUrlEncoded
    @POST("/api/customer/resetPassword")
    fun apiForgotPassword(@Field("email") email: String): Call<LoginResponse>


    @FormUrlEncoded
    @POST("/api/customer/likeImage")
    fun apiLikeImage(@Field("userId") userId: String, @Field("imageId") imageId: String, @Field("like") isLike: Boolean): Call<LoginResponse>


    @POST("/api/customer/SyncContacs")
    fun apiSynContacts(@Body params: ApiContacts): Call<LoginResponse>


    @FormUrlEncoded
    @POST("/api/customer/getFeeds")
    fun apiGetFeeds(@Field("customerId") email: String): Call<Feed>

    @FormUrlEncoded
    @POST("/api/customer/reportImage")
    fun apiReport(@Field("imageId") imageId: String,@Field("userId") userId: String): Call<LoginResponse>

    @FormUrlEncoded
    @POST("/api/customer/deletePic")
    fun apiDeletePic(@Field("imageId") imageId: String, @Field("hotspotId") id: String): Call<LoginResponse>

    @POST("/api/customer/logout")
    fun apiLogout(): Call<LoginResponse>

    @Multipart
    @POST("/api/customer/EditProfile")
    fun apiEditProfile(@PartMap params: Map<String,@JvmSuppressWildcards RequestBody>): Call<LoginResponse>

    @FormUrlEncoded
    @POST("/api/customer/blockListAdd")
    fun apiBlockUser(@Field("userId") params: String, @Field("blockContact") blockId: String,
                     @Field("block") isBlock: Boolean): Call<LoginResponse>


    @FormUrlEncoded
    @POST("/api/customer/getProfile")
    fun apiGetProfile(@Field("profileId") blockId: String, @Field("userId") params: String): Call<ProfileData>

    @FormUrlEncoded
    @POST("/api/customer/editFeedRadius")
    fun apiFeedRadius(@Field("userId") userId: String, @Field("radius") radius: Any, @Field("feed") checked: Boolean): Call<LoginResponse>

    @FormUrlEncoded
    @POST("/api/customer/blockListing")
    fun apiBlockList(@Field("userId") params: String): Call<BlockList>

    @FormUrlEncoded
    @POST("/api/customer/chatHistory")
    fun apiChatHistory(@Field("userId") userId: String,
                       @Field("otherUser") otherUserId: String,
                       @Field("skip") skip: Int,
                       @Field("limit") limit: Int): Call<ChatData>

    @FormUrlEncoded
    @POST("/api/customer/messageListing")
    fun apiAllChatuser(@Field("userId") userId: String,
                       @Field("skip") skip: Int,
                       @Field("limit") limit: Int): Call<ChatUser>

    @FormUrlEncoded
    @POST("/api/customer/deleteMessage")
    fun apiDelete(@Field("userId") userId: String,
                  @Field("otherUser") id: String): Call<NotificationMain>


    @FormUrlEncoded
    @POST("/api/customer/getNotification")
    fun apiNotificaiton(@Field("userId") userId: String): Call<NotificationMain>

    @POST("/api/customer/notificationsEdit")
    fun apiEditNotification(@Body notification:ApiNotification): Call<NotificationListing>

    @FormUrlEncoded
    @POST("/api/customer/notificationslisting")
    fun apiNotificationList(@Field("userId") userId: String): Call<NotificationListing>

    @FormUrlEncoded
    @POST("/api/customer/clearNotifications")
    fun clearNotification(@Field("userId") userId: String?):Call<LoginResponse>

    @FormUrlEncoded
    @POST("/api/customer/changePassword")
    fun apiChangePassword(@Field("userId") userId: String,@Field("oldPassword") old: String,@Field("newPassword") new: String?):Call<LoginResponse>



    @FormUrlEncoded
    @POST("/api/customer/favouriteHotspot")
    fun apiFavorite(@Field("hotspotId") id: String,
                    @Field("userId") userId: String,
                    @Field("favourite") status: Boolean ): Call<DetailData>


    @FormUrlEncoded
    @POST("/api/customer/favouriteHotspotListing")
    fun apiGetFavoriteList( @Field("userId") userId: String): Call<FavoriteListData>

    @FormUrlEncoded
    @POST("/api/customer/Happening")
    fun apiGetHappeningList( @Field("skip") skip: Int,
                         @Field("limit") limit: Int,
                         @Field("userId") userId: String,
                             @Field("radius") radius: Int,
                             @Field("latitude") latitude: String,
                             @Field("longitude") longitude: String): Call<HappeningListData>

//    @FormUrlEncoded
//    @POST("/api/customer/Happening")
//    fun apiGetEventList( @Field("skip") skip: Int,
//                         @Field("limit") limit: Int,
//                         @Field("userId") userId: String): Call<EventListData>

    @FormUrlEncoded
    @POST("/api/customer/PromotedEventLisiting")
    fun apiGetEventList(@FieldMap params: Map<String, String>,
                   @Field("limit") limit: Int,@Field("skip") skip: Int, @Field("radius") radius: Int): Call<EventListData>

    @FormUrlEncoded
    @POST("/api/customer/deleteUser")
    fun apiDeleteProfile(@Field("userId") userId: String): Call<LoginResponse>


    @POST("/api/customer/signupWithReferralCode")
    fun signupWithReferralCode(@Header("Authorization") authorization :String,
                                @Body params: Map<String, String>): Call<Referalcodedata>

    @GET("/api/customer/getRewardDetail")
    fun getRewardDetail(@Header("Authorization") authorization :String): Call<RewardDetailsData>

    @GET("/api/admin/getLocation")
    fun getLocation(@Header("Authorization") authorization :String): Call<LocationResposeData>

    @GET("/api/customer/redeemRequest")
    fun redeemRequest(@Header("Authorization") authorization :String): Call<Referalcodedata>
}