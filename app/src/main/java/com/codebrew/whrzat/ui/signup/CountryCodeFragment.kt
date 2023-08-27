package com.codebrew.tagstrade.fragment

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codebrew.tagstrade.adapter.CountryCodeAdapter
import com.codebrew.tagstrade.util.Country
import com.codebrew.tagstrade.util.CountryDb
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentCountryCodeBinding
import com.codebrew.whrzat.util.GeneralMethods

class CountryCodeFragment : Fragment(), CountryCodeAdapter.SetCountryCode {
    private var countryCodeListener: CountryCodeAdapter.SetCountryCode? = null
    private var countrydb: CountryDb = CountryDb()
    private var mContext: Context? = null
    private  val TAG = "CountryCodeFragment"
    private lateinit var binding: FragmentCountryCodeBinding

    override fun code(countryCode: String, country: String) {
        if (countryCodeListener != null) {
            countryCodeListener?.code(countryCode, country)
            activity?.onBackPressed()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCountryCodeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        setAdapter(countrydb.countryList)
       binding. tvBack.setOnClickListener {
            GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding. etSearchCountryCode)
            activity?.onBackPressed()
        }
        binding. etSearchCountryCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(query: CharSequence, start: Int, before: Int, count: Int) {
                var query = query
                query = query.toString().toLowerCase().trim()
                val searchCountryList = ArrayList<Country>()
                if (query == "") {
                    searchCountryList.clear()
                    setAdapter(countrydb.countryList)
                   // searchCountryList.add(countrydb.countryList)
                } else {
                    for (countries in countrydb.countryList) {
                        if (countries.name!!.toLowerCase().startsWith(query)) {
                            searchCountryList.add(countries)
                        }
                    }
                    setAdapter(searchCountryList)
                }
            }
        })
    }
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.  llcountry.setBackgroundColor(Color.parseColor("#000000"))
                binding. tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                binding.   tvTitle.setTextColor(Color.WHITE)
                binding.  etSearchCountryCode.setBackgroundResource(R.drawable.edit_text_background)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.  llcountry.setBackgroundColor(Color.WHITE)
                binding.  tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                binding.   tvTitle.setTextColor(Color.parseColor("#000000"))
                binding.   etSearchCountryCode.setBackgroundResource(R.drawable.edittext)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    fun setAdapter(countryList: List<Country>) {
        val countryCodeAdapter = CountryCodeAdapter(context!!, countryList as ArrayList<Country>)
        countryCodeAdapter.setCountryCodeListener(this)
        binding. rvCountryCode.layoutManager = LinearLayoutManager(activity)
        binding. rvCountryCode.adapter = countryCodeAdapter
    }

    fun setCountryCodeListener(listener: CountryCodeAdapter.SetCountryCode) {
        this.countryCodeListener = listener
    }
}