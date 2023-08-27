package com.codebrew.whrzat.webservice.pojo.HotspotDetail

import android.graphics.Picture


class HotSpotDetail(var _id: String,
                    var isCheckedIn:Boolean,
                    var area: String,
                    var createdBy: String,
                    var description: String,
                    var location: List<Double>,
                    var registrationDate: Long,
                    var checkedIn: List<String>,
                    var events: List<String>,
                    var count : Int?,
                    var isFavouriteCount : Int?,
                    var tags: List<String>,
                    var picture: Picture,
                    var name: String,
                    //var __v: Int,
                    var hotness:String,
                    var isFavouriteColor:String,
                    var isFavourite:Boolean,
                    val images:List<ImagesFeed>)

