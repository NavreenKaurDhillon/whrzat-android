package com.codebrew.whrzat.ui.feed.happening

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
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
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
import com.bumptech.glide.load.resource.bitmap.*
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.codebrew.whrzat.R
import com.codebrew.whrzat.ui.chat.userchat.ChatActivity
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.ui.otherprofile.ProfileOtherActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData
import com.makeramen.roundedimageview.RoundedImageView

class FeedHappeningAdapter(private var mContext: Context) : RecyclerView.Adapter<FeedHappeningAdapter.FeedHappeningViewHolder>() {
    var list = ArrayList<HappeningListData.ImageDatum>()
    private lateinit var listener: OnHappeningItemClick

    private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(2f)

    override fun getItemCount(): Int = list.size

    fun setListener(listener: OnHappeningItemClick) {
        this.listener = listener
    }

    inner class FeedHappeningViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.findViewById<TextView>(R.id.tvChat).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.tvReport).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.tvUserName).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.ivSpot).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.ivProfilePic).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.tvEventName).setOnClickListener(this)

        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.tvLove -> {
                    if (GeneralMethods.isNetworkActive(mContext)) {
                        var likeCount = list[adapterPosition].isLikedCount
                        if (list[adapterPosition].isLiked!!) {

                            if (likeCount != null) {
                                likeCount = --likeCount
                            }
                        /*    if (likeCount==0){
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true,  list[adapterPosition].id!!, R.drawable.ic_like_post) }

                            }else{
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_profile, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true,  list[adapterPosition].id!!, R.drawable.ic_like_profile) }

                            }*/
                            when {
                                likeCount!! < 1 -> {
                                    setImageCount(likeCount, false, list[adapterPosition].id!!, R.drawable.ic_like_post)
                                }
                                likeCount <= 3 -> {
                                    setImageCount(likeCount, false, list[adapterPosition].id!!, R.drawable.heart_blue_icon)
                                }
                                likeCount <= 6 -> {
                                    setImageCount(likeCount, false,  list[adapterPosition].id!!, R.drawable.heart_yellow_icon)
                                }
                                likeCount <= 9 -> {
                                    setImageCount(likeCount, false,  list[adapterPosition].id!!, R.drawable.heart_orange_icon)
                                }
                                else -> {
                                    setImageCount(likeCount, false, list[adapterPosition].id!!, R.drawable.heart_red_icon)
                                }
                            }

                        } else {
                            if (likeCount != null) {
                                likeCount = ++likeCount
                            }
                          /*  if (likeCount==0){
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true,  list[adapterPosition].id!!, R.drawable.ic_like_post) }
                            }else{
                                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_profile, 0, 0, 0)
                                likeCount?.let { setImageCount(it, true,  list[adapterPosition].id!!, R.drawable.ic_like_profile) }
                            }*/
                            when {
                                likeCount!! <=3 -> {

                                        setImageCount(likeCount, true,  list[adapterPosition].id!!, R.drawable.heart_blue_icon)

                                }
                                likeCount <= 6 -> {
                                    setImageCount(likeCount, true,  list[adapterPosition].id!!, R.drawable.heart_yellow_icon)
                                }
                                likeCount <= 9 -> {
                                    setImageCount(likeCount, true,  list[adapterPosition].id!!, R.drawable.heart_orange_icon)
                                }
                                else -> {
                                    setImageCount(likeCount, true, list[adapterPosition].id!!, R.drawable.heart_red_icon)
                                }
                            }
                            //  animateHeartButton(itemView.findViewById<TextView>(R.id.tvLove))
                        }
                    } else {
                        GeneralMethods.showToast(mContext, R.string.error_no_connection)
                    }
                }
                R.id.ivSpot,R.id.tvEventName -> {
                    val intent = Intent(mContext, DetailActivity::class.java)
                    intent.putExtra(Constants.HOTSPOT_ID, list.get(adapterPosition).hotspotId?.id)
                    mContext.startActivity(intent)
                }
                R.id.tvReport -> {
                    if (GeneralMethods.isNetworkActive(mContext)) {
                        confirmReport()
                    } else {
                        GeneralMethods.showToast(mContext, R.string.error_no_connection)
                    }
                }
                R.id.tvUserName, R.id.ivProfilePic -> {
                    val intent = Intent(mContext, ProfileOtherActivity::class.java)
                    intent.putExtra(Constants.OTHER_USER_ID, list.get(adapterPosition).createdBy?.id)
                    intent.putExtra(ProfileOtherActivity.PIC_URL, list.get(adapterPosition).createdBy?.profilePicURL?.original)
                    intent.putExtra(ProfileOtherActivity.USERNAME, list.get(adapterPosition).createdBy?.name)
                    val p1 = Pair.create(itemView.findViewById<TextView>(R.id.ivProfilePic) as View, "pic")
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(mContext as FragmentActivity, p1)
                    mContext.startActivity(intent, options.toBundle())
                }
                R.id.tvChat -> {
                    val intent = Intent(mContext, ChatActivity::class.java)
                    intent.putExtra(Constants.OTHER_USER_ID, list.get(adapterPosition).createdBy?.id)
                    intent.putExtra(Constants.PIC, list.get(adapterPosition).createdBy?.profilePicURL?.original)
                    val p1 = Pair.create(itemView.findViewById<TextView>(R.id.ivProfilePic) as View, "pic")
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(mContext as FragmentActivity, p1)
                    mContext.startActivity(intent)
                }

