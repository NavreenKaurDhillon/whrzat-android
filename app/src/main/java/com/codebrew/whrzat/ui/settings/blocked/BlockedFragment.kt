package com.codebrew.whrzat.ui.settings.blocked

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentBlockBinding
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData
import okhttp3.ResponseBody

class BlockedFragment : Fragment(), BlockedContract.View, View.OnClickListener, BlockedAdapter.OnUnBlockListner {

    private  val TAG = "BlockedFragment"

    private lateinit var mContext: Context
    private lateinit var blockedAdapter: BlockedAdapter
    private lateinit var presenter: BlockedContract.Presenter
        private lateinit var progressDialog: ProgressDialog
        private lateinit var binding: FragmentBlockBinding


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBlockBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: StartActivity")
        progressDialog = ProgressDialog(mContext)
        presenter = BlockedPresenter()
        presenter.attachView(this)
        setAdapter()
        clickListeners()
        presenter.apiBlockList(Prefs.with(mContext).getString(Constants.USER_ID, ""))

        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        setFontType()
    }
    private fun setFontType() {
        val face_semi = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_semibold.ttf")
        binding.tvBlock.setTypeface(face_semi)
        binding.tvNoData.setTypeface(face_semi)
    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
               binding. llMain.setBackgroundColor(Color.parseColor("#000000"))
               binding. toolbar.setBackgroundColor(Color.parseColor("#000000"))
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
               binding. tvBlock.setTextColor(Color.WHITE)

            }
            Configuration.UI_MODE_NIGHT_NO -> {

              binding.  llMain.setBackgroundColor(Color.WHITE)
              binding.  toolbar.setBackgroundColor(Color.WHITE)
               binding. tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
               binding. tvBlock.setTextColor(Color.parseColor("#000000"))
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }

    private fun clickListeners() {
        binding.tvBack.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvBack -> {
                activity?.onBackPressed()
            }
        }
    }


    private fun setAdapter() {
        blockedAdapter = BlockedAdapter(mContext)
      binding.  rvBlockedContacts.layoutManager = LinearLayoutManager(mContext)
       binding. rvBlockedContacts.adapter = blockedAdapter
        blockedAdapter.setListener(this)
    }

    override fun onUnblock(id: String) {
        presenter.apiUnBlock(Prefs.with(mContext).getString(Constants.USER_ID, ""), id)
    }

    override fun successBlockedApi(data: List<UserData>) {
        if (data.isEmpty()) {
           binding. tvNoData.visibility=View.VISIBLE
        } else {
            blockedAdapter.addList(data)
        }
    }
    override fun successUnBlock() {
        if(blockedAdapter.itemCount==0){
           binding. tvNoData.visibility=View.VISIBLE
        }else{
            binding.tvNoData.visibility=View.GONE

        }
    }


    override fun sessionExpired() {
        GeneralMethods.tokenExpired(mContext)
    }

    override fun errorBlockedApi(errorBody: ResponseBody) {
    }

    override fun failureBlockedApi() {
        GeneralMethods.showToast(mContext, R.string.error_no_connection)
    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }
}
