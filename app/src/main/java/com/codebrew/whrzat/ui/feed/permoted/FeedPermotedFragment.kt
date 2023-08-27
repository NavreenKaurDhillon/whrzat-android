package com.codebrew.whrzat.ui.feed.permoted

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentPermotedFeedBinding

class FeedPermotedFragment : Fragment(){

    private lateinit var mContext: Context
    private lateinit var binding: FragmentPermotedFeedBinding
    private lateinit var feedPermotedAdapter: FeedPermotedAdapter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPermotedFeedBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
       binding.btnCreateEvent.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://whrzat.com/even.html")))
        }
    }

    private fun setAdapter() {
//        feedPermotedAdapter = FeedPermotedAdapter(mContext, eventList)
        binding.rvPermotedFeed.layoutManager = LinearLayoutManager(mContext)
        binding.rvPermotedFeed.adapter = feedPermotedAdapter
       binding.rvPermotedFeed.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))

    }
}