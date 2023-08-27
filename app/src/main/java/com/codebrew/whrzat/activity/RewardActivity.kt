package com.codebrew.whrzat.activity

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivityRewardBinding
import com.codebrew.whrzat.ui.LocationGet.locationActivity
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.Referalcodedata
import com.codebrew.whrzat.webservice.pojo.RewardDetailsData
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RewardActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "RewardActivity"
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityRewardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_reward)
//        setContentView(R.layout.activity_reward)
        Log.d(TAG, "onCreate: StartActivity")
        progressDialog= ProgressDialog(this)
       binding. txtreferalcode.text= Prefs.with(this).getString(Constants.Referralcode, "")

        if (intent.getBooleanExtra("locationStatus",false)==true){
            //btn_location is true then visibility visible
            binding. btnLocation.visibility=View.GONE
        }else{
            binding. btnLocation.visibility=View.GONE
        }

        if(intent.getBooleanExtra("redeemRequest",false)==true){
            binding. tvRedeem.isEnabled=false
            binding. tvRedeem.isClickable=false
            binding. tvRedeem.setTextColor(Color.parseColor("#BBBBBB"))
        }else{
            binding. tvRedeem.isEnabled=true
            binding. tvRedeem.isClickable=true
            binding. tvRedeem.setTextColor(Color.parseColor("#F14545"))
        }

        if(intent.getBooleanExtra("isRedeem",false)==true){
            binding.  tvRedeem.visibility=View.VISIBLE
        }else{
            binding.  tvRedeem.visibility=View.GONE
        }

        if (intent.getStringExtra("rewardAmount").isNullOrEmpty()){
            binding. tvamount.text="$0"
        }else{
            binding. tvamount.text="$"+intent.getStringExtra("rewardAmount")
        }

        binding. tvRedeem.setOnClickListener(this)
        binding.tvBack.setOnClickListener(this)
        binding.txtreferalcode.setOnClickListener(this)
        binding.btnLocation.setOnClickListener(this)
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
                binding.tvTitle.setTextColor(Color.WHITE)
                binding. llRewards.setBackgroundColor(Color.parseColor("#000000"))
                binding. toolbar.setBackgroundColor(Color.parseColor("#000000"))
                binding. tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor= ContextCompat.getColor(this, R.color.black)
                binding. txtreferalcode.setBackgroundResource(R.drawable.edittext_white)
                binding. tvReferal.setTextColor(Color.WHITE)
                binding.txtreferalcode.setTextColor(Color.WHITE)
                binding.tvreferfriend.setTextColor(Color.WHITE)

                binding. tvEarn.setTextColor(Color.WHITE)
                binding. tvEarnValue.setTextColor(Color.WHITE)
                binding. tvRewardPoint.setTextColor(Color.WHITE)
                binding. tvRewardPointValue.setTextColor(Color.WHITE)
                binding. tvDisclaimer.setTextColor(Color.WHITE)
                binding. tvDisclaimerValue.setTextColor(Color.WHITE)

               // tvFavEvent.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.tvTitle.setTextColor(Color.parseColor("#000000"))
               // tvTotalEvents.setTextColor(Color.parseColor("#000000"))
                binding. llRewards.setBackgroundColor(Color.WHITE)
                binding. tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
            //    window.navigationBarColor= ContextCompat.getColor(this, R.color.white)
                binding.txtreferalcode.setBackgroundResource(R.drawable.edittext)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.tvBack->{
                finish()
            }
            R.id.txtreferalcode->{
                val cm: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setText( binding.txtreferalcode.getText())
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
            }
            R.id.btn_location->{
                startActivity(Intent(this, locationActivity::class.java))
            }
            R.id.tvRedeem->{
                RedeemDialog()
            }
        }
    }

    private fun RedeemDialog() {
        AlertDialog.Builder(this, R.style.MyDialog)
                .setTitle(R.string.do_you_want_to_redeem)
                .setPositiveButton("Yes", { dialogInterface, i ->
                    if (GeneralMethods.isNetworkActive(this)) {
                       apiRedeem()
                    } else {
                        GeneralMethods.showToast(this, R.string.error_no_connection)
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(false)
                .show()
    }

    private fun apiRedeem() {
        progressDialog.show()
        RetrofitClient.get().redeemRequest("Bearer "+Prefs.with(this).getString(Constants.ACCESS_TOKEN,"")).enqueue(object : Callback<Referalcodedata> {
            override fun onResponse(call: Call<Referalcodedata>, response: Response<Referalcodedata>) {
                if(response.isSuccessful){
                   Log.e("redeem response",response.body().toString())
                    if(response.code()==200){
                        GeneralMethods.showToast(this@RewardActivity,"Redeem request successfully initiated!")
                        //   tvamount.text="$0"
                        binding.tvRedeem.isEnabled=false
                        binding.tvRedeem.isClickable=false
                        binding. tvRedeem.setTextColor(Color.GRAY)
                    }else{
                        GeneralMethods.showToast(this@RewardActivity,"No amount to redeem")
                    }
                }
                else{
                    if(response.code()== ApiConstants.UNAUTHORIZED_ACCESS){
                        GeneralMethods.tokenExpired(this@RewardActivity)
                    }else {
                        GeneralMethods.showToast(this@RewardActivity,"No amount to redeem")
                    }

                }
                progressDialog.dismiss()
            }

            override fun onFailure(call: Call<Referalcodedata>, t: Throwable) {
                progressDialog.dismiss()
                GeneralMethods.showToast(this@RewardActivity, t.message.toString())
            }
        })

    }


}