package com.codebrew.whrzat.ui.profile.favorite

import android.util.Log
import com.codebrew.whrzat.ui.profile.ProfileContract
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.webservice.pojo.explore.ExploreData
import com.codebrew.whrzat.webservice.pojo.explore.FavoriteListData
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritePresenter:FavoriteContract.Presenter {
    private val TAG = "FavoritePresenter"

    private var view: FavoriteContract.View? = null


    override fun performFavoriteApi(userId: String) {
        Log.d("performFavoriteApi",userId)
        view?.showLoading()
        RetrofitClient.get().apiGetFavoriteList(userId).enqueue(object : Callback<FavoriteListData> {
            override fun onResponse(call: Call<FavoriteListData>, response: Response<FavoriteListData>) {
                if (response.isSuccessful) {
                   // Log.e("performFavoriteApi", (response.body()))
                    if (response.body()?.statusCode==200) {
                        if (response.body()?.data?.listFavorite?.isNotEmpty()!!) {
                            response.body()?.data?.let { view?.successFavorite(response.body()!!) }
                        }else{
                            Log.e("performFavoriteApi","no data")
                            view?.noSpotFound()

                        }
                    } else {
                        view?.noSpotFound()
                    }
                } else {
                    if (response.code() == 401) {
                       // view?.sessionExpire()
                        Log.d(TAG, "onResponse:  // view?.sessionExpire()")
                    } else if (response.code() == 400) {
                        Log.e("performFavoriteApi","code 400")
                        view?.noSpotFound()
                    } else {
                        if (response.errorBody() != null) {
                            view?.errorFavoriteApi(response.errorBody()!!)
                        }
                    }
                }

                view?.dismissLoading()
            }

            override fun onFailure(call: Call<FavoriteListData>, t: Throwable) {
                Log.d("performFavoriteApi","Error:-"+t?.message)
                view?.dismissLoading()
                view?.failureApi()
            }
        })

    }

    override fun attachView(view: FavoriteContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

}