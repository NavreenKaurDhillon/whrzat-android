package com.codebrew.whrzat.ui.profile

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.codebrew.tagstrade.adapter.FeedContactAdapter
import com.codebrew.whrzat.R
import com.codebrew.whrzat.ui.allevents.AllEventsAdapter
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.VerticalImageSpan
import com.codebrew.whrzat.webservice.pojo.otherprofile.ImageData
import com.google.gson.Gson
import com.makeramen.roundedimageview.RoundedImageView
import com.stfalcon.frescoimageviewer.ImageViewer
import es.dmoral.toasty.Toasty


class ProfileOtherAdapter(var mContext: Context) : RecyclerView.Adapter<ProfileOtherAdapter.ProfileViewHolder>() {

    private var imagesList = ArrayList<ImageData>()
    private lateinit var listener: FeedContactAdapter.Love
    private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(2f)

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bindItems(imagesList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view:View= LayoutInflater.from(mContext).inflate(R.layout.rv_items_feed_new, parent, false)
        return ProfileViewHolder(view)
    }

    override fun getItemCount(): Int = imagesList.size
    fun setListener(listener: FeedContactAdapter.Love) {
        this.listener = listener
    }

    inner class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        init {
            itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(null)
            itemView.findViewById<TextView>(R.id.tvEventName).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.tvReport).setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.tvLove -> {
                    if (GeneralMethods.isNetworkActive(mContext)) {
                        var likeCount = imagesList[adapterPosition].imageId.likesCount
                        if (imagesList[adapterPosition].isLiked) {
                            setImageCount(--likeCount, false, imagesList[adapterPosition].imageId._id, R.drawable.ic_like_post)
                        } else {
                            when {
                                likeCount < 3 -> {
                                    setImageCount(++likeCount, true, imagesList[adapterPosition].imageId._id, R.drawable.heart_blue_icon)
                                    itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_blue_icon, 0, 0, 0)
                                }
                                likeCount < 6 -> {
                                    setImageCount(++likeCount, true, imagesList[adapterPosition].imageId._id, R.drawable.heart_yellow_icon)
                                    itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_yellow_icon, 0, 0, 0)
                                }
                                likeCount < 9 -> {
                                    setImageCount(++likeCount, true, imagesList[adapterPosition].imageId._id, R.drawable.heart_orange_icon)
                                    itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_orange_icon, 0, 0, 0)
                                }
                                else -> {
                                    setImageCount(++likeCount, true, imagesList[adapterPosition].imageId._id, R.drawable.heart_red_icon)
                                    itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_red_icon, 0, 0, 0)
                                }
                            }
                        /*    if (likeCount==0){
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true,  imagesList[adapterPosition]._id!!, R.drawable.ic_like_post) }
                            }else{
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_profile, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true,  imagesList[adapterPosition]._id!!, R.drawable.ic_like_profile) }
                            }*/
                        }
                    } else {
                        GeneralMethods.showToast(mContext, R.string.error_no_connection)
                    }
                }
                R.id.tvReport -> {
                    if (GeneralMethods.isNetworkActive(mContext)) {
                        listener.report(imagesList[adapterPosition]._id)
                    } else {
                        GeneralMethods.showToast(mContext, R.string.error_no_connection)
                    }
                }
                else -> {
                    listener.spotDetail(imagesList[adapterPosition].hotspotId._id)
                }
            }
        }

        @SuppressLint("NewApi")
        fun bindItems(imageData: ImageData) {
        //    Log.e("Favorite list===", (imageData))
            val face_regular = Typeface.createFromAsset(mContext!!.assets, "fonts/opensans_regular.ttf")
            val face_semi = Typeface.createFromAsset(mContext!!.assets, "fonts/opensans_semibold.ttf")
            itemView.findViewById<TextView>(R.id.tvUserName).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvEventName).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvLove).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvChat).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvReport).setTypeface(face_regular)


            itemView.findViewById<RoundedImageView>(R.id.ivSpot).visibility=View.GONE
            itemView.findViewById<TextView>(R.id.tvLove).visibility=View.GONE
            itemView.findViewById<TextView>(R.id.tvChat).visibility=View.GONE
            itemView.findViewById<TextView>(R.id.tvReport).visibility=View.GONE
            val mode = mContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    itemView.findViewById<TextView>(R.id.tvUserName).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvEventName).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvLove).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvChat).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvLove).compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.white))
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
            itemView.findViewById<TextView>(R.id.tvUserName).setOnClickListener {
                if (imageData.hotspotId.deleted){
                    Toasty.error(mContext,"Hotspot has expired." , Toast.LENGTH_LONG).show()
                }else{
                    val intent = Intent(mContext, DetailActivity::class.java)
                    intent.putExtra(Constants.HOTSPOT_ID, imageData.hotspotId._id)
                    mContext.startActivity(intent)
                }

            }

           // itemView.findViewById<TextView>(R.id.tvUserName).text = imageData.createdBy.name.plus(" ").plus(mContext.getString(R.string.label_posted))

            if (imageData.isCheckin && imageData.hotspotId!=null){
               // itemView.findViewById<TextView>(R.id.tvUserName).setText("You checked in at [email-icon]"+imageData.hotspotId.name)
                when (imageData.hotspotId.hotness) {
                    Constants.BLUE -> {
                        // setHotspotName(imageData.hotspotId.name, R.color.blueChill, R.drawable.flame_blue_listing_icon)
                        itemView.findViewById<TextView>(R.id.tvUserName).setText(Html.fromHtml("You checked in at [email-icon] "+"<font color=#5FABFF>"+imageData.hotspotId.name+"</font>"))
                        itemView.findViewById<TextView>(R.id.tvUserName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20))
                    }
                    Constants.RED -> {
                        itemView.findViewById<TextView>(R.id.tvUserName).setText(Html.fromHtml("You checked in at [email-icon] "+"<font color=#DB4437>"+imageData.hotspotId.name+"</font>"))
                        itemView.findViewById<TextView>(R.id.tvUserName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        //setHotspotName(imageData.hotspotId.name, R.color.redVeryPopular, R.drawable.flame_red_listing_icon)
                    }
                    Constants.YELLOW -> {
                        itemView.findViewById<TextView>(R.id.tvUserName).setText(Html.fromHtml("You checked in at [email-icon] "+"<font color=#fdd835>"+imageData.hotspotId.name+"</font>"))
                        itemView.findViewById<TextView>(R.id.tvUserName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        //setHotspotName(imageData.hotspotId.name, R.color.yellow, R.drawable.flame_yellow_listing_icon)
                    }
                    else -> {
                        itemView.findViewById<TextView>(R.id.tvUserName).setText(Html.fromHtml("You checked in at [email-icon] "+"<font color=#FB7200>"+imageData.hotspotId.name+"</font>"))
                        itemView.findViewById<TextView>(R.id.tvUserName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        // setHotspotName(imageData.hotspotId.name, R.color.orangePopular, R.drawable.flame_orange_listing_icon)
                    }
                }

            }else if(imageData.hotspotId!=null){

                when (imageData.hotspotId.hotness) {
                    Constants.BLUE -> {
                        // setHotspotName(imageData.hotspotId.name, R.color.blueChill, R.drawable.flame_blue_listing_icon)
                        itemView.findViewById<TextView>(R.id.tvUserName).setText(Html.fromHtml("You posted at [email-icon] "+"<font color=#5FABFF>"+imageData.hotspotId.name+"</font>"))
                        itemView.findViewById<TextView>(R.id.tvUserName).addImage("[email-icon]", R.drawable.flame_blue_listing_icon,
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20))
                    }
                    Constants.RED -> {
                        itemView.findViewById<TextView>(R.id.tvUserName).setText(Html.fromHtml("You posted at [email-icon] "+"<font color=#DB4437>"+imageData.hotspotId.name+"</font>"))
                        itemView.findViewById<TextView>(R.id.tvUserName).addImage("[email-icon]", R.drawable.flame_red_listing_icon,
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        //setHotspotName(imageData.hotspotId.name, R.color.redVeryPopular, R.drawable.flame_red_listing_icon)
                    }
                    Constants.YELLOW -> {
                        itemView.findViewById<TextView>(R.id.tvUserName).setText(Html.fromHtml("You posted at [email-icon] "+"<font color=#fdd835>"+imageData.hotspotId.name+"</font>"))
                        itemView.findViewById<TextView>(R.id.tvUserName).addImage("[email-icon]", R.drawable.flame_yellow_listing_icon,
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20))
                       // setHotspotName(imageData.hotspotId.name, R.color.yellow, R.drawable.flame_yellow_listing_icon)
                    }
                    else -> {
                        itemView.findViewById<TextView>(R.id.tvUserName).setText(Html.fromHtml("You posted at [email-icon] "+"<font color=#FB7200>"+imageData.hotspotId.name+"</font>"))
                        itemView.findViewById<TextView>(R.id.tvUserName).addImage("[email-icon]", R.drawable.flame_orange_listing_icon,
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20),
                                mContext.resources.getDimensionPixelOffset(R.dimen.dp_20))
                        // setHotspotName(imageData.hotspotId.name, R.color.orangePopular, R.drawable.flame_orange_listing_icon)
                    }
                }

            }
            //itemView.findViewById<TextView>(R.id.tvUserName).text = "You"+" posted at"
           //  itemView.findViewById<TextView>(R.id.tvUserName).text = Html.fromHtml("<b><font color=\"#000000\">You</font></b>"+" posted at")

            itemView.findViewById<TextView>(R.id.tvChat).visibility = View.INVISIBLE

           /* if(imageData.hotspotId!=null){
                itemView.findViewById<TextView>(R.id.tvEventName).text = imageData.hotspotId.name
            }*/

            val reqs = RequestOptions()
                    .transform(RoundedCorners(mContext.resources.getDimensionPixelOffset(R.dimen.dp_6)))
                    .placeholder(R.drawable.profile_avatar_placeholder_large)

            Glide.with(mContext)
                    .load(imageData.createdBy.profilePicURL!!.original)
                    .apply(reqs)
                    .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))

            val req = RequestOptions()
            reqs.transform(RoundedCorners(mContext.resources.getDimensionPixelOffset(R.dimen.dp_1)))
                    .placeholder(R.drawable.feed_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

            Glide.with(mContext)
                    .load(imageData.imageId.picture.original)
                    .apply(req)
                    .into(itemView.findViewById<RoundedImageView>(R.id.ivSpot))

            val likeCount = imageData.imageId.likes.size
           // itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Loves")
            if(likeCount>1){
                itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Likes")
            }else{
                itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Like")
            }
            imagesList[adapterPosition].imageId.likesCount = likeCount
         /*   if (likeCount==0){
                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
            }else{
                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_profile, 0, 0, 0)
            }
*/
            val list = ArrayList<String>()
            list.add(imageData.imageId.picture.original)
        /*    itemView.findViewById<RoundedImageView>(R.id.ivSpot).setOnClickListener {
                ImageViewer.Builder(mContext, list)
                        .setFormatter { file -> file }
                        .setStartPosition(0)
                        .hideStatusBar(false)
                        .show()
            }*/



            if (imageData.createdBy._id == Prefs.with(mContext).getString(Constants.USER_ID, "")) {
                itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(null)
            } else {
                itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(this)
            }


            itemView.findViewById<TextView>(R.id.tvReport).visibility = View.INVISIBLE
            itemView.findViewById<TextView>(R.id.tvChat).visibility = View.INVISIBLE

            // if (imageData.isLiked) {
            when {
                likeCount < 1 -> itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                likeCount <= 3 -> itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_blue_icon, 0, 0, 0)
                likeCount <= 6 -> itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_yellow_icon, 0, 0, 0)
                likeCount <= 9 -> itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_orange_icon, 0, 0, 0)
                else -> itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_red_icon, 0, 0, 0)
            }

        }

        private fun setImageCount(likeCount: Int, isLike: Boolean, imageId: String, hearIcon: Int) {
            imagesList[adapterPosition].isLiked = isLike
            imagesList[adapterPosition].imageId.likesCount = likeCount
            listener.clickLove(imageId, isLike, likeCount.toString(), adapterPosition)
            itemView.findViewById<TextView>(R.id.tvLove).text = "$likeCount Likes"
            itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(hearIcon, 0, 0, 0)

            val animatorSet = AnimatorSet()

            val bounceAnimX = ObjectAnimator.ofFloat(itemView.findViewById<TextView>(R.id.tvLove), "scaleX", 0.2f, 1f)
            bounceAnimX.duration = 300
            bounceAnimX.interpolator = OVERSHOOT_INTERPOLATOR

            val bounceAnimY = ObjectAnimator.ofFloat(itemView.findViewById<TextView>(R.id.tvLove), "scaleY", 0.2f, 1f)
            bounceAnimY.duration = 300
            bounceAnimY.interpolator = OVERSHOOT_INTERPOLATOR
            bounceAnimY.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    itemView.findViewById<TextView>(R.id.tvLove).setCompoundDrawablesRelativeWithIntrinsicBounds(hearIcon, 0, 0, 0)
                }

                override fun onAnimationEnd(animation: Animator) {
                }
            })

            animatorSet.play(bounceAnimX).with(bounceAnimY)/*.after(rotationAnim)*/
            animatorSet.start()

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

        private fun setHotspotName(name: String, color: Int, icon: Int) {
            itemView.findViewById<TextView>(R.id.tvEventName).setTextColor(ContextCompat.getColor(mContext, color))
            itemView.findViewById<TextView>(R.id.tvEventName).text = name
            val dr = mContext.resources.getDrawable(icon)
            val bitmap = (dr as BitmapDrawable).bitmap
            val d: Drawable = BitmapDrawable(mContext.resources, Bitmap.createScaledBitmap(bitmap, 48, 48, true))
            itemView.findViewById<TextView>(R.id.tvEventName).setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        }

    }


    interface Love {
        fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int)
        fun report(id: String)
        fun spotDetail(id: String)

    }

    fun addList(images: List<ImageData>) {
        imagesList.clear()
        imagesList.addAll(images)
        notifyDataSetChanged()
    }


}
