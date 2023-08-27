package com.codebrew.whrzat.ui.fblogin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.HomeActivity
import com.codebrew.whrzat.databinding.FbLoginNewBinding
import com.codebrew.whrzat.ui.login.LoginFragment
import com.codebrew.whrzat.ui.signup.SignupFragment
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.google.firebase.crashlytics.FirebaseCrashlytics
//import com.facebook.*
//import com.facebook.login.LoginManager
import io.github.inflationx.viewpump.ViewPumpContextWrapper

import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject

/*ImageGetterActivity(), View.OnClickListener, FacebookLoginListener, ImageGetterActivity.ImagePickerForFragment, FbContract.View*/
class FbLoginActivity :AppCompatActivity(), View.OnClickListener, FacebookLoginListener, FbContract.View {


    private  val TAG = "FbLoginActivity"
//    private var facebookLogin: FacebookLogin? = null
    private lateinit var presenter: FbContract.Presenter
    private lateinit var progress: ProgressDialog
    private val bundle = Bundle()
    private var id = ""
    private var first_name=""
    private var last_name=""
    private var profilepicUrl=""
    private var terms: TermsCondDFragment = TermsCondDFragment()
    private lateinit var binding: FbLoginNewBinding


    var requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        Log.e(
            TAG,
            "onActivityResult: notification Permisstion result :-- $result"
        )
        if (Build.VERSION.SDK_INT >= 33) {
            if (!result) {
                askNotificationPermission()
            }
        }
    }



    @SuppressLint("NewApi", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.fb_login_new)
        //setContentView(R.layout.fb_login_new)
        Log.d(TAG, "onCreate: StartActivity")
      //  FirebaseApp.initializeApp(this)
        val window = window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askNotificationPermission()
        }


//        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
        presenter = FbPresenter()
        presenter.attachView(this)

        progress = ProgressDialog(this)

//        facebookLogin = FacebookLogin(this, this)
//        FacebookSdk.sdkInitialize(this)
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        val face_bold = Typeface.createFromAsset(assets, "fonts/opensans_bold.ttf")
        val face_regular = Typeface.createFromAsset(assets, "fonts/opensans_regular.ttf")
        binding.tvDiscover.setTypeface(face_bold)
        binding.tvWhrzat.setTypeface(face_bold)
        binding.tvExplore.setTypeface(face_regular)
        binding.tvTerms.setTypeface(face_regular)
        binding.tvSignUp.setTypeface(face_bold)
        binding.tvLogin.setTypeface(face_bold)


       // cameraSet()
        clickListener()

        if (Prefs.with(this).getBoolean(Constants.LOGIN_STATUS, false)) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

       /* binding.tvTerms.text = customizeStyle()
        binding.tvTerms.movementMethod = LinkMovementMethod()
        binding.tvTerms.highlightColor = ContextCompat.getColor(this, R.color.black)*/
    }
     var isNightMode:Boolean=false
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                isNightMode = true
                binding.tvDiscover.setTextColor(Color.WHITE)
                binding.tvExplore.setTextColor(Color.WHITE)
                binding.tvTerms.setTextColor(Color.WHITE)
                binding.rlMain.setBackgroundColor(Color.parseColor("#000000"))
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                isNightMode = false
                binding.tvDiscover.setTextColor(Color.parseColor("#000000"))
                binding.tvExplore.setTextColor(Color.parseColor("#000000"))
                binding.tvTerms.setTextColor(Color.parseColor("#000000"))
                binding.rlMain.setBackgroundColor(Color.WHITE)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
                //  window.navigationBarColor=ContextCompat.getColor(this, R.color.white)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }

    /*  private fun cameraSet() {
          setAuthorityForURI(BuildConfig.APPLICATION_ID)
          setImageLocation(EXTERNAL_STORAGE_APP_DIRECTORY_DEFAULT, getString(R.string.app_name))
          setCropperEnabled(false, SHAPE_OVAL)
      }*/

    private fun clickListener() {
        binding.ivFacebook.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        binding.tvSignUp.setOnClickListener(this)
        binding.tvTerms.setOnClickListener(this)
       // facebookLogin?.setFacebookLoginListener(this)
    }

/*
    keytool -exportcert -alias YOUR_RELEASE_KEY_ALIAS -keystore YOUR_RELEASE_KEY_PATH | openssl sha1 -binary | openssl base64
*/

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivFacebook -> {
                // facebookLogin?.printHashKey()
                if (GeneralMethods.isNetworkActive(this)) {
//                    facebookLogin?.performLogin()
                } else {
                    GeneralMethods.showToast(this, R.string.error_server_busy)
                }

            }
            R.id.tvLogin -> {
                if (supportFragmentManager.findFragmentByTag("login_fragment") == null) {
                    supportFragmentManager.inTransaction {
                        add(android.R.id.content, LoginFragment(), "login_fragment")
                    }
                }
            }
            R.id.tvTerms -> {
                val bundle = Bundle()
                bundle.putString(Constants.TERMS_LINK, "1")
                terms.arguments = bundle
                terms.show(supportFragmentManager, "tag_1")
            }
            R.id.tvSignUp ->
                if (supportFragmentManager.findFragmentByTag("signup_fragment") == null) {
                    supportFragmentManager.inTransaction {
                        add(android.R.id.content, SignupFragment(), "signup_fragment")
                    }
                }
        }

    }

    override fun successFb() {
        presenter.loginFb(id)

    }

    override fun errorFbLogin(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(this, errorBody)
    }

    override fun failureFbLogin() {
        GeneralMethods.showToast(this, "failure ")
    }

    override fun dismissLoading() {
        progress.dismiss()
    }

    override fun showLoading() {
        progress.show()
    }

    override fun sessionExpire() {
        GeneralMethods.tokenExpired(this)
    }


    override fun onFbLoginCancel() {
        // GeneralMethods.showToast(this, "fb cancel")
    }

    override fun onFbLoginSuccess() {
//        facebookLogin?.getUserProfile()
    }

