package com.codebrew.whrzat.ui.login

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.codebrew.tagstrade.adapter.CountryCodeAdapter
import com.codebrew.tagstrade.fragment.CountryCodeFragment
import com.codebrew.tagstrade.util.CountryDb
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.HomeActivity
import com.codebrew.whrzat.databinding.FragmentLoginBinding
import com.codebrew.whrzat.ui.otp.ActivityOtpverify
import com.codebrew.whrzat.ui.referralCode.ReferalActivity
import com.codebrew.whrzat.ui.signup.SignupFragment
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.rilixtech.CountryCodePicker

import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody
import java.util.concurrent.TimeUnit

class LoginFragment : Fragment(), View.OnClickListener, LoginContract.View, ForgotPassword.OnOkClickListener, CountryCodeAdapter.SetCountryCode {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var mContext: Context
    private lateinit var presenter: LoginContract.Presenter

    private  val TAG = "LoginFragment"
    private lateinit var binding: FragmentLoginBinding
    private var countryzCode = ""
    private var countryName: String = "United States"
    private var countrydb: CountryDb = CountryDb()
    var auth:FirebaseAuth?=null
    var phoneNumber:String?=null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")

        progressDialog = ProgressDialog(mContext)
        presenter = LoginPresenter()
        presenter.attachView(this)
        initCountryPicker()
        clickListeners()
        GeneralMethods.timeZone()
        //setCountryNameCode()
        auth= FirebaseAuth.getInstance()
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        setFontType()

    }

    private fun setFontType() {
        val face_bold = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_bold.ttf")
        val face_regular = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_regular.ttf")
        binding.txtloginTitle.setTypeface(face_bold)
        binding.txtloginmessage.setTypeface(face_regular)
        binding.tvLogin.setTypeface(face_bold)
        binding.tvSignup.setTypeface(face_bold)

    }

    var mCode = "";
    private fun initCountryPicker() {
        val manager = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        mCode = manager.simCountryIso.toUpperCase()
        if (mCode.equals("", ignoreCase = true)) {
            binding.ccpNumber?.setDefaultCountryUsingNameCode("US")
            binding.ccpNumber?.resetToDefaultCountry()
        } else {
            binding.ccpNumber?.setDefaultCountryUsingNameCode(mCode)
            binding.ccpNumber?.resetToDefaultCountry()
        }
        mCode = binding.ccpNumber?.getSelectedCountryCodeWithPlus()!!
        binding.tvCountryCode.text=mCode
        Prefs.with(mContext).save(Constants.CODE, mCode)
        binding.ccpNumber?.setOnCountryChangeListener(CountryCodePicker.OnCountryChangeListener {
            mCode = binding.ccpNumber?.getSelectedCountryCodeWithPlus()!!
        })
    }

    private fun clickListeners() {
        binding.llBack.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        binding.tvCountryCode.setOnClickListener(this)
        binding.tvSignup.setOnClickListener(this)
    }
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.txtloginTitle.setTextColor(Color.WHITE)
                binding.txtloginmessage.setTextColor(Color.WHITE)
                binding.tvSignup.setTextColor(Color.WHITE)
                binding.tvCountryCode.setTextColor(Color.WHITE)
                binding.llloginMain.setBackgroundColor(Color.parseColor("#000000"))
               binding.imgback.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.txtloginTitle.setTextColor(Color.parseColor("#000000"))
                binding.txtloginmessage.setTextColor(Color.parseColor("#000000"))
                binding.tvSignup.setTextColor(Color.parseColor("#000000"))
                binding.tvCountryCode.setTextColor(Color.parseColor("#000000"))
                binding.llloginMain.setBackgroundColor(Color.WHITE)
                binding.imgback.setImageResource(R.drawable.ic_baseline_arrow_back_24)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun setCountryNameCode() {
        countryName = Prefs.with(mContext).getString(Constants.COUNTRY_NAME, "")
        countrydb.countryList.indices.forEach { i ->
            if (countrydb.countryList[i].name == countryName) {
                countryzCode = countrydb.countryList[i].dialingCode as String
                Prefs.with(mContext).save(Constants.CODE, countryzCode)
            }
        }
        val code = "$countryName ($countryzCode)"
        // countryzCode = Prefs.with(mContext).getString(Constants.COUNTRY_CODE, "")
        if (Prefs.with(mContext).getString(Constants.CODE, "").isEmpty()) {
            binding.tvCountryCode.text = countryzCode
            Prefs.with(mContext).save(Constants.CODE, code)
        } else {
            binding.tvCountryCode.text = Prefs.with(mContext).getString(Constants.CODE, "")
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> {
                GeneralMethods.hideSoftKeyboard(requireActivity(),binding.llBack);
                activity?.onBackPressed()
            }
            R.id.tvForgotPassword -> {
                val forgotPassword = ForgotPassword(mContext)
                forgotPassword.setListener(this)
                forgotPassword.show()
            }
            R.id.tvLogin -> {
                if (GeneralMethods.isNetworkActive(mContext)) {
                    phoneNumber=binding.tvCountryCode.text.toString().trim()+binding.etPhone.text.toString().trim()
                    //Firebase phone number verification
                   //phoneNumberVarification()
                   presenter.performLogin(binding.tvCountryCode.text.toString().trim(), binding.etPhone.text.toString().trim())

                } else {
                    GeneralMethods.showSnackbar(binding.tvForgotPassword, R.string.error_no_connection)
                }
            }
            R.id.tvCountryCode->{
                val countryCode = CountryCodeFragment()
                countryCode.setCountryCodeListener(this)
                if (activity?.supportFragmentManager?.findFragmentByTag("countryCode_fragment") == null) {
                    fragmentManager?.inTransaction {
                        add(android.R.id.content, countryCode, "countryCode_fragment")
                    }
                    GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.etPhone)
                }
            }
            R.id.tvSignup->{
                if (activity?.supportFragmentManager?.findFragmentByTag("signup_fragment") == null) {
                    activity?.supportFragmentManager?.inTransaction {
                        add(android.R.id.content, SignupFragment(), "signup_fragment")
                    }
                }
            }
        }
    }
    override fun code(countryCode: String, country: String) {
        binding.tvCountryCode.text = countryCode
        countryzCode = countryCode
        countryName = country
    }
    override fun onButtonClick(emailId: String) {
        presenter.performForgotPassword(emailId)
    }

    override fun errorPasswordApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(mContext, errorBody)
    }


    override fun invalidEmail() {
        binding.tilEmail.error = getString(R.string.error_invalid_email)
        GeneralMethods.errorRemove(binding.tilEmail, binding.etEmail)
    }

    override fun invalidPassword() {
        binding.tilPassword.error = getString(R.string.error_invalid_password)
        GeneralMethods.errorRemove(binding.tilPassword, binding.etEmail)

    }

   var rewardStatus:Boolean?=null
    override fun loginSuccess(data: LoginData?) {
        Prefs.with(mContext).save(Constants.LOGIN_DATA, data)
//        Prefs.with(mContext).save(Constants.LOGIN_STATUS, true)
        Prefs.with(mContext).save(Constants.ACCESS_TOKEN, data?.accessToken)
        Prefs.with(mContext).save(Constants.COUNTRY_CODE,binding.tvCountryCode.text.toString().trim())
        Prefs.with(mContext).save(Constants.USER_ID, data?._id)
        Prefs.with(mContext).save(Constants.FACEBOOK_LOGIN, data?.fromFacebook ?: false)
        Prefs.with(mContext).save(Constants.ISFEEDONBOOL, data?.feedsOn ?: false)
        Prefs.with(mContext).save(Constants.Referralcode, data?.referralCode)
        rewardStatus=data?.rewardStatus
//        val intent = Intent(mContext, HomeActivity::class.java)
       /* val intent = Intent(mContext, ActivityOtpverify::class.java)//8283093315
        intent.putExtra("rewardStatus",data?.rewardStatus)
        startActivity(intent)*/
        //Firebase phone number verification
        phoneNumberVarification()
//        activity?.finishAffinity()
    /*    Prefs.with(activity).save(Constants.LOGIN_STATUS, true)
        val intent = Intent(mContext, HomeActivity::class.java)
        startActivity(intent)
        activity?.finishAffinity()*/
    }

    private fun phoneNumberVarification() {
       // FirebaseAuth.getInstance().firebaseAuthSettings.forceRecaptchaFlowForTesting(true)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
              
                Log.e("firebae phone", "onVerificationCompleted:$credential")

                //signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e("firebae phone", "onVerificationFailed", e)
                dismissLoading()
                GeneralMethods.showToast(mContext,e.message.toString())
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("firebae phone", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                var storedVerificationId = verificationId
                var resendToken = token
                Prefs.with(mContext).save("verificationId",storedVerificationId)
                Prefs.with(mContext).save("token",resendToken)

                val intent = Intent(mContext, ActivityOtpverify::class.java)//8283093315
                intent.putExtra("rewardStatus",rewardStatus)
                startActivity(intent)
                dismissLoading()
            }
        }
        
        val options = PhoneAuthOptions.newBuilder(auth!!)
                .setPhoneNumber(phoneNumber!!)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(activity!!)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth?.signInWithCredential(credential)?.addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.e("firebae phone", "signInWithCredential:success")

                        val user = task.result?.user
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.e("firebae phone", "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                        // Update UI

                    }
                }
    }

    override fun successForgotPass() {
        Toasty.success(mContext, getString(R.string.success_password), Toast.LENGTH_SHORT, true).show()
        //GeneralMethods.showToast(mContext, R.string.success_password)
    }

    override fun fbNewUser() {
    }

    override fun showLoading() {
        progressDialog.show()

    }

    override fun dismissLoading() {
        progressDialog.dismiss()

    }

    override fun sessionExpired() {
        GeneralMethods.tokenExpired(mContext)

    }

    override fun loginFailure() {
        GeneralMethods.showToast(mContext, R.string.error_server_busy)

    }

    override fun loginError(errorMessage: ResponseBody) {
        GeneralMethods.showErrorMsg(mContext, errorMessage)
    }
    override fun invalidContact() {

       /* tilPhone.error = getString(R.string.error_invalid_contact)
        GeneralMethods.errorRemove(tilPhone, etPhone)*/
    }
    override fun emptyEmail() {
        binding.etPhone.requestFocus()
        Toasty.error(mContext,getString(R.string.error_empty_contact), Toast.LENGTH_SHORT, true).show()

       /* etPhone.error = getString(R.string.error_empty_contact)
        GeneralMethods.errorRemove(tilPhone, etPhone)*/


    }

    override fun emptyPassword() {
        binding.tilPassword.requestFocus()
       binding. tilPassword.error = getString(R.string.error_password_empty)
        GeneralMethods.errorRemove(binding.tilPassword,binding. etPassword)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

}
