//package com.codebrew.whrzat.notifications
//
//
//import android.util.Log
//
//import com.google.firebase.iid.FirebaseInstanceId
//import com.google.firebase.iid.FirebaseInstanceIdService
//
//class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
//
//    override fun onTokenRefresh() {
//
//        val refreshedToken = FirebaseInstanceId.getInstance().token
//        Log.d(TAG, "Refreshed token: " + refreshedToken!!)
//        //mPrefs.setRegId(refreshedToken);
//
//    }
//
//    companion object {
//        private val TAG = "MyFirebaseIDService"
//    }
//}
