package com.codebrew.tagstrade.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.webservice.pojo.feed.FeedData
import com.makeramen.roundedimageview.RoundedImageView
import com.stfalcon.frescoimageviewer.ImageViewer


class FeedContactAdapter(private var mContext: Context) : RecyclerView.Adapter<FeedContactAdapter.FeedViewHolder>() {

    private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(2f)
    private var feedList = ArrayList<FeedData>()
    private lateinit var listener: Love

    override fun getItemCount(): Int = feedList.size

    fun setListener(listener: Love) {
        this.listener = listener
    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.findViewById<TextView>(R.id.tvChat).setOnClickListener(this)
             itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(this)
             itemView.findViewById<TextView>(R.id.tvReport).setOnClickListener(this)
             itemView.findViewById<TextView>(R.id.tvUserName).setOnClickListener(this)
             itemView.findViewById<ImageView>(R.id.ivSpot).setOnClickListener(this)
             itemView.findViewById<RoundedImageView>(R.id.ivProfilePic).setOnClickListener(this)
             itemView.findViewById<TextView>(R.id.tvEventName).setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.tvLove -> {
                    if (GeneralMethods.isNetworkActive(mContext)) {
                        var likeCount = feedList[adapterPosition].imageId.likesCount
                        if (feedList[adapterPosition].isLiked) {
                            likeCount = --likeCount
                         /*   if (likeCount==0){
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true,  feedList[adapterPosition].imageId._id!!, R.drawable.ic_like_post) }
                            }else{
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_profile, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true, feedList[adapterPosition].imageId._id!!, R.drawable.ic_like_profile) }
                            }*/
                            when {
                                likeCount < 1 -> {
                                    setImageCount(likeCount, false, feedList[adapterPosition].imageId._id, R.drawable.ic_like_post)
                                }
                                likeCount <= 3 -> {
                                    setImageCount(likeCount, false, feedList[adapterPosition].imageId._id, R.drawable.heart_blue_icon)
                                }
                                likeCount <= 6 -> {
                                    setImageCount(likeCount, false, feedList[adapterPosition].imageId._id, R.drawable.heart_yellow_icon)
                                }
                                likeCount <= 9 -> {
                                    setImageCount(likeCount, false, feedList[adapterPosition].imageId._id, R.drawable.heart_orange_icon)
                                }
                                else -> {
                                    setImageCount(likeCount, false, feedList[adapterPosition].imageId._id, R.drawable.heart_red_icon)
                                }
                            }

                        } else {
                            likeCount = ++likeCount
                         /*   if (likeCount==0){
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true,  feedList[adapterPosition].imageId._id!!, R.drawable.ic_like_post) }
                            }else{
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_profile, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true,  feedList[adapterPosition].imageId._id!!, R.drawable.ic_like_profile) }
                            }*/
                            when {
                                likeCount <=3 -> {
                                    setImageCount(likeCount, true, feedList[adapterPosition].imageId._id, R.drawable.heart_blue_icon)
                                }
                                likeCount <= 6 -> {
                                    setImageCount(likeCount, true, feedList[adapterPosition].imageId._id, R.drawable.heart_yellow_icon)
                                }
                                likeCount <= 9 -> {
                                    setImageCount(likeCount, true, feedList[adapterPosition].imageId._id, R.drawable.heart_orange_icon)
                                }
                                else -> {
                                    setImageCount(likeCount, true, feedList[adapterPosition].imageId._id, R.drawable.heart_red_icon)
                                }
                            }
                            //  animateHeartButton( itemView.findViewById<TextView>(R.id.tvLove))
                        }
                    } else {
                        GeneralMethods.showToast(mContext, R.string.error_no_connection)
                    }
                }
                R.id.tvReport -> {
                    if (GeneralMethods.isNetworkActive(mContext)) {
                        confirmReport()
                    } else {
                        GeneralMethods.showToast(mContext, R.string.error_no_connection)

                    }

                }
                R.id.tvUserName, R.id.ivProfilePic -> {
                    listener.viewProfile(feedList[adapterPosition].createdBy._id,  itemView.findViewById<RoundedImageView>(R.id.ivProfilePic)
                            ,  itemView.findViewById<TextView>(R.id.tvUserName), feedList[adapterPosition].createdBy.profilePicURL!!.original, feedList[adapterPosition].createdBy.name)

                }
                R.id.tvChat -> {
                    listener.onChatClick(feedList[adapterPosition].createdBy._id,  itemView.findViewById<RoundedImageView>(R.id.ivProfilePic),
                            feedList[adapterPosition].createdBy.profilePicURL!!.original)
                }
                else -> {
                    listener.spotDetail(feedList[adapterPosition].hotspotId._id)
                }
            }
        }

        private fun setImageCount(likeCount: Int, isLike: Boolean, imageId: String, heartIcon: Int) {
            feedList[adapterPosition].isLiked = isLike
            feedList[adapterPosition].imageId.likesCount = likeCount
            listener.clickLove(imageId, isLike, likeCount.toString(), adapterPosition)
             itemView.findViewById<TextView>(R.id.tvLove).text = "$likeCount Likes"

            val animatorSet = AnimatorSet()
            val bounceAnimX = ObjectAnimator.ofFloat( itemView.findViewById<TextView>(R.id.tvLove), "scaleX", 0.2f, 1f)
            bounceAnimX.duration = 300
            bounceAnimX.interpolator = OVERSHOOT_INTERPOLATOR

            val bounceAnimY = ObjectAnimator.ofFloat( itemView.findViewById<TextView>(R.id.tvLove), "scaleY", 0.2f, 1f)
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

        private fun confirmReport() {
            AlertDialog.Builder(mContext, R.style.MyDialog)
                    .setTitle(R.string.dialog_do_you_want_to_report_this_item)
                    .setPositiveButton(android.R.string.ok) { dialogInterface, i ->

                        if (GeneralMethods.isNetworkActive(mContext)) {
                            listener.report(feedList[adapterPosition].imageId._id)

                        } else {
                            GeneralMethods.showToast(mContext, R.string.error_no_connection)

                        }
                    }
                .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true)
                    .show()
        }


        @SuppressLint("NewApi")
        fun bindItems(imagesFeed: FeedData) {

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

            if(imagesFeed.createdBy!=null){
                Glide.with(mContext)
                        .load(imagesFeed.createdBy.profilePicURL!!.original)
                        .apply(reqs)
                        .into( itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))
                 itemView.findViewById<TextView>(R.id.tvUserName).text = imagesFeed.createdBy.name
            }

           //  itemView.findViewById<TextView>(R.id.tvEventName).text = imagesFeed.hotspotId.name
