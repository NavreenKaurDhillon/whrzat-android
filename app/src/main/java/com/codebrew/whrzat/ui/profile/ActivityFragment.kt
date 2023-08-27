package com.codebrew.whrzat.ui.profile


import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.codebrew.tagstrade.adapter.FeedContactAdapter
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentActivityProfileBinding
import com.codebrew.whrzat.event.RefreshProfileApi
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.explore.HotspotData
import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ActivityFragment : Fragment(), FeedContactAdapter.Love, ProfileContract.View {

    private  val TAG = "ActivityFragment"
    private lateinit var mContext: Context
    private lateinit var profileAdapter: ProfileOtherAdapter
    private lateinit var presenter: ProfileContract.Presenter
    private lateinit var progressDialog: ProgressDialog
    private var userId = ""
    private var otherUserId = ""
    private lateinit var binding : FragmentActivityProfileBinding
    private var totalCount=0
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: android.os.Bundle?): android.view.View? {
        binding = FragmentActivityProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")
        setAdapter()
        presenter = ProfilePresenter()
        presenter.attachView(this)

        progressDialog = ProgressDialog(mContext)
        userId = Prefs.with(mContext).getString(Constants.USER_ID, "")

        presenter.apiGetProfile(userId, userId)
        binding.cv.visibility=View.GONE

        val face_semi = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_semibold.ttf")
        binding.tvNoProfile.setTypeface(face_semi)
    }

    private fun setAdapter() {
        profileAdapter = ProfileOtherAdapter(mContext)
        binding.rvProfile.layoutManager = LinearLayoutManager(mContext)
        binding.rvProfile.adapter = profileAdapter
        profileAdapter.setListener(this)
        binding.rvProfile.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))

    }

    override fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int) {
        presenter.love(userId, imageId, isLike, likeCount, position)
    }

    override fun report(id: String) {
    }

    override fun viewProfile(otherUserid: String, ivProfilePic: ImageView, tvUserName: TextView, original: String, name: String) {
    }


    override fun successProfileApi(data: UserData) {
        //Log.e("activity list=====:",(data))
        if (data.images.isNotEmpty()) {
            binding.tvNoProfile?.visibility = View.GONE
            for (i in data.images.indices){
                if (data.images.get(i).hotspotId!=null){
                    profileAdapter.addList(data.images)
                }
            }

        } else {
            binding.tvNoProfile?.visibility = View.VISIBLE
            binding.tvNoProfile?.text = getString(R.string.label_no_activity)
        }

        totalCount=data.loves
    }

    override fun favoriteList(exploreList: HotspotData) {

    }

    public fun loves():Int=totalCount

    override fun errorApi(errorBody: ResponseBody) {
    }

    override fun failureApi() {
    }

    override fun sessionExpire() {
    }

    override fun dismissLoading() {
        progressDialog.dismiss()

    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun successBlockApi() {
    }

    override fun successLoveApi(likeCount: String, pos: Int) {
    }

    override fun errorFavoriteApi(errorBody: ResponseBody) {

    }

    override fun noSpotFound() {
    }

    override fun failureFavoriteApi() {
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNotification(refershApi: RefreshProfileApi) {

        EventBus.getDefault().removeStickyEvent<RefreshProfileApi>(RefreshProfileApi::class.java)

        if (refershApi.isApiRefers) {
            presenter.apiGetProfile(userId, userId)
            // EventBus.getDefault().postSticky(FeedApi(true))

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

    override fun onChatClick(id: String, ivProfilePic: ImageView, original: String) {
    }


    override fun spotDetail(id: String) {
        val intent = Intent(mContext, DetailActivity::class.java)
        intent.putExtra(Constants.HOTSPOT_ID, id)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

}
