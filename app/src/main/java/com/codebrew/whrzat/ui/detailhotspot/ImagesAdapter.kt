package com.codebrew.tagstrade.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateUtils
import android.text.format.DateUtils.getRelativeTimeSpanString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.Transition
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.webservice.pojo.HotspotDetail.ImagesFeed
import com.makeramen.roundedimageview.RoundedImageView
import com.stfalcon.frescoimageviewer.ImageViewer



class ImagesAdapter(private var mContext: Context) : RecyclerView.Adapter<ImagesAdapter.FeedViewHolder>() {

    private var feedList = ArrayList<ImagesFeed>()
    private lateinit var listener: Love
    private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(2f)

    override fun getItemCount(): Int = feedList.size

    fun setListener(listener: Love) {
        this.listener = listener
    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {


        init {
            itemView.findViewById<TextView>(R.id.tvChat).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.tvReport).setOnClickListener(this)
            itemView.findViewById<ImageView>(R.id.ivSpot).setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.tvLove -> {
                    if (GeneralMethods.isNetworkActive(mContext)) {
                        var likeCount = feedList[adapterPosition].likesCount
                        if (feedList[adapterPosition].isLiked) {
                            if (likeCount != null) {
                                likeCount = --likeCount
                                when {
                                    likeCount < 1 -> {
                                        setImageCount(likeCount, false, feedList[adapterPosition]._id, R.drawable.ic_like_post)
                                    }
                                    likeCount <= 3 -> {
                                        setImageCount(likeCount, false, feedList[adapterPosition]._id, R.drawable.heart_blue_icon)
                                    }
                                    likeCount <= 6 -> {
                                        setImageCount(likeCount, false, feedList[adapterPosition]._id, R.drawable.heart_yellow_icon)
                                    }
                                    likeCount <= 9 -> {
                                        setImageCount(likeCount, false, feedList[adapterPosition]._id, R.drawable.heart_orange_icon)
                                    }
                                    else -> {
                                        setImageCount(likeCount, false, feedList[adapterPosition]._id, R.drawable.heart_red_icon)
                                    }
                                }
                            }
                        } else {
                            if (likeCount != null) {
                                likeCount = ++likeCount
                                when {
                                    likeCount <= 3 -> {
                                        setImageCount(likeCount, true, feedList[adapterPosition]._id, R.drawable.heart_blue_icon)
                                    }
                                    likeCount <= 6 -> {
                                        setImageCount(likeCount, true, feedList[adapterPosition]._id, R.drawable.heart_yellow_icon)
                                    }
                                    likeCount <= 9 -> {
                                        setImageCount(likeCount, true, feedList[adapterPosition]._id, R.drawable.heart_orange_icon)
                                    }
                                    else -> {
                                        setImageCount(likeCount, true, feedList[adapterPosition]._id, R.drawable.heart_red_icon)
                                    }
                                }
                            }
                        }
                    } else {
                        GeneralMethods.showToast(mContext, R.string.error_no_connection)
                    }
                }

                R.id.tvReport -> {
                    if (GeneralMethods.isNetworkActive(mContext)) {
                        if (itemView.findViewById<TextView>(R.id.tvReport).text.toString() == mContext.getString(R.string.label_report)) {

                            confirmReport()
                        } else {
                            confirmDelete()

                        }
                    } else {
                        GeneralMethods.showToast(mContext, R.string.error_no_connection)
                    }
                }

                R.id.tvUserName, R.id.ivProfilePic -> {
                    feedList[adapterPosition].createdBy?._id?.let {
                        feedList[adapterPosition].createdBy?.profilePicURL?.original?.let { it1 ->
                            feedList[adapterPosition].createdBy?.name?.let { it2 ->
                                listener.viewProfile(it, itemView.findViewById(R.id.ivProfilePic)
                                        , itemView.findViewById(R.id.tvUserName), it1, it2)
                            }
                        }
                    }

                }

                R.id.tvChat -> {
                    feedList[adapterPosition].createdBy?._id?.let {
                        feedList[adapterPosition].createdBy?.profilePicURL?.original?.let { it1 ->
                            listener.onChatClick(it, itemView.findViewById(R.id.ivProfilePic),
                                    it1)
                        }
                    }
                }
            }


        }

