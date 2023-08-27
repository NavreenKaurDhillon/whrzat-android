package com.codebrew.whrzat.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivitySplashBinding
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import io.branch.referral.Branch
import io.branch.referral.BranchError
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"
    private var isLocationEnabled = true
    private var handler = Handler()
  var hotspotIdGot =""
    private lateinit var binding: ActivitySplashBinding

    companion object{
        var token:String="abcddssjs"
    }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    this.setIntent(intent);
    Branch.sessionBuilder(this).withCallback { referringParams, error ->
      if (error != null) {
        Log.e("BranchSDK_Tester", error.message)
      } else if (referringParams != null) {
        Log.i("BranchSDK_Tester", referringParams.toString())
      }
    }.reInit()
  }

  override fun onStart() {
    super.onStart()
    Branch.sessionBuilder(this).withCallback { branchUniversalObject, linkProperties, error ->
      if (error != null) {
        Log.e("BranchSDK_Tester", "branch init failed. Caused by -" + error.message)
      } else {
        Log.i("BranchSDK_Tester", "branch init complete!")
        if (branchUniversalObject != null) {
          Log.i("BranchSDK_Tester", "title " + branchUniversalObject.title)
          Log.i("BranchSDK_Tester", "CanonicalIdentifier " + branchUniversalObject.canonicalIdentifier)
          Log.i("BranchSDK_Tester", "metadata " + branchUniversalObject.contentMetadata.convertToJson())
        }
        if (linkProperties != null) {
          Log.i("BranchSDK_Tester", "Channel " + linkProperties.channel)
          Log.i("BranchSDK_Tester", "control params " + linkProperties.controlParams)
        }
      }
    }.withData(this.intent.data).init()

    //to check firebase dynamic link
    checkForDynamicLinks()

//    checkForLinkInBranch()
  }

  private fun checkForLinkInBranch() {
    // Initialize Branch session
    val deepLink = this.intent.data
    if (deepLink != null) {
      val hotspotId = deepLink.getQueryParameter("hotspotId")
      Log.d(TAG, "checkForLinkInBranch: ////tg id="+hotspotId)
      // Use the hotspotId as needed in your fragment
      // For example, display content based on the hotspotId parameter
    }
    }
  private val branchReferralInitListener = Branch.BranchReferralInitListener { referringParams, error ->
    if (error == null && referringParams != null && referringParams.has("hotspotID")) {
      // Extract hotspotID from referringParams
      val hotspotID = referringParams.optString("hotspotID")
      Log.d(TAG, "/// id = : "+hotspotID)
      // Use the hotspotID as needed in your app
    }
  }

  private fun checkForDynamicLinks() : Boolean{
    var hasLink : Boolean = false
    Firebase.dynamicLinks
      .getDynamicLink(intent)
      .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
        // Get deep link from result (may be null if no link is found)
        var deepLink: Uri? = null
        if (pendingDynamicLinkData != null) {
          val deepLink = pendingDynamicLinkData.link.toString()
          Log.d(TAG, "checkForDynamicLinks: // deep lik = "+deepLink)
          val hotspotId= deepLink?.toUri()?.getQueryParameter("hotspotId")
          Log.d(TAG, "checkForDynamicLinks: // hotspot id sent = "+hotspotId)
          hasLink = true
          hotspotIdGot = hotspotId.toString()
//          val i = Intent(this, DetailActivity::class.java)
//          i.putExtra(Constants.HOTSPOT_ID, hotspotId)
//          startActivity(i)
//          handler.removeCallbacksAndMessages(null);
        }
      }
      .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }

    return hasLink
  }

  override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash)
//        setContentView(R.layout.activity_splash)
        Log.d(TAG, "onCreate: StartActivity")
        //setDialogsCancelable(false)

        getSignatures()
