package com.codebrew.whrzat.ui.settings.changepassword

import android.content.Context
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import androidx.fragment.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.LayoutChangePasswordBinding
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChangePasswrod : Fragment(), View.OnClickListener {

    private  val TAG = "ChangePasswrod"
    private lateinit var mContext: Context
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: LayoutChangePasswordBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutChangePasswordBinding.inflate(inflater,container,false)
        return binding.root
//        return super.onCreateView(inflater, container, savedInstanceState)
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")
        progressDialog = ProgressDialog(mContext)
        clickListeners()

    }


    private fun clickListeners() {
       binding. tvSubmit.setOnClickListener(this)
        binding. ivCancel.setOnClickListener(this)
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvSubmit -> {
                validation()
            }
            R.id.ivCancel -> {
                activity?.onBackPressed()
            }
        }
    }

    private fun validation() {
        if ( binding.etOldPassword.text.toString().trim().isEmpty()) {
            binding.tilOldPassword.error = mContext.getString(R.string.error_old_pass)
            errorRemove( binding.tilOldPassword, binding. etOldPassword)
        } else if ( binding.etNewPassword.text.toString().trim().isEmpty()) {
            binding.tilNewPassword.error = mContext.getString(R.string.error_new_password)
            errorRemove( binding.tilNewPassword, binding. etNewPassword)
        } else if ( binding.etConfirmPass.text.toString().trim() !=  binding.etNewPassword.text.toString().trim()) {
            binding.tilConfirmPassword.error = mContext.getString(R.string.error_new_pass)
            errorRemove( binding.tilConfirmPassword, binding. etConfirmPass)
        } else {
            progressDialog.show()
            var userId=Prefs.with(mContext).getString(Constants.USER_ID,"")
            apiChangePass(userId, binding.etOldPassword.text.toString(), binding.etNewPassword.text.toString())
        }
    }

    private fun apiChangepassword() {

    }


    fun apiChangePass(userId: String, oldPass: String, newpass: String) {
        RetrofitClient.get().apiChangePassword(userId, oldPass, newpass).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    //GeneralMethods.showToast(mContext, "Success")
                    activity?.onBackPressed()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        GeneralMethods.tokenExpired(mContext)
                    } else {
                        response.errorBody()?.let { GeneralMethods.showErrorMsg(mContext, it) }
                    }

                }

                progressDialog.dismiss()

                //view?.dismissLoading()
            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // view?.dismissLoading()
                // view?.apiFailure()
                progressDialog.dismiss()

            }
        })
    }

    private fun errorRemove(tilEmailId: TextInputLayout, etEmailId: EditText) {
        etEmailId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                tilEmailId.isErrorEnabled = false
            }
        })
    }
}