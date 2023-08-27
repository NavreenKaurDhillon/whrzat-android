package com.codebrew.whrzat.ui.allevents

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivityAllEventsBinding
import com.codebrew.whrzat.ui.createvent.EventActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.allevents.EventList
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import okhttp3.ResponseBody

class AllEventsActivity : AppCompatActivity(), View.OnClickListener, AllEventsContract.View {

    private lateinit var presenter: AllEventsContract.Presenter
    private lateinit var eventAdapter: AllEventsAdapter
    private lateinit var progressDialog: ProgressDialog
    private var isRefresh=false
    private lateinit var binding: ActivityAllEventsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_all_events)
       // setContentView(R.layout.binding.activityAllEvents)


        progressDialog = ProgressDialog(this)
        presenter = AllEventsPresenter()
        presenter.attachView(this)

        intent.getStringExtra(Constants.HOTSPOT_ID)?.let {
            (presenter as AllEventsPresenter).getEvents(Prefs.with(this).getString(Constants.USER_ID, ""),
                it
            )
        }
        clickListener()
        setAdapter()
        getDataFromIntent()
        val window = window
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        setFontType()

    }
    private fun setFontType() {
        val face_semi = Typeface.createFromAsset(assets, "fonts/opensans_semibold.ttf")
        val face_regular = Typeface.createFromAsset(assets, "fonts/opensans_regular.ttf")
        binding.tvAddEvents.setTypeface(face_semi)
        binding.tvEventsAt.setTypeface(face_semi)
        binding.tvEventTitle.setTypeface(face_semi)
    }

    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tvEventTitle.setTextColor(Color.WHITE)
                binding.activityAllEvents.setBackgroundColor(Color.parseColor("#000000"))
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor=ContextCompat.getColor(this, R.color.black)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.tvEventTitle.setTextColor(Color.parseColor("#000000"))
                binding.activityAllEvents.setBackgroundColor(Color.WHITE)
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
           //     window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }


    private fun getDataFromIntent() {
        binding.tvEventTitle.text = intent.getStringExtra(Constants.SPOT_NAME)
        val color = intent.getStringExtra(Constants.COLOR)
     /*   when (color) {
            Constants.BLUE -> {
                setHotspotColor(getString(R.string.label_chill), R.color.sky_blue, R.drawable.hotspot_detail_blue_icon)
            }
            Constants.RED -> {
                setHotspotColor(getString(R.string.label_very_popular), R.color.red_create, R.drawable.hotspot_detail_red_icon)
            }
            Constants.YELLOW -> {
                setHotspotColor(getString(R.string.label_Just_in), R.color.yellow, R.drawable.hotspot_detail_yellow_icon)
            }
            Constants.ORANGE -> {
                setHotspotColor(getString(R.string.label_popular), R.color.orangePopular, R.drawable.hotspot_detail_orange_icon)

            }
        }*/
    }

    private fun setHotspotColor(hotspotText: String, color: Int, icon: Int) {
        binding.tvEventsAt.text = hotspotText
        binding.tvEventsAt.setTextColor(ContextCompat.getColor(this, color))
        val dr = resources.getDrawable(icon)
        val bitmap = (dr as BitmapDrawable).bitmap
        val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 70, 70, true))
        binding.tvEventsAt.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
    }

    private fun setAdapter() {
        eventAdapter = AllEventsAdapter(this)
        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        binding.rvEvents.adapter = eventAdapter
        binding.rvEvents.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    private fun clickListener() {
        binding.tvAddEvents.setOnClickListener(this)
        binding.tvBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvAddEvents -> {
                if (intent.getBooleanExtra(Constants.IS_CHECK_IN, false)) {
                    val addEventIntent = Intent(this, EventActivity::class.java)
                    addEventIntent.putExtra(Constants.HOTSPOT_ID, intent.getStringExtra(Constants.HOTSPOT_ID))
                    addEventIntent.putExtra(Constants.CREATED_BY, intent.getStringExtra(Constants.CREATED_BY))
                    addEventIntent.putExtra(Constants.COLOR, intent.getStringExtra(Constants.COLOR))
                    addEventIntent.putExtra(Constants.IS_CHECK_IN, true)
                    addEventIntent.putExtra(Constants.SPOT_NAME, intent.getStringExtra(Constants.SPOT_NAME))
                    startActivityForResult(addEventIntent, Constants.REQ_CODE_ADD_EVENT)
                } else {
                    GeneralMethods.showToast(this, R.string.label_need_to_checkin_add)
                }
            }
            R.id.tvBack -> {
                finish()
            }
        }
    }

    override fun successEventsApi(events: List<EventList>) {
        eventAdapter.addList(events)
    }

    override fun failureApi() {
        GeneralMethods.showToast(this, R.string.error_server_busy)
    }

    override fun errorApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(this, errorBody)
    }

    override fun sessionExpire() {
        GeneralMethods.tokenExpired(this)
    }

    override fun dismissLoading() {
        progressDialog.dismiss()

    }

    override fun onBackPressed() {
        if(isRefresh){
            setResult(Activity.RESULT_OK)
        }else{
            setResult(Activity.RESULT_CANCELED)

        }
        super.onBackPressed()

    }

    override fun showLoading() {
        progressDialog.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }


    override fun attachBaseContext(newBase: Context?) {
        //super.attachBaseContext(newBase);
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!));
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            isRefresh=true
            intent.getStringExtra(Constants.HOTSPOT_ID)
                ?.let { presenter.getEvents(Prefs.with(this).getString(Constants.USER_ID, ""), it) }
        }
    }

}