//        checkForDynamicLinks()
    checkForLinks()


        // Creates a button that mimics a crash when clicked
       /* val crashButton = Button(this)
        crashButton.setText("Crash!")
        crashButton.setOnClickListener {
            throw RuntimeException("Test Crash") // Force a crash
        }

        addContentView(crashButton, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT))*/
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM token", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            token = task.result
            Log.e("token",token)

        })
        val window = window
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        handler.postDelayed({
            if (getVersion() > Prefs.with(this).getInt(Constants.APK_VERSION, 0)) {
                //setCountryCode(latitude!!, longitude!!)
            }
            if (Prefs.with(this).getBoolean(Constants.LOGIN_STATUS, false)) {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra(Constants.NOTIFICATION_TYPE, getIntent().getStringExtra(Constants.NOTIFICATION_TYPE))
                intent.putExtra(Constants.ID, getIntent().getStringExtra(Constants.ID))
                Log.d(TAG, "onCreate: hotspot if = "+hotspotIdGot)
               intent.putExtra(Constants.HOTSPOT_ID,hotspotIdGot)
                startActivity(intent)
                finishAffinity()
            }
          else{
                val intent = Intent(this@SplashActivity, TutorialActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }

        }, 3000)


    }

  private fun checkForLinks() {
    Branch.sessionBuilder(this).withCallback(object : Branch.BranchReferralInitListener {
      override fun onInitFinished(referringParams: JSONObject?, error: BranchError?) {
        if (error == null) {
          Log.i("BRANCH SDK", referringParams.toString())
          val hotspotId= referringParams?.get("hotspotId")
          Log.d(TAG, "checkForDynamicLinks: // hotspot id sent = "+hotspotId)
          hotspotIdGot = hotspotId.toString()
        } else {
          Log.e("BRANCH SDK", error.message)
        }
      }
    }).withData(this.intent.data).init()

    // latest
    val sessionParams = Branch.getInstance().latestReferringParams

    // first
    val installParams = Branch.getInstance().firstReferringParams
  }

  @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {

                binding.activitySplash.setBackgroundColor(Color.parseColor("#000000"))
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor=ContextCompat.getColor(this, R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {

               binding. activitySplash.setBackgroundColor(Color.WHITE)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
              //  window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }





   /* override fun onLocationUpdateFailure() {
    }*/

    /*override fun onLocationUpdated(location: Location?) {
        Prefs.with(this).save(Constants.LAT, location?.latitude)
        Prefs.with(this).save(Constants.LNG, location?.longitude)
        setCountryCode(location?.latitude as Double, location.longitude)
        stopLocationUpdates()
        if (isLocationEnabled) {
            isLocationEnabled = false
            if (GeneralMethods.isNetworkActive(this)) {
                handler.postDelayed({
                    if (getVersion() > Prefs.with(this).getInt(Constants.APK_VERSION, 0)) {
                        Prefs.with(this).removeAll()
                        Prefs.with(this).save(Constants.LAT, location.latitude)
                        Prefs.with(this).save(Constants.LNG, location.longitude)
                        setCountryCode(location?.latitude as Double, location.longitude)
                    }
                    if (Prefs.with(this).getBoolean(Constants.LOGIN_STATUS, false)) {
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra(Constants.NOTIFICATION_TYPE, getIntent().getStringExtra(Constants.NOTIFICATION_TYPE))
                        intent.putExtra(Constants.ID, getIntent().getStringExtra(Constants.ID))
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        val intent = Intent(this@SplashActivity, TutorialActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }

                }, 3000)
            } else {
                Toasty.error(this, getString(R.string.error_no_connection), Toast.LENGTH_SHORT, true).show()
                // GeneralMethods.showToast(this, R.string.error_no_connection)
            }
        }
    }*/

    private fun getVersion(): Int {
        val manager = this.packageManager
        try {
            val info = manager.getPackageInfo(this.packageName, 0)
            return info.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return 1
    }

    private fun setCountryCode(latitude: Double, longitude: Double) {

        val geoCoder = Geocoder(this)
        try {
            val addresses: List<Address> = geoCoder.getFromLocation(latitude, longitude, 1)?:ArrayList<Address>()

            //picker sometimes give null values like in Gujrat,no postal code get.
            if (addresses[0].countryName != null) {
                Prefs.with(this).save(Constants.COUNTRY_NAME, addresses[0].countryName)
                // country = addresses[0].country
            }


        } catch (e: IOException) {
            // GeneralMethods.showToast(this, getString(R.string.label_no_address))
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
    private fun getSignatures() {
        try {
            val info = packageManager.getPackageInfo(
                    "com.codebrew.whrzat",
                    PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }

    }
}
