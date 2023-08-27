package com.codebrew.whrzat.ui.feed

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentFeedHomeBinding
import com.codebrew.whrzat.ui.feed.contacts.FeedContactFragment
import com.codebrew.whrzat.ui.feed.happening.FeedHappeningFragment
import com.codebrew.whrzat.ui.feed.permoted.FeedPermotedFragment
import com.codebrew.whrzat.util.ViewPagerAdapter

class FeedHomeFragment: Fragment() {
    private lateinit var binding: FragmentFeedHomeBinding
    private  val TAG = "FeedHomeFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFeedHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")
        val adapter = ViewPagerAdapter(childFragmentManager)
        binding.viewPager.adapter = adapter

        binding.tabLayout.setupWithViewPager(binding.viewPager)
        setUpViewPager(binding.viewPager)
    }

    fun setUpViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(childFragmentManager)
//        adapter.addFragment(FeedPermotedFragment(),"Promoted")
        adapter.addFragment(FeedHappeningFragment(), "Happening")
        adapter.addFragment(FeedContactFragment(), "Contacts")
        viewPager.adapter = adapter
    }
}