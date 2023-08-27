package com.codebrew.tagstrade.adapter

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.webservice.pojo.chat.ChatAllUser
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatAllAdapter(private var context: Context) : RecyclerView.Adapter<ChatAllAdapter.AddAlertViewHolder>() {
    private  val TAG = "ChatAllAdapter"

    private lateinit var userClick: UserClick
    private var chatAlluserList = ArrayList<ChatAllUser>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddAlertViewHolder {
        var view:View= LayoutInflater.from(context).inflate(R.layout.rv_items_all_chats_new, parent, false)
        return AddAlertViewHolder(view)
    }
    override fun getItemCount(): Int = chatAlluserList.size

    override fun onBindViewHolder(holder: AddAlertViewHolder, position: Int) {
        holder?.bindItems(chatAlluserList[position])
    }

    fun setListener(userClick: UserClick) {
        this.userClick = userClick
    }

    inner class AddAlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.findViewById<RelativeLayout>(R.id.clParent).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.ivDelete).setOnClickListener(this)
        }


        override fun onClick(view: View?) {
            Log.d(TAG, "onClick: ")
            when (view?.id) {
                R.id.clParent -> {
                    Log.d(TAG, "onClick: R.id.clParent ")

                    if (chatAlluserList[adapterPosition].message.senderId._id.equals(Prefs.with(context).getString(Constants.USER_ID, ""))) {
                        userClick.userClick(
                                chatAlluserList[adapterPosition].message.receiverId._id,
                                chatAlluserList[adapterPosition].message.receiverId.profilePicURL.original,
                                chatAlluserList[adapterPosition].message.receiverId.name, itemView.findViewById(R.id.ivPic),
                                itemView.findViewById(R.id.tvName))
                    } else {
                        userClick.userClick(
                                chatAlluserList[adapterPosition].message.senderId._id,
                                chatAlluserList[adapterPosition].message.senderId.profilePicURL.original,
                                chatAlluserList[adapterPosition].message.senderId.name, itemView.findViewById(R.id.ivPic),
                                itemView.findViewById(R.id.tvName))
                    }

                }
                R.id.ivDelete -> {
                    Log.d(TAG, "onClick: R.id.ivDelete")
                    if (chatAlluserList[adapterPosition].message.senderId._id.equals
                            (Prefs.with(context).getString(Constants.USER_ID, ""))) {

                        userClick.onDelete(chatAlluserList[adapterPosition].message.receiverId._id)
                    } else {
                        userClick.onDelete(chatAlluserList[adapterPosition].message.senderId._id)
                    }
                    chatAlluserList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                }
            }
        }


        fun bindItems(chatAllUser: ChatAllUser) {
            val face_semi = Typeface.createFromAsset(context.assets, "fonts/opensans_semibold.ttf")
            val face_regular = Typeface.createFromAsset(context.assets, "fonts/opensans_regular.ttf")

            itemView.findViewById<TextView>(R.id.tvName).setTypeface(face_semi)
            itemView.findViewById<TextView>(R.id.tvLastMessage).setTypeface(face_regular)

            val mode = context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    itemView.findViewById<TextView>(R.id.tvName).setTextColor(Color.WHITE)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    itemView.findViewById<TextView>(R.id.tvName).setTextColor(Color.parseColor("#000000"))
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }


            val req = RequestOptions()
                    .placeholder(R.drawable.profile_avatar_placeholder_large)
            if (chatAllUser.message.senderId._id.equals(Prefs.with(context).getString(Constants.USER_ID, ""))) {
                itemView.findViewById<TextView>(R.id.tvName).text = chatAllUser.message.receiverId.name
                Glide.with(context)
//                    .load(chatAllUser.pro.profilePicURL.original)
                        .load(chatAllUser.message.receiverId.profilePicURL.original)
                        .apply(req)
                        .into(itemView.findViewById(R.id.ivPic))
            } else {

                Glide.with(context)
                        .load(chatAllUser.message.senderId.profilePicURL.original)
                        .apply(req)
                        .into(itemView.findViewById(R.id.ivPic))
                itemView.findViewById<TextView>(R.id.tvName).text = chatAllUser.message.senderId.name

            }
            itemView.findViewById<TextView>(R.id.tvTime).text = DateUtils.getRelativeTimeSpanString(chatAllUser.message.timeStamp.toLong(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)

            val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS")
            val oldDate: Date = formatter.parse(getDate(chatAllUser.message.timeStamp.toLong()))
            val sysDate: Date = formatter.parse(getDate(System.currentTimeMillis()))

          //  itemView.tvTime.text = timeCalCulate(oldDate,sysDate).toString()


            itemView.findViewById<TextView>(R.id.tvLastMessage).text = chatAllUser.message.message
            if (chatAllUser.unread == 0) {
                itemView.findViewById<TextView>(R.id.tvChatCount).visibility = View.GONE
            } else {
                itemView.findViewById<TextView>(R.id.tvChatCount).visibility = View.VISIBLE
            }
            itemView.findViewById<TextView>(R.id.tvChatCount).text = chatAllUser.unread.toString()

        }

    }

    fun getDate(milliSeconds: Long): String? { // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS")
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(milliSeconds)
        return formatter.format(calendar.getTime())
    }

    fun timeCalCulate(date1: Date, date2: Date):String {
        val mills: Long = date1.time - date2.time
        val hours: Int = (mills / (1000 * 60 * 60)).toInt()
        val mins = (mills / (1000 * 60) % 60).toInt()
//
        val diffDays: Int = (mills / (24 * 60 * 60 * 1000)).toInt()
        val Secs = ((mills / 1000).toInt() % 60).toInt()
        val diff = "$hours:$mins"
        if(diffDays!=0){
            if(diffDays==1){
                return diffDays.toString()+" Day"
            }else{

                return diffDays.toString()+" Days"
            }
        }
        if(hours!=0){
            if(hours==1){
                return hours.toString()+" hour"
            }else{
                return hours.toString()+" hours"
            }
        }
        if(mins!=0){
                return hours.toString()+" min"

        }
        if(Secs!=0){
                 return hours.toString()+" sec"

        }
    return "Just Now"
    }

    interface UserClick {
        fun userClick(id: String, imageUrl: String, name: String, ivProfilePic: ImageView, tvName: TextView)
        fun onDelete(id: String)
    }

    fun addList(data: List<ChatAllUser>) {
        chatAlluserList.clear()
        chatAlluserList.addAll(data)
        notifyDataSetChanged()
    }

    fun clearList() {
        chatAlluserList.clear()
        notifyDataSetChanged()

    }



}