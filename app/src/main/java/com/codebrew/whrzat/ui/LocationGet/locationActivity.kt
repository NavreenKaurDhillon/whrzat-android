package com.codebrew.whrzat.ui.LocationGet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivityLocationBinding
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.LocationResposeData
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody

class locationActivity : AppCompatActivity(),LocationContarct.View {
    private lateinit var locationsAdapter: LocationsAdapter
    private lateinit var presenter: LocationContarct.Presenter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityLocationBinding

    private  val TAG = "locationActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_location)
//        setContentView(R.layout.activity_location)
        Log.d(TAG, "onCreate: StartActivity")
        progressDialog = ProgressDialog(this)
        presenter = LocationsPresenter()
        presenter.attachView(this)
        presenter.performgetlocation(Prefs.with(this).getString(Constants.ACCESS_TOKEN,""))
        setAdapter()


        binding.tvBack.setOnClickListener {
            finish()
        }
    }

    private fun setAdapter() {
       binding. rvlocation.layoutManager = LinearLayoutManager(this)
     //   locationsAdapter.setListener(this)
       binding. rvlocation.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    override fun locationSuccess(data: LocationResposeData?) {
        locationsAdapter= LocationsAdapter(this,data?.data)
       binding. rvlocation.adapter = locationsAdapter
    }

    override fun locationError(errorMessage: ResponseBody) {
        GeneralMethods.showErrorMsg(this, errorMessage)
    }

    override fun locationFailer(message: String) {
        Toasty.error(this,message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun dismissLoading() {
       progressDialog.dismiss()
    }


}