package com.codebrew.whrzat.ui.Home

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.webservice.pojo.explore.Hotspot
import com.codebrew.whrzat.webservice.pojo.explore.Loading
import com.codebrew.whrzat.webservice.pojo.explore.SpotItem
import com.makeramen.roundedimageview.RoundedImageView

class ExploreAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private var exploreList = ArrayList<SpotItem>()
    private  val TAG = "ExploreAdapter"
    private lateinit var onSpotItemClickListener: OnSpotItemClickListener
    private var mContext: Context
    private var inflater: LayoutInflater

    constructor(context: Context) {
        inflater = LayoutInflater.from(context)
        mContext = context
    }

    override fun getItemCount(): Int = exploreList.size

    private fun progressViewHolder(progressHolder: ProgressHolder) {

    }

    inner class ProgressHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    private fun spotViewHolder(holder: ExploreViewHolder, hotspot: Hotspot) {
        holder.bindItems(hotspot)
    }

    fun setonSpotClickListener(onSpotItemClickListener: OnSpotItemClickListener) {
        this.onSpotItemClickListener = onSpotItemClickListener
    }

    override fun getItemViewType(position: Int): Int = exploreList[position].getViewType()


    inner class ExploreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                onSpotItemClickListener.onHotSpotClick((exploreList[adapterPosition] as Hotspot)._id)
            }
        }

        fun bindItems(hotspot: Hotspot) {

            val face_regular = Typeface.createFromAsset(mContext.assets, "fonts/opensans_regular.ttf")
            val face_semi = Typeface.createFromAsset(mContext.assets, "fonts/opensans_semibold.ttf")
            itemView.findViewById<TextView>(R.id.tvSpotTitle).setTypeface(face_semi)
            itemView.findViewById<TextView>(R.id.tvPopularityType).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvSpotSubTitle).setTypeface(face_regular)

            val req = RequestOptions()
            req.transform(RoundedCorners(mContext.resources.getDimensionPixelOffset(R.dimen.dp_6)))
                    .placeholder(R.drawable.feed_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)


            Glide.with(mContext).
                    load(hotspot.picture?.thumbnail)
                    .apply(req)
                    .into(itemView.findViewById<RoundedImageView>(R.id.ivSpotPic))


            val mode = mContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                  itemView.findViewById<TextView>(R.id.tvSpotTitle).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvSpotSubTitle).setTextColor(Color.WHITE)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                 itemView.findViewById<TextView>(R.id.tvSpotTitle).setTextColor(Color.parseColor("#000000"))
                    itemView.findViewById<TextView>(R.id.tvSpotSubTitle).setTextColor(Color.parseColor("#000000"))
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                }
            }
            itemView.findViewById<TextView>(R.id.tvSpotTitle).text = hotspot.name
            var tags:String=""
            for(i in hotspot.tags.indices){
                if(i==0){
                    tags=hotspot.tags.get(i)
                }else{
                   tags=tags+" #"+hotspot.tags.get(i).trim()
                }
            }
            itemView.findViewById<TextView>(R.id.tvSpotSubTitle).text = "#"+tags
            //itemView.findViewById<TextView>(R.id.tvSpotSubTitle).text = "#"+hotspot.description

            when (hotspot.hotness) {
                Constants.BLUE -> {
                    setPopularityType(mContext.getString(R.string.label_chill), ContextCompat.getColor(mContext, R.color.blueChill), R.drawable.flame_blue_listing_icon)
                }
                Constants.RED -> {
                    setPopularityType(mContext.getString(R.string.label_very_popular),
                            ContextCompat.getColor(mContext, R.color.redVeryPopular),
                            R.drawable.flame_red_listing_icon)
                }
                Constants.YELLOW -> {
                    setPopularityType(mContext.getString(R.string.label_Just_in),
                            ContextCompat.getColor(mContext, R.color.yellow),
                            R.drawable.flame_yellow_listing_icon)
                }
                else -> {
                    setPopularityType(mContext.getString(R.string.label_popular), ContextCompat.getColor(mContext, R.color.orangePopular),
                            R.drawable.flame_orange_listing_icon)
                }
            }
        }
    }

    private fun ExploreViewHolder.setPopularityType(popularityText: String, textColor: Int, popularity_icon: Int) {
        itemView.findViewById<TextView>(R.id.tvPopularityType).apply {
            text = popularityText
            setTextColor(textColor)
          try {
            val dr = resources.getDrawable(popularity_icon)
            val bitmap = (dr as BitmapDrawable).bitmap
            val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 28, 28, true))
            setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
          }
         catch (e : Exception){
           Log.d(TAG, "setPopularityType: e"+e.message.toString())
         }

        }
    }

    fun addList(list: List<Hotspot>) {
        //exploreList.clear()
        exploreList.addAll(list)
        notifyDataSetChanged()
    }

    fun addListWithClear(list: List<Hotspot>) {
        exploreList.clear()
        exploreList.addAll(list)
        notifyDataSetChanged()
    }

    fun clearList() {
        exploreList.clear()
        notifyDataSetChanged()
    }

    interface OnSpotItemClickListener {
        fun onHotSpotClick(id: String)
    }

    fun remove(size: Int) {
        if(size!=-1){
            exploreList.removeAt(size)
        }
    }

    fun addLoading() {
        exploreList.add(Loading())
        notifyItemInserted(itemCount - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        return when (viewType) {
            SpotItem.TYPE_SPOT -> {
                view = inflater.inflate(R.layout.rv_item_explore, parent, false)
                ExploreViewHolder(view)
            }
            else -> {
                view = inflater.inflate(R.layout.progress_bar_bottom, parent, false)
                ProgressHolder(view)
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder?.itemViewType) {
            SpotItem.TYPE_SPOT -> {
                spotViewHolder(holder as ExploreViewHolder, exploreList[position] as Hotspot)
            }
            else -> {
                progressViewHolder(holder as ProgressHolder)
            }
        }
    }

}
