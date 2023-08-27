package com.codebrew.whrzat.webservice.pojo.explore

import com.codebrew.whrzat.webservice.pojo.PicURL


data class Hotspot(var _id: String,
                   var createdBy: String,
                   var description: String,
                   var location: List<Double>,
                   var registrationDate: Long,
                   var checkedIn: List<String>,
                   var events: List<String>,
                   var tags: List<String>,
                   var name: String,
                   var __v: Long,
                   var hotness: String,
                   var picture: PicURL):SpotItem() {
    override fun getViewType(): Int=SpotItem.TYPE_SPOT
}


/*
var type: Int=ExploreAdapter.SPOTLOADING
var _id: String?=null
var createdBy: String?=null
var description: String?=null
var location: List<Double>?=null
var registrationDate: Long?=null
var checkedIn: List<String>?=null
var events: List<String>?=null
var tags: List<String>?=null
var name: String?=null
var __v: Long?=null
var hotness: String?=null
var picture: PicURL?=null
*/
