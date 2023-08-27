package com.codebrew.whrzat.ui.createvent

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivityCreateEventBinding
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.HotspotDetail.SendEventData
import es.dmoral.toasty.Toasty
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import java.util.*


class EventActivity : AppCompatActivity(), EventContract.View, View.OnClickListener,
  TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

  private val TAG = "EventActivity"
  private lateinit var presenter: EventContract.Presenter
  private lateinit var progressDialog: ProgressDialog

  // private var datePicker=DatePickerFragment()
  private var dateInMillis: Long = 0
  private lateinit var cal: Calendar
  private var isStartDateClicked = false
  private var startTime: Long = 0L
  private var endTime: Long = 0L
  private var hotspotId = ""
  private var createdBy = ""
  private var day = 0
  private var monthSelected = 0
  private var yearSelected = 0
  private lateinit var calendar: Calendar
  private var startHour = 0
  private lateinit var binding: ActivityCreateEventBinding


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_create_event)
//        setContentView(R.layout.activity_create_event)

    Log.d(TAG, "onCreate: StartActivity")

    setClickListener()
    progressDialog = ProgressDialog(this)
    presenter = EventPresenter()
    presenter.attachView(this)

    calendar = Calendar.getInstance(TimeZone.getDefault())
    yearSelected = calendar.get(Calendar.YEAR)
    monthSelected = calendar.get(Calendar.MONTH)
    day = calendar.get(Calendar.DAY_OF_MONTH)
    getDataFromIntent()
    val window = window
    // enable night mode
    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
    enableNightmode()
    setFontType()
    val dr = resources.getDrawable(R.drawable.add_event_star_icon)
    val bitmap = (dr as BitmapDrawable).bitmap
    val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 60, 60, true))
    binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
  }

  private fun setFontType() {
    val face_semi = Typeface.createFromAsset(assets, "fonts/opensans_semibold.ttf")
    val face_regular = Typeface.createFromAsset(assets, "fonts/opensans_regular.ttf")
    binding.tvTitle.setTypeface(face_semi)
    binding.tvPopularityType.setTypeface(face_semi)
    binding.tvSpotName.setTypeface(face_semi)
    binding.tvCancel.setTypeface(face_semi)
    binding.tvCreate.setTypeface(face_semi)
  }

  @SuppressLint("NewApi")
  private fun enableNightmode() {
    val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
    when (mode) {
      Configuration.UI_MODE_NIGHT_YES -> {
        binding.tvTitle.setTextColor(Color.WHITE)
        binding.tvCancel.setTextColor(Color.WHITE)
        binding.tvSpotName.setTextColor(Color.WHITE)
        binding.llMain.setBackgroundColor(Color.parseColor("#000000"))
        binding.toolbarSpot.setBackgroundColor(Color.parseColor("#000000"))
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

      }

      Configuration.UI_MODE_NIGHT_NO -> {
        binding.tvTitle.setTextColor(Color.parseColor("#000000"))
        binding.tvCancel.setTextColor(Color.parseColor("#000000"))
        binding.tvSpotName.setTextColor(Color.parseColor("#000000"))
        binding.llMain.setBackgroundColor(Color.WHITE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        // window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
      }

      Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
    }
  }

  private fun getDataFromIntent() {
    if (intent.getStringExtra(Constants.HOTSPOT_ID) != null) {
      hotspotId = intent.getStringExtra(Constants.HOTSPOT_ID)!!
    }
    if (intent.getStringExtra(Constants.CREATED_BY) != null) {
      createdBy = intent.getStringExtra(Constants.CREATED_BY)!!
    }

    binding.tvSpotName.text = intent.getStringExtra(Constants.SPOT_NAME)
    val color = intent.getStringExtra(Constants.COLOR)
    when (color) {
      Constants.BLUE -> {
        setHotspotColor(
          getString(R.string.label_chill),
          R.color.sky_blue,
          R.drawable.hotspot_detail_blue_icon
        )
      }

      Constants.RED -> {
        setHotspotColor(
          getString(R.string.label_very_popular),
          R.color.red_create,
          R.drawable.hotspot_detail_red_icon
        )
      }

      Constants.YELLOW -> {
        setHotspotColor(
          getString(R.string.label_Just_in),
          R.color.yellow,
          R.drawable.hotspot_detail_yellow_icon
        )
      }

      Constants.ORANGE -> {
        setHotspotColor(
          getString(R.string.label_popular),
          R.color.orangePopular,
          R.drawable.hotspot_detail_orange_icon
        )

      }
    }
  }

  private fun setHotspotColor(hotspotText: String, color: Int, icon: Int) {
//        binding.tvPopularityType.text = hotspotText
//        binding.tvPopularityType.setTextColor(ContextCompat.getColor(this, color))
//        val dr = resources.getDrawable(icon)
//        val bitmap = (dr as BitmapDrawable).bitmap
//        val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 70, 70, true))
//        binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
//       // binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
//
    //xml code

    binding.tvPopularityType.text = hotspotText
    binding.tvPopularityType.setTextColor(ContextCompat.getColor(this, color))
    val db: Drawable? = ContextCompat.getDrawable(this, icon)
    binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
      db,
      null,
      null,
      null
    )
    //here
    binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(db, null, null, null)
  }

  private fun setClickListener() {
    binding.tvCreate.setOnClickListener(this)
    binding.tvStartDate.setOnClickListener(this)
    binding.tvEndDate.setOnClickListener(this)
    binding.tvCancel.setOnClickListener(this)

  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.tvCreate -> {
        if (startTime != null && endTime != null) {
          if (intent.getBooleanExtra(Constants.IS_CHECK_IN, false)) {
            if (checkValidation()) {
              val sendData = SendEventData(
                binding.etEventName.text.toString().trim(),
                binding.etDescription.text.toString().trim(),
                startTime,
                endTime,
                Prefs.with(this).getString(Constants.USER_ID, ""),
                hotspotId
              )
              presenter.createEventApi(sendData)
              // Toasty.error(this,"craete event", Toast.LENGTH_LONG).show()
            }

          } else {
            GeneralMethods.showToast(this, R.string.label_need_to_checkin)

          }
        } else {
          GeneralMethods.showToast(this, R.string.error_enter_time)

        }
      }

      R.id.tvStartDate -> {
        GeneralMethods.hideSoftKeyboard(this, binding.etDescription)
        isStartDateClicked = true

        val obj = DatePickerFragment()
        obj.setListener(this)
        obj.setDate(dateInMillis)
        obj.setCalenderType(DatePickerFragment.TYPE_MIN_TODAY)
        obj.show(supportFragmentManager, "my_datepicker")
        cal = Calendar.getInstance()

      }

      R.id.tvEndDate -> {
        GeneralMethods.hideSoftKeyboard(this, binding.etDescription)
        isStartDateClicked = false
        val obj = DatePickerFragment()
        obj.setListener(this)
        obj.setDate(dateInMillis)
        obj.setCalenderType(DatePickerFragment.TYPE_MIN_TODAY)
        obj.show(supportFragmentManager, "my_end_date")
        cal = Calendar.getInstance()


        /*   val obj = DatePickerFragment()
           obj.setListener(this)
           obj.setDate(dateInMillis)
           obj.setCalenderType(DatePickerFragment.TYPE_MIN_TODAY)
           obj.show(supportFragmentManager, "my_end_date")
           cal = Calendar.getInstance()*/

      }

      R.id.tvCancel -> {
        finish()
      }
    }

  }

  private fun checkValidation(): Boolean {
    val c = Calendar.getInstance()
    val currenttime = c.timeInMillis + 86400000

    if (binding.etEventName.text.toString().trim().isEmpty()) {
      Toasty.error(this, "Enter the Event Name", Toast.LENGTH_LONG).show()
      return false
    } else if (binding.etDescription.text.toString().trim().isEmpty()) {
      Toasty.error(this, "Enter the Description", Toast.LENGTH_LONG).show()
      return false
    } else if (binding.tvStartDate.text.toString().trim().isEmpty()) {
      Toasty.error(this, "Enter the Start Time", Toast.LENGTH_LONG).show()
      return false
    } else if (binding.tvEndDate.text.toString().trim().isEmpty()) {
      Toasty.error(this, "Enter the End Time", Toast.LENGTH_LONG).show()
      return false
    } else if (endTime > currenttime) {
      Toasty.error(
        this,
        "You cannot create an event for more than 24 hours from the time of event creation.",
        Toast.LENGTH_LONG
      ).show()
      return false
    }


    return true
  }

  override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
    yearSelected = p1
    monthSelected = p2
    day = p3
    if (isStartDateClicked) {
      val timePicker = TimePicker()
      timePicker.setListener(this)
      timePicker.show(supportFragmentManager, "time_start")
    } else {
      val timePicker = TimePicker()
      timePicker.setListener(this)
      timePicker.show(supportFragmentManager, "time_end")
    }

  }

  override fun successEventApi() {
    Toasty.success(this, getString(R.string.lable_events_created), Toast.LENGTH_LONG).show()
    // GeneralMethods.showToast(this, R.string.lable_events_created)
    setResult(Activity.RESULT_OK)
    finish()
  }

  override fun errorEventApi(errorBody: ResponseBody) {
    GeneralMethods.showErrorMsg(this, errorBody)
  }

  override fun failureEventApi() {
    GeneralMethods.showToast(this, R.string.error_server_busy)
  }

  override fun sessionExpire() {
    GeneralMethods.tokenExpired(this)
  }

  override fun invalidName() {
  }

  override fun invalidDescription() {
  }

  override fun invalidStartDate() {
  }

  override fun invalidEndDate() {
  }

  override fun showLoading() {
    progressDialog.show()
  }

  override fun dismissLoading() {
    progressDialog.dismiss()
  }

  val currentDateTimeString = ""
  override fun onTimeSet(view: android.widget.TimePicker?, hourOfDay: Int, minute: Int) {

    /*var aMpM = ""
    if (hourOfDay > 11) {
        aMpM = "PM"
    } else {
        aMpM = "AM"

    }*/


    val sdf = SimpleDateFormat("MMM d, hh:mm a")
    val datetime = Calendar.getInstance()
    val c = Calendar.getInstance()
    datetime.set(Calendar.HOUR_OF_DAY, hourOfDay)
    datetime.set(Calendar.MINUTE, minute)
    datetime.set(yearSelected, monthSelected, day, hourOfDay, minute, 0)
    if (isStartDateClicked) {
      if (datetime.timeInMillis > c.timeInMillis) {
        binding.tvStartDate.text = null
        val currentDateTimeString = sdf.format(datetime.timeInMillis)
        binding.tvStartDate.text = currentDateTimeString
        startHour = hourOfDay
        //binding.tvStartDate.append(hourOfDay.toString() + ":" + minute.toString() + " " + aMpM)
        // val calendar = Calendar.getInstance()
      } else {
        Toasty.error(this, getString(R.string.error_start_time_low), Toast.LENGTH_SHORT, true)
          .show()
      }

      //calendar.set(yearSelected, monthSelected, day, hourOfDay, minute, 0)
      startTime = datetime.timeInMillis
      isStartDateClicked = false

    } else {
      binding.tvEndDate.text = null
      val currentDateTimeString = sdf.format(datetime.timeInMillis)
      endTime = datetime.timeInMillis
      if (endTime < startTime || endTime - startTime > 86400000) {
        endTime = 0
        Toasty.error(this, "Event duration cannot be more than 24 hours", Toast.LENGTH_SHORT, true)
          .show()
      } else {
        binding.tvEndDate.text = currentDateTimeString
      }
      /*else {
          if (hourOfDay < startHour) {
              binding.tvEndDate.text = currentDateTimeString
              endTime = datetime.timeInMillis + 86400000
          } else {
              binding.tvEndDate.text = currentDateTimeString
              endTime = datetime.timeInMillis
          }
      }*/

    }
  }

  override fun attachBaseContext(newBase: Context?) {
    // super.attachBaseContext(newBase);
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!));
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.detachView()
  }

}
