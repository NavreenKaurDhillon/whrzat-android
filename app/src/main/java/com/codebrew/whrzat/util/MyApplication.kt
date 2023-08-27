package com.codebrew.whrzat.util

//import com.crashlytics.android.Crashlytics
//import io.fabric.sdk.android.Fabric

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.codebrew.whrzat.R
//import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import io.branch.referral.Branch
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump


class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Firebase.messaging.isAutoInitEnabled = true
      // Branch logging for debugging
      Branch.enableTestMode()

      // Branch object initialization
      Branch.getAutoInstance(this)

        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath(resources.getString(R.string.bariol))
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())





        RetrofitClient.setUpRetrofitClient(applicationContext)

        val appToken = "{YourAppToken}"
        val environment = AdjustConfig.ENVIRONMENT_SANDBOX
        val config = AdjustConfig(this, appToken, environment)
        Adjust.onCreate(config)
        registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())
//        Tracker.configure(Tracker.Configuration(applicationContext)
//                .setAppGuid("kowhrzat-5uwh0k7z")
//                .setLogLevel(Tracker.LOG_LEVEL_INFO)
//        )
    }

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
        MultiDex.install(this)
    }


    companion object {

        private class AdjustLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityDestroyed(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivityCreated(activity: Activity, p1: Bundle?) {
            }

            override fun onActivityResumed(activity: Activity) {
                Adjust.onResume()
            }

            override fun onActivityPaused(activity: Activity) {
                Adjust.onPause()
            }
        }

    }
}
