package com.codebrew.whrzat.ui.referralCode

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.HomeActivity
import com.codebrew.whrzat.databinding.ActivityReferalBinding
import com.codebrew.whrzat.ui.otp.OtpPresenter
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.Referalcodedata
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import es.dmoral.toasty.Toasty

import okhttp3.ResponseBody

class ReferalActivity : AppCompatActivity(),ReferralContract.View {
    private lateinit var presenter:ReferralContract.Presenter
    private lateinit var progressDialog: ProgressDialog
    private val TAG = "ReferalActivity"
    private lateinit var binding: ActivityReferalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_referal)
      //  setContentView(R.layout.activity_referal)
        Log.d(TAG, "onCreate: StartActivity")
        progressDialog = ProgressDialog(this)
        presenter = ReferralPresenter()
        presenter.attachView(this)

        val window = window
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()


       binding. tvSubmit.setOnClickListener {
            presenter.performreferral(Prefs.with(this).getString(Constants.ACCESS_TOKEN,""),binding.etreferralCode.text.toString())
        }
        binding. llBackOtp.setOnClickListener {
            finish()
        }
        binding. tvSkip.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
//        val intent = Intent(mContext, ActivityOtpverify::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.  txtreferalcode.setTextColor(Color.WHITE)
                binding. tvSkip.setTextColor(Color.WHITE)
                binding. llreferal.setBackgroundColor(Color.parseColor("#000000"))
                binding.imgRefralback.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor=ContextCompat.getColor(this, R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding. txtreferalcode.setTextColor(Color.parseColor("#000000"))
                binding. tvSkip.setTextColor(Color.parseColor("#000000"))
                binding. llreferal.setBackgroundColor(Color.WHITE)
                binding. imgRefralback.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
               // window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }
    override fun referralSuccess(data: Referalcodedata?) {
        val intent = Intent(this, HomeActivity::class.java)
//        val intent = Intent(mContext, ActivityOtpverify::class.java)
        startActivity(intent)
        finishAffinity()
    }

    override fun referralError(errorMessage: ResponseBody) {
        GeneralMethods.showErrorMsg(this, errorMessage)
    }

    override fun referralFailer(message: String) {
        Toasty.error(this,message, Toast.LENGTH_SHORT).show()
    }

    override fun invalidreferral() {
        binding.  etreferralCode.requestFocus()
       /* etreferralCode.error ="Please enter referral code"
        GeneralMethods.errorRemove(tilreferralCode, etreferralCode)*/
        Toasty.error(this,"Please enter referral code", Toast.LENGTH_SHORT, true).show()

    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }
}