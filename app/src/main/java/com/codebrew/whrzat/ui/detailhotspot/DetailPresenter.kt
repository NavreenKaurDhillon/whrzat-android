package com.codebrew.whrzat.ui.detailhotspot

import android.util.Log
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.HotspotDetail.DetailData
import com.codebrew.whrzat.webservice.pojo.login.LoginResponse
import com.google.gson.Gson
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private const val TAG = "DetailPresenter"
class DetailPresenter : DetailContract.Presenter {

    private var view: DetailContract.View? = null
    override fun getHotspotDetail(hotspotId: String, userId: String) {
        view?.showLoading()
        Log.e("hotspotId ", hotspotId + " userId" + userId)
        RetrofitClient.get().apiHotspotDetail(hotspotId, userId).enqueue(object : Callback<DetailData> {
            override fun onResponse(call: Call<DetailData>, response: Response<DetailData>) {
                if (response.isSuccessful) {
                    //Log.e("detail screen", (response.body()))
                  Log.d(TAG, "onResponse: /// data = "+response.body()?.data?.hotness)
                  Log.d(TAG, "onResponse: /// checke d = "+response.body()?.data?.checkedIn)
                  Log.d(TAG, "onResponse: /// is checke d = "+response.body()?.data?.isCheckedIn)
                    response.body()?.data?.let {

                      view?.successHotspotDetailApi(it) }

                } else {
                    if (response.code() == 401) {
                        view?.sessionExpire()
                    } else {
                        // response.errorBody()?.let { view?.errorApi(it) }
                       // Log.e("error",response.errorBody()!!.string())
                        var jsonObject = JSONObject(response.errorBody()!!.string())
                        val userMessage: String = jsonObject.getString("message")
                        view?.Apimesaage(userMessage)
                    }
                }


                view?.dismissLoading()
            }

            override fun onFailure(call: Call<DetailData>, t: Throwable) {
                view?.dismissLoading()
                view?.failureApi()

            }
        })
    }

    override fun checkIn(hotspotId: String, userId: String) {
        view?.showLoading()
        Log.e("hotspotId ", hotspotId + " userId " + userId)
        RetrofitClient.get().apiCheckIn(hotspotId, userId).enqueue(object : Callback<DetailData> {
            override fun onResponse(call: Call<DetailData>, response: Response<DetailData>) {
                if (response.isSuccessful) {
                 //   Log.e("checkedIn", (r))
                    view?.successCheckInApi(response.body())
                } else {
                    if (response.code() == 401) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()

            }


            override fun onFailure(call: Call<DetailData>, t: Throwable) {
                view?.dismissLoading()
                view?.failureApi()
            }
        })
    }

    override fun favoriteHotSpot(hotspotId: String, userId: String, status: Boolean) {
        view?.showLoading()
        Log.e("hotspotId ", hotspotId + " userId " + userId + " status " + status)
        RetrofitClient.get().apiFavorite(hotspotId, userId, status).enqueue(object : Callback<DetailData> {
            override fun onResponse(call: Call<DetailData>, response: Response<DetailData>) {
                if (response.isSuccessful) {
                    view?.successFavoriteApi()
                } else {
                    if (response.code() == 401) {
                        view?.sessionExpire()
                    } else {
                        //response.errorBody()?.let { view?.errorMassage(it) }
                        var jsonObject = JSONObject(response.errorBody()!!.string())
                        val userMessage: String = jsonObject.getString("message")
                        view?.Apimesaage(userMessage)
                    }
                }

                view?.dismissLoading()

            }

            override fun onFailure(call: Call<DetailData>, t: Throwable) {

                view?.dismissLoading()
                view?.failureApi()
            }
        })
    }

    override fun checkOut(hotspotId: String, userId: String) {
        view?.showLoading()
        RetrofitClient.get().apiCheckOut(hotspotId, userId).enqueue(object : Callback<DetailData> {
            override fun onResponse(call: Call<DetailData>, response: Response<DetailData>) {
                if (response.isSuccessful) {
                    view?.successCheckOutApi(response.body())
                } else {
                    if (response.code() == 401) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()

            }


            override fun onFailure(call: Call<DetailData>, t: Throwable) {
                view?.dismissLoading()
                view?.failureApi()
            }
        })
    }


    override fun addImages(image: File, createdBy: String, hotspotId: String) {
        view?.showLoading()
        val map = HashMap<String, RequestBody>()
        map.put(GeneralMethods.imageToRequestBodyKey(ApiConstants.IMAGE, image.toString()),
                GeneralMethods.imageToRequestBody(image.absolutePath))
        map.put(ApiConstants.CREATED_BY, GeneralMethods.stringToRequestBody(createdBy))
        map.put(ApiConstants.HOTSPOT_ID, GeneralMethods.stringToRequestBody(hotspotId))
        RetrofitClient.get().apiAddImages(map).enqueue(object : Callback<DetailData> {
            override fun onResponse(call: Call<DetailData>, response: Response<DetailData>) {
                if (response.isSuccessful) {
                    view?.successAddImagesApi()
                } else {
                    if (response.code() == 401) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

                view?.dismissLoading()
            }

            override fun onFailure(call: Call<DetailData>, t: Throwable) {
                Log.e(TAG, "onFailure: addImages ::  ",t )
                view?.dismissLoading()
                view?.failureApi()

            }
        })
    }

    override fun love(userId: String, imageId: String, isLike: Boolean) {
        RetrofitClient.get().apiLikeImage(userId, imageId, isLike).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.successLoveApi()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }

            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.failureApi()
                view?.dismissLoading()
            }
        })
    }


    override fun apiReport(imageId: String, userId: String) {
        view?.showLoading()
        RetrofitClient.get().apiReport(imageId, userId).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.successReportApi()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {
                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }
                view?.dismissLoading()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.failureApi()
            }
        })
    }

    override fun apiDelete(imageId: String, hotspotId: String) {
        //view?.showLoading()
        RetrofitClient.get().apiDeletePic(imageId, hotspotId).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    view?.successDeleteApi()
                } else {
                    if (response.code() == ApiConstants.UNAUTHORIZED_ACCESS) {
                        view?.sessionExpire()
                    } else {

                        response.errorBody()?.let { view?.errorApi(it) }
                    }
                }
                view?.dismissLoading()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                view?.dismissLoading()
                view?.failureApi()
            }
        })
    }

    override fun attachView(view: DetailContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }


}
