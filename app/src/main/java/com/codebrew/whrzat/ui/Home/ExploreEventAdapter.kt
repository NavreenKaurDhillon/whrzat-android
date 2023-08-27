package com.codebrew.whrzat.ui.Home

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.ui.feed.event_detail.EventDetailActivity
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.webservice.pojo.explore.EventListData
import com.makeramen.roundedimageview.RoundedImageView

class ExploreEventAdapter( var mContext: Context, val list: ArrayList<EventListData.Event>) :
        RecyclerView.Adapter<ExploreEventAdapter.ExploreEventViewHolder>() {

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ExploreEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItem(data: EventListData.Event) {
            val face_regular = Typeface.createFromAsset(mContext!!.assets, "fonts/opensans_regular.ttf")
            val face_bold = Typeface.createFromAsset(mContext!!.assets, "fonts/opensans_bold.ttf")
            val face_semi = Typeface.createFromAsset(mContext!!.assets, "fonts/opensans_semibold.ttf")
            itemView.findViewById<TextView>(R.id.tvEvent_name).setTypeface(face_semi)
            itemView.findViewById<TextView>(R.id.tvEvent_location).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvEvent_date).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvEventType).setTypeface(face_bold)

            val mode = mContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    itemView.findViewById<TextView>(R.id.tvEvent_name).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvEvent_location).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvEvent_date).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvEventType).setTextColor(Color.WHITE)
                    itemView.findViewById<RelativeLayout>(R.id.rlmain).setBackgroundResource(R.drawable.rounded_corners_dark_event)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    itemView.findViewById<TextView>(R.id.tvEvent_name).setTextColor(Color.parseColor("#000000"))
                    itemView.findViewById<TextView>(R.id.tvEvent_location).setTextColor(Color.parseColor("#898989"))
                    itemView.findViewById<TextView>(R.id.tvEvent_location).setTextColor(Color.parseColor("#898989"))
                    itemView.findViewById<RelativeLayout>(R.id.rlmain).setBackgroundResource(R.drawable.rounded_corner_white_event)
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                }
            }


            itemView.findViewById<TextView>(R.id.tvEvent_name)?.text = data.name
            itemView.findViewById<TextView>(R.id.tvEvent_location)?.text = data.locationName
            itemView.findViewById<TextView>(R.id.tvEventType)?.text=data.description

            itemView.findViewById<TextView>(R.id.tvEvent_date)?.text=GeneralMethods.convertDate(data.startDate!!,"MMM d, h:mm a")+" to "+ GeneralMethods.convertDate(data.endDate!!,"MMM d, h:mm a")

            itemView.findViewById<RoundedImageView>(R.id.ivSpot)?.setOnClickListener {
                var time= GeneralMethods.convertDate(data.startDate!!,"MMM d, h:mm a")+" to "+ GeneralMethods.convertDate(data.endDate!!,"MMM d, h:mm a")

                EventDetailActivity.start(mContext, data.name!!,data.description!!,
                        data.locationName!!,time,data.picture!!.original!!
                        , data.refund!!)
            }

            val req = RequestOptions()
                    .transform(CenterCrop())
                    .placeholder(R.drawable.feed_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

            Glide.with(mContext)
                    .load(data.picture?.original)
                    .apply(req)
                    .into(itemView.findViewById<RoundedImageView>(R.id.ivSpot))
        }
    }

    fun addList(list: ArrayList<EventListData.Event>) {
        //exploreList.clear()
        list.addAll(list)
        notifyDataSetChanged()
    }

    fun remove(size: Int) {
        if (size != -1) {
            list.removeAt(size)
        }
    }

    fun addListWithClear(list: ArrayList<EventListData.Event>) {
        list.clear()
        list.addAll(list)
        notifyDataSetChanged()
    }


    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreEventViewHolder {
       var view:View= LayoutInflater.from(mContext).inflate(R.layout.rv_items_feed_permoted, parent, false)
        return ExploreEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExploreEventViewHolder, position: Int) {
        holder?.bindItem(list[position])
    }
}