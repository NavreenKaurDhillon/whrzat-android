package com.codebrew.whrzat.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.core.util.Pair
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.codebrew.tagstrade.adapter.ChatAllAdapter
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentChatBinding
import com.codebrew.whrzat.event.ChatApi
import com.codebrew.whrzat.event.ChatHistoryRefershApi
import com.codebrew.whrzat.event.RefershChatScreen
import com.codebrew.whrzat.ui.chat.allchats.ChatAllContract
import com.codebrew.whrzat.ui.chat.allchats.ChatAllPresenter
import com.codebrew.whrzat.ui.chat.userchat.ChatActivity
import com.codebrew.whrzat.ui.profile.NotificationSocket
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.chat.ChatAllUser
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ChatAllFragment : Fragment(), ChatAllAdapter.UserClick, ChatAllContract.View, TextWatcher,
        NotificationSocket.SocketCallback, SwipeRefreshLayout.OnRefreshListener {
    private  val TAG = "ChatAllFragment"
    private lateinit var chatAdapter: ChatAllAdapter
    private lateinit var mContext: Context
    private lateinit var presenter: ChatAllContract.Presenter
    private var userChatList = ArrayList<ChatAllUser>()
    private var userId = ""
    private lateinit var progressDialog: ProgressDialog
    private var isChatCount = false
    private lateinit var countSocket: NotificationSocket
    private lateinit var binding: FragmentChatBinding


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")

        progressDialog = ProgressDialog(mContext)
        setAdapter()
        userId = Prefs.with(mContext).getString(Constants.USER_ID, "")
        presenter = ChatAllPresenter()
        binding.swipeToRefresh.setOnRefreshListener(this)
        presenter.attachView(this)
        chatAdapter.clearList()
        Prefs.with(context).getString(Constants.ACCESS_TOKEN, "")
        presenter.chatALlUserApi(userId, 0, 0)
        searchUserChat()
        searchCursor()
        binding.etSearch.isFocusable = false
        binding.etSearch.hint = getString(R.string.label_search_chat)
        binding.etSearch.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.etSearch.setRawInputType(InputType.TYPE_CLASS_TEXT)


        countSocket = NotificationSocket(activity as FragmentActivity, this)
        countSocket.connectSocket(userId, userId)
        countSocket.getchatCount(userId, userId)
        countSocket.sendTextMsg(userId)
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        val face_semi = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_semibold.ttf")
        binding.tvAllChatTitle.setTypeface(face_semi)
        binding.tvNoChat.setTypeface(face_semi)

    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tvAllChatTitle.setTextColor(Color.WHITE)
                binding.rlAllChat.setBackgroundColor(Color.parseColor("#000000"))
                //imgback.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                binding.toolbarAllChat.setBackgroundColor(Color.parseColor("#000000"))
                binding.etSearch.setHintTextColor(Color.parseColor("#000000"))
                binding.etSearch.setTextColor(Color.parseColor("#000000"))
                //imgPlaceholder.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.tvAllChatTitle.setTextColor(Color.parseColor("#000000"))
                binding.rlAllChat.setBackgroundColor(Color.WHITE)
                //imgback.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                binding.toolbarAllChat.setBackgroundColor(Color.WHITE)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    override fun onRefresh() {

        presenter.chatALlUserApi(userId, 0, 0)
    }

    private fun searchCursor() {
        binding.etSearch.setOnTouchListener { v, event ->
            binding.etSearch.isFocusableInTouchMode = true
            false
        }
    }

    private fun searchUserChat() {
        binding.etSearch.addTextChangedListener(this)
    }

    override fun successDeleteUser() {
        presenter.chatALlUserApi(userId,0,0)
        if (chatAdapter.itemCount == 0) {
            binding.tvNoChat?.visibility = View.VISIBLE
            binding.tvSwipeLeft?.visibility = View.GONE
        } else {
            binding.tvNoChat?.visibility = View.GONE
            binding.tvSwipeLeft?.visibility = View.VISIBLE

        }
    }

    override fun onMessageReceived(message: String) {
    }

    override fun onChatMessage(message: String) {
    }


    override fun afterTextChanged(query: Editable) {
    }

    override fun beforeTextChanged(query: CharSequence, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(query: CharSequence, p1: Int, p2: Int, p3: Int) {
        if (!userChatList.isEmpty()) {
            val queryText = query.toString().toLowerCase().trim()

            if (!queryText.isEmpty()) {
                val userFilteredChat = ArrayList<ChatAllUser>()
                userFilteredChat.clear()
                userChatList.filterTo(userFilteredChat) {

                    it.message.receiverId.name.toLowerCase().startsWith(queryText)
                    if (it.message.senderId._id.equals(Prefs.with(context).getString(Constants.USER_ID, ""))) {
                        it.message.receiverId.name.toLowerCase().startsWith(queryText)
                    } else {
                        it.message.senderId.name.toLowerCase().startsWith(queryText)
                    }
                }
                if (userFilteredChat.isEmpty()) {
                    binding.tvNoChat?.visibility = View.VISIBLE
                } else {
                    binding.tvNoChat?.visibility = View.GONE
                }
                binding.tvSwipeLeft?.visibility = View.GONE
                chatAdapter.addList(userFilteredChat)

            } else {
                binding.tvNoChat?.visibility = View.GONE
                binding.tvSwipeLeft?.visibility = View.VISIBLE
                chatAdapter.addList(userChatList)
            }
        } else {

        }
    }

    private fun setAdapter() {
        chatAdapter = ChatAllAdapter(mContext)
        binding.rvChatAll.layoutManager = LinearLayoutManager(mContext)
        binding.rvChatAll.adapter = chatAdapter
        binding.rvChatAll.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
        chatAdapter.setListener(this)

    }

    override fun onDelete(id: String) {
        presenter.apiDelete(userId, id)
    }


    override fun successChatAllUserApi(data: List<ChatAllUser>) {

        binding.swipeToRefresh.isRefreshing = false
        if (data.isNotEmpty()) {
            binding.tvNoChat?.visibility = View.GONE
            binding.tvSwipeLeft?.visibility = View.VISIBLE
            userChatList.clear()
            userChatList.addAll(data)
            chatAdapter.addList(data)
        } else {
            userChatList.clear()
            userChatList.addAll(data)
            chatAdapter.addList(data)
            chatAdapter.notifyDataSetChanged()
            binding.tvNoChat?.visibility = View.VISIBLE
            binding.tvSwipeLeft?.visibility = View.GONE
        }
    }

    override fun userClick(id: String, imageUrl: String, name: String, ivProfilePic: ImageView, tvName: TextView) {
        val intent = Intent(mContext, ChatActivity::class.java)
        intent.putExtra(Constants.OTHER_USER_ID, id)
        intent.putExtra(Constants.PIC, imageUrl)
        intent.putExtra(Constants.FIRST_NAME, name)
        val p1 = Pair.create(ivProfilePic as View, "pic")
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as FragmentActivity, p1)
        startActivity(intent, options.toBundle())
    }


    override fun failureApi() {
        binding.swipeToRefresh.isRefreshing = false
        // GeneralMethods.showToast(mContext,R.string.error_server_busy)
    }

    override fun errorApi(errorBody: ResponseBody) {
        binding.swipeToRefresh.isRefreshing = false
        GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun sessionExpire() {
        binding.swipeToRefresh.isRefreshing = false
        GeneralMethods.tokenExpired(mContext)
    }

    override fun showLoading() {
        progressDialog.show()

    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onChatAll(chatAllRefresh: ChatHistoryRefershApi) {

        EventBus.getDefault().removeStickyEvent<ChatHistoryRefershApi>(ChatHistoryRefershApi::class.java)

        if (chatAllRefresh.isApiRefresh) {
            isChatCount = true
            // presenter.chatALlUserApi(userId, 0, 0)

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onApiChat(chatApi: ChatApi) {

        EventBus.getDefault().removeStickyEvent<ChatApi>(ChatApi::class.java)

        if (chatApi.isChatApi) {
            userChatList.clear()
            presenter.chatALlUserApi(userId, 0, 0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onHideChatCount(refreshScreen: RefershChatScreen) {

        EventBus.getDefault().removeStickyEvent<RefershChatScreen>(RefershChatScreen::class.java)

        if (refreshScreen.isApiChat) {
            //   if (isChatCount) {
            // isChatCount = false
            userChatList.clear()
            presenter.chatALlUserApi(userId, 0, 0)
            //}

        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

    }


}
