package com.codebrew.whrzat.adapter

import android.content.Context
import android.util.Log
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R

class TutorailImageAdapter(private val mContext: Context, private val mImages: List<Int>) : PagerAdapter() {

    private  val TAG = "TutorailImageAdapter"

    override fun getCount(): Int = mImages.size

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = LayoutInflater.from(mContext).inflate(R.layout.item_viewpager_tutorial, container, false)

        Log.d(TAG, "instantiateItem: $position")
        when (position) {
            0 -> {
                itemView.findViewById<TextView>(R.id.tvTopTitle).text = mContext.getString(R.string.label_walk_1_top)
                itemView.findViewById<TextView>(R.id.tvBottomTitle).setText(R.string.label_wak_1_bottom)
            }
            1 -> {
                itemView.findViewById<TextView>(R.id.tvTopTitle).text = mContext.getString(R.string.label_walk_2_top)
                itemView.findViewById<TextView>(R.id.tvBottomTitle).setText(R.string.label_wak_2_bottom)
            }
            2 -> {
                itemView.findViewById<TextView>(R.id.tvTopTitle).text = mContext.getString(R.string.label_walk_3_top)
                itemView.findViewById<TextView>(R.id.tvBottomTitle).setText(R.string.label_wak_3_bottom)
            }
        }

        /*  val pageTransformer = ParallaxPageTransformer()
          .addViewToParallax(ParallaxPageTransformer.ParallaxTransformInformation(R.id.tvTopTitle, 2f, 2f))
          .addViewToParallax(ParallaxPageTransformer.ParallaxTransformInformation(R.id.tvBottomTitle, -0.75f,
                  ParallaxPageTransformer.ParallaxTransformInformation.PARALLAX_EFFECT_DEFAULT))
          .addViewToParallax(ParallaxPageTransformer.ParallaxTransformInformation(R.id.ivImage, -1.05f,
                  ParallaxPageTransformer.ParallaxTransformInformation.PARALLAX_EFFECT_DEFAULT))*/
        val mDefaultBackground = mContext.resources.getDrawable(R.drawable.circular_placeholder_products)

        val reqs = RequestOptions()
        reqs.error(mDefaultBackground)

        Glide.with(mContext)
                .load(mImages[position])
                .apply(reqs)
                .into(itemView.findViewById(R.id.ivImage))
        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as RelativeLayout)
    }
}


