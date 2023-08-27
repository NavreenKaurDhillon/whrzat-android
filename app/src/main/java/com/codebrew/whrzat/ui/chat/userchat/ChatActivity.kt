package com.codebrew.whrzat.ui.chat.userchat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivityUserChatBinding
import com.codebrew.whrzat.event.ChatApi
import com.codebrew.whrzat.event.ChatSocketReferesh
import com.codebrew.whrzat.ui.otherprofile.ProfileOtherActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.chat.ChatMsgProfile
import com.codebrew.whrzat.webservice.pojo.chat.Message
import es.dmoral.toasty.Toasty
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.util.*

class ChatActivity : AppCompatActivity(), View.OnClickListener, ChatSocket.SocketCallback, ChatContract.View {


    private  val TAG = "ChatActivity"
    private lateinit var binding: ActivityUserChatBinding

    private var messageList = ArrayList<Message>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatSocket: ChatSocket
    private lateinit var presenter: ChatContract.Presenter
    private lateinit var progressDialog: ProgressDialog
    private var userId = ""
    private var otherUserId = ""
    private var profilePic = ""
    private var blocked = false
    private var blockedBy = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_user_chat)
       // setContentView(R.layout.activity_user_chat)
        Log.d(TAG, "onCreate: StartActivity")
        progressDialog = ProgressDialog(this)
        setAdapter()
        clickListeners()
      //  messageList.clear()
      //  chatAdapter.addMessageList(messageList)

        getDataFromIntent()

        presenter = ChatPresenter()
        presenter.attachView(this)

        // circularImage()
        userId = Prefs.with(this).getString(Constants.USER_ID, "")
        otherUserId = intent.getStringExtra(Constants.OTHER_USER_ID).toString()
        Prefs.with(this).save(Constants.OTHER_USER_ID, otherUserId)

        connectToSocket()
        presenter.receiveChatApi(userId, otherUserId)
        // EventBus.getDefault().postSticky(ChatApi(true))

        val window = window
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()

        val face_semi = Typeface.createFromAsset(assets, "fonts/opensans_semibold.ttf")
        binding.tvUserName.setTypeface(face_semi)
    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tvUserName.setTextColor(Color.WHITE)
                binding.llMain.setBackgroundColor(Color.parseColor("#C8242526"))
                binding.toolbar.setBackgroundColor(Color.parseColor("#000000"))
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor= ContextCompat.getColor(this, R.color.black)
                binding.tvAbout.setColorFilter(getResources().getColor(R.color.white))
                binding.rvChat.alpha= 0.3F
                binding.etText.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)
              //  window.navigationBarColor= ContextCompat.getColor(this, R.color.white)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun getDataFromIntent() {
        if (intent.getStringExtra(Constants.PIC) != null) {
            val req = RequestOptions()
            Glide.with(this)
                    .load(intent.getStringExtra(Constants.PIC))
                    .into(binding.ivSpot)

        }
        if (intent.getStringExtra(Constants.FIRST_NAME) != null) {
            binding.tvUserName.text = intent.getStringExtra(Constants.FIRST_NAME)
        }
    }

    private fun connectToSocket() {
        chatSocket = ChatSocket(this, this)
        chatSocket.connectSocket(otherUserId, userId)

    }

    private fun setAdapter() {
        binding.rvChat.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (binding.rvChat.layoutManager as LinearLayoutManager).reverseLayout = true
        chatAdapter = ChatAdapter(this)
        binding.rvChat.adapter = chatAdapter
        //  (binding.rvChat.layoutManager as LinearLayoutManager).stackFromEnd=true
    }

    private fun clickListeners() {
        binding.tvSend.setOnClickListener(this)
        binding.tvAbout.setOnClickListener(this)
        binding.tvBack.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvSend -> {
                if (GeneralMethods.isNetworkActive(this)) {
                    if (!blocked) {
                        if (!binding.etText.text.toString().trim().isEmpty()) {
                            val message = binding.etText.text.toString().trim()
                            val currentTimeStamp = System.currentTimeMillis()
                            val time = timeCalculation(currentTimeStamp)

                            binding.tvNoMessage.visibility = View.GONE
                            binding.rvChat.smoothScrollToPosition(0)
                            if (!blockedBy) {
                                messageList.add(0, Message(message, time, ChatAdapter.TEXT_MSG))
                                chatAdapter.addMessageList(messageList)
                                chatSocket.sendTextMsg(otherUserId, userId, message, System.currentTimeMillis().toString())
                            } else {
                                Toasty.error(this, "You are blocked by this user", Toast.LENGTH_SHORT).show()
                            }
                            binding.etText.text.clear()
                            EventBus.getDefault().postSticky(ChatApi(true))
                        } else {
                            binding.tvSend.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error))
                        }

                    } else {
                        Toasty.info(this, getString(R.string.label_string_blocked), Toast.LENGTH_SHORT, true).show();
                        // Toast.makeText(this, getString(R.string.label_string_blocked), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toasty.error(this, getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show()

                }
            }
            R.id.tvAbout -> {
                val profileIntent = Intent(this, ProfileOtherActivity::class.java)
                profileIntent.putExtra(Constants.OTHER_USER_ID, otherUserId)
                profileIntent.putExtra(ProfileOtherActivity.PIC_URL, profilePic)
                val p1 = Pair.create(binding.ivSpot as View, "pic")
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1)
                startActivity(profileIntent, options.toBundle())

            }
            R.id.tvBack -> {
                supportFinishAfterTransition()
            }
        }
    }


    override fun onUnMatchByOtherUser() {
    }

    override fun onMessageReceived(message: String, timeStamp: Double, from: String) {
        val time = timeCalculation(timeStamp.toLong())
        messageList.add(0, Message(message, time, ChatAdapter.TEXT_MSG_RECEIVE))
        chatAdapter.addMessageList(messageList)
        binding.tvNoMessage.visibility = View.GONE
        chatSocket.seen(otherUserId, userId)
        binding.rvChat.smoothScrollToPosition(0)
        EventBus.getDefault().postSticky(ChatApi(true))
        EventBus.getDefault().postSticky(ChatSocketReferesh(true))

    }


    override fun onMessageSentSuccessfully(timestamp: String) {
    }


    private fun timeCalculation(TimeStamp: Long): String {
        val cl = Calendar.getInstance()
        cl.timeInMillis = TimeStamp

        val hour = cl.get(Calendar.HOUR_OF_DAY) % 12
        val time = String.format("%02d:%02d %s", if (hour == 0) 12 else hour,
                cl.get(Calendar.MINUTE), if (cl.get(Calendar.HOUR_OF_DAY) < 12) "AM" else "PM")

        return time
    }

    override fun onDestroy() {
        super.onDestroy()
        Prefs.with(this).remove(Constants.OTHER_USER_ID)
        chatSocket.closeSocket()
        presenter.detachView()
    }

    override fun successChatReceiveApi(data: ChatMsgProfile) {
        /**/
        messageList.clear()
        EventBus.getDefault().postSticky(ChatApi(true))
        binding.tvUserName.text = data.profile.name
        blocked = data.blocked
        blockedBy = data.blockedBy

        val req = RequestOptions()
        req.centerCrop()
        Glide.with(this)
                .load(data.profile.profilePicURL.original)
                .apply(req)
                .into(binding.ivSpot)

        profilePic = data.profile.profilePicURL.original

        if (data.arr.isNotEmpty()) {
            binding.tvNoMessage.visibility = View.GONE
        } else {
            binding.tvNoMessage.visibility = View.VISIBLE

        }

        data.arr.indices.forEach { i ->
            val senderId = data.arr[i].senderId
            val time = timeCalculation(data.arr[i].timeStamp.toLong())

            if (senderId == userId) {
                messageList.add(0, Message(data.arr[i].message, time, ChatAdapter.TEXT_MSG))
            } else {
                messageList.add(0, Message(data.arr[i].message, time, ChatAdapter.TEXT_MSG_RECEIVE))

            }
        }
        Collections.reverse(messageList)
        chatAdapter.addMessageList(messageList)
    }

    override fun failureApi() {
        GeneralMethods.showToast(this, R.string.error_server_busy)
        binding.tvAbout.setOnClickListener(null)
    }

    override fun errorApi(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(this, errorBody)
    }
    override fun userDeleted() {
        AlertDialog.Builder(this)
                .setMessage(R.string.dialog_user_deleted)
                .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                    finish()
                }
                .setCancelable(false)
                .show()
    }
    override fun sessionExpire() {
        GeneralMethods.tokenExpired(this)
    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!));
        //super.attachBaseContext(newBase);
    }

    override fun onResume() {
        super.onResume()
     //   setAdapter()
      //  presenter.receiveChatApi(userId, otherUserId)

        if (GeneralMethods.isNetworkActive(this)) {
             //messageList.clear()
             //Collections.reverse(messageList)
            // chatAdapter.addMessageList(messageList)
            // chatAdapter.addMessageList(messageList)
             presenter.receiveChatApi(userId, otherUserId)
         } else {
             GeneralMethods.showToast(this, R.string.error_no_connection)
         }

    }

    override fun onBlockedReceived(isBlocked: Boolean) {
        blockedBy = isBlocked
    }


}
