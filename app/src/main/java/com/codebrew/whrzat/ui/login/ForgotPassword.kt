package com.codebrew.whrzat.ui.login

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.DialogForgotpassBinding

class ForgotPassword(private val mContext: Context)
    : Dialog(mContext, R.style.TransparentDilaog), View.OnClickListener {

    private  val TAG = "ForgotPassword"
    private lateinit var binding:DialogForgotpassBinding

    private lateinit var listener: OnOkClickListener
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),R.layout.dialog_forgotpass,null,false)
//        setContentView(R.layout.dialog_forgotpass)
        Log.d(TAG, "onCreate: StartActivity")
        // setCancelable(mIsDismiss)
        clickListeners()
    }

    fun setListener(listener: OnOkClickListener) {
        this.listener = listener
    }



    override fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener?) {
        super.setOnKeyListener(onKeyListener)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivCross -> dismiss()
            R.id.tvSendEmail -> sendEmail()
        }
    }

    private fun sendEmail() {

        if (binding.etEmail.text.toString().trim().isEmpty()) {
            binding.tilEmail.error = mContext.getString(R.string.error_email_empty)
            errorRemove(binding.tilEmail, binding.etEmail)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()) {
            binding.tilEmail.error = mContext.getString(R.string.error_invalid_email)
            errorRemove(binding.tilEmail, binding.etEmail)
        } else {
            listener.onButtonClick(binding.etEmail.text.toString().trim())
            dismiss()
        }
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

    interface OnOkClickListener {
        fun onButtonClick(emailId: String)
    }

    private fun clickListeners() {
        binding.tvSendEmail.setOnClickListener(this)
        binding.ivCross.setOnClickListener(this)
    }
}
