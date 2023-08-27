package com.codebrew.whrzat.ui.Home

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.webservice.pojo.explore.Hotspot
import com.makeramen.roundedimageview.RoundedImageView


class SpotAdapter(private var mContext: Context) : RecyclerView.Adapter<SpotAdapter.SpotViewHolder>() {

    private var exploreList = ArrayList<Hotspot>()
    private lateinit var onSpotItemClickListener: OnSpotItemClickListener

    override fun getItemCount(): Int = exploreList.size

   /* fun addList(list: List<Hotspot>) {
        exploreList.addAll(list)
        notifyDataSetChanged()
    }*/

    fun addListWithClear(list: List<Hotspot>) {
        exploreList.clear()
        exploreList.addAll(list)
        notifyDataSetChanged()
    }

    fun clear() {
        exploreList.clear()
        notifyDataSetChanged()
    }




    fun setonSpotClickListener(onSpotItemClickListener:OnSpotItemClickListener){
        this.onSpotItemClickListener=onSpotItemClickListener
    }

    inner class SpotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val intent = Intent(mContext, DetailActivity::class.java)
                intent.putExtra(Constants.HOTSPOT_ID, exploreList[adapterPosition]._id)
                mContext.startActivity(intent)
            }

        }

        fun bindItems(hotspot: Hotspot) {
            val mode = mContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    itemView.findViewById<TextView>(R.id.tvSpotTitle).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvSpotSubTitle).setTextColor(Color.WHITE)
                    itemView.findViewById<RelativeLayout>(R.id.rlSpot).setBackgroundColor(Color.BLACK)

                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    itemView.findViewById<TextView>(R.id.tvSpotTitle).setTextColor(Color.parseColor("#000000"))
                    itemView.findViewById<TextView>(R.id.tvSpotSubTitle).setTextColor(Color.parseColor("#000000"))

                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                }
            }

            val req= RequestOptions()
            req.transform(RoundedCorners(mContext.resources.getDimensionPixelOffset(R.dimen.dp_6)))
                    .placeholder(R.drawable.circular_placeholder_products)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

            Glide.with(mContext)
                    .load(hotspot.picture.original)
                    .apply(req)
                    .into(itemView.findViewById<RoundedImageView>(R.id.ivSpotPic))

            itemView.findViewById<TextView>(R.id.tvSpotTitle).text = hotspot.name
            itemView.findViewById<TextView>(R.id.tvSpotSubTitle).text = hotspot.description

            when (hotspot.hotness) {
                Constants.BLUE -> {
                    setPopularityType(mContext.getString(R.string.label_chill),
                            ContextCompat.getColor(mContext, R.color.sky_blue),
                            R.drawable.flame_blue_listing_icon)
                }
                Constants.RED -> {
                    setPopularityType(mContext.getString(R.string.label_very_popular),
                            ContextCompat.getColor(mContext, R.color.red),
                            R.drawable.flame_red_listing_icon)
                }
                Constants.ORANGE -> {
                    setPopularityType(mContext.getString(R.string.label_popular)
                            , ContextCompat.getColor(mContext, R.color.orangePopular),
                            R.drawable.flame_orange_listing_icon)
                }

                else -> {
                    setPopularityType(mContext.getString(R.string.label_Just_in),
                            ContextCompat.getColor(mContext, R.color.yellow),
                            R.drawable.flame_yellow_listing_icon)
                }
            }


            /*if (hotspot.hotness == Constants.BLUE) {
                itemView.findViewById<TextView>(R.id.tvPopularityType).text = mContext.getString(R.string.label_chill)
                itemView.findViewById<TextView>(R.id.tvPopularityType).setTextColor(ContextCompat.getColor(mContext, R.color.sky_blue))
                itemView.findViewById<TextView>(R.id.tvPopularityType).setCompoundDrawablesWithIntrinsicBounds(R.drawable.flame_blue_listing_icon, 0, 0, 0)
            } else if (hotspot.hotness == Constants.RED) {
                itemView.findViewById<TextView>(R.id.tvPopularityType).text = mContext.getString(R.string.label_very_popular)
                itemView.findViewById<TextView>(R.id.tvPopularityType).setTextColor(ContextCompat.getColor(mContext, R.color.red_create))
                itemView.findViewById<TextView>(R.id.tvPopularityType).setCompoundDrawablesWithIntrinsicBounds(R.drawable.flame_red_listing_icon, 0, 0, 0)
            } else if (hotspot.hotness == Constants.YELLOW) {
                itemView.findViewById<TextView>(R.id.tvPopularityType).text = mContext.getString(R.string.label_Just_in)
                itemView.findViewById<TextView>(R.id.tvPopularityType).setTextColor(ContextCompat.getColor(mContext, R.color.yellow))
                itemView.findViewById<TextView>(R.id.tvPopularityType).setCompoundDrawablesWithIntrinsicBounds(R.drawable.flame_yellow_listing_icon, 0, 0, 0)
            } else {
                itemView.findViewById<TextView>(R.id.tvPopularityType).text = mContext.getString(R.string.label_popular)
                itemView.findViewById<TextView>(R.id.tvPopularityType).setTextColor(ContextCompat.getColor(mContext, R.color.orangePopular))
                itemView.findViewById<TextView>(R.id.tvPopularityType).setCompoundDrawablesWithIntrinsicBounds(R.drawable.flame_orange_listing_icon, 0, 0, 0)
            }


        }*/
        }

        private fun SpotViewHolder.setPopularityType(popularityText: String, textColor: Int, popularity_icon: Int) {
            itemView.findViewById<TextView>(R.id.tvPopularityType).text = popularityText
            itemView.findViewById<TextView>(R.id.tvPopularityType).setTextColor(textColor)
            val dr = mContext.resources.getDrawable(popularity_icon)
            val bitmap = (dr as BitmapDrawable).bitmap
            val d: Drawable = BitmapDrawable(mContext.resources, Bitmap.createScaledBitmap(bitmap, 40, 40, true))
            itemView.findViewById<TextView>(R.id.tvPopularityType).setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        }


    }

    interface OnSpotItemClickListener{
        fun onHotSpotClick(id:String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
         var view:View=LayoutInflater.from(mContext).inflate(R.layout.rv_item_spot, parent, false)
         return SpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        holder?.bindItems(exploreList[position])
    }

}


