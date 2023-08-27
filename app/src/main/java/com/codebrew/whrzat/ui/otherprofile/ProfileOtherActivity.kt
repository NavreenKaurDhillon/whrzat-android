package com.codebrew.whrzat.ui.otherprofile

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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codebrew.tagstrade.adapter.FeedContactAdapter
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentProfileOtherBinding
import com.codebrew.whrzat.event.FeedApi
import com.codebrew.whrzat.ui.chat.userchat.ChatActivity
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.GeneralMethods
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.ProgressDialog
import com.codebrew.whrzat.webservice.pojo.otherprofile.ImageData
import com.codebrew.whrzat.webservice.pojo.otherprofile.UserData
import es.dmoral.toasty.Toasty
import io.github.inflationx.viewpump.ViewPumpContextWrapper

import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus


class ProfileOtherActivity : AppCompatActivity(), View.OnClickListener, ProfileContract.View, FeedContactAdapter.Love {


    companion object {
        var PIC_URL = "pic"
        var USERNAME = "username"
    }

    private  val TAG = "ProfileOtherActivity"
    private lateinit var presenter: ProfileContract.Presenter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var profileAdapter: ProfileOtherAdapter
    private var userId = ""
    private var otherUserId = ""
    private lateinit var binding: FragmentProfileOtherBinding
    private var pic = ""
    var imagesList = ArrayList<ImageData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.fragment_profile_other)
