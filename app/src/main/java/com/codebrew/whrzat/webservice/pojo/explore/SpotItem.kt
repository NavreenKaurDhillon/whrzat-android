package com.codebrew.whrzat.webservice.pojo.explore


abstract class SpotItem {
    companion object {
        const val TYPE_SPOT = 0
        const val TYPE_LOADING = 1

    }
    abstract fun getViewType(): Int
}