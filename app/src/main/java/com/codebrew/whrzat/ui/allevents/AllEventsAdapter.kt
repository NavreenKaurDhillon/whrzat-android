package com.codebrew.whrzat.ui.allevents

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.codebrew.whrzat.R
import com.codebrew.whrzat.ui.allevents.AllEventsAdapter.AllEventsViewHolder
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.webservice.pojo.allevents.EventList
import java.util.*

class AllEventsAdapter(var mContext: Context) : RecyclerView.Adapter<AllEventsViewHolder>() {
    private var eventList = ArrayList<EventList>()
    override fun getItemCount(): Int = eventList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllEventsViewHolder {
        var view:View= LayoutInflater.from(mContext).inflate(R.layout.rv_items_events, parent, false)
        return AllEventsViewHolder(view)
    }
    override fun onBindViewHolder(holder: AllEventsViewHolder, position: Int) {
        holder?.bindItems(eventList[position])
    }
    inner class AllEventsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(eventList: EventList) {
            val face_semi = Typeface.createFromAsset(mContext.assets, "fonts/opensans_semibold.ttf")
            val face_regular = Typeface.createFromAsset(mContext.assets, "fonts/opensans_regular.ttf")
            itemView.findViewById<TextView>(R.id.tvEventTitle).setTypeface(face_semi)
            itemView.findViewById<TextView>(R.id.tvEventDescription).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvTime).setTypeface(face_regular)
            val mode = mContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    itemView.findViewById<TextView>(R.id.tvEventTitle).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvEventDescription).setTextColor(Color.WHITE)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    itemView.findViewById<TextView>(R.id.tvEventTitle).setTextColor(Color.BLACK)
                    itemView.findViewById<TextView>(R.id.tvEventDescription).setTextColor(Color.BLACK)
                }
            }
            itemView.findViewById<TextView>(R.id.tvEventTitle).text = eventList.name
            itemView.findViewById<TextView>(R.id.tvEventDescription).text = eventList.description
            val startTime=GeneralMethods.convertDate(eventList.startDate.toDouble().toLong().toString(),"MMM d, h:mm a")
            val endTime=GeneralMethods.convertDate(eventList.endDate.toDouble().toLong().toString(),"MMM d, h:mm a")
            itemView.findViewById<TextView>(R.id.tvTime).text = "$startTime to $endTime"
        }
    }

    fun addList(events: List<EventList>) {
        eventList.clear()
        eventList.addAll(events)
        notifyDataSetChanged()
    }
}