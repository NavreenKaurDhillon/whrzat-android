package com.codebrew.whrzat.ui.LocationGet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.whrzat.R
import com.codebrew.whrzat.webservice.pojo.LocationResposeData

class LocationsAdapter(var locationActivity: locationActivity, var data: Array<LocationResposeData.Data?>?) :RecyclerView.Adapter<LocationsAdapter.ViewHolder> (){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
     var tvlocation_name=itemView.findViewById<TextView>(R.id.tvlocation_name)
        var tvlocation_address=itemView.findViewById<TextView>(R.id.tvlocation_address)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view:View=LayoutInflater.from(locationActivity).inflate(R.layout.rv_location_adapter,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.tvlocation_name.text=data?.get(position)?.name
        holder.tvlocation_address.text=data?.get(position)?.address
    }

    override fun getItemCount(): Int {
        return data!!.size
    }
}