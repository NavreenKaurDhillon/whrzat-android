package com.codebrew.whrzat.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.tagstrade.adapter.FeedContactAdapter
import com.codebrew.whrzat.R

class HotSpotDetailActivity : AppCompatActivity() {
    private  val TAG = "HotSpotDetailActivity"
    private lateinit var feedContactAdapter: FeedContactAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_hotspot_detail)
        Log.d(TAG, "onCreate: StartActivity")
        setAdapter()
    }

    private fun setAdapter() {
        feedContactAdapter = FeedContactAdapter(this)
      var recyclerView = findViewById<RecyclerView>(R.id. rvHotspotDetail)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = feedContactAdapter
    }
}
