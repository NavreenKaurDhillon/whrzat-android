package com.codebrew.whrzat.ui.feed.happening

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentHappeningFeedBinding
import com.codebrew.whrzat.event.FeedApi
import com.codebrew.whrzat.event.LoveApi
import com.codebrew.whrzat.event.LoveApiContact
import com.codebrew.whrzat.ui.hotspotlocation.HotspotLocationFragment
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.feed.HappeningListData
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FeedHappeningFragment : Fragment(), FeedHappeningContract.View, FeedHappeningAdapter.OnHappeningItemClick {
    private lateinit var mContext: Context
    private lateinit var feedHappeningAdapter: FeedHappeningAdapter
    private  val TAG = "FeedHappeningFragment"
    private lateinit var binding: FragmentHappeningFeedBinding
    private lateinit var progress: ProgressDialog
    private lateinit var presenter: FeedHappeningContract.Presenter

    val locationFrament = HotspotLocationFragment()
    private var happeningList = ArrayList<HappeningListData.ImageDatum>()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHappeningFeedBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")
        setAdapter()

        progress = ProgressDialog(mContext)
        presenter = FeedHappeningPresenter()

        presenter.attachView(this)

        if (Prefs.with(mContext).getString(Constants.RADIUS, "").isNotEmpty()) {
            presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                    Prefs.with(mContext).getString(Constants.RADIUS, "").toInt(),
                    Prefs.with(mContext).getString(Constants.LAT, ""),
                    Prefs.with(mContext).getString(Constants.LNG, ""))
        } else {
            presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                    Constants.SPOT_RADIUS,
                    Prefs.with(mContext).getString(Constants.LAT, ""),
                    Prefs.with(mContext).getString(Constants.LNG, ""))
        }


        swipeToRefresh()
    }

    private fun swipeToRefresh() {
        binding.swipeToRefresh.setOnRefreshListener {
            if (Prefs.with(mContext).getString(Constants.RADIUS, "").isNotEmpty()) {

                presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                        Prefs.with(mContext).getString(Constants.RADIUS, "").toInt(),

                        Prefs.with(mContext).getString(Constants.LAT, ""),
                        Prefs.with(mContext).getString(Constants.LNG, ""))
            } else {
                presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                        Constants.SPOT_RADIUS,

                        Prefs.with(mContext).getString(Constants.LAT, ""),
                        Prefs.with(mContext).getString(Constants.LNG, ""))
            }

        }
    }

    private fun setAdapter() {
        feedHappeningAdapter = FeedHappeningAdapter(mContext)
        binding.rvHappeningFeed.layoutManager = LinearLayoutManager(mContext)
        binding.rvHappeningFeed.adapter = feedHappeningAdapter
        binding.rvHappeningFeed.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
        feedHappeningAdapter.setListener(this)
    }

    override fun successFeedHappeningApi(data: HappeningListData.Data) {
        binding.tvFeed.visibility = View.GONE
        binding.swipeToRefresh.isRefreshing = false
        feedHappeningAdapter.addList(data)
    }

    override fun onReportClick(id: String) {

        presenter.apiReport(id, Prefs.with(mContext).getString(Constants.USER_ID, ""))
    }

    override fun onLocationClick(data: HappeningListData.ImageDatum) {
        //Log.d("Datga", (data))

        activity?.supportFragmentManager?.let { locationFrament.show(it, locationFrament.tag) }

    }

    override fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int) {

        EventBus.getDefault().postSticky(LoveApi(true))
        presenter.love(Prefs.with(mContext).getString(Constants.USER_ID, ""), imageId, isLike)
    }

    override fun noEventFound() {
        Log.d("NoEvent", "NoEventFound")
       binding. swipeToRefresh.isRefreshing = false
        binding.tvFeed.visibility = View.VISIBLE
        feedHappeningAdapter.clearList()
    }

    override fun errorApi(errorBody: ResponseBody) {
        binding.swipeToRefresh.isRefreshing = false
        feedHappeningAdapter.clearList()
        binding.tvFeed.visibility = View.VISIBLE

       // GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun errorReportApi(errorBody: ResponseBody) {

        GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun failureReportApi() {

      //  GeneralMethods.showToast(mContext, R.string.error_server_busy)
    }

    override fun failureApi() {
       binding. swipeToRefresh.isRefreshing = false
        feedHappeningAdapter.clearList()
        binding.tvFeed.visibility = View.VISIBLE

       // GeneralMethods.showToast(mContext, R.string.error_server_busy)
    }


    override fun sessionExpire() {
        binding.swipeToRefresh.isRefreshing = false
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

        Toasty.success(mContext, getString(R.string.success_reported), Toast.LENGTH_SHORT, true).show()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNotification(feedApi: FeedApi) {
        EventBus.getDefault().removeStickyEvent<FeedApi>(FeedApi::class.java)

        if (Prefs.with(mContext).getString(Constants.RADIUS, "").isNotEmpty()) {
            presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                    Prefs.with(mContext).getString(Constants.RADIUS, "").toInt(),
                    Prefs.with(mContext).getString(Constants.LAT, ""),
                    Prefs.with(mContext).getString(Constants.LNG, ""))
        } else {
            presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                    Constants.SPOT_RADIUS,
                    Prefs.with(mContext).getString(Constants.LAT, ""),
                    Prefs.with(mContext).getString(Constants.LNG, ""))
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onNotification(loveApi: LoveApiContact) {
//        Toast.makeText(context,"LoveApi1111",Toast.LENGTH_SHORT).show()
        if (loveApi.isLoveApi) {
            if (Prefs.with(mContext).getString(Constants.RADIUS, "").isNotEmpty()) {
                presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                        Prefs.with(mContext).getString(Constants.RADIUS, "").toInt(),
                        Prefs.with(mContext).getString(Constants.LAT, ""),
                        Prefs.with(mContext).getString(Constants.LNG, ""))
            } else {
                presenter.feedHappeningApi(0, Prefs.with(mContext).getString(Constants.USER_ID, ""),
                        Constants.SPOT_RADIUS,
                        Prefs.with(mContext).getString(Constants.LAT, ""),
                        Prefs.with(mContext).getString(Constants.LNG, ""))
            }

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

}