//                R.id.tvEventName->{
//                    val intent=Intent(mContext, HotspotLocationActivity::class.java)
//                    intent.putExtra(Constants.HOTSPOT_ID,list.get(adapterPosition).hotspotId?.id)
//                    intent.putExtra(Constants.SPOT_NAME,list.get(adapterPosition).hotspotId?.name)
//                    intent.putExtra(Constants.SPOT_DESCRIPTION,list.get(adapterPosition).hotspotId?.description)
//                    intent.putExtra(Constants.COLOR,list.get(adapterPosition).hotspotId?.hotness)
//                    intent.putExtra(Constants.PIC,list.get(adapterPosition).hotspotId?.picture?.original)
//                    intent.putExtra(Constants.LAT,list.get(adapterPosition).hotspotId?.location?.get(0))
//                    intent.putExtra(Constants.LNG,list.get(adapterPosition).hotspotId?.location?.get(1))
//                    mContext.startActivity(intent)
//                }
            }
        }

        @SuppressLint("NewApi")
        fun bindItems(data: HappeningListData.ImageDatum) {
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


            itemView.findViewById<TextView>(R.id.tvUserName)?.text = data.createdBy?.name
            if (data.createdBy?.id == Prefs.with(mContext).getString(Constants.USER_ID, "")) {
                itemView.findViewById<TextView>(R.id.tvReport).visibility=View.INVISIBLE
            }else{
                itemView.findViewById<TextView>(R.id.tvReport).visibility=View.VISIBLE
            }

            val req = RequestOptions()
                    //.transform(CenterCrop())
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL)
                    .placeholder(R.drawable.feed_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

            Glide.with(mContext)
                    .load(data.hotspotId?.picture?.original)
                    .apply(req)
                    .into(itemView.findViewById<ImageView>(R.id.ivSpot))
            /*reqs.transform(CircleCrop())*/

            val reqs = RequestOptions()
                    .transform(RoundedCorners(mContext.resources.getDimensionPixelOffset(R.dimen.dp_6)))
                    .placeholder(R.drawable.profile_avatar_placeholder_large)

            Glide.with(mContext)
                    .load(data.createdBy?.profilePicURL?.original)
                    .apply(reqs)
                    .into(itemView.findViewById<RoundedImageView>(R.id.ivProfilePic))

           // itemView?.tvEventName.text=data.hotspotId?.name
            val likeCount = data.isLikedCount
            if(likeCount!! >1){
                itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Likes")
            }else{
                itemView.findViewById<TextView>(R.id.tvLove).text = likeCount.toString().plus(" Like")
            }

          /*  if (likeCount==0){
                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
            }else{
                itemView?.tvLove?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_profile, 0, 0, 0)
            }*/

            when {
                likeCount < 1 -> itemView.findViewById<TextView>(R.id.tvLove)?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_post, 0, 0, 0)
                likeCount <= 3 -> itemView.findViewById<TextView>(R.id.tvLove)?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_blue_icon, 0, 0, 0)
                likeCount <= 6 -> itemView.findViewById<TextView>(R.id.tvLove)?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_yellow_icon, 0, 0, 0)
                likeCount <= 9 -> itemView.findViewById<TextView>(R.id.tvLove)?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_orange_icon, 0, 0, 0)
                else -> itemView.findViewById<TextView>(R.id.tvLove)?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_red_icon, 0, 0, 0)
            }

            when (data.hotspotId?.hotness) {
                Constants.BLUE -> {
                    setPopularityType(data.hotspotId?.name.toString(),
                            ContextCompat.getColor(mContext, R.color.blueChill),
                            R.drawable.flame_blue_listing_icon, itemView.findViewById<TextView>(R.id.tvEventName)!!)
                }
                Constants.RED -> {
                    setPopularityType(data.hotspotId?.name.toString(),
                            ContextCompat.getColor(mContext, R.color.redVeryPopular),
                            R.drawable.flame_red_listing_icon, itemView.findViewById<TextView>(R.id.tvEventName)!!)
                }
                Constants.YELLOW -> {
                    setPopularityType(data.hotspotId?.name.toString(),
                            ContextCompat.getColor(mContext, R.color.yellow),
                            R.drawable.flame_yellow_listing_icon, itemView.findViewById<TextView>(R.id.tvEventName)!!)
                }

                else -> {
                    setPopularityType(data.hotspotId?.name.toString()
                            , ContextCompat.getColor(mContext, R.color.orangePopular),
                            R.drawable.flame_orange_listing_icon, itemView.findViewById<TextView>(R.id.tvEventName)!!)
                }
            }

            if (data.createdBy?.id.equals(Prefs.with(mContext).getString(Constants.USER_ID, ""))) {
                itemView.findViewById<TextView>(R.id.tvChat).visibility = View.INVISIBLE
                itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(null)
            } else {
                itemView.findViewById<TextView>(R.id.tvChat).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.tvLove).setOnClickListener(this)
            }
        }
        private fun setImageCount(likeCount: Int, isLike: Boolean, imageId: String, heartIcon: Int) {
            list[adapterPosition].isLiked = isLike
            list[adapterPosition].isLikedCount = likeCount
            listener.clickLove(imageId, isLike, likeCount.toString(), adapterPosition)
            itemView.findViewById<TextView>(R.id.tvLove).text = "$likeCount Likes"

            val animatorSet = AnimatorSet()
            val bounceAnimX = ObjectAnimator.ofFloat(itemView.findViewById<TextView>(R.id.tvLove), "scaleX", 0.2f, 1f)
            bounceAnimX.duration = 300
            bounceAnimX.interpolator = OVERSHOOT_INTERPOLATOR

            val bounceAnimY = ObjectAnimator.ofFloat(itemView.findViewById<TextView>(R.id.tvLove), "scaleY", 0.2f, 1f)
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
                            listener.onReportClick(list.get(adapterPosition).id.toString())
                        } else {
                            GeneralMethods.showToast(mContext, R.string.error_no_connection)
                        }
                    }
                .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true)
                    .show()
        }
    }

    fun addList(data: HappeningListData.Data) {
        list.clear()
        list.addAll(data.imageData!!)
        notifyDataSetChanged()

    }

    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }

    private fun setPopularityType(popularityText: String, textColor: Int, popularity_icon: Int, view: TextView) {
        view.apply {
            text = popularityText
            setTextColor( textColor)
            val dr = resources.getDrawable(popularity_icon)
            val bitmap = (dr as BitmapDrawable).bitmap
            val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 38, 38, true))
            setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

        }
    }

    interface OnHappeningItemClick {
        fun onReportClick(id: String)
        fun onLocationClick(data: HappeningListData.ImageDatum)
        fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedHappeningViewHolder {
        val view:View=LayoutInflater.from(mContext).inflate(R.layout.rv_items_feed_happening, parent, false)
        return FeedHappeningViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedHappeningViewHolder, position: Int) {
        holder.bindItems(list[position])
    }
}
