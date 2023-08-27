package com.codebrew.whrzat.ui.profile

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentActivityProfileBinding
import com.codebrew.whrzat.event.EditProfileEvent
import com.codebrew.whrzat.event.NotificationTab
import com.codebrew.whrzat.event.RefershNotificationApi
import com.codebrew.whrzat.event.RefreshProfileApi
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.ui.otherprofile.ProfileOtherActivity
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import com.codebrew.whrzat.webservice.pojo.notifications.NotificationMain
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationsFragment : Fragment(), NotificationAdapter.OnNotificationClick {

    private  val TAG = "NotificationsFragment"

    private lateinit var mContext: Context
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var progressDialog: ProgressDialog
    private var isCountVisible=false
    private lateinit var binding: FragmentActivityProfileBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentActivityProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: StartActivity")

        notificationAdapter = NotificationAdapter(mContext)
       binding. rvProfile.layoutManager = LinearLayoutManager(context)
        binding.rvProfile.adapter = notificationAdapter
        notificationAdapter.setListener(this)
        binding.rvProfile.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
        notificationApi()

        progressDialog = ProgressDialog(mContext)

        val face_semi = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_semibold.ttf")
        binding. tvNoProfile.setTypeface(face_semi)
        binding. tvNoProfile.text = getString(R.string.label_notification)

        binding. cv.setOnClickListener {
            confirmClearNotification()
        }
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
    }

    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.  tvClearAll.setTextColor(Color.WHITE)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding. tvClearAll.setTextColor(Color.BLACK)
            }
        }
    }

    private fun confirmClearNotification() {
        AlertDialog.Builder(mContext, R.style.MyDialog)
                .setTitle(getString(R.string.label_clear_notification))
                .setPositiveButton(android.R.string.ok, { dialogInterface, i ->

                    clearNotificationApi()

                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .show()
    }

    private fun clearNotificationApi() {

        if (GeneralMethods.isNetworkActive(mContext)) {
            val userId = Prefs.with(mContext).getString(Constants.USER_ID, "")
            progressDialog.show()
            RetrofitClient.get().clearNotification(userId).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {

                        notificationAdapter.clearList()
                        binding.  cv.visibility = View.GONE
                        binding.  tvNoProfile.visibility = View.VISIBLE
                        binding.  tvNoProfile?.text = getString(R.string.label_notification)

                    }

                    progressDialog.dismiss()
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    GeneralMethods.showToast(mContext, R.string.error_server_busy)
                    progressDialog.dismiss()

                }

            })
        } else {
            GeneralMethods.showToast(mContext, R.string.error_no_connection)
        }
    }
   // 60533b15b99a2d136e17a99b
    private fun notificationApi() {
        if (GeneralMethods.isNetworkActive(mContext)) {
            val userId = Prefs.with(mContext).getString(Constants.USER_ID, "")
//            Log.d("Token",Prefs.with(context).getString(Constants.ACCESS_TOKEN, ""))
            RetrofitClient.get().apiNotificaiton(userId).enqueue(object : Callback<NotificationMain> {
                override fun onResponse(call: Call<NotificationMain>, response: Response<NotificationMain>) {
                    if (response.isSuccessful) {

                        if (!response.body()?.data?.isEmpty()!!) {
                            binding.  tvNoProfile?.visibility = View.GONE
                            binding. cv?.visibility = View.VISIBLE
                            Log.e("response", "" + (response))

                            response.body()?.data?.let { notificationAdapter.addList(it) }
                        } else {
                            binding. cv?.visibility = View.GONE
                            binding. tvNoProfile?.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<NotificationMain>, t: Throwable) {
                    GeneralMethods.showToast(mContext, R.string.error_server_busy)
                }

            })
        } else {
            GeneralMethods.showToast(mContext, R.string.error_no_connection)
        }
    }

    override fun onProfilePic(id: String, imageUr: String, ivProfilePic: ImageView) {
        val intent = Intent(mContext, ProfileOtherActivity::class.java)
        intent.putExtra(Constants.OTHER_USER_ID, id)

        intent.putExtra(ProfileOtherActivity.PIC_URL, imageUr)
        val p1 = Pair.create(ivProfilePic as View, "pic")
        //val p3 = Pair.create(tvUserName as View, "name")

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as FragmentActivity, p1)
        startActivity(intent, options.toBundle())
    }

    override fun spotDetail(id: String, deleted: Boolean) {
        if(deleted){
            Toasty.error(mContext,"Hotspot has expired." , Toast.LENGTH_LONG).show()
        }else{
            val intent = Intent(mContext, DetailActivity::class.java)
            intent.putExtra(Constants.HOTSPOT_ID, id)
            startActivity(intent)
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNotification(refreshApi: RefershNotificationApi) {

        EventBus.getDefault().removeStickyEvent<RefershNotificationApi>(RefershNotificationApi::class.java)

        if (refreshApi.isApiNotification) {
          //  isCountVisible=true
            binding.  cv.visibility = View.VISIBLE
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefershNotification(notifcationTab: NotificationTab) {

        EventBus.getDefault().removeStickyEvent<NotificationTab>(NotificationTab::class.java)

        if (notifcationTab.isTab) {
           // if(isCountVisible) {
                isCountVisible = false
                notificationApi()
                EventBus.getDefault().postSticky(EditProfileEvent(true))
                EventBus.getDefault().postSticky(RefreshProfileApi(true))

          //  }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

    }
}
