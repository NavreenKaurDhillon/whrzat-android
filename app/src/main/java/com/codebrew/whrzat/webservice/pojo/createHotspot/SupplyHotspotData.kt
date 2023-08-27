package com.codebrew.whrzat.webservice.pojo.createHotspot

import java.io.File


data class SupplyHotspotData(val hotspotName: String,
                             val tags: String,
                             val description: String,
                             val image: File? = null,
                             val createdBy:String,
                             val lat: String,
                             val lng: String,
                             val area:String)
