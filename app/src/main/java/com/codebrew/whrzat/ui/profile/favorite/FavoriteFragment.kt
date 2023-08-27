package com.codebrew.whrzat.ui.profile.favorite

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
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
import com.codebrew.whrzat.databinding.FragmentProfileFavoriteBinding
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.explore.FavoriteListData
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody

class FavoriteFragment : Fragment(), FavoriteContract.View,FavoriteAdapter.OnSpotItemClickListener ,
        SwipeRefreshLayout.OnRefreshListener{

    private  val TAG = "FavoriteFragment"
    private lateinit var mContext: Context
    private lateinit var binding: FragmentProfileFavoriteBinding
    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var presenter: FavoriteContract.Presenter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var favoriteListItem: ArrayList<FavoriteListData.ListFavorite>
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileFavoriteBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")
        favoriteListItem = ArrayList<FavoriteListData.ListFavorite>()
        setAdapter()
        presenter = FavoritePresenter()
        presenter.attachView(this)
        progressDialog = ProgressDialog(mContext)
        binding.swipeToRefresh.setOnRefreshListener(this)
//        swipeToRefresh.isEnabled = false

        Log.d("OnViewCreated", Prefs.with(context).getString(Constants.ACCESS_TOKEN, ""))
       // presenter.performFavoriteApi(Prefs.with(mContext).getString(Constants.USER_ID, ""))
        val face_semi = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_semibold.ttf")
        binding.tvNoProfile.setTypeface(face_semi)
    }

    override fun onResume() {
        super.onResume()
        presenter.performFavoriteApi(Prefs.with(mContext).getString(Constants.USER_ID, ""))
    }

    private fun setAdapter() {
        favoriteAdapter = FavoriteAdapter(mContext, favoriteListItem,this)
       binding. rvFavorite.layoutManager = LinearLayoutManager(mContext)
       binding .rvFavorite.adapter = favoriteAdapter
        binding.rvFavorite.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))

    }

    override fun onRefresh() {

        presenter.performFavoriteApi(Prefs.with(mContext).getString(Constants.USER_ID, ""))
    }

    override fun successFavorite(favoriteList: FavoriteListData) {

       binding. tvNoProfile.visibility=View.GONE
       binding. swipeToRefresh.isRefreshing = false
        //Log.e("FavoriteList", (favoriteList))
        favoriteListItem.clear()
        favoriteListItem.addAll(favoriteList.data?.listFavorite!!)
        setAdapter()
    }

    override fun errorApi(errorBody: ResponseBody) {
        binding.swipeToRefresh.isRefreshing = false
        GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun errorFavoriteApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun failureApi() {
        GeneralMethods.showToast(mContext, R.string.error_server_busy)
    }

    override fun sessionExpire() {
        GeneralMethods.tokenExpired(requireContext())
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun noSpotFound() {
       binding. swipeToRefresh.isRefreshing = false
        binding.tvNoProfile.visibility=View.VISIBLE
        favoriteListItem.clear()
        setAdapter()
    }

    override fun onHotSpotClick(id: String, deleted: Boolean?) {
        if(deleted == true){
            Toasty.error(mContext,"Hotspot has expired." , Toast.LENGTH_LONG).show()
        }else{
            val intent = Intent(mContext, DetailActivity::class.java)
            intent.putExtra(Constants.HOTSPOT_ID, id)
            startActivity(intent)
        }
    }
}