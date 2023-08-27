package com.codebrew.whrzat.ui.profile.favorite

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.webservice.pojo.explore.FavoriteListData
import com.makeramen.roundedimageview.RoundedImageView

class FavoriteAdapter(private var mContext: Context,
                      val list: ArrayList<FavoriteListData.ListFavorite>,
                      val onSpotItemClickListener:OnSpotItemClickListener) : RecyclerView.Adapter<FavoriteAdapter.FavotiteViewHolder>() {

    override fun getItemCount(): Int {
        return list.size
    }


    private fun setPopularityType(popularityText: String, textColor: Int, popularity_icon: Int,tv:TextView) {
        tv.apply {
            text = popularityText
            setTextColor(textColor)
            val dr = resources.getDrawable(popularity_icon)
            val bitmap = (dr as BitmapDrawable).bitmap
            val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 48, 48, true))
            setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        }


    }
    class FavotiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){


    }

    interface OnSpotItemClickListener {
        fun onHotSpotClick(id: String, deleted: Boolean?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavotiteViewHolder {
        val view:View=LayoutInflater.from(mContext).inflate(R.layout.rv_favorite_adapter, parent, false)

        return FavotiteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavotiteViewHolder, position: Int) {
        val face_regular = Typeface.createFromAsset(mContext.assets, "fonts/opensans_regular.ttf")
        val face_semi = Typeface.createFromAsset(mContext.assets, "fonts/opensans_semibold.ttf")
        holder.itemView.findViewById<TextView>(R.id.tvSpotTitle)?.setTypeface(face_regular)
        holder.itemView.findViewById<TextView>(R.id.tvPopularityType).setTypeface(face_regular)
        val mode = mContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                holder.itemView.findViewById<TextView>(R.id.tvSpotTitle)?.setTextColor(Color.WHITE)
                holder.itemView.findViewById<TextView>(R.id.tvSpotSubTitle)?.setTextColor(Color.WHITE)
           //     holder?.itemView.view.setBackgroundColor(Color.parseColor("#7B7B7B"))

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                holder.itemView.findViewById<TextView>(R.id.tvSpotTitle)?.setTextColor(Color.parseColor("#000000"))
                holder.itemView.findViewById<TextView>(R.id.tvSpotSubTitle)?.setTextColor(Color.parseColor("#000000"))
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }


        holder.itemView.findViewById<TextView>(R.id.tvSpotTitle)?.text=list.get(position).name
        holder.itemView.findViewById<TextView>(R.id.tvSpotSubTitle).text = list.get(position).description
        val req = RequestOptions()
        req.transform(RoundedCorners(mContext.resources.getDimensionPixelOffset(R.dimen.dp_6)))
                .placeholder(R.drawable.circular_placeholder_products)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

        Glide.with(mContext).
        load(list.get(position).picture?.thumbnail)
                .apply(req)
                .into( holder.itemView.findViewById<RoundedImageView>(R.id.ivSpotPic))

        when (list.get(position).hotness) {
            Constants.BLUE -> {
                setPopularityType(mContext.getString(R.string.label_chill),
                        ContextCompat.getColor(mContext, R.color.blueChill),
                        R.drawable.flame_blue_listing_icon,  holder.itemView.findViewById<TextView>(R.id.tvPopularityType))
            }
            Constants.RED -> {
                setPopularityType(mContext.getString(R.string.label_very_popular),
                        ContextCompat.getColor(mContext, R.color.redVeryPopular),
                        R.drawable.flame_red_listing_icon, holder.itemView.findViewById<TextView>(R.id.tvPopularityType))
            }
            Constants.YELLOW -> {
                setPopularityType(mContext.getString(R.string.label_Just_in),
                        ContextCompat.getColor(mContext, R.color.yellow),
                        R.drawable.flame_yellow_listing_icon, holder.itemView.findViewById<TextView>(R.id.tvPopularityType))
            }

            else -> {
                setPopularityType(mContext.getString(R.string.label_popular)
                        , ContextCompat.getColor(mContext, R.color.orangePopular),
                        R.drawable.flame_orange_listing_icon, holder.itemView.findViewById<TextView>(R.id.tvPopularityType))
            }
        }


        when {
            list.get(position).isFavouriteColor == Constants.BLUE -> {
                val color = Color.rgb(95, 171, 255)
                val myDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_star_yellow)
                myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                holder.itemView.findViewById<ImageView>(R.id.tvFavEvent)?.setImageDrawable( myDrawable);
            }

            list.get(position).isFavouriteColor == Constants.YELLOW -> {

                var color = Color.rgb(253, 216, 53)
                val myDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_star_yellow)
                myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                holder.itemView.findViewById<ImageView>(R.id.tvFavEvent)?.setImageDrawable( myDrawable);
            }
            list.get(position).isFavouriteColor == Constants.ORANGE -> {

                var color = Color.rgb(251, 114, 0)
                val myDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_star_yellow)
                myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                holder.itemView.findViewById<ImageView>(R.id.tvFavEvent)?.setImageDrawable( myDrawable);
            }
            list.get(position).isFavouriteColor == Constants.RED -> {
                var color = Color.rgb(251, 114, 0)
                val myDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_star_yellow)
                myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                holder.itemView.findViewById<ImageView>(R.id.tvFavEvent)?.setImageDrawable( myDrawable);
            }
            else -> {
                val myDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_star_empty)
                holder.itemView.findViewById<ImageView>(R.id.tvFavEvent)?.setImageDrawable( myDrawable);
            }
        }

        holder.itemView.setOnClickListener {
            onSpotItemClickListener.onHotSpotClick(list.get(position).id!!,list.get(position).deleted)
        }
    }
}