//             itemView.findViewById<TextView>(R.id.tvUserName).text = imagesFeed.createdBy.name.plus(" ").plus(mContext.getString(R.string.label_posted))

           if (imagesFeed.hotspotId!=null){

               when (imagesFeed.hotspotId?.hotness) {
                   Constants.BLUE -> {
                       setHotspotName(imagesFeed.hotspotId.name, R.color.blueChill, R.drawable.flame_blue_listing_icon)
                   }
                   Constants.RED -> {
                       setHotspotName(imagesFeed.hotspotId.name, R.color.redVeryPopular, R.drawable.flame_red_listing_icon)
                   }
                   Constants.YELLOW -> {
                       setHotspotName(imagesFeed.hotspotId.name, R.color.yellow, R.drawable.flame_yellow_listing_icon)
                   }
                   else -> {
                       setHotspotName(imagesFeed.hotspotId.name, R.color.orangePopular, R.drawable.flame_orange_listing_icon)
                   }
               }
           }

            val req = RequestOptions()
                  //  .transform(CenterCrop())
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL)
                    .placeholder(R.drawable.feed_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

            Glide.with(mContext)
                    .load(imagesFeed.imageId.picture.original)
                    .apply(req)
                    .into( itemView.findViewById<ImageView>(R.id.ivSpot))


            val list = ArrayList<String>()
            list.add(imagesFeed.imageId.picture.original)
        /*     itemView.findViewById<ImageView>(R.id.ivSpot).setOnClickListener {
                ImageViewer.Builder(mContext, list)
                        .setFormatter { file -> file }
                        .setStartPosition(0)
                        .hideStatusBar(false)
                        .show()
            }*/

            if(imagesFeed.createdBy!=null){
                if (imagesFeed.createdBy._id == Prefs.with(mContext).getString(Constants.USER_ID, "")) {
                     itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(null)
                } else {
                     itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(this)
                }
            }

            val likeCount = imagesFeed.imageId.likes.size
            if(likeCount>1){
                 itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Likes")
            }else{
                 itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Like")
            }

            feedList[adapterPosition].imageId.likesCount = likeCount

          /*  if (likeCount==0){
                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
            }else{
                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_profile, 0, 0, 0)
            }*/
            when {
                likeCount < 1 ->{
                    if (mode== Configuration.UI_MODE_NIGHT_YES){
                         itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                         itemView.findViewById<TextView>(R.id.tvLove).compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))
                    }else{
                         itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                    }
                }
                likeCount <= 3 ->  itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_blue_icon, 0, 0, 0)
                likeCount <= 6 ->  itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_yellow_icon, 0, 0, 0)
                likeCount <= 9 ->  itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_orange_icon, 0, 0, 0)
                else ->  itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_red_icon, 0, 0, 0)
            }
        }
    }


    private fun FeedViewHolder.setHotspotName(name: String, color: Int, flame_icon: Int) {
         itemView.findViewById<TextView>(R.id.tvEventName).setTextColor(ContextCompat.getColor(mContext, color))
         itemView.findViewById<TextView>(R.id.tvEventName).text = name
        val dr = mContext.resources.getDrawable(flame_icon)
        val bitmap = (dr as BitmapDrawable).bitmap
        val d: Drawable = BitmapDrawable(mContext.resources, Bitmap.createScaledBitmap(bitmap, 38, 38, true))
         itemView.findViewById<TextView>(R.id.tvEventName).setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

        //  itemView.findViewById<TextView>(R.id.tvEventName).setCompoundDrawablesWithIntrinsicBounds(flame_icon, 0, 0, 0)
    }

    fun addList(images: List<FeedData>) {
        feedList.clear()
        feedList.addAll(images)
        notifyDataSetChanged()

    }

    interface Love {
        fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int)
        fun report(id: String)
        fun spotDetail(id: String)
        fun viewProfile(otherUserid: String, ivProfilePic: ImageView, tvUserName: TextView, original: String, name: String)
        fun onChatClick(id: String, ivProfilePic: ImageView, original: String)
    }

    fun clearList() {
        feedList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
       var view:View= LayoutInflater.from(mContext).inflate(R.layout.rv_items_feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder?.bindItems(feedList[position])
    }


}
