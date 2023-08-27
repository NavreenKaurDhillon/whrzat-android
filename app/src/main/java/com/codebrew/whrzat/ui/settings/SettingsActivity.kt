package com.codebrew.whrzat.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.arvind.imageandlocationmanager.activities.ImageGetterActivity
import com.codebrew.whrzat.BuildConfig
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.RewardActivity
import com.codebrew.whrzat.activity.SplashActivity
import com.codebrew.whrzat.databinding.ActivitySettingsBinding
import com.codebrew.whrzat.event.FeedApi
import com.codebrew.whrzat.event.MapReferesh
import com.codebrew.whrzat.ui.fblogin.TermsCondDFragment
import com.codebrew.whrzat.ui.settings.Notification.NotificationFragment
import com.codebrew.whrzat.ui.settings.blocked.BlockedFragment
import com.codebrew.whrzat.ui.settings.changepassword.ChangePasswrod
import com.codebrew.whrzat.ui.settings.editProfile.EditProfileFragment
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.Referalcodedata
import com.codebrew.whrzat.webservice.pojo.RewardDetailsData
//import com.facebook.login.LoginManager
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.io.File


class SettingsActivity : ImageGetterActivity(), View.OnClickListener, ImageGetterActivity.ImagePickerForFragment,
        SettingsContract.View, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, ChangepassDialog.OnOkClickListener {


    private  val TAG = "SettingsActivity"
    private lateinit var progressDialog: ProgressDialog
    private lateinit var presenter: SettingsContract.Presenter
    private lateinit var binding: ActivitySettingsBinding
    private var userId = ""
    private var radius = 0
    private var distance = 0
    private var terms: TermsCondDFragment = TermsCondDFragment()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_settings)