//    override fun onFbLoginError(exception: FacebookException?) {
//        GeneralMethods.showToast(this, R.string.error_fb_login)
//
//    }

    override fun signupScreen() {
        val signupScreen = SignupFragment()
        signupScreen.arguments = bundle

        supportFragmentManager.inTransaction {
            add(android.R.id.content, signupScreen)
        }
    }

    override fun homeScreen(data: LoginData?) {
        Prefs.with(this).save(Constants.LOGIN_DATA, data)
        Prefs.with(this).save(Constants.LOGIN_STATUS, true)
        Prefs.with(this).save(Constants.USER_ID, data?._id)
        Prefs.with(this).save(Constants.ACCESS_TOKEN, data?.accessToken)
        Prefs.with(this).save(Constants.ISFEEDONBOOL, data?.feedsOn ?: false)
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
       // this line is already commented       facebookLogin?.onActivityResult(requestCode, resultCode, data)

    }*/

//    override fun onGetProfileSuccess(obj: JSONObject?, response: GraphResponse?) {
//
//        try {
//            Log.e("FB ID", obj?.getString(facebookLogin?.ID))
//
//            id = obj?.getString(facebookLogin?.ID) as String
//            // name = obj.getString(facebookLogin?.FULL_NAME)
//            first_name = obj.getString(facebookLogin?.FIRST_NAME)
//            last_name = obj.getString(facebookLogin?.LAST_NAME)
//            Log.e("Name", first_name + " " + last_name)
//            profilepicUrl = "http://graph.facebook.com/$id/picture?type=large"
//            //gender = object.getString(facebookLogin.GENDER);
//
//            if (obj.get(facebookLogin?.EMAIL) != null) {
//                val email = obj.getString(facebookLogin?.EMAIL)
//                bundle.putString(Constants.EMAIL, email)
//            } else {
//                val email = ""
//            }
//            // mPrefs.setEmail(email);
//
//            bundle.putString(Constants.FB_ID, id)
//            bundle.putString(Constants.FIRST_NAME, first_name)
//            bundle.putString(Constants.LAST_NAME, last_name)
//            bundle.putString(Constants.PIC, profilepicUrl)
//
//            if (!id.isEmpty()) {
//                presenter.performLoginFb(id)
//            }
//
////            LoginManager.getInstance().logOut()
//
//        } catch (e: JSONException) {
//            e.printStackTrace()
//            bundle.putString(Constants.FB_ID, id)
//            bundle.putString(Constants.FIRST_NAME, first_name)
//            bundle.putString(Constants.LAST_NAME, last_name)
//            bundle.putString(Constants.PIC, profilepicUrl)
//
//            if (!id.isEmpty()) {
//                presenter.performLoginFb(id)
//            }
//
////            LoginManager.getInstance().logOut()
//        }
//    }


    private fun customizeStyle(): SpannableStringBuilder {
        val text1 = getString(R.string.label_sigin)
        val text2 = getString(R.string.label_click)
        val finalString = "$text1 $text2 "
        val sb2 = SpannableStringBuilder(finalString)
        sb2.setSpan(
                StyleSpan(Typeface.BOLD), 32, sb2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sb2.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) {
                if (terms.dialog == null || !terms.dialog!!.isShowing) {
                    GeneralMethods.hideSoftKeyboard(this@FbLoginActivity, binding.tvLogin)
                    val bundle = Bundle()
                    bundle.putString(Constants.TERMS_LINK, "1")
                    terms.arguments = bundle
                    terms.show(supportFragmentManager, "tag_1")
                }

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.bgColor = ContextCompat.getColor(this@FbLoginActivity, R.color.white)
                ds.linkColor = ContextCompat.getColor(this@FbLoginActivity, R.color.white)
                ds.color = ContextCompat.getColor(this@FbLoginActivity, R.color.white)
                ds.isUnderlineText = false
            }
        }, finalString.indexOf(text2), finalString.indexOf(text2) + text2.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        sb2.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.black)),
                finalString.indexOf(text2),
                finalString.indexOf(text2) + text2.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return sb2
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
       // super.attachBaseContext(newBase);
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        } else {
            super.onBackPressed()
        }
    }


   /* override fun file(p0: File?) {
    }*/

/*    override fun openImagePickerFromFragment() {
        openImageSelector()
    }

    override fun setImagePickedListener(listener: OnImageSelectListener?) {
        setOnImageSelectListener(listener)
    }*/

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }


    fun askNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(
                        this@FbLoginActivity, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(
                        TAG,
                        "onCreate: PERMISSION GRANTED"
                    )
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this@FbLoginActivity)
                    builder.setTitle(getString(R.string.notification_permission_title))
                    builder.setMessage(getString(R.string.notification_permission_description))
                    builder.setPositiveButton(
                        getString(R.string.str_allow)
                    ) { dialog, which ->
                        dialog.dismiss()
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val uri = Uri.fromParts("package", "com.codebrew.whrzat", null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    val dialog = builder.create()
                    dialog.show()
                } else {
                    requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }
    }


}
