package com.codebrew.tagstrade.adapter



import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.codebrew.whrzat.ui.chat.ChatAllFragment
import com.codebrew.whrzat.ui.Home.HomeFragment
import com.codebrew.whrzat.ui.addhotspot.AddSpotFragment
import com.codebrew.whrzat.ui.profile.ProfileFragment


class ContainerPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private  val TAG = "ContainerPagerAdapter"

    private var TOTAL_ITEMS=4

 /*   override fun getItem(position: Int): Fragment {
        when (position) {
            EXPLORE -> return HomeFragment()
//            FEED -> return FeedContactFragment()
  //          FEED -> return FeedHomeFragment()
            ADD -> return AddSpotFragment()
            CHAT -> return ChatAllFragment()
            PROFILE -> return ProfileFragment()
        }


    }*/
 override fun getItem(p0: Int): Fragment {
     Log.d(TAG, "getItem: $p0")

     when(p0){

         EXPLORE -> return HomeFragment()
         ADD -> return AddSpotFragment()
         CHAT -> return ChatAllFragment()
         PROFILE -> return ProfileFragment()
         else -> return HomeFragment()
     }
 }

        override fun getCount(): Int = TOTAL_ITEMS

    companion object {
        val EXPLORE = 0
       // val FEED = 1
        val ADD = 1
        val CHAT = 2
        val PROFILE = 3
    }
}
