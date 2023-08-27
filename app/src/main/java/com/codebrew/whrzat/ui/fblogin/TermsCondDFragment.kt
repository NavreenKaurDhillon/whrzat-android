package com.codebrew.whrzat.ui.fblogin

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.databinding.TermsCondtionFragmentBinding

class TermsCondDFragment : DialogFragment() {
    private val TAG = "TermsCondDFragment"
    private var link: String = ""
    private lateinit var mContext: Context
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: TermsCondtionFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DIALOG_FULL_SCREEN)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TermsCondtionFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")
        progressDialog = ProgressDialog(mContext)
        recevieData()
        val wvDescriptionSettings = binding.wvTerms.settings
        wvDescriptionSettings.javaScriptEnabled = true
        wvDescriptionSettings.defaultFontSize = 12
        //

        binding.toolbar.setNavigationIcon(R.drawable.back_chevron_icon)
       binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        //  wvTerms.setWebViewClient(WebViewClient())


       binding.wvTerms.webViewClient = object : WebViewClient() {
            //If you will not use this method url links are opeen in new brower not in webview
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            //Show loader on url load
            override fun onLoadResource(view: WebView, url: String) {
                if (url == link)
                    progressDialog.show()

            }

            override fun onPageFinished(view: WebView, url: String) {
                if (url == link)
                    progressDialog.dismiss()
            }

        }
        binding.wvTerms.loadUrl(link)

    }

    private fun recevieData() {
        val bundle = this.arguments
        val type = bundle?.getString(Constants.TERMS_LINK)

        if (type == "1") {
            link = Constants.BASE_URL+"api/customer/tc"
            binding.tvTitle.text = getString(R.string.label_terms_condition)
        } else {
            link = Constants.BASE_URL+"api/customer/tc"
            binding.tvTitle.text = getString(R.string.label_policy)
        }
    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)

            dialog.window?.setWindowAnimations(R.style.MyAnimation_DialogFragment)

        }
    }
}