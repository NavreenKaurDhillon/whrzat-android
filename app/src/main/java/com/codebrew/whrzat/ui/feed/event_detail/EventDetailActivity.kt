package com.codebrew.whrzat.ui.feed.event_detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.DatabaseUtils
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.codebrew.whrzat.R
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import java.lang.Exception
import android.text.util.Linkify
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.databinding.ActivityEventDetailBinding


class EventDetailActivity: AppCompatActivity() {

    private val TAG = "EventDetailActivity"
    private lateinit var binding: ActivityEventDetailBinding


    companion object{
        fun start(mContext: Context,spotName:String,des:String,address:String,time:String,pic:String,web:String){
            val intent = Intent(mContext, EventDetailActivity::class.java)
            intent.putExtra(Constants.SPOT_NAME,spotName)
            intent.putExtra(Constants.SPOT_DESCRIPTION,des)
            intent.putExtra(Constants.SPOT_ADDRESS,address)
            intent.putExtra(Constants.EVENT_TIME,time)
            intent.putExtra(Constants.PIC,pic)
            intent.putExtra(Constants.SPOT_WEBSITE,web)
            mContext.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_event_detail)
       // setContentView(R.layout.activity_event_detail)
        Log.d(TAG, "onCreate: StartActivity")
        setData()
        val window = window
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
    }

    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tvSpotTitle.setTextColor(Color.WHITE)
                binding.tvSpotDescription.setTextColor(Color.WHITE)
                binding.detailLayout.setBackgroundColor(Color.parseColor("#000000"))
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                binding.appbar.setBackgroundColor(Color.parseColor("#000000"))
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.tvSpotTitle.setTextColor(Color.parseColor("#000000"))
                binding.tvSpotDescription.setTextColor(Color.parseColor("#000000"))
                binding.detailLayout.setBackgroundColor(Color.WHITE)
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                binding.appbar.setBackgroundColor(Color.WHITE)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)

                //window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }


    fun setData(){
        val req = RequestOptions()
               // .transform(CenterCrop())
                .format(DecodeFormat.PREFER_ARGB_8888)
                .override(Target.SIZE_ORIGINAL)
                .placeholder(R.drawable.feed_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

        Glide.with(this)
                .load(intent.getStringExtra(Constants.PIC))
                .apply(req)
            .into(binding.ivSpot)
//                .into(ivSpot)

        binding.tvSpotTitle.text=intent.getStringExtra(Constants.SPOT_NAME)
        binding.tvSpotDescription.text=intent.getStringExtra(Constants.SPOT_DESCRIPTION)
        binding.tvSpotDescription.setLinkTextColor(getResources().getColor(R.color.light_blue));
        Linkify.addLinks(binding.tvSpotDescription, Linkify.WEB_URLS or Linkify.PHONE_NUMBERS)
        Linkify.addLinks(binding.tvSpotDescription, Linkify.ALL)
        binding.tvEventLocation.text=intent.getStringExtra(Constants.SPOT_ADDRESS)
        binding.tvEventTime.text=intent.getStringExtra(Constants.EVENT_TIME)

        if(intent.getStringExtra(Constants.SPOT_WEBSITE)!!.isNotEmpty()){
            binding.tvEventWebsite.visibility=View.VISIBLE
            binding.tvEventWebsite.text=intent.getStringExtra(Constants.SPOT_WEBSITE)
        }else{
            binding.tvEventWebsite.visibility=View.GONE
        }

    }
    fun onEventDetailClickData(v: View){
        when(v){
            binding.tvBack ->{
                finish()
            }
            binding.tvEventWebsite ->{
                if (binding.tvEventWebsite.text.isNotEmpty()) {
//                    if ( Patterns.WEB_URL.matcher(binding.tvEventWebsite.text).matches()) {
                        val url=binding.tvEventWebsite.text.toString() as String
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
//                    }
                }
            }
        }
    }
}