package com.codebrew.whrzat.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.arvind.imageandlocationmanager.activities.ImageGetterActivity
import com.codebrew.whrzat.R
import com.codebrew.whrzat.ui.settings.editProfile.EditProfileFragment
import com.codebrew.whrzat.util.inTransaction
import java.io.File

class EditProfileActivity : ImageGetterActivity(),ImageGetterActivity.ImagePickerForFragment {
    private  val TAG = "EditProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate: StartActivity")
         val fragment=EditProfileFragment()
        supportFragmentManager.beginTransaction().add(R.id.frContainer,fragment).commit()
        val window = window
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {

                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor= ContextCompat.getColor(this, R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)

                //window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }
    override fun file(file: File) {

    }

    override fun setImagePickedListener(listener: OnImageSelectListener?) {
        setOnImageSelectListener(listener)
    }

    override fun openImagePickerFromFragment() {
        openImageSelector()
    }
}