//        setContentView(R.layout.activity_settings)
        Log.d(TAG, "onCreate: StartActivity")

        userId = Prefs.with(this).getString(Constants.USER_ID, "")
        progressDialog = ProgressDialog(this)
        presenter = SettingsPresenter()
        presenter.attachView(this)

        if (Prefs.with(this).getBoolean(Constants.IS_INFINITY, false)) {
           binding. seekBarNew.progress = 200
            binding.  tvMiles.visibility = View.GONE
        } else {
            if (Prefs.with(this).getString(Constants.RADIUS, "").isNotEmpty()) {
                val value = Prefs.with(this).getString(Constants.RADIUS, "")
                binding.    tvMiles.text = value
                binding.    seekBarNew.progress = value.toInt()
            }
        }

        setCamera()
        binding.  sbFeed.isChecked = Prefs.with(this).getBoolean(Constants.ISFEEDONBOOL, false)

        clickListeners()

        if (Prefs.with(this).getBoolean(Constants.FACEBOOK_LOGIN, false)) {
            binding.    tvChangePass.visibility = View.GONE
        } else {
            binding.    tvChangePass.visibility = View.GONE

        }

        binding.  tvVersionName.text = "Version " + GeneralMethods.getAppVersion(this)
        enableNightmode()
        setFontType()
    }
    private fun setFontType() {
        val face_semi = Typeface.createFromAsset(assets, "fonts/opensans_semibold.ttf")
        val face_regular = Typeface.createFromAsset(assets, "fonts/opensans_regular.ttf")
        binding.   tvTitle.setTypeface(face_semi)
        binding.  tvVersionName.setTypeface(face_regular)
    }
    override fun onResume() {
        super.onResume()
        presenter.apigetRewardsDetails(Prefs.with(this).getString(Constants.ACCESS_TOKEN,""))
    }

    private fun setCamera() {
        setAuthorityForURI(BuildConfig.APPLICATION_ID)
        setImageLocation(ImageGetterActivity.EXTERNAL_STORAGE_APP_DIRECTORY_DEFAULT, getString(R.string.app_name))
        setCropperEnabled(true, ImageGetterActivity.SHAPE_RECT)
        setMinCropSize(700, 700)
    }

    private fun clickListeners() {
        binding.  tvLogout.setOnClickListener(this)
        binding.  tvEditProfile.setOnClickListener(this)
        binding. tvBlockedContacts.setOnClickListener(this)
        binding. tvSyncContacts.setOnClickListener(this)
        binding. tvHelpSupport.setOnClickListener(this)
        binding. tvNotification.setOnClickListener(this)
        binding. sbFeed.setOnCheckedChangeListener(this)
        binding. seekBarNew.setOnSeekBarChangeListener(this)
        binding. tvCenterRange.setOnClickListener(this)
        binding. tvBack.setOnClickListener(this)
        binding. tvChangePass.setOnClickListener(this)
        binding. tvTermsPolicy.setOnClickListener(this)
        binding. tvDeleteProfile.setOnClickListener(this)
        binding.  tvreferfriend.setOnClickListener(this)
        binding. tvreward.setOnClickListener(this)
    }


    override fun onProgressChanged(p0: SeekBar?, distance: Int, p2: Boolean) {
        this.distance = distance


        // when {
        /*distance in 101..120 -> {
            seekBarNew?.progress = 100
            Prefs.with(this).save(Constants.IS_INFINITY, false)
            Prefs.with(this).save(Constants.RADIUS, 100.toString())
        }*/
        /*distance > 120 -> {
            seekBarNew?.progress = 200
            tvMiles.visibility = View.GONE
            Prefs.with(this).save(Constants.IS_INFINITY, true)
        }*/
        //else -> {
        Prefs.with(this).save(Constants.IS_INFINITY, false)
        binding.  seekBarNew.progress = distance
        binding.  tvMiles.visibility = View.VISIBLE
        binding.  tvMiles.text = distance.toString()
        Prefs.with(this).save(Constants.IS_INFINITY, false)
        Prefs.with(this).save(Constants.RADIUS, distance.toString())
        //  }
        //     }


        EventBus.getDefault().postSticky(MapReferesh(true))


    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.   tvTitle.setTextColor(Color.WHITE)
                binding.   llSetting.setBackgroundColor(Color.parseColor("#000000"))
                binding.  toolbar.setBackgroundColor(Color.parseColor("#000000"))
                binding.  tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor= ContextCompat.getColor(this, R.color.black)
                binding.  tvLogout.setColorFilter(getResources().getColor(R.color.white))
                binding.  tvNotification.setTextColor(Color.WHITE)
                binding.  tvBlockedContacts.setTextColor(Color.WHITE)
                binding.  sbFeed.setTextColor(Color.WHITE)
                binding.  tvRadius.setTextColor(Color.WHITE)
                binding.  tvreferfriend.setTextColor(Color.WHITE)
                binding. tvreward.setTextColor(Color.WHITE)
                binding. tvTermsPolicy.setTextColor(Color.WHITE)
                binding. tvHelpSupport.setTextColor(Color.WHITE)
                binding. tvVersionName.setTextColor(Color.WHITE)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.  tvTitle.setTextColor(Color.parseColor("#000000"))
                binding.  toolbar.setBackgroundColor(Color.WHITE)
               // tvTotalEvents.setTextColor(Color.parseColor("#000000"))
                binding.   llSetting.setBackgroundColor(Color.WHITE)
                binding.   tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
               // window.navigationBarColor= ContextCompat.getColor(this, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }
    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekbar: SeekBar?) {

    }


    override fun onCheckedChanged(button: CompoundButton?, isChecked: Boolean) {
        when (button?.id) {
            R.id.tvCenterRange -> {
                binding. seekBarNew.progress = 100
                binding. tvMiles.text = 100.toString()
                EventBus.getDefault().postSticky(MapReferesh(true))
                Prefs.with(this).save(Constants.IS_INFINITY, false)
                Prefs.with(this).save(Constants.RADIUS, 100.toString())
            }
            R.id.sbFeed -> {
                if (GeneralMethods.isNetworkActive(this)) {
                    if (isChecked) {
                        presenter.apiFeedRadius(userId, radius, isChecked)
                    } else {
                        presenter.apiFeedRadius(userId, radius, isChecked)
                    }
                    //isFeedOn = isChecked
                    Prefs.with(this).save(Constants.ISFEEDONBOOL, isChecked)
                    EventBus.getDefault().postSticky(FeedApi(true))
                } else {
                    Toasty.error(this, getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun file(imageFile: File?) {

    }

    override fun openImagePickerFromFragment() {
        openImageSelector()
    }

    override fun setImagePickedListener(listener: OnImageSelectListener?) {
        setOnImageSelectListener(listener)
    }

    override fun onButtonClick(oldPassword: String, newPassword: String, confirmPassword: String) {
        val userId = Prefs.with(this).getString(Constants.USER_ID, "")
        presenter.apiChangePass(userId, oldPassword, newPassword, confirmPassword)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvLogout -> {
                logOutDialog()
            }
            R.id.tvBlockedContacts -> {
                if (supportFragmentManager.findFragmentByTag("block_fragment") == null) {
                    supportFragmentManager.inTransaction {
                        add(android.R.id.content, BlockedFragment(), "block_fragment")
                    }
                }

            }
            R.id.tvNotification -> {
                if (supportFragmentManager.findFragmentByTag("notification_fragment") == null) {
                    supportFragmentManager.inTransaction {
                        add(android.R.id.content, NotificationFragment(), "notification_fragment")
                    }
                }
            }

            R.id.tvEditProfile -> {
                if (supportFragmentManager.findFragmentByTag("edit_fragment") == null) {
                    supportFragmentManager.inTransaction {
                        add(android.R.id.content, EditProfileFragment(), "edit_fragment")
                    }
                }
            }

            R.id.tvSyncContacts -> {
                //getPermissionToReadUserContacts()
            }

            R.id.tvBack -> {
                finish()
            }

            R.id.tvHelpSupport -> {
                val url = "https://whrzat.com"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }

            R.id.tvTermsPolicy -> {

                if (terms.dialog == null || !terms.dialog!!.isShowing) {
                    GeneralMethods.hideSoftKeyboard(this@SettingsActivity,  binding. tvBlockedContacts)
                    val bundle = Bundle()
                    bundle.putString(Constants.TERMS_LINK, "0")
                    terms.arguments = bundle
                    terms.show(supportFragmentManager, "tag_1")
                }

            }

            R.id.tvChangePass -> {
                if (supportFragmentManager.findFragmentByTag("change") == null) {
                    supportFragmentManager.inTransaction {
                        add(android.R.id.content, ChangePasswrod(), "change")
                    }
                }
            }
            R.id.tvDeleteProfile -> {
                deleteProfileDialog()
            }

            R.id.tvreferfriend->{
                shareApp()
            }
            R.id.tvreward->{
                var intent=Intent(this,RewardActivity::class.java)
                intent.putExtra("locationStatus",locationStatus)
                intent.putExtra("isRedeem",isRedeem)
                intent.putExtra("rewardAmount",rewardAmount)
                intent.putExtra("redeemRequest",redeemRequest)
                startActivity(intent)
            }

            // val changePass = ChangepassDialog(this)
            //changePass.setListener(this)
            //changePass.show()
        }
    }
    private fun shareApp() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = "Join me on WhrzAt, a perfect app to create Hotspots,"+"\n" +
                "share pictures and create events which guide other community members to where the most interesting places to be at a particular time is.\n" +"\n"+
                "Enter my code "+Prefs.with(this).getString(Constants.Referralcode,"")+" to earn \$5 back after posting your first picture or creating a hotspot!\n"+ "Download the app: http://onelink.to/a3gr8n"
        // sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share using"))
    }

    override fun apiSuccessChagnePassword() {
        GeneralMethods.showToast(this, R.string.password_change)
    }
     var locationStatus:Boolean?=null
     var isRedeem:Boolean?=null
     var rewardAmount:String?=null
     var redeemRequest:Boolean?=null

    override fun apiSuccessRewardDetails(data: RewardDetailsData) {
        Log.e("Setting", " ----------- getRewardDetail() " + (data))

         locationStatus=data.data?.finalResponse?.locationStatus
         isRedeem=data.data?.finalResponse?.isRedeem
         rewardAmount=data.data?.finalResponse?.rewardAmount
         redeemRequest=data.data?.finalResponse?.redeemRequest
        if (data.data?.finalResponse?.rewardStatus==true){
            binding. tvreferfriend.visibility=View.VISIBLE
            binding. tvreward.visibility= View.VISIBLE
        }else{
            binding.  tvreferfriend.visibility=View.GONE
            binding.  tvreward.visibility= View.GONE
        }
    }

    //AlertDialog.Builder(this, R.style.MyDialog)
    private fun logOutDialog() {
      val builder = android.app.AlertDialog.Builder(this)
      val customView : View = layoutInflater.inflate(R.layout.custom_layout2, null);
      builder.setView(customView);
      val textViewTitle = customView.findViewById<TextView>(R.id.heading2)
      val textViewMessage = customView.findViewById<TextView>(R.id.description2)
      textViewTitle.setText(R.string.dialog_do_you_want_to_logout)
      // Setting DialogHelp Message
      textViewMessage.visibility = View.INVISIBLE
      val okButton = customView.findViewById<TextView>(R.id.noBT)
      val noButton = customView.findViewById<TextView>(R.id.okBT)
      okButton.setText("Log Out")
      noButton.setText("Cancel")

      builder.setView(customView)
      // Create the AlertDialog
      val alertDialog: android.app.AlertDialog = builder.create()
      // Set other dialog properties
      alertDialog.setCancelable(false)

      okButton.setOnClickListener {
        if (GeneralMethods.isNetworkActive(this)) {
          presenter.apiLogout()
        } else {
          GeneralMethods.showToast(this, R.string.error_no_connection)
        }
      }
      noButton.setOnClickListener {
        alertDialog.dismiss()
      }
      alertDialog.show()



//        AlertDialog.Builder(this)
//                .setTitle(R.string.dialog_do_you_want_to_logout)
//                .setPositiveButton(android.R.string.ok) { _, i ->
//
//                    if (GeneralMethods.isNetworkActive(this)) {
//                        presenter.apiLogout()
//                    } else {
//                        GeneralMethods.showToast(this, R.string.error_no_connection)
//                    }
//                }
//            .setNegativeButton(android.R.string.cancel, null)
//                .setCancelable(true)
//                .show()
    }

    private fun deleteProfileDialog() {
      val builder = android.app.AlertDialog.Builder(this)
      val customView : View = layoutInflater.inflate(R.layout.custom_layout2, null);
      builder.setView(customView);
      val textViewTitle = customView.findViewById<TextView>(R.id.heading2)
      val textViewMessage = customView.findViewById<TextView>(R.id.description2)
      textViewTitle.setText(R.string.dialog_do_you_want_to_delete_profile)
      // Setting DialogHelp Message
      textViewMessage.visibility = View.INVISIBLE
      val okButton = customView.findViewById<TextView>(R.id.noBT)
      val noButton = customView.findViewById<TextView>(R.id.okBT)
      okButton.setText("Delete")
      noButton.setText("Cancel")

      builder.setView(customView)
      // Create the AlertDialog
      val alertDialog: android.app.AlertDialog = builder.create()
      // Set other dialog properties
      alertDialog.setCancelable(false)

      okButton.setOnClickListener {
        if (GeneralMethods.isNetworkActive(this)) {
          val userId = Prefs.with(this).getString(Constants.USER_ID, "")
          presenter.apiDeleteProfile(userId)
        } else {
          GeneralMethods.showToast(this, R.string.error_no_connection)
        }
      }
      noButton.setOnClickListener {
        alertDialog.dismiss()
      }
      alertDialog.show()



//      AlertDialog.Builder(this)
//                .setTitle(R.string.dialog_do_you_want_to_delete_profile)
//                .setPositiveButton(android.R.string.ok, { dialogInterface, i ->
//
//                    if (GeneralMethods.isNetworkActive(this)) {
//                        val userId = Prefs.with(this).getString(Constants.USER_ID, "")
//                        presenter.apiDeleteProfile(userId)
//                    } else {
//                        GeneralMethods.showToast(this, R.string.error_no_connection)
//                    }
//                })
//                .setNegativeButton(android.R.string.cancel, null)
//                .setCancelable(true)
//                .show()
    }

    override fun apiFeedSuccess() {
    }

    override fun apiDeleteProfileSuccess() {
//        Toasty.error(this,"Your profile has been deleted", Toast.LENGTH_LONG).show()
        //Your profile has been deleted
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        LoginManager.getInstance().logOut()

        val lat = Prefs.with(this).getString(Constants.LAT, "")
        val lng = Prefs.with(this).getString(Constants.LNG, "")
        val apkVersion = Prefs.with(this).getInt(Constants.APK_VERSION, 0)

        Prefs.with(this).removeAll()

        Prefs.with(this).save(Constants.LAT, lat)
        Prefs.with(this).save(Constants.LNG, lng)
        Prefs.with(this).save(Constants.APK_VERSION, apkVersion)
        GeneralMethods.deleteProfile(this)
//        finishAffinity()
//        startActivity(intent)
    }

    override fun apiLogoutSuccess() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        LoginManager.getInstance().logOut()

        val lat = Prefs.with(this).getString(Constants.LAT, "")
        val lng = Prefs.with(this).getString(Constants.LNG, "")
        val apkVersion = Prefs.with(this).getInt(Constants.APK_VERSION, 0)

        Prefs.with(this).removeAll()

        Prefs.with(this).save(Constants.LAT, lat)
        Prefs.with(this).save(Constants.LNG, lng)
        Prefs.with(this).save(Constants.APK_VERSION, apkVersion)
        startActivity(intent)
        finishAffinity()
    }

    override fun apiSuccessRadius() {
    }


    override fun apiSuccessContacts() {
        GeneralMethods.showToast(this, R.string.success_contacts)
    }


    override fun apiFailure() {
        GeneralMethods.showToast(this, R.string.error_server_busy)
    }

    override fun apiError(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(this, errorBody)
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun sessionExpire() {
        GeneralMethods.tokenExpired(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!));
        //super.attachBaseContext(newBase);
    }

}
