package com.codebrew.whrzat.webservice.pojo

import java.io.File

data class EditData(var image:File?,
                    var userId:String,
                    var name:String,
                    var contact:String,
                    var password:String,
                    var bio:String)
