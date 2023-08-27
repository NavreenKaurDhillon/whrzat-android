package com.codebrew.whrzat.ui.settings

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.LayoutChangePasswordBinding


class ChangepassDialog(private val mContext: Activity) : Dialog(mContext, R.style.TransparentDilaog), View.OnClickListener {

    private lateinit var binding: LayoutChangePasswordBinding
    private  val TAG = "ChangepassDialog"
    private lateinit var listener: OnOkClickListener


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),R.layout.layout_change_password,null,false)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: StartActivty")


        clickListeners()
    }


    fun setListener(listener: OnOkClickListener) {
        this.listener = listener
    }


    private fun clickListeners() {
        binding.tvSubmit.setOnClickListener(this)
        binding.ivCancel.setOnClickListener(this)
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvSubmit -> {
                validation()
            }
            R.id.ivCancel -> {
                dismiss()
            }
        }
    }

    private fun validation() {
        if (binding.etOldPassword.text.toString().trim().isEmpty()) {
            binding. tilOldPassword.error = mContext.getString(R.string.error_old_pass)
            errorRemove(binding.tilOldPassword,binding. etOldPassword)
        } else if (binding.etOldPassword.text.toString().trim().isEmpty()) {
            binding.tilNewPassword.error = mContext.getString(R.string.error_new_password)
            errorRemove(binding.tilNewPassword, binding.etNewPassword)
        } else if (binding.etConfirmPass.text.toString().trim() != binding.etNewPassword.text.toString().trim()) {
            binding. tilConfirmPassword.error = mContext.getString(R.string.error_new_pass)
            errorRemove(binding.tilConfirmPassword, binding.etConfirmPass)
        } else {
            listener.onButtonClick(binding.etOldPassword.text.toString(),
                binding. etOldPassword.text.toString(),
                binding.etConfirmPass.text.toString())
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
        fun onButtonClick(oldPassword: String, newPassword: String, confirmPassword: String)
    }
}