package com.codebrew.whrzat.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.adapter.TutorailImageAdapter
import com.codebrew.whrzat.databinding.ActivityTutorialsBinding
import com.codebrew.whrzat.ui.fblogin.FbLoginActivity
import java.util.ArrayList
import com.codebrew.whrzat.util.ParallaxPageTransformer
import io.github.inflationx.viewpump.ViewPumpContextWrapper


class TutorialActivity :AppCompatActivity(), ViewPager.OnPageChangeListener {

    private  val TAG = "TutorialActivity"


    private lateinit var mImages: MutableList<Int>
    private lateinit var mLandingAdapter: TutorailImageAdapter
    private var mDotsCount: Int = 0
    private lateinit var binding: ActivityTutorialsBinding
    private var ivDots: Array<ImageView?>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_tutorials)
//        setContentView(R.layout.activity_tutorials)
        Log.d(TAG, "onCreate: StartActivity")

        init()

        binding.tvSkip.setOnClickListener{
            val intent = Intent(this, FbLoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        val window = window
        setUpIndicator()
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()

    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.llmain.setBackgroundColor(Color.parseColor("#000000"))
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor=ContextCompat.getColor(this, R.color.black)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
              binding.llmain.setBackgroundColor(Color.WHITE)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
           //     window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun setViewPager() {
        /*val pageTransformer = ParallaxPageTransformer()
                .addViewToParallax(ParallaxPageTransformer.ParallaxTransformInformation(R.id.ivBackground, 2f, 2f))
                .addViewToParallax(ParallaxPageTransformer.ParallaxTransformInformation(R.id.tvHeading, -0.75f,
                        ParallaxPageTransformer.ParallaxTransformInformation.PARALLAX_EFFECT_DEFAULT))
                .addViewToParallax(ParallaxPageTransformer.ParallaxTransformInformation(R.id.tvSubHeading, -1.05f,
                        ParallaxPageTransformer.ParallaxTransformInformation.PARALLAX_EFFECT_DEFAULT))*/

       // val adapter = CommonFragmentPagerAdapter(supportFragmentManager)
      //  vpTutorial.setAdapter(adapter)
      //  vpTutorial.setPageTransformer(true, pageTransformer)

    }

    private fun init() {
        val pageTransformer = ParallaxPageTransformer()
                .addViewToParallax(ParallaxPageTransformer.ParallaxTransformInformation(R.id.tvTopTitle, 2f, 2f))
                .addViewToParallax(ParallaxPageTransformer.ParallaxTransformInformation(R.id.tvBottomTitle, 2f, 2f))
                .addViewToParallax(ParallaxPageTransformer.ParallaxTransformInformation(R.id.ivImage, -0.75f,
                        ParallaxPageTransformer.ParallaxTransformInformation.PARALLAX_EFFECT_DEFAULT))
        mImages = ArrayList()
       binding.viewPager.addOnPageChangeListener(this)

        addImagesToList()
        mLandingAdapter = TutorailImageAdapter(this, mImages)
        binding.viewPager?.adapter = mLandingAdapter
        binding.viewPager?.setPageTransformer(true,pageTransformer)
    }

    private fun addImagesToList() {
        mImages.add(R.drawable.image_walkthrough_1)
        mImages.add(R.drawable.image_walkthrough_2)
        mImages.add(R.drawable.image_walkthrough_3)
    }

    private fun setUpIndicator() {
        mDotsCount = mLandingAdapter.count
        ivDots = arrayOfNulls<ImageView>(mDotsCount)

        for (i in 0..mDotsCount - 1) {
            (ivDots as Array<ImageView?>)[i] = ImageView(this)

            // Set all dots to de-selected by default
            (ivDots as Array<ImageView?>)[i]?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.unselected_dot, null))

            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)


            params.setMargins(8, 0, 8, 0)  // Set left and right margins for the dots

            binding.llIndicator!!.addView((ivDots as Array<ImageView?>)[i], params)    // Add dots to the Linear Layout
        }
        if (mDotsCount > 0)
            (ivDots as Array<ImageView?>)[0]?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.selected_dots, null))


    }


    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        for (i in 0..mDotsCount - 1) {
            ivDots?.get(i)?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.unselected_dot, null))

            ivDots?.get(position)?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.selected_dots, null))

            if(position==2){
                binding.tvSkip.text=getString(R.string.label_done)
            }else{
                binding.tvSkip.text=getString(R.string.label_skip)
            }

        }
    }

    override fun attachBaseContext(newBase: Context?) {
      //  super.attachBaseContext(newBase);
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!));
    }
}
