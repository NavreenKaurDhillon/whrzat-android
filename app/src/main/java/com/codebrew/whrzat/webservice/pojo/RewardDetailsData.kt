package com.codebrew.whrzat.webservice.pojo


class RewardDetailsData {

    var data: Data? = null

    var message: String? = null

    var statusCode: String? = null


    override fun toString(): String {
        return "ClassPojo [data = $data, message = $message, statusCode = $statusCode]"
    }
    class Data {
        var finalResponse: FinalResponse? = null

        override fun toString(): String {
            return "ClassPojo [finalResponse = $finalResponse]"
        }
    }
    class FinalResponse {
        var locationStatus: Boolean? = null
        var referralCode: String? = null
        var rewardAmount: String? = null
        var rewardStatus:Boolean? = null
        var rewards: ArrayList<Rewards>?=null
        var isRedeem:Boolean? = null
        var redeemRequest:Boolean? = null

        override fun toString(): String {
            return "ClassPojo [locationStatus = $locationStatus, referralCode = $referralCode, rewardAmount = $rewardAmount, rewardStatus = $rewardStatus, rewards = $rewards]"
        }
    }

    class Rewards {
        var area: String? = null
        var createdAt: String? = null
        var userId: String? = null
        private var __v: String? = null
        var location: Array<String>?=null
        private var _id: String? = null
        var postId: String? = null
        var type: String? = null
        var paymentStatus: String? = null

        override fun toString(): String {
            return "ClassPojo [area = " + area + ", createdAt = " + createdAt + ", UserId = " + userId + ", __v = " + __v + ", location = " + location + ", _id = " + _id + ", postId = " + postId + ", type = " + type + ", paymentStatus = " + paymentStatus + "]"
        }
    }

}