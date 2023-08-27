package com.codebrew.whrzat.util

import android.os.Environment

class Constants {

    companion object {
        var VISITED_HOME ="visited_home"
        var LAT = "lat"
        var LNG = "lng"
        val ACCESS_TOKEN = "access_token"
        //val BASE_URL = "http://ec2-52-35-123-233.us-west-2.compute.amazonaws.com:8001/"
        val BASE_URL = "https://whrzat.com:8001/"
//        val BASE_URL = "http://192.168.1.18:8001/"
        val DEV_URL = "http://52.35.123.233:8000/"
        val TEST_URL = "http://52.35.123.233:8001/"
        val LOGIN_DATA = "login_data"
        val LOGIN_STATUS = "login_success"
        val FB_ID = "fb_id"
        val FIRST_NAME = "first_name"
        val LAST_NAME = "last_name"
        val PIC = "pic"
        val EMAIL = "email"
        val FACEBOOK_ID = "FB_ID"
        val HOTSPOT_ID = "hotspot_id"
        val REQ_CODE_SPOT_CREATED = 1
        val BLUE = "blue"
        val RED = "red"
        val ORANGE = "orange"
        val YELLOW = "yellow"
        val USER_ID = "uid"
        val TITLE = "title"
        val CREATED_BY = "create_by"
        val REQ_CODE_EVENT_CREATED = 240
        val IS_CHECK_IN = "checkIn"
        val IS_CHECK = "check"
        val OTHER_USER_ID = "other_id"
        val COUNTRY_NAME = "countryname"
         val COUNTRY_CODE = "countrycode"
        val CODE = "code"
        val RADIUS = "radius"
        val ISFEEDONBOOL = "feedBool"
        val IS_INFINITY = "finit"
        val LOCAL_STORAGE_BASE_PATH_FOR_IMAGES =
                Environment.getExternalStorageDirectory().toString() + "/WhrzAt" + "/Images"
        val COLOR = "color"
        val SPOT_NAME = "spotname"
        val SPOT_DESCRIPTION= "spotdescription"
        val SPOT_ADDRESS= "spotaddress"
        val EVENT_TIME= "eventtime"
        val SPOT_WEBSITE= "spotwebsite"
        val REQ_CODE_ADD_EVENT = 52
        val REQ_CODE_EVENT_COUNT = 62
        val NOTIFICATION_0 = "0"
        val NOTIFICATION_1 = "1"
        val NOTIFICATION_2 = "2"
        val NOTIFICATION_3 = "3"
        val NOTIFICATION_4 = "4"
        val NOTIFICATION_5 = "5"
        val NOTIFICATION_TYPE = "type"
        val ID = "Id"
        val CHAT = "0"
        val TERMS_LINK = "terms"
        val FACEBOOK_LOGIN = "FB_LOGIN"
        val RECEIVER = "receiverId"
        val SENDER = "senderId"
        val NOTIFICATION = "2"
        val APK_VERSION = "apk_version"
        val BLOCK_USER = "blockuser"
        val SPOT_RADIUS=-1
        val Referralcode = "referralcode"
    }
}
