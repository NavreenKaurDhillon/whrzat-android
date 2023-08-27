package com.codebrew.whrzat.ui.otp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.database.DatabaseUtils
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.HomeActivity
import com.codebrew.whrzat.databinding.ActivityVerifyBinding
import com.codebrew.whrzat.ui.referralCode.ReferalActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody

class ActivityOtpverify:AppCompatActivity() ,OtpContract.View{

    private  val TAG = "ActivityOtpverify"
    private lateinit var presenter: OtpContract.Presenter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityVerifyBinding
    var rewardStatus: Boolean?=null
    var auth:FirebaseAuth?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_verify)
//        setContentView(R.layout.activity_verify)

        Log.d(TAG, "onCreate: StartActivity")

        progressDialog = ProgressDialog(this)
        presenter = OtpPresenter()
        presenter.attachView(this)

        rewardStatus=intent.getBooleanExtra("rewardStatus",false)
        auth= FirebaseAuth.getInstance()
        val window = window
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()

        binding.tvSubmit.setOnClickListener {
           // presenter.performOtpVerify(etPhone.text.toString())
            if (binding.etPhone.text.toString().trim().isEmpty()){
                Toasty.error(applicationContext,getString(R.string.error_otp_empty), Toast.LENGTH_SHORT, true).show()
            }else{
                progressDialog.show()
                val credential = PhoneAuthProvider.getCredential(Prefs.with(this).getString("verificationId",""),binding.etPhone.text.toString())
                signInWithPhoneAuthCredential(credential)
            }

        }
        binding.llBackOtp.setOnClickListener {
            finish()
        }
    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.txtotpTitle.setTextColor(Color.WHITE)
                binding.txtotpmessage.setTextColor(Color.WHITE)
                binding.llotp.setBackgroundColor(Color.parseColor("#000000"))
                binding.imgOtpback.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor=ContextCompat.getColor(this, R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.txtotpTitle.setTextColor(Color.parseColor("#000000"))
               binding. txtotpmessage.setTextColor(Color.parseColor("#000000"))
                binding.llotp.setBackgroundColor(Color.WHITE)
                binding.imgOtpback.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
               // window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    override fun otpSuccess(data: LoginData?) {
//        Prefs.with(this).save(Constants.LOGIN_DATA, data)
        Prefs.with(this).save(Constants.LOGIN_STATUS, true)
//        Prefs.with(this).save(Constants.ACCESS_TOKEN, data?.accessToken)
//        Prefs.with(this).save(Constants.USER_ID, data?._id)
//        Prefs.with(this).save(Constants.FACEBOOK_LOGIN, data?.fromFacebook ?: false)
//        Prefs.with(this).save(Constants.ISFEEDONBOOL, data?.feedsOn ?: false)
       // intent.putExtra("rewardStatus",data?.rewardStatus)

           if (rewardStatus==true){
               val intent = Intent(this, ReferalActivity::class.java)
               startActivity(intent)
               finishAffinity()
           }else{
               val intent = Intent(this, HomeActivity::class.java)
//        val intent = Intent(mContext, ActivityOtpverify::class.java)
               startActivity(intent)
               finishAffinity()
           }


    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth?.signInWithCredential(credential)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.e("firebae phone", "signInWithCredential:success")
                 progressDialog.dismiss()
                val user = task.result?.user
                Prefs.with(this).save(Constants.LOGIN_STATUS, true)
                // Update UI
                if (rewardStatus==true){
                    val intent = Intent(this, ReferalActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }else{
                    val intent = Intent(this, HomeActivity::class.java)
//                  val intent = Intent(mContext, ActivityOtpverify::class.java)
                    startActivity(intent)
                    finishAffinity()

                }
                auth?.signOut()
                //Firebase.auth.signOut()
            } else {
                // Sign in failed, display a message and update the UI
                Log.e("firebae phone", "signInWithCredential:failure", task.exception)
                Toasty.error(this,"Invalid verification code. Please enter the valid code", Toast.LENGTH_SHORT, true).show()
                progressDialog.dismiss()
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                }

            }
        }
    }

    override fun otpFailure() {
        GeneralMethods.showToast(this, R.string.error_server_busy)
    }

    override fun otpError(errorMessage: ResponseBody) {
       GeneralMethods.showErrorMsg(this, errorMessage)
    }
    override fun otpWrong() {

        GeneralMethods.showToast(this, "Enter correct verification code")
    }
    override fun invalidOtp() {
        binding.etPhone.requestFocus()
        //etPhone.error = getString(R.string.error_otp_empty)
        Toasty.error(this,getString(R.string.error_otp_empty), Toast.LENGTH_SHORT, true).show()
       /* GeneralMethods.errorRemove(tilPhone, etPhone)*/
    }

    override fun showLoading() {

        progressDialog.show()
    }

    override fun dismissLoading() {

        progressDialog.dismiss()
    }
}