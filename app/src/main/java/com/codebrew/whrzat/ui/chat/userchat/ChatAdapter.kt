package com.codebrew.whrzat.ui.chat.userchat

import android.content.Context
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.codebrew.whrzat.R
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.webservice.pojo.chat.Message


class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private val TAG = "ChatAdapter"
    private var inflater: LayoutInflater
    private var messageList: ArrayList<Message>
    private var myId: String
    private var mContext: Context

    constructor(context: Context) {
        inflater = LayoutInflater.from(context)
        messageList = ArrayList<Message>()
        myId = Prefs.with(context).getString(Constants.USER_ID, "")
        mContext = context
    }

    companion object {
        var TEXT_MSG = 0
        val TEXT_MSG_RECEIVE = 1
    }


    private fun TextLeftBindHolder(holder: TextLeftViewholder, position: Int) {
        holder.bindItems(messageList[position],mContext)
    }

    private fun TextRightBindholder(holder: TextRightViewholder, position: Int) {
        holder.bindItems(messageList[position],mContext)
    }

    override fun getItemCount(): Int = messageList.size

    override fun getItemViewType(position: Int): Int = messageList[position].type

    class TextRightViewholder(view: View?) : RecyclerView.ViewHolder(view!!) {
        fun bindItems(message: Message, mContext: Context) {
            val face_regular = Typeface.createFromAsset(mContext.assets, "fonts/opensans_regular.ttf")
            itemView.findViewById<TextView>(R.id.tvTextRight).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvTextRight).text = message.text
            itemView.findViewById<TextView>(R.id.tvTimeRight).text = message.time
           // itemView.ivTriangle.setColorFilter(ContextCompat.getColor(mContext, R.color.yellow_dark), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }

    class TextLeftViewholder(view: View?) : RecyclerView.ViewHolder(view!!) {
        fun bindItems(message: Message, mContext: Context) {
            val face_regular = Typeface.createFromAsset(mContext.assets, "fonts/opensans_regular.ttf")
            itemView.findViewById<TextView>(R.id.tvTextLeft).setTypeface(face_regular)
            itemView.findViewById<TextView>(R.id.tvTextLeft).text = message.text
            itemView.findViewById<TextView>(R.id.tvTimeLeft).text = message.time
        }
    }

    fun addMessageList(list: ArrayList<Message>) {
        messageList.clear()
        messageList.addAll(list)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        return when (viewType) {
            TEXT_MSG -> {
                view = inflater.inflate(R.layout.rv_items_text_right, parent, false)
                TextRightViewholder(view)
            }
            else -> {
                view = inflater.inflate(R.layout.rv_items_text_left, parent, false)
                TextLeftViewholder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder?.itemViewType) {
            TEXT_MSG -> {
                TextRightBindholder(holder as TextRightViewholder, position)
            }
            else -> {
                TextLeftBindHolder(holder as TextLeftViewholder, position)
            }
        }
    }

}