//        setContentView(R.layout.fragment_profile_other)
        Log.d(TAG, "onCreate: StartActivity")


        progressDialog = ProgressDialog(this)
        presenter = ProfilePresenter()
        presenter.attachView(this)

        clickListeners()
        setAdapter()

        userId = Prefs.with(this).getString(Constants.USER_ID, "")
        otherUserId = intent.getStringExtra(Constants.OTHER_USER_ID).toString()
        pic = intent.getStringExtra(PIC_URL).toString()
        //  binding.tvUserName.text=intent.getStringExtra(USERNAME)

        val req = RequestOptions()
        req.placeholder(R.drawable.profile_avatar_placeholder_large)

        Glide.with(this)
                .load(intent.getStringExtra(PIC_URL))
                .apply(req)
                .into(binding.ivSpot)

        if (userId == otherUserId) {
            binding.llLayout.visibility = View.GONE
        }
        presenter.apiGetProfile(otherUserId, userId)

        val window = window
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        setFontType()
    }
    private fun setFontType() {
        val face_semi = Typeface.createFromAsset(assets, "fonts/opensans_semibold.ttf")
        val face_regular = Typeface.createFromAsset(assets, "fonts/opensans_regular.ttf")
        val face_bold = Typeface.createFromAsset(assets, "fonts/opensans_bold.ttf")

        binding.tvTitle.setTypeface(face_semi)
        binding.tvUserName.setTypeface(face_bold)
        binding.tvTotalLoves.setTypeface(face_regular)
        binding.tvNoActivity.setTypeface(face_semi)

    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.appbarProfile.setBackgroundColor(Color.parseColor("#000000"))
                binding.toolbar.setBackgroundColor(Color.parseColor("#000000"))
                binding.ctlBar.setBackgroundColor(Color.parseColor("#000000"))
                binding.tvTitle.setTextColor(Color.WHITE)
                binding.tvUserName.setTextColor(Color.WHITE)
                binding.tvTotalLoves.setTextColor(Color.WHITE)
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                binding.tvChat.setTextColor(Color.WHITE)
                binding.tvBlock.setTextColor(Color.WHITE)
                binding.tvChat.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                binding.tvBlock.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))

                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                window.navigationBarColor=ContextCompat.getColor(this, R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {

                binding.appbarProfile.setBackgroundColor(Color.WHITE)
                binding.toolbar.setBackgroundColor(Color.WHITE)
                binding.ctlBar.setBackgroundColor(Color.WHITE)
                binding.tvChat.setTextColor(Color.BLACK)
                binding.tvBlock.setTextColor(Color.BLACK)
                binding.tvChat.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
                binding.tvBlock.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)

                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                window.statusBarColor = ContextCompat.getColor(this, R.color.white)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }

    private fun setAdapter() {
        binding.rvProfileOthers.visibility=View.VISIBLE
        profileAdapter = ProfileOtherAdapter(this,imagesList)
        binding.rvProfileOthers.layoutManager = LinearLayoutManager(this)
        binding.rvProfileOthers.adapter = profileAdapter
        profileAdapter.setListener(this)
        binding.rvProfileOthers.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    private fun clickListeners() {
        binding.tvBack.setOnClickListener(this)
        binding.tvBlock.setOnClickListener(this)
        binding.tvChat.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvBack -> {
                supportFinishAfterTransition()
            }

            R.id.tvChat -> {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(Constants.OTHER_USER_ID, otherUserId)
                intent.putExtra(Constants.PIC, pic)
                val p1 = Pair.create(binding.ivSpot as View, "pic")
                // val p2 = Pair.create(tvName as View, "name")
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1)
                startActivity(intent, options.toBundle())
            }

            R.id.tvBlock -> {
                if (binding.tvBlock.text.toString() == getString(R.string.label_block)) {
                    ConfirmBlock()
                } else {
                    presenter.apiBlockProfile(userId, otherUserId, false)
                }
            }
        }
    }

    private fun ConfirmBlock() {
        AlertDialog.Builder(this, R.style.MyDialog)
                .setTitle(getString(R.string.R_string_label_block_user))
                .setPositiveButton(android.R.string.ok) { dialogInterface, i ->

                    presenter.apiBlockProfile(userId, otherUserId, true)

                }
            .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .show()
    }


    override fun successProfileApi(data: UserData) {
        //binding.ctlBar.visibility=View.VISIBLE
        /*  val req= RequestOptions()
          req.circleCrop()
          req.placeholder(R.drawable.profile_avatar_placeholder_large)

          Glide.with(this)
                  .load(data.profilePicURL.original)
                  .apply(req)
                  .into(ivProfilePic)*/

        binding.tvUserDiscription.text = data.bio
        if(data.loves!! >1){
            binding.tvTotalLoves.text = data.loves.toString().plus(" Likes")
        }else{
            binding.tvTotalLoves.text = data.loves.toString().plus(" Like")
        }

        binding.tvCity.text = data.contact
        binding.tvUserName.text = data.name

        if (!data.blockedBy.isEmpty()) {
            for (id in data.blockedBy) {
                if (userId == id) {
                    binding.tvBlock.text = getString(R.string.label_unblock)
                  //  Prefs.with(this).save(Constants.BLOCK_USER,true)
                } else {
                    binding.tvBlock.text = getString(R.string.label_block)
                }
            }
        } else {
            binding.tvBlock.text = getString(R.string.label_block)

        }
        if (data.images.size==0){
            binding.tvNoActivity.visibility=View.VISIBLE
        }else{
            binding.tvNoActivity.visibility=View.GONE
        }
        imagesList.addAll(data.images)
        Log.e("imagelist",""+imagesList.size)
        setAdapter()
       // profileAdapter.addList(imagesList)

    }

    override fun successReportApi() {
        Toasty.success(this, getString(R.string.label_report_success), Toast.LENGTH_SHORT, true).show()

        // GeneralMethods.showToast(this, R.string.label_report_success)
    }

    override fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int) {
        presenter.love(userId, imageId, isLike, likeCount, position)
        EventBus.getDefault().postSticky(FeedApi(true))
    }

    override fun report(id: String) {
        presenter.apiReport(id, userId)
    }

    override fun viewProfile(otherUserid: String, ivProfilePic: ImageView, tvUserName: TextView, original: String, name: String) {
    }


    override fun successBlockApi() {

        if (binding.tvBlock.text == getString(R.string.lable_block)) {
            binding.tvBlock.text = getString(R.string.label_unblock)
        } else {
            binding.tvBlock.text = getString(R.string.label_block)
        }

        EventBus.getDefault().postSticky(FeedApi(true))

    }

    override fun successLoveApi(likeCount: String, pos: Int) {
        EventBus.getDefault().postSticky(FeedApi(true))

    }

    override fun errorApi(errorBody: ResponseBody) {

        GeneralMethods.showErrorMsg(this, errorBody)
    }

    override fun failureApi() {
        GeneralMethods.showToast(this, R.string.error_server_busy)
    }

    override fun sessionExpire() {
        GeneralMethods.tokenExpired(this)
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun onChatClick(id: String, ivProfilePic: ImageView, original: String) {
    }

    override fun attachBaseContext(newBase: Context?) {
       // super.attachBaseContext(newBase);
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!));
    }


    override fun spotDetail(id: String) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(Constants.HOTSPOT_ID, id)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

}