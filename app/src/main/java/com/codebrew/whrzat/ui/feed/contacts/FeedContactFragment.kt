package com.codebrew.whrzat.ui.feed.contacts

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.arvind.imageandlocationmanager.utils.PermissionDialog
import com.codebrew.tagstrade.adapter.FeedContactAdapter
import com.codebrew.whrzat.R
import com.codebrew.whrzat.event.FeedApi
import com.codebrew.whrzat.event.LoveApi
import com.codebrew.whrzat.event.LoveApiContact
import com.codebrew.whrzat.event.RefreshExploreApi
import com.codebrew.whrzat.ui.chat.userchat.ChatActivity
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.ui.otherprofile.ProfileOtherActivity
import com.codebrew.whrzat.ui.settings.FetchContacts
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.ApiContacts
import com.codebrew.whrzat.webservice.pojo.feed.FeedData
import com.codebrew.whrzat.databinding.FragmentFeedBinding
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.MessageFormat

class FeedContactFragment : Fragment(), FeedContactContract.View, FeedContactAdapter.Love, FetchContacts.FetchContactsListener {
    private val TAG = "FeedContactFragment"
    private lateinit var feedContactAdapter: FeedContactAdapter
    private lateinit var mContext: Context
    private lateinit var presenter: FeedContactContract.Presenter
    private lateinit var progress: ProgressDialog
    private val READ_CONTACTS_PERMISSIONS_REQUEST = 21
    private var isContactSynced = false
    private var isPermissionGranted = false
    private var isPermissionFirst = false
    private var isPermissionSec = false
    private var isFeedApiHit = false
    private lateinit var binding:FragmentFeedBinding
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFeedBinding.inflate(inflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: SartActivity")
        progress = ProgressDialog(mContext)
        presenter = FeedContactPresenter()
        presenter.attachView(this)
        setAdapter()
        if (Prefs.with(mContext).getBoolean(Constants.ISFEEDONBOOL, false)) {
            binding.rvFeed.visibility = View.VISIBLE
            binding.tvFeed.visibility = View.GONE
            binding.swipeToRefresh.isEnabled = true
            binding.swipeToRefresh.isRefreshing = true
             presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""),false)
        } else {
            binding.rvFeed.visibility = View.GONE
            binding.tvFeed.visibility = View.VISIBLE
            binding.tvFeed.text = getString(R.string.label_settings_text)
            binding.swipeToRefresh.isEnabled = false
            binding.swipeToRefresh.isRefreshing = false
        }

        swipeToRefresh()
        getPermissionToReadUserContacts()
    }

    private fun swipeToRefresh() {
        Log.d("Token",Prefs.with(mContext).getString(Constants.ACCESS_TOKEN, ""))
        binding.swipeToRefresh.setOnRefreshListener {
            presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""), false)
        }
    }

    private fun setAdapter() {
        feedContactAdapter = FeedContactAdapter(mContext)
        binding.rvFeed.layoutManager = LinearLayoutManager(mContext)
        binding.rvFeed.adapter = feedContactAdapter
        binding.rvFeed.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
        feedContactAdapter.setListener(this)
    }


    override fun successFeedApi(data: List<FeedData>) {
        if (data.isEmpty()) {
            feedContactAdapter.clearList()
            binding.tvFeed.visibility = View.VISIBLE
//            binding.tvFeed.text = getString(R.string.label_no_feed)
            binding.tvFeed.text = "Your contacts haven't been out recently"
        } else {
            binding.tvFeed.visibility = View.GONE
            feedContactAdapter.addList(data)
        }
        isFeedApiHit = true
        binding.swipeToRefresh.isRefreshing = false
    }


    override fun viewProfile(otherUserid: String, ivProfilePic: ImageView, tvUserName: TextView, original: String, name: String) {
        val intent = Intent(mContext, ProfileOtherActivity::class.java)
        intent.putExtra(Constants.OTHER_USER_ID, otherUserid)
        intent.putExtra(ProfileOtherActivity.PIC_URL, original)
        intent.putExtra(ProfileOtherActivity.USERNAME, name)
        val p1 = Pair.create(ivProfilePic as View, "pic")
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as FragmentActivity, p1)
        startActivity(intent, options.toBundle())
    }


    override fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int) {
        EventBus.getDefault().postSticky(LoveApiContact(true))
        presenter.love(Prefs.with(mContext).getString(Constants.USER_ID, ""), imageId, isLike)
    }

    override fun onChatClick(id: String, ivProfilePic: ImageView, original: String) {
        val intent = Intent(mContext, ChatActivity::class.java)
        intent.putExtra(Constants.OTHER_USER_ID, id)
        intent.putExtra(Constants.PIC, original)
        val p1 = Pair.create(ivProfilePic as View, "pic")
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as FragmentActivity, p1)
        startActivity(intent, options.toBundle())
    }

    override fun report(id: String) {
        presenter.apiReport(id, Prefs.with(mContext).getString(Constants.USER_ID, ""))
    }

    override fun errorApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun failureApi() {
        GeneralMethods.showToast(mContext, R.string.error_server_busy)
    }

    override fun sessionExpire() {
        GeneralMethods.tokenExpired(mContext)
    }

    override fun dismissLoading() {
        progress.dismiss()
    }

    override fun showLoading() {
        progress.show()
    }

    override fun successLoveApi() {

    }

    override fun successReport() {
        // GeneralMethods.showToast(mContext, R.string.label_report_success)
        Toasty.success(mContext, getString(R.string.success_reported), Toast.LENGTH_SHORT, true).show()

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }


    override fun onContactsReceived(contacts: List<String>) {
        isContactSynced = true
        val contactsObj = ApiContacts(Prefs.with(mContext).getString(Constants.USER_ID, ""), contacts)
        presenter.apiSyncContacts(contactsObj)
    }

    override fun onErrorFetchingContacts() {
        GeneralMethods.showToast(mContext, "error")

    }

    override fun apiSuccessContacts() {
        isContactSynced = true
        //GeneralMethods.showToast(mContext, "Contacts Synced Successfully")
        presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""), true)

    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPermissionToReadUserContacts() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            //  GeneralMethods.showToast(mContext, R.string.label_fetching_contacts)
            FetchContacts(mContext,mContext.contentResolver, this).execute()
            //  progress.show()
            isPermissionGranted = true

        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                    READ_CONTACTS_PERMISSIONS_REQUEST)
            /* ActivityCompat.requestPermissions(activity,
                        String[]{Manifest.permission.READ_CONTACTS},
                        READ_CONTACTS_PERMISSIONS_REQUEST)*/
        }

    }

    private lateinit var permissionDialog: PermissionDialog

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //  if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                FetchContacts(mContext,mContext.contentResolver, this).execute()

            } else if (permissions.isNotEmpty()) {
                isContactSynced = false
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity as FragmentActivity, permissions[0])) {
                    this.permissionDialog = PermissionDialog(activity, MessageFormat.format("{0} {1} {2}",
                            *arrayOf<Any>(this.getString(R.string.str_permission_contacts_1),
                                    this.getString(R.string.app_name),
                                    this.getString(R.string.str_permission_contacts_2))),
                            false, PermissionDialog.OnAllowClickListener
                    { openAppSettings(activity as FragmentActivity) })
                    isPermissionGranted = true
                    isPermissionSec = false
                    this.permissionDialog.setCancelable(false)
                } else {
                    this.permissionDialog = PermissionDialog(activity, MessageFormat.format("{0} {1} {2} {3}",
                            *arrayOf<Any>(this.getString(R.string.str_permission_contacts_1),
                                    this.getString(R.string.app_name),
                                    this.getString(R.string.str_permission_contacts_2),
                                    this.getString(R.string.str_open_setting_contacts))),
                            true, PermissionDialog.OnAllowClickListener

                    { openAppSettings(activity as FragmentActivity) })
                    isPermissionGranted = false
                    isPermissionSec = true
                    this.permissionDialog.setCancelable(false)
                }
            }

        }
    }

    private fun openAppSettings(context: Activity) {
        val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
        val uri = Uri.fromParts("package", context.packageName, null as String?)
        intent.data = uri
        context.startActivityForResult(intent, 25)
    }

    override fun spotDetail(id: String) {
        val intent1=Intent(mContext, DetailActivity::class.java)
        intent1.putExtra(Constants.HOTSPOT_ID, id)
        startActivity(intent1)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNotification(feedApi: FeedApi) {

//        Toast.makeText(context,"FeedApi",Toast.LENGTH_SHORT).show()
        EventBus.getDefault().removeStickyEvent<FeedApi>(FeedApi::class.java)

        if (Prefs.with(mContext).getBoolean(Constants.ISFEEDONBOOL, false)) {
            binding.rvFeed.visibility = View.VISIBLE
            binding.tvFeed.visibility = View.GONE
            binding.swipeToRefresh.isEnabled = true
            if (feedApi.isContactSync && !isContactSynced) {
                getPermissionToReadUserContacts()
            } else {
                presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""), !isFeedApiHit)

            }
        } else {
            binding.tvFeed.visibility = View.VISIBLE
            binding.tvFeed.text = getString(R.string.label_settings_text)
            binding.rvFeed.visibility = View.GONE
            binding.swipeToRefresh.isEnabled = false
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNotification(loveApi: LoveApi) {
//        Toast.makeText(context,"LoveApi",Toast.LENGTH_SHORT).show()
        if (loveApi.isLoveApi) {
            presenter.feedApi(Prefs.with(mContext).getString(Constants.USER_ID, ""), !isFeedApiHit)
        }

        EventBus.getDefault().removeStickyEvent<LoveApi>(LoveApi::class.java)
    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

    }

    override fun onResume() {
        super.onResume()
    }


}