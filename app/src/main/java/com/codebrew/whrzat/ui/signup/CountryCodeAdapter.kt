package com.codebrew.tagstrade.adapter


import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.codebrew.tagstrade.util.Country
import com.codebrew.whrzat.R
import kotlin.collections.ArrayList

class CountryCodeAdapter(var context: Context,private var searchList: ArrayList<Country>) :
        RecyclerView.Adapter<CountryCodeAdapter.ViewHolder>() {

    private var countryCodeListener: SetCountryCode? = null



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_item_country_code, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(searchList[position])

    }

    fun setCountryCodeListener(countryCodeListener: SetCountryCode?) {
        this.countryCodeListener = countryCodeListener
    }

    override fun getItemCount(): Int = searchList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {



        init {
            itemView.findViewById<LinearLayout>(R.id.llCode).setOnClickListener(this)
        }

        fun bindItems(country: Country) {
            //itemView.findViewById<TextView>(R.id.tvDialingCode).text = (String.format(Locale.ENGLISH, "+%s", country.dialingCode))

            val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    itemView.findViewById<TextView>(R.id.tvDialingCode).setTextColor(Color.WHITE)
                    itemView.findViewById<TextView>(R.id.tvCountryName).setTextColor(Color.WHITE)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    itemView.findViewById<TextView>(R.id.tvDialingCode).setTextColor(Color.parseColor("#000000"))
                    itemView.findViewById<TextView>(R.id.tvCountryName).setTextColor(Color.parseColor("#000000"))
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }
            itemView.findViewById<TextView>(R.id.tvDialingCode).text =country.dialingCode
            itemView.findViewById<TextView>(R.id.tvCountryName).text = country.name

        }

        override fun onClick(v: View?) {
            when(v?.id){
                R.id.llCode->{
                    if (countryCodeListener != null) {
                        countryCodeListener?.code( searchList[adapterPosition].dialingCode!!, searchList[adapterPosition].name!!)
                    }
                }
            }
        }
    }

    interface SetCountryCode {
        fun code(countryCode: String, country: String)
    }
}