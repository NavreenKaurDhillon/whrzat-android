package com.codebrew.whrzat.ui.settings.Notification

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentNotificationsBinding
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.ApiNotification
import okhttp3.ResponseBody


class NotificationFragment : Fragment(), CompoundButton.OnCheckedChangeListener, NotificationContract.View, View.OnClickListener {


    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var presenter: NotificationContract.Presenter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mContext: Context
    private var notificationList = ArrayList<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        presenter = NotificationPresenter()
        presenter.attachView(this)

        clickListeners()


        val userId = Prefs.with(mContext).getString(Constants.USER_ID, "")
        presenter.getNotificationListing(userId)
        //  getSelectedNotification()       //show the user selected notification
       binding. sbNewMessage.setOnCheckedChangeListener(null)
        binding.sbNewMessage.isChecked = Prefs.with(mContext).getBoolean(Constants.NOTIFICATION_0, false)
        binding. sbNewMessage.setOnCheckedChangeListener(this)

        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        setFontType()
    }
    private fun setFontType() {
        val face_bold = Typeface.createFromAsset(requireActivity().assets, "fonts/opensans_bold.ttf")
        val face_semi = Typeface.createFromAsset(requireActivity().assets, "fonts/opensans_semibold.ttf")
        binding. tvTitle.setTypeface(face_semi)
        binding. tvSave.setTypeface(face_semi)
    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.  llMain.setBackgroundColor(Color.parseColor("#000000"))
                binding. toolbar.setBackgroundColor(Color.parseColor("#000000"))
                binding. tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)

                binding.tvTitle.setTextColor(Color.WHITE)
                binding.sbNewMessage.setTextColor(Color.WHITE)

                binding. sbHotspot.setTextColor(Color.WHITE)
                binding. sblovesPhoto.setTextColor(Color.WHITE)
                binding. sbPopularPhoto.setTextColor(Color.WHITE)
                binding. sbPreviousPlace.setTextColor(Color.WHITE)
                binding.  sbContactCreate.setTextColor(Color.WHITE)

            }
            Configuration.UI_MODE_NIGHT_NO -> {

                binding. llMain.setBackgroundColor(Color.WHITE)
                binding. toolbar.setBackgroundColor(Color.WHITE)
                binding. tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                binding. tvTitle.setTextColor(Color.parseColor("#000000"))
                binding. sbNewMessage.setTextColor(Color.parseColor("#000000"))
                binding. sbHotspot.setTextColor(Color.parseColor("#000000"))
                binding.sblovesPhoto.setTextColor(Color.parseColor("#000000"))
                binding.sbPopularPhoto.setTextColor(Color.parseColor("#000000"))
                binding.sbPreviousPlace.setTextColor(Color.parseColor("#000000"))
                binding.sbContactCreate.setTextColor(Color.parseColor("#000000"))
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }


    private fun getSelectedNotification() {
        binding.sbNewMessage.isChecked = Prefs.with(mContext).getBoolean(Constants.NOTIFICATION_0, false)
        binding.sbHotspot.isChecked = Prefs.with(mContext).getBoolean(Constants.NOTIFICATION_1, false)
        binding.sblovesPhoto.isChecked = Prefs.with(mContext).getBoolean(Constants.NOTIFICATION_2, false)
        binding.sbPopularPhoto.isChecked = Prefs.with(mContext).getBoolean(Constants.NOTIFICATION_3, false)
        binding.sbPreviousPlace.isChecked = Prefs.with(mContext).getBoolean(Constants.NOTIFICATION_4, false)
        binding.sbContactCreate.isChecked = Prefs.with(mContext).getBoolean(Constants.NOTIFICATION_5, false)
    }

    private fun clickListeners() {
        binding.tvBack.setOnClickListener(this)
        binding.sbNewMessage.setOnCheckedChangeListener(this)
        binding.sbHotspot.setOnCheckedChangeListener(this)
        binding.sblovesPhoto.setOnCheckedChangeListener(this)
        binding.sbPopularPhoto.setOnCheckedChangeListener(this)
        binding.sbPreviousPlace.setOnCheckedChangeListener(this)
        binding.sbContactCreate.setOnCheckedChangeListener(this)
        binding.tvSave.setOnClickListener {
            val userId = Prefs.with(mContext).getString(Constants.USER_ID, "")
            val notificationData = ApiNotification(userId, notificationList)
            presenter.notificationApi(notificationData)
        }


    }

    override fun onCheckedChanged(switchButton: CompoundButton?, isChecked: Boolean) {
        when (switchButton?.id) {
            R.id.sbNewMessage -> {
                if (isChecked) {
                    notificationList.add("0")
                    Prefs.with(mContext).save(Constants.NOTIFICATION_0, true)
                } else {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_0, false)
                    notificationList.remove("0")
                    notificationList.add("0")

                }
            }
            R.id.sbHotspot -> {
                if (isChecked) {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_1, true)
                    notificationList.add("1")
                } else {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_1, false)
                    notificationList.remove("1")
                }

            }
            R.id.sblovesPhoto -> {
                if (isChecked) {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_2, true)
                    notificationList.add("2")
                } else {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_2, false)
                    notificationList.remove("2")
                }

            }
            R.id.sbPopularPhoto -> {
                if (isChecked) {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_3, true)
                    notificationList.add("3")
                } else {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_3, false)
                    notificationList.remove("3")
                }

            }
            R.id.sbPreviousPlace -> {
                if (isChecked) {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_4, true)
                    notificationList.add("4")
                } else {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_4, false)
                    notificationList.remove("4")
                }

            }
            else -> {
                if (isChecked) {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_5, true)
                    notificationList.add("5")
                } else {
                    Prefs.with(mContext).save(Constants.NOTIFICATION_5, false)
                    notificationList.remove("5")
                }

            }

        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvBack -> {
                activity?.onBackPressed()
            }
        }
    }

    override fun notificationListing(data: List<String>) {
        Log.e(TAG, "notificationListing: data ${data}", )
        if (data.contains("0")) {
            Log.e(TAG, "notificationListing: contains 0 " )
            binding.sbNewMessage.setOnCheckedChangeListener(null)
            binding.sbNewMessage.isChecked = true//Prefs.with(mContext).getBoolean(Constants.NOTIFICATION_0,false)
            binding.sbNewMessage.setOnCheckedChangeListener(this)

        }else{
            binding.sbNewMessage.isChecked = false;
            binding.sbNewMessage.setOnCheckedChangeListener(null)
        }
        binding.sbHotspot.isChecked = data.contains("1")
        binding. sblovesPhoto.isChecked = data.contains("2")
        binding. sbPopularPhoto.isChecked = data.contains("3")
        binding.sbPreviousPlace.isChecked = data.contains("4")
        binding.sbContactCreate.isChecked = data.contains("5")

    }


    override fun successNotificationApi(data: List<String>) {
        activity?.onBackPressed()
        //GeneralMethods.showToast(mContext, "you will get the notification")
    }

    override fun failureNotificationApi() {
        GeneralMethods.showToast(mContext, R.string.error_server_busy)
    }

    override fun errorNotificationApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun sessionExpire() {
        GeneralMethods.tokenExpired(mContext)
    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
    
    companion object{
        private const val TAG = "NotificationFragment"
    }

}