        private fun confirmReport() {
            AlertDialog.Builder(mContext, R.style.MyDialog)
                    .setTitle(R.string.dialog_do_you_want_to_report_this_item)
                    .setPositiveButton(android.R.string.ok, { dialogInterface, i ->

                        if (GeneralMethods.isNetworkActive(mContext)) {
                            listener.report(feedList[adapterPosition]._id)

                        } else {
                            GeneralMethods.showToast(mContext, R.string.error_no_connection)

                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true)
                    .show()
        }


        private fun confirmDelete() {
            AlertDialog.Builder(mContext, R.style.MyDialog)
                    .setTitle(R.string.dialog_do_you_want_to_delete)
                    .setPositiveButton(android.R.string.ok) { dialogInterface, i ->

                        if (GeneralMethods.isNetworkActive(mContext)) {
                            if (feedList.size != 1) {
                                itemView.findViewById<TextView>(R.id.tvReport).visibility =
                                    View.VISIBLE
                                listener.delete(feedList[adapterPosition]._id)
                                itemView.findViewById<TextView>(R.id.tvReport).visibility =
                                    View.VISIBLE

                                feedList.removeAt(adapterPosition)
                                notifyItemRemoved(adapterPosition)
                                //notifyItemRangeChanged(adapterPosition, itemCount)
                            } else {
                                GeneralMethods.showToast(mContext, R.string.error_delet_last_item)
                                // itemView.findViewById<TextView>(R.id.tvReport).visibility = View.INVISIBLE
                            }
                        } else {
                            GeneralMethods.showToast(mContext, R.string.error_no_connection)

                        }
                    }
                .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true)
                    .show()
        }

        private fun setImageCount(likeCount: Int, isLike: Boolean, imageId: String, heartIcon: Int) {
            feedList[adapterPosition].isLiked = isLike
            feedList[adapterPosition].likesCount = likeCount
            listener.clickLove(imageId, isLike, likeCount.toString(), adapterPosition)

            if(likeCount==1){
                itemView.findViewById<TextView>(R.id.tvLove).text = "$likeCount Like"
            }else if(likeCount==0){
                itemView.findViewById<TextView>(R.id.tvLove).text = "$likeCount Likes"
            }else{
                itemView.findViewById<TextView>(R.id.tvLove).text = "$likeCount Likes"
            }


            //itemView.tvLove.setCompoundDrawablesRelativeWithIntrinsicBounds(hearIcon, 0, 0, 0)
            val animatorSet = AnimatorSet()

            val bounceAnimX = ObjectAnimator.ofFloat(itemView.findViewById(R.id.tvLove), "scaleX", 0.2f, 1f)
            bounceAnimX.duration = 300
            bounceAnimX.interpolator = OVERSHOOT_INTERPOLATOR

            val bounceAnimY = ObjectAnimator.ofFloat(itemView.findViewById(R.id.tvLove), "scaleY", 0.2f, 1f)
            bounceAnimY.duration = 300
            bounceAnimY.interpolator = OVERSHOOT_INTERPOLATOR
            bounceAnimY.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(heartIcon, 0, 0, 0)
                }

                override fun onAnimationEnd(animation: Animator) {
                }
            })

            animatorSet.play(bounceAnimX).with(bounceAnimY)/*.after(rotationAnim)*/
            animatorSet.start()

        }


        @SuppressLint("NewApi")
        fun bindItems(imagesFeed: ImagesFeed) {
            val face_regular = Typeface.createFromAsset(mContext!!.assets, "fonts/opensans_regular.ttf")
            val face_bold = Typeface.createFromAsset(mContext!!.assets, "fonts/opensans_bold.ttf")
            itemView.findViewById<TextView>(R.id.tvUserName).setTypeface(face_bold)
            itemView.findViewById<TextView>(R.id.tvEventName).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvLove).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvChat).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvReport).setTypeface(face_regular)

            val mode = mContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    itemView.findViewById<TextView>(R.id.tvUserName).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvEventName).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvLove).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvChat).setTextColor(Color.WHITE)
                //   itemView.findViewById<TextView>(R.id.tvLove).compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))
                    itemView.findViewById<TextView>(R.id.tvChat).compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))

                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    itemView.findViewById<TextView>(R.id.tvUserName).setTextColor(Color.parseColor("#000000"))
                    itemView.findViewById<TextView>(R.id.tvEventName).setTextColor(Color.parseColor("#000000"))
                    itemView.findViewById<TextView>(R.id.tvLove).setTextColor(Color.parseColor("#000000"))
                    itemView.findViewById<TextView>(R.id.tvChat).setTextColor(Color.parseColor("#000000"))
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                }
            }
            val reqs = RequestOptions()
            reqs.transform(RoundedCorners(mContext.resources.getDimensionPixelOffset(R.dimen.dp_6)))
                    .placeholder(R.drawable.profile_avatar_placeholder_large)


            if (imagesFeed.createdBy==null || imagesFeed.createdBy?.deleted ==true ){
                itemView.findViewById<TextView>(R.id.tvUserName).text = "WhrzAt"
                itemView.findViewById<TextView>(R.id.tvChat).visibility = View.INVISIBLE
                Glide.with(mContext)
                        .load(R.mipmap.ic_launcher)
                        .apply(reqs)
                        .into(itemView.findViewById(R.id.ivProfilePic))
                itemView.findViewById<RoundedImageView>(R.id.ivProfilePic).isClickable = false;
                itemView.findViewById<RoundedImageView>(R.id.ivProfilePic).isEnabled=false;
            }else{
                itemView.findViewById<TextView>(R.id.tvChat).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.tvUserName).text = (imagesFeed.createdBy?.name)
                Glide.with(mContext)
                        .load(imagesFeed.createdBy?.profilePicURL?.original)
                        .apply(reqs)
                        .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                itemView.findViewById<RoundedImageView>(R.id.ivProfilePic).isClickable=true;
                itemView.findViewById<RoundedImageView>(R.id.ivProfilePic).isEnabled=true;
                itemView.findViewById<RoundedImageView>(R.id.ivProfilePic).setOnClickListener(this)
                itemView.findViewById<TextView>(R.id.tvUserName).setOnClickListener(this)

            }
            itemView.findViewById<TextView>(R.id.tvEventName).text = imagesFeed.registrationDate?.let { getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS) }

            val req = RequestOptions()
                   // .transform(CenterCrop())
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL)
                    .placeholder(R.drawable.feed_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

            Glide.with(mContext)
                    .load(imagesFeed.picture?.original)
                    .apply(req)
                    .into(itemView.findViewById(R.id.ivSpot))


            val likeCount = imagesFeed.likes?.size
          //  itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Loves")
            if(likeCount==1){
                itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Like")
            }else if(likeCount==0){
                itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Likes")
            }else{
                itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Likes")
            }
            feedList[adapterPosition].likesCount = likeCount


            val list = ArrayList<String>()
            imagesFeed.picture?.original?.let { list.add(it) }
        /*    itemView.ivSpot.setOnClickListener {
                ImageViewer.Builder(mContext, list)
                        .setFormatter { file -> file }
                        .setStartPosition(0)
                        .hideStatusBar(false)
                        .show()
            }*/

            if (likeCount != null) {
                when {
                    likeCount < 1 ->{
                        if (mode== Configuration.UI_MODE_NIGHT_YES){
                            itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                            itemView.findViewById<TextView>(R.id.tvLove).compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))
                        }else{
                            itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                        }
                    }
                    likeCount <= 3 -> itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_blue_icon, 0, 0, 0)
                    likeCount <= 6 -> itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_yellow_icon, 0, 0, 0)
                    likeCount <= 9 -> itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_orange_icon, 0, 0, 0)
                    else -> itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_red_icon, 0, 0, 0)
                }
            }



            if (imagesFeed.createdBy?._id == Prefs.with(mContext).getString(Constants.USER_ID, "")) {
                itemView.findViewById<TextView>(R.id.tvChat).visibility = View.INVISIBLE
                itemView.findViewById<TextView>(R.id.tvReport).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.tvReport).text = mContext.getString(R.string.label_deleted)
                itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(null)
                if (feedList.size == 1) {
                    itemView.findViewById<TextView>(R.id.tvReport).visibility = View.INVISIBLE
                } else {
                    itemView.findViewById<TextView>(R.id.tvReport).text = mContext.getString(R.string.label_deleted)
                }
            } else {
                itemView.findViewById<TextView>(R.id.tvReport).text = mContext.getString(R.string.label_report)
               // itemView.findViewById<TextView>(R.id.tvChat).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.tvChat).setOnClickListener(this)

            }

           // itemView.findViewById<TextView>(R.id.tvLove).text = imagesFeed.likes?.size.toString().plus(" Loves")

            if (imagesFeed.addedBy == "ADMIN") {
                itemView.findViewById<TextView>(R.id.tvChat).visibility = View.INVISIBLE
                itemView.findViewById<TextView>(R.id.tvReport).visibility = View.INVISIBLE
                itemView.findViewById<RoundedImageView>(R.id.ivProfilePic).setOnClickListener(null)
                itemView.findViewById<TextView>(R.id.tvUserName).text="WhrzAt"
            }

        }


    }

    fun addList(images: List<ImagesFeed>) {
        feedList.clear()
        feedList.addAll(images)
        notifyDataSetChanged()

    }

    interface Love {
        fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int)

        fun report(imageId: String)

        fun delete(imageId: String)
        fun viewProfile(otherUserid: String, ivProfilePic: ImageView, tvUserName: TextView, original: String, name: String)
        fun onChatClick(id: String, ivProfilePic: ImageView, original: String)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        var view:View=LayoutInflater.from(mContext).inflate(R.layout.rv_items_feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder?.bindItems(feedList[position])
    }

}
