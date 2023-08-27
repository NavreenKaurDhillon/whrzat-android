package com.codebrew.whrzat.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.VerticalImageSpan
import com.codebrew.whrzat.webservice.pojo.notifications.NotificationData
import com.google.gson.Gson
import com.makeramen.roundedimageview.RoundedImageView

class NotificationAdapter(private var context: Context) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    private var notificaitonList = ArrayList<NotificationData>()
    private lateinit var onNotification: OnNotificationClick
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bindItems(notificaitonList[position], context)
    }


    override fun getItemCount(): Int = notificaitonList.size

    fun addList(data: List<NotificationData>) {
        notificaitonList.clear()
        notificaitonList.addAll(data)
        notifyDataSetChanged()

    }

    fun setListener(onNotification: OnNotificationClick) {
        this.onNotification = onNotification
    }

    inner class NotificationViewHolder(itemview: View, context: Context) : RecyclerView.ViewHolder(itemview), View.OnClickListener {


        init {
            itemView.findViewById<RoundedImageView>(R.id.ivProfilePic).setOnClickListener(this)
            itemview.setOnClickListener(this)
            /* if (!notificaitonList[adapterPosition].hotspotId._id.isEmpty()) {
                 val intent = Intent(context, DetailActivity::class.java)
                 intent.putExtra(Constants.HOTSPOT_ID, notificaitonList[adapterPosition].hotspotId._id)
                 context.startActivity(intent)

             } */

        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.ivProfilePic -> {
                    if (notificaitonList[adapterPosition].userId._id.isNotEmpty() && notificaitonList[adapterPosition].likedBy!=null) {
                        onNotification.onProfilePic(notificaitonList[adapterPosition].likedBy._id,
                                notificaitonList[adapterPosition].likedBy.profilePicURL!!.original, itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                    }

                }
                else -> {
                    if (notificaitonList[adapterPosition].hotspotId._id.isNotEmpty()) {
                        onNotification.spotDetail(notificaitonList[adapterPosition].hotspotId._id,notificaitonList[adapterPosition].hotspotId.deleted)
                    }
                }
            }
        }

        @SuppressLint("CheckResult")
        fun bindItems(notification: NotificationData, context: Context) {
          // Log.e("notifaction list===", (notification))
            val face_regular = Typeface.createFromAsset(context.assets, "fonts/opensans_regular.ttf")
            val face_semi = Typeface.createFromAsset(context.assets, "fonts/opensans_semibold.ttf")
            itemView.findViewById<TextView>(R.id.tvName).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvSpot).setTypeface(face_regular)

              val mode =context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                itemView.findViewById<TextView>(R.id.tvName).setTextColor(Color.WHITE)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                itemView.findViewById<TextView>(R.id.tvName).setTextColor(Color.BLACK)
            }
        }
            val reqs = RequestOptions()
                    .placeholder(R.drawable.profile_avatar_placeholder_large)

            when(notification.event){
                "0"->{
                    if(notification.likedBy!=null){

                        Glide.with(context)
                                .load(notification.likedBy.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                        itemView.findViewById<TextView>(R.id.tvName).setTextColor(Color.BLACK)
                        itemView.findViewById<TextView>(R.id.tvName).text = notification.likedBy.name.plus(" ").plus(context.getString(R.string.sent_you_a_message))
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                      //  itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.VISIBLE
                    }
                }
                "1"->{
                    Glide.with(context)
                            .load(notification.userId.profilePicURL!!.original)
                            .apply(reqs)
                            .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                    itemView.findViewById<TextView>(R.id.tvName).setTextColor(Color.BLACK)
                    itemView.findViewById<TextView>(R.id.tvName).text ="Since the "+notification.hotspotId.name+" "+context.getString(R.string.daily_notification)
                    itemView.findViewById<TextView>(R.id.tvMessage).text=""
                    itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                }
                "2"->{
                    if(notification.likedBy!=null){
                        Glide.with(context)
                                .load(notification.likedBy.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))

                       //itemView.findViewById<TextView>(R.id.tvName).text=Html.fromHtml("<b><font color=\"#000000\">"+notification.likedBy.name+"</font></b>"+" "+context.getString(R.string.label_loves_pic))
                        //itemView.findViewById<TextView>(R.id.tvName).text = notification.likedBy.name.plus(" ").plus(context.getString(R.string.label_loves_pic))
                            when (notification.hotness) {
                                Constants.BLUE -> {
                                    itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.label_loves_pic)+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"))
                                    itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                            context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                            context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                                }
                                Constants.RED -> {
                                    itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.label_loves_pic)+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"))
                                    itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                            context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                            context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                                }
                                Constants.YELLOW -> {
                                    itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.label_loves_pic)+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"))
                                    itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                            context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                            context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                                }
                                else -> {
                                    itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.label_loves_pic)+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"))
                                    itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                            context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                            context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                                }
                            }
                        itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    }
                }
                "3"->{
                    if(notification.likedBy!=null){
                        Glide.with(context)
                                .load(notification.likedBy.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                        itemView.findViewById<TextView>(R.id.tvName).setTextColor(Color.BLACK)
                        itemView.findViewById<TextView>(R.id.tvName).text = "Your picture posted to "+notification.hotspotId.name+"is very popular."
                        itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    }
                }
                "4"->{
                    if(notification.hotspotId!=null){
                        Glide.with(context)
                                .load(notification.likedBy.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                       // itemView.findViewById<TextView>(R.id.tvName).text=Html.fromHtml("<b><font color=\"#000000\">"+notification.likedBy.name+"</font></b>"+" "+context.getString(R.string.posted_a_picture_to))
                         // itemView.findViewById<TextView>(R.id.tvName).text = notification.likedBy.name.plus(" ").plus(context.getString(R.string.posted_a_picture_to))
                        when (notification.hotness) {
                            Constants.BLUE -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.posted_a_picture_to)+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.RED -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.posted_a_picture_to)+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                               }
                            Constants.YELLOW -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.posted_a_picture_to)+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            else -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.posted_a_picture_to)+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                        }
                        itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    }
                }
                "5"->{
                    if(notification.userId!=null){
                        Glide.with(context)
                                .load(notification.userId.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                        if (notification.deleted==true){
                         //   itemView.findViewById<TextView>(R.id.tvName).text=Html.fromHtml("<b><font color=\"#000000\">"+"Whrzat"+"</font></b>"+" "+" put "+notification.hotspotId.name+" "+context.getString(R.string.on_the_map))
                             itemView.findViewById<TextView>(R.id.tvName).text = "Whrzat".plus(" put ").plus(notification.hotspotId.name).plus(" ").plus(context.getString(R.string.on_the_map))
                        }else{
                            //itemView.findViewById<TextView>(R.id.tvName).text=Html.fromHtml("<b><font color=\"#000000\">"+notification.userId.name+"</font></b>"+" "+" put "+notification.hotspotId.name+" "+context.getString(R.string.on_the_map))
                            itemView.findViewById<TextView>(R.id.tvName).text = notification.userId.name.plus(" put ").plus(notification.hotspotId.name).plus(" ").plus(context.getString(R.string.on_the_map))
                        }

                        itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    }
                }

                "6"->{
                    if(notification.likedBy!=null){
                        Glide.with(context)
                                .load(notification.likedBy.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                       // itemView.findViewById<TextView>(R.id.tvName).text=Html.fromHtml("<b><font color=\"#000000\">"+notification.likedBy.name+"</font></b>"+context.getString(R.string.cheked_out_your_hotspot))
                         //itemView.findViewById<TextView>(R.id.tvName).text = notification.likedBy.name.plus(" ").plus(context.getString(R.string.cheked_out_your_hotspot))
                        when (notification.hotness) {
                            Constants.BLUE -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.cheked_out_your_hotspot)+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.RED -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.cheked_out_your_hotspot)+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.YELLOW -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.cheked_out_your_hotspot)+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            else -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.cheked_out_your_hotspot)+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                        }


                        itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    }
                }
                "7"->{
                    if(notification.likedBy!=null){
                        Glide.with(context)
                                .load(notification.likedBy.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                       /* itemView.findViewById<TextView>(R.id.tvName).text = notification.likedBy.name.plus(" ").plus(notification.hotspotId.name)
                                .plus(" ").plus(context.getString(R.string.on_the_map))*/
                        when (notification.hotness) {
                            Constants.BLUE -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"+" "+context.getString(R.string.on_the_map)))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.RED -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"+" "+context.getString(R.string.on_the_map)))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.YELLOW -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"+" "+context.getString(R.string.on_the_map)))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            else -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"+" "+context.getString(R.string.on_the_map)))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                        }


                        itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    }
                }
                "8"->{

                }
                "9"->{
                    if(notification.likedBy!=null){
                        Glide.with(context)
                                .load(notification.likedBy.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                        //itemView.findViewById<TextView>(R.id.tvName).text=Html.fromHtml("<b><font color=\"#000000\">"+notification.likedBy.name+"</font></b>"+context.getString(R.string.added_an_event_to_your_favourite))
                        //itemView.findViewById<TextView>(R.id.tvName).text = notification.likedBy.name.plus(" ").plus(context.getString(R.string.added_an_event_to_your_favourite))
                        when (notification.hotness) {
                            Constants.BLUE -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.added_an_event_to_your_favourite)+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.RED -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.added_an_event_to_your_favourite)+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.YELLOW -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.added_an_event_to_your_favourite)+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            else -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.added_an_event_to_your_favourite)+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                        }
                        itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    }

                }
                "10"->{

                }
                "11"->{
                    if(notification.likedBy!=null){
                        Glide.with(context)
                                .load(notification.likedBy.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                        //itemView.findViewById<TextView>(R.id.tvName).text=Html.fromHtml(context.getString(R.string.added_an_event_to_your_favourite)+" "+"<b><font color=\"#000000\">"+notification.likedBy.name+"</font></b>")
                       // itemView.findViewById<TextView>(R.id.tvName).text = context.getString(R.string.hotspot_has_been_favourite_by).plus(" ").plus(notification.likedBy.name)
                        when (notification.hotness) {
                            Constants.BLUE -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.hotspot_has_been_favourite_by)+" "+notification.likedBy.name+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.RED -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.hotspot_has_been_favourite_by)+" "+notification.likedBy.name+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.YELLOW -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.hotspot_has_been_favourite_by)+" "+notification.likedBy.name+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            else -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.hotspot_has_been_favourite_by)+" "+notification.likedBy.name+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                        }

                        itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    }
                }
                "12"->{
                    if(notification.likedBy!=null){
                        Glide.with(context)
                                .load(notification.likedBy.profilePicURL!!.original)
                                .apply(reqs)
                                .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                        //itemView.findViewById<TextView>(R.id.tvName).text=Html.fromHtml("<b><font color=\"#000000\">"+notification.likedBy.name+"</font></b>"+context.getString(R.string.label_loves_pic)+" "+notification.userId.name)
                        //itemView.findViewById<TextView>(R.id.tvName).text = notification.likedBy.name.plus(" ").plus(context.getString(R.string.label_loves_pic).plus(notification.userId.name))
                        when (notification.hotness) {
                            Constants.BLUE -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.label_loves_pic)+" "+notification.userId.name+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.RED -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.label_loves_pic)+" "+notification.userId.name+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            Constants.YELLOW -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.label_loves_pic)+" "+notification.userId.name+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                            else -> {
                                itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(notification.likedBy.name+" "+context.getString(R.string.label_loves_pic)+" "+notification.userId.name+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"))
                                itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                        context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                            }
                        }
                        itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                        itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    }
                }
                "13"->{
                    Glide.with(context)
                            .load(notification.userId.profilePicURL!!.original)
                            .apply(reqs)
                            .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                   // itemView.findViewById<TextView>(R.id.tvName).text = context.getString(R.string.you_have_earned_a_reward_for_your__reference)
                    when (notification.hotness) {
                        Constants.BLUE -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_earned_a_reward_for_your__reference)+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                        Constants.RED -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_earned_a_reward_for_your__reference)+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                        Constants.YELLOW -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_earned_a_reward_for_your__reference)+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                        else -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_earned_a_reward_for_your__reference)+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                    }

                    itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                }
                "14"->{
                    Glide.with(context)
                            .load(notification.userId.profilePicURL!!.original)
                            .apply(reqs)
                            .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                   // itemView.findViewById<TextView>(R.id.tvName).text =context.getString(R.string.you_have_earned_a_reward)
                    when (notification.hotness) {
                        Constants.BLUE -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_earned_a_reward)+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                        Constants.RED -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_earned_a_reward)+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                        Constants.YELLOW -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_earned_a_reward)+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                        else -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_earned_a_reward)+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                    }

                    itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                }
                "15"->{
                    Glide.with(context)
                            .load(notification.userId.profilePicURL!!.original)
                            .apply(reqs)
                            .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                  //  itemView.findViewById<TextView>(R.id.tvName).text =context.getString(R.string.you_have_got_a_reward_for_posting_first_picture)
                    when (notification.hotness) {
                        Constants.BLUE -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_got_a_reward_for_posting_first_picture)+" [email-icon] "+"<font color=#5FABFF>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                        Constants.RED -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_got_a_reward_for_posting_first_picture)+" [email-icon] "+"<font color=#DB4437>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                        Constants.YELLOW -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_got_a_reward_for_posting_first_picture)+" [email-icon] "+"<font color=#fdd835>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                        else -> {
                            itemView.findViewById<TextView>(R.id.tvName).setText(Html.fromHtml(context.getString(R.string.you_have_got_a_reward_for_posting_first_picture)+" [email-icon] "+"<font color=#FB7200>"+notification.hotspotId.name+"</font>"))
                            itemView.findViewById<TextView>(R.id.tvName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                    context.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        }
                    }

                    itemView.findViewById<TextView>(R.id.tvMessage).text=" "
                    itemView.findViewById<TextView>(R.id.tvSpot).visibility=View.GONE
                }

            }


            val req = RequestOptions()
                    .placeholder(R.drawable.feed_placeholder)

          if(notification.imageId!=null){
              Glide.with(context)
                      .load(notification.imageId.picture.original)
                      .apply(req)
                      .into(itemView.findViewById<ImageView>(R.id.ivSpot))
          }else{
              if(notification.hotspotId.picture!=null){
                  Glide.with(context)
                          .load(notification.hotspotId.picture!!.original)
                          .apply(req)
                          .into(itemView.findViewById<ImageView>(R.id.ivSpot))
              }
          }

            when (notification.hotness) {
                Constants.BLUE -> {
                    setHotspotName(notification.hotspotId.name, R.color.blueChill, R.drawable.flame_blue_listing_icon,notification)
                }
                Constants.RED -> {
                    setHotspotName(notification.hotspotId.name, R.color.redVeryPopular, R.drawable.flame_red_listing_icon,notification)
                }
                Constants.YELLOW -> {
                    setHotspotName(notification.hotspotId.name, R.color.yellow, R.drawable.flame_yellow_listing_icon,notification)
                }
                else -> {
                    setHotspotName(notification.hotspotId.name, R.color.orangePopular, R.drawable.flame_orange_listing_icon,notification)
                }
            }
        }

        fun TextView.addImage(atText: String, @DrawableRes imgSrc: Int, imgWidth: Int, imgHeight: Int) {
            val ssb = SpannableStringBuilder(this.text)
            val drawable = ContextCompat.getDrawable(this.context, imgSrc) ?: return
            drawable.mutate()
            drawable.setBounds(0, 0, imgWidth, imgHeight)
            val start = text.indexOf(atText)
            ssb.setSpan(VerticalImageSpan(drawable), start, start + atText.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            this.setText(ssb, TextView.BufferType.SPANNABLE)
        }

        private fun setHotspotName(name: String, color: Int, icon: Int, notification: NotificationData) {
            itemView.findViewById<TextView>(R.id.tvSpot).setTextColor(ContextCompat.getColor(context, color))
            itemView.findViewById<TextView>(R.id.tvSpot).text = name
            val dr = context.resources.getDrawable(icon)
            val bitmap = (dr as BitmapDrawable).bitmap
            val d: Drawable = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(bitmap, 48, 48, true))
            itemView.findViewById<TextView>(R.id.tvSpot).setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

        }

    }

    interface OnNotificationClick {
        fun onProfilePic(id: String, imageUr: String, ivProfilePic: ImageView)
        fun spotDetail(id: String, deleted: Boolean)
    }

    fun clearList() {
        notificaitonList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        var  view:View=LayoutInflater.from(context).inflate(R.layout.rv_notification, parent, false)
        return NotificationViewHolder(view,context)
    }

}