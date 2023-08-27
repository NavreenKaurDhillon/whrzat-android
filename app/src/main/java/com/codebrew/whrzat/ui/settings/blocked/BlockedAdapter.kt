package com.codebrew.whrzat.ui.settings.blocked

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData



class BlockedAdapter(var mContext: Context) : RecyclerView.Adapter<BlockedAdapter.BlockedViewHolder>() {

    private var blockList = ArrayList<UserData>()
    private lateinit var unblockListner: OnUnBlockListner

    fun setListener(unblockListner: OnUnBlockListner) {
        this.unblockListner = unblockListner
    }

    override fun getItemCount(): Int = blockList.size

    inner class BlockedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {


        init {

            itemView.findViewById<TextView>(R.id.tvBlock).setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if (GeneralMethods.isNetworkActive(mContext)) {
                unblockListner.onUnblock(blockList[adapterPosition]._id)
                blockList.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
            } else {
                GeneralMethods.showToast(mContext, R.string.error_no_connection)
            }
        }

        fun bindItems(userData: UserData) {

            val mode = mContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            when (mode) {
                Configuration.UI_MODE_NIGHT_YES -> {

                    itemView.findViewById<TextView>(R.id.tvName).setTextColor(Color.WHITE)
                }
                Configuration.UI_MODE_NIGHT_NO -> {

                    itemView.findViewById<TextView>(R.id.tvName).setTextColor(Color.parseColor("#000000"))
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                }
            }
            val req = RequestOptions()
            Glide.with(mContext)
                    .load(userData.profilePicURL.original)
                    .apply(req)
                    .into(itemView.findViewById(R.id.ivSpot))

            itemView.findViewById<TextView>(R.id.tvName).text = userData.name

        }
    }

    fun addList(blockList: List<UserData>) {
        this.blockList.addAll(blockList)
        notifyDataSetChanged()
    }

    interface OnUnBlockListner {
        fun onUnblock(id: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedViewHolder {
        val view:View=LayoutInflater.from(mContext).inflate(R.layout.rv_tem_blocked, parent, false)
        return BlockedViewHolder(view)

    }

    override fun onBindViewHolder(holder: BlockedViewHolder, position: Int) {
        holder.bindItems(blockList[position])
    }

}