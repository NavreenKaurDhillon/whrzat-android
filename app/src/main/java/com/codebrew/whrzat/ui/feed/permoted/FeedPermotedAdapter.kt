package com.codebrew.whrzat.ui.feed.permoted

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.ui.feed.event_detail.EventDetailActivity
import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData

class FeedPermotedAdapter(private var mContext: Context,val happeningList: ArrayList<HappeningListData.ImageDatum>) : RecyclerView.Adapter<FeedPermotedAdapter.FeedPermotedViewHolder>() {


    override fun getItemCount(): Int {
       return happeningList.size
    }


    class FeedPermotedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    }
    fun addList(list:  ArrayList<HappeningListData.ImageDatum>) {
        //exploreList.clear()
        happeningList.addAll(list)
        notifyDataSetChanged()
    }

    fun remove(size: Int) {
        if(size!=-1){
            happeningList.removeAt(size)
        }
    }
    fun addListWithClear(list: ArrayList<HappeningListData.ImageDatum>) {
        happeningList.clear()
        happeningList.addAll(list)
        notifyDataSetChanged()
    }


    fun clearList() {
        happeningList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedPermotedViewHolder {
        var view:View=LayoutInflater.from(mContext).inflate(R.layout.rv_items_feed_permoted, parent, false)
        return FeedPermotedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedPermotedViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.tvEvent_name).text=happeningList.get(position).hotspotId?.name
        holder.itemView.findViewById<TextView>(R.id.tvEvent_location).text=happeningList.get(position).hotspotId?.area

//        holder?.itemView?.ivSpot?.setOnClickListener {
//            EventDetailActivity.start(mContext)
//        }

        val req = RequestOptions()
                .transform(RoundedCorners(mContext.resources.getDimensionPixelOffset(R.dimen.dp_16)))
                .placeholder(R.drawable.feed_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

        Glide.with(mContext)
                .load(happeningList.get(position).hotspotId?.picture?.original)
                .apply(req)
                .into(holder.itemView.findViewById(R.id.ivSpot))
    }
}