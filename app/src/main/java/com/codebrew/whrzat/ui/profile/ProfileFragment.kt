package com.codebrew.whrzat.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.EditProfileActivity
import com.codebrew.whrzat.databinding.FragmentProfileBinding
import com.codebrew.whrzat.event.EditProfileEvent
import com.codebrew.whrzat.event.RefreshProfileApi
import com.codebrew.whrzat.ui.settings.SettingsActivity
import com.codebrew.whrzat.ui.settings.editProfile.EditProfileFragment
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import com.codebrew.whrzat.util.RetrofitClient
import com.codebrew.whrzat.util.inTransaction
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.codebrew.whrzat.webservice.pojo.otherprofile.ProfileData
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ProfileFragment : Fragment(), View.OnClickListener {


    private  val TAG = "ProfileFragment"
    private lateinit var mContext: Context

    private var fragmentList = ArrayList<Fragment>()
    private lateinit var binding: FragmentProfileBinding
    private lateinit var mPagerAdapter: ProfilePagerAdapter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")

        val userId = Prefs.with(mContext).getString(Constants.USER_ID, "")


        binding.tabProfile.addTab(binding.tabProfile.newTab().setText("Activity"))
        binding.tabProfile.addTab(binding.tabProfile.newTab().setText("Favorites"))
        binding.tabProfile.addTab(binding.tabProfile.newTab().setText("Notifications"))
        binding.tabProfile.tabGravity = TabLayout.GRAVITY_FILL
        setupViewPager()
        setupTabListener()

        clickListener()
        imageCircle()

        if (activity != null) {
            apiProfile()
        }
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        setFontType()
    }
    private fun setFontType() {
        val face_semi = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_semibold.ttf")
        val face_regular = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_regular.ttf")
        val face_bold = Typeface.createFromAsset(activity!!.assets, "fonts/opensans_bold.ttf")

        binding.tvProfileTitle.setTypeface(face_semi)
        binding.tvUserName.setTypeface(face_bold)
        binding.tvTotalLoves.setTypeface(face_regular)
    }
    @SuppressLint("NewApi")
    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.appbar.setBackgroundColor(Color.parseColor("#000000"))
                binding.toolbar.setBackgroundColor(Color.parseColor("#000000"))
                binding.llmainProfile.setBackgroundColor(Color.parseColor("#000000"))
                binding.tabProfile.setBackgroundColor(Color.parseColor("#000000"))
                binding.tabProfile.setSelectedTabIndicatorColor(Color.WHITE)
                binding. tabProfile.setTabTextColors(Color.GRAY, Color.WHITE)
                binding. ivSettings.setColorFilter(getResources().getColor(R.color.white))
                binding. ivEdit.setColorFilter(getResources().getColor(R.color.white))
                binding. tvProfileTitle.setTextColor(Color.WHITE)
                binding.  tvUserName.setTextColor(Color.WHITE)
                /*  val success: Boolean = mMap.setMapStyle(MapStyleOptions(resources
                          .getString(R.string.style_json)))
                  if (!success) {
                      Log.e("map stayle", "Style parsing failed.")
                  }*/
            }
            Configuration.UI_MODE_NIGHT_NO -> {

                binding.  appbar.setBackgroundColor(Color.WHITE)
                binding. tabProfile.setBackgroundColor(Color.WHITE)
                binding. tabProfile.setSelectedTabIndicatorColor(Color.BLACK)
                binding.  tabProfile.setTabTextColors(Color.parseColor("#80000000"), Color.BLACK)


            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
    }
    private fun imageCircle() {

        val userData = Prefs.with(mContext).getObject(Constants.LOGIN_DATA, LoginData::class.java)

        val req = RequestOptions()
        req.placeholder(R.drawable.profile_avatar_placeholder_large)

        try {
            Glide.with(requireActivity())
                    .load(userData.profilePicURL.original)
                    .apply(req)
                    .into(binding.ivSpot)
        }catch (e:Exception){
            e.printStackTrace()
        }

        binding. tvUserDiscription.text = userData.bio
        binding.  tvUserName.text = userData.name
        binding. tvCity.text = userData.contact
        //tvTotalLoves.text = userData.loves.toString().plus(" Loves")

        // (vpProfile?.getChildAt(0) as ActivityFragment).loves()

        // tvTotalLoves.text = (mPagerAdapter.getItem(0) as ActivityFragment).loves().toString().plus(" Loves")
    }

    private fun apiProfile() {
        var userId = Prefs.with(mContext).getString(Constants.USER_ID, "")
        var tok=Prefs.with(context).getString(Constants.ACCESS_TOKEN, "")
        RetrofitClient.get().apiGetProfile(userId, userId).enqueue(object : Callback<ProfileData> {
            override fun onResponse(call: Call<ProfileData>, response: Response<ProfileData>) {
                if (response.isSuccessful) {
                    if (activity != null) {

                        if(response.body()?.data?.loves!! >1){
                            binding.  tvTotalLoves?.text = response.body()?.data?.loves.toString().plus(" Likes")
                        }else{
                            binding. tvTotalLoves?.text = response.body()?.data?.loves.toString().plus(" Like")
                        }


                    }
                } else {

                }

                //  view?.dismissLoading()

            }

            override fun onFailure(call: Call<ProfileData>, t: Throwable) {
                // view?.failureApi()
                //  view?.dismissLoading()
            }
        })
    }

    private fun clickListener() {
        binding.   ivSettings.setOnClickListener(this)
        binding.   ivEdit.setOnClickListener(this)
    }

    private fun setupTabListener() {
        binding.   tabProfile.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.   vpProfile.currentItem = tab.position

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }


    private fun setupViewPager() {
        mPagerAdapter = ProfilePagerAdapter(mContext, childFragmentManager)
        binding.  vpProfile.adapter = mPagerAdapter
        //var fragment=mPagerAdapter.getItem(0)
        binding. vpProfile.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabProfile))
        //  (ActivityFragment  as get).yourMethodName()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSettings -> {
                val intent = Intent(mContext, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.ivEdit->{
                val intent = Intent(mContext, EditProfileActivity::class.java)
                startActivity(intent)

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEditProfileSuccess(editData: EditProfileEvent) {

        EventBus.getDefault().removeStickyEvent<EditProfileEvent>(EditProfileEvent::class.java)
        if (editData.isProfileEdited) {
            //EventBus.getDefault().postSticky(FeedApi(true))
            imageCircle()
            apiProfile()
            EventBus.getDefault().postSticky(RefreshProfileApi(true))

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
