package com.codebrew.whrzat.ui.profile

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.codebrew.whrzat.ui.profile.favorite.FavoriteFragment

class ProfilePagerAdapter(fm: Context, childFragmentManager: FragmentManager) : FragmentPagerAdapter(childFragmentManager) {

    private var TOTAL_ITEMS = 3

    override fun getItem(position: Int): Fragment {
        when (position) {
            ACTIVITY -> return ActivityFragment()
            FAVORITE -> return FavoriteFragment()
            NOTIFICATIONS -> return NotificationsFragment()
           else->return ActivityFragment()
        }

    }

    override fun getCount(): Int = TOTAL_ITEMS

    companion object {
        val ACTIVITY = 0
        val FAVORITE = 1
        val NOTIFICATIONS = 2

    }
}
