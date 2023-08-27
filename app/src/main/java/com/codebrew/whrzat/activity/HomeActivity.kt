package com.codebrew.whrzat.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.codebrew.tagstrade.adapter.ContainerPagerAdapter
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActivityContainerBinding
import com.codebrew.whrzat.event.*
import com.codebrew.whrzat.ui.addhotspot.AddSpotActivity
import com.codebrew.whrzat.ui.chat.userchat.ChatActivity
import com.codebrew.whrzat.ui.detailhotspot.DetailActivity
import com.codebrew.whrzat.ui.profile.NotificationSocket
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import com.google.firebase.crashlytics.FirebaseCrashlytics
import es.dmoral.toasty.Toasty
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import repository.GPSTracker
import java.io.IOException
import java.util.*

//val badgeDrawable = binding.bottomNavigation.getBadge(R.id.tvChatMenu)
//if (badgeDrawable != null) {
//  badgeDrawable.isVisible = false
//  badgeDrawable.clearNumber()
//}

class HomeActivity : AppCompatActivity(), View.OnClickListener, NotificationSocket.SocketCallback {

  private val TAG = "HomeActivity"
  private var mBackPressed = 0L
  private val TIME_INTERVAL = 3000
  private var userId = ""
  private lateinit var countSocket: NotificationSocket
  var gps: GPSTracker? = null
  var mlongitude: String = ""
  var mlatitude: String = ""
  private lateinit var binding: ActivityContainerBinding

  var requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { result ->
    Log.e(
      TAG,
      "onActivityResult: notification Permisstion result :-- $result"
    )
    if (Build.VERSION.SDK_INT >= 33) {
      if (!result) {
        askNotificationPermission()
      }
    }
  }


  companion object{
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
  }
  @SuppressLint("NewApi")
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this,R.layout.activity_container)
//        setContentView(R.layout.activity_container)
    Log.d(TAG, "onCreate: StartActivity"+savedInstanceState)

    // setDialogsCancelable(false)
    //  checkPermissions()
    Log.d(TAG, "onCreate: // visited = "+(Prefs.with(this).getInt(Constants.VISITED_HOME,0)).toString())
    if (Prefs.with(this).getInt(Constants.VISITED_HOME,0)==0) {

      Log.d(TAG, "onCreate: visited = 0")
      if (ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
      ) {
        Log.d(TAG, "onCreate: // already granted ")
        // Permission is already granted, proceed with your functionality
        // ...
      } else {
        Log.d(TAG, "onCreate: /// denied")
          showDialogForPermission()

      }
    }
    else {
      Log.d(TAG, "onCreate: // vsisted = 1")
      if (ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        explain(
          "Alert",
          "Please switch on location services in settings to get nearby hotspots.",
          "Settings"
        )
      }
    }

    Prefs.with(this).save(Constants.VISITED_HOME,1)

//        checkAndRequestPermissions()

    val badgeDrawable = binding.bottomNavigation.getBadge(R.id.tvChatMenu)
if (badgeDrawable != null) {
  badgeDrawable.isVisible = false
  badgeDrawable.clearNumber()
}
    if(mlatitude == "" && mlongitude == ""){
      Prefs.with(this).save(Constants.LAT, latitude)
      Prefs.with(this).save(Constants.LNG, longitude)
      setupPagerAdapter()
    }
    clickEvent()
//        changeViews(ContainerPagerAdapter.EXPLORE, binding.tvExplore, R.drawable.ic_home_active)
    changeViews(ContainerPagerAdapter.EXPLORE,binding.bottomNavigation.menu.getItem(0).itemId)
    binding.bottomNavigation.menu.findItem(R.id.tvExploreMenu).setChecked(true)
    //dentifierAction(R.id.tvExploreMenu, 0);
    userId = Prefs.with(this).getString(Constants.USER_ID, "")
    countSocket = NotificationSocket(this, this)
    countSocket.connectSocket(userId, userId)
    countSocket.sendTextMsg(userId)
    countSocket.getchatCount(userId, userId)

    onNotificationReceived()
    //EventBus.getDefault().postSticky(FeedApi(true))
    val window = window
    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
    enableNightmode()
    getVersion()


    val id=intent.getStringExtra(Constants.HOTSPOT_ID)
    Log.d(TAG, "onCreate:  // uid = "+id)
    if (id.isNullOrEmpty() || id.length==0){

    }
    else{
      val detailSpot = Intent(this, DetailActivity::class.java)
      detailSpot.putExtra(Constants.HOTSPOT_ID, id)
      startActivity(detailSpot)
    }
    Log.d(TAG, "onCreate: 1//"+id.isNullOrEmpty())
    Log.d(TAG, "onCreate: 2//"+(id==null))
    Log.d(TAG, "onCreate: 3//"+(id=="null"))
    Log.d(TAG, "onCreate: 4//"+id?.length)
  }

  override fun onPause() {
    super.onPause()

    if (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
//      explain("Alert","Please switch on location services in settings to get nearby hotspots.","Settings")

      Log.d(TAG, "onCreate: // pause ")
    }
  }
  private fun showDialogForPermission() {

    val builder = AlertDialog.Builder(this)
    val customView : View= getLayoutInflater().inflate(R.layout.custom_layout, null);
    builder.setView(customView);

    val textViewTitle = customView.findViewById<TextView>(R.id.heading)
    val textViewMessage = customView.findViewById<TextView>(R.id.description)
    textViewTitle.setText(R.string.permission_title)
    textViewMessage.setText(R.string.permission_description)
    val okButton = customView.findViewById<TextView>(R.id.okBT)

    builder.setView(customView)
    // Create the AlertDialog
    val alertDialog: AlertDialog = builder.create()
    // Set other dialog properties
    alertDialog.setCancelable(false)
    okButton.setOnClickListener {
      checkAndRequestPermissions()
      alertDialog.dismiss()
    }
    alertDialog.show()
  }

  private fun checkDynamicLinkData() {
    val hotspotIdFromIntent = intent.getStringExtra(Constants.HOTSPOT_ID)
    Log.d(TAG, "checkDynamicLinkData: // hotspot id in home = " + hotspotIdFromIntent)
    if (hotspotIdFromIntent.isNullOrEmpty() == false) {
      val detailSpot = Intent(this, DetailActivity::class.java)
      detailSpot.putExtra(Constants.HOTSPOT_ID, hotspotIdFromIntent)
      startActivity(detailSpot)
    }
  }

  @SuppressLint("NewApi")
  private fun enableNightmode() {
    val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
    when (mode) {
      Configuration.UI_MODE_NIGHT_YES -> {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor=ContextCompat.getColor(this, R.color.black)
        binding.llBottomBar.setBackgroundColor(Color.BLACK)
//                binding.tvAddMenu.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_grey))
//                 binding. tvChatMenu.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_grey))
//                binding.tvProfileMenu.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_grey))
      }
      Configuration.UI_MODE_NIGHT_NO -> {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        // window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
        binding. llBottomBar.setBackgroundColor(Color.WHITE)
      }
      Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun getCurrentlocation() {
    gps = GPSTracker(this)
    if (gps!!.canGetLocation()) {
      latitude = gps!!.getLatitude()
      longitude = gps!!.getLongitude()

      Log.e("aaaa", "$latitude  $longitude")
      val geocoder: Geocoder
      val returnAddress: Address
      val addresses: List<Address>
      geocoder = Geocoder(this, Locale.getDefault())
      try {
        if (latitude != 0.0 && longitude != 0.0) {
          addresses = geocoder.getFromLocation(
            latitude!!,
            longitude!!,
            2
          )?:ArrayList<Address>() // Here 1 represent max location result to returned, by documents it recommended 1 to 5
          returnAddress = addresses[0]
          Log.e("returnAddress", addresses.toString())

          mlongitude = longitude.toString()
          mlatitude = latitude.toString()
          Prefs.with(this).save(Constants.LAT, mlatitude)
          Prefs.with(this).save(Constants.LNG, mlongitude)
          /*"29.5791961","-90.6913207"*/
          setupPagerAdapter()
        }

      } catch (e: IOException) {
        Log.e("error message", e.message.toString())
        e.printStackTrace()
      }
    } else {
        Log.d(TAG, "getCurrentlocation: /// showw")
        gps!!.showSettingsAlert()
    }
  }

  private fun getVersion(): Int {
    val manager = this.packageManager
    try {
      val info = manager.getPackageInfo(this.packageName, 0)
      Prefs.with(this).save(Constants.APK_VERSION, info.versionCode)
      return info.versionCode
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }

    return 1
  }

  private fun onNotificationReceived() {
    if (intent.getStringExtra(Constants.NOTIFICATION_TYPE) != null) {
      val type = intent.getStringExtra(Constants.NOTIFICATION_TYPE)
      val id = intent.getStringExtra(Constants.ID)

      when (type) {
        Constants.CHAT -> {
//                    changeViews(ContainerPagerAdapter.CHAT,binding. tvChatMenu, R.drawable.ic_chat_active)
          changeViews(ContainerPagerAdapter.CHAT,binding.bottomNavigation.menu.getItem(3).itemId)
          binding.bottomNavigation.menu.findItem(R.id.tvChatMenu).setChecked(true)
          //dentifierAction(R.id.tvChatMenu, 0);


          val intent = Intent(this, ChatActivity::class.java)
          intent.putExtra(Constants.OTHER_USER_ID, id)
          startActivity(intent)
          EventBus.getDefault().postSticky(ChatHistoryRefershApi(true))
        }
        Constants.NOTIFICATION -> {
//                    changeViews(ContainerPagerAdapter.PROFILE,binding. tvProfileMenu, R.drawable.ic_profile_active)
          changeViews(ContainerPagerAdapter.PROFILE,binding.bottomNavigation.menu.getItem(4).itemId)
          binding.bottomNavigation.menu.findItem(R.id.tvProfileMenu).setChecked(true)
          //dentifierAction(R.id.tvProfileMenu, 0);


        }
        else -> {
          Log.d(TAG, "onNotificationReceived: // here = "+intent.getStringExtra(Constants.HOTSPOT_ID))
          val detailSpot = Intent(this, DetailActivity::class.java)
          detailSpot.putExtra(Constants.HOTSPOT_ID, id)
          startActivity(detailSpot)
        }
      }

    }
  }

  private fun setupPagerAdapter() {
    val containerAdapter = ContainerPagerAdapter(supportFragmentManager)
    binding.vpContainer.setSwipeable(false)
    binding. vpContainer.offscreenPageLimit = 4
    binding. vpContainer.adapter = containerAdapter
  }


  private fun clickEvent() {
//       binding. tvExplore.setOnClickListener(this)
//       binding. tvFeed.setOnClickListener(this)
//       binding. tvAddMenu.setOnClickListener(this)
//       binding. tvProfileMenu.setOnClickListener(this)
//       binding. tvChatMenu.setOnClickListener(this)

    binding.bottomNavigation.setOnItemSelectedListener {

      when(it.itemId){
        R.id.tvExploreMenu -> {
          //EventBus.getDefault().postSticky(MapReferesh(true))
//                changeViews(ContainerPagerAdapter.EXPLORE,binding. tvExplore, R.drawable.ic_home_active)
          changeViews(ContainerPagerAdapter.EXPLORE,binding.bottomNavigation.menu.getItem(0).itemId)
          binding.bottomNavigation.menu.findItem(R.id.tvExploreMenu).setChecked(true)
          //dentifierAction(R.id.tvExploreMenu, 0);


        }
        /* R.id.tvFeed -> {
             EventBus.getDefault().postSticky(FeedApi(true))
             changeViews(ContainerPagerAdapter.FEED, tvFeed, R.drawable.tab_feed_red_icon)
         }*/
        R.id.tvAddMenu -> {
          binding.bottomNavigation.menu.findItem(R.id.tvExploreMenu).setChecked(true)

          if (ContextCompat.checkSelfPermission(
              this,
              Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
              this,
              Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
          ) {
            Log.d(TAG, "onCreate: // already granted ")
            // Permission is already granted, proceed with your functionality
            // ...
            binding.bottomNavigation.menu.findItem(R.id.tvExploreMenu).setChecked(true)
            val addScreen = Intent(this, AddSpotActivity::class.java)
            startActivityForResult(addScreen, Constants.REQ_CODE_SPOT_CREATED)
          }
//          if(checkAndRequestPermissions() == true){
//            val addScreen = Intent(this, AddSpotActivity::class.java)
//            startActivityForResult(addScreen, Constants.REQ_CODE_SPOT_CREATED)
//          }
          else{
//            checkAndRequestPermissions()
//            binding.bottomNavigation.menu.findItem(R.id.tvExploreMenu).setChecked(true)
            binding.bottomNavigation.menu.findItem(R.id.tvExploreMenu).setChecked(true)
            explain("Alert",
              "Please switch on location services in settings to get nearby hotspots.",
              "Settings")
          }
          binding.bottomNavigation.menu.findItem(R.id.tvExploreMenu).setChecked(true)
          //  changeViews(ContainerPagerAdapter.ADD, tvAddMenu, R.drawable.ic_add_active)
        }

        R.id.tvChatMenu -> {
//                changeViews(ContainerPagerAdapter.CHAT,binding. tvChatMenu, R.drawable.ic_chat_active)
          changeViews(ContainerPagerAdapter.CHAT,binding.bottomNavigation.menu.getItem(3).itemId)
          binding.bottomNavigation.menu.findItem(R.id.tvChatMenu).setChecked(true)
          //dentifierAction(R.id.tvChatMenu, 0);

          Log.e(TAG, "clickEvent: chat :  ${ContainerPagerAdapter.CHAT}", )
          //  EventBus.getDefault().postSticky(RefershChatScreen(true))
        }
        R.id.tvProfileMenu -> {
          EventBus.getDefault().postSticky(NotificationTab(true))
//                changeViews(ContainerPagerAdapter.PROFILE, binding.tvProfileMenu, R.drawable.ic_profile_active)
          changeViews(ContainerPagerAdapter.PROFILE,binding.bottomNavigation.menu.getItem(4).itemId)
          binding.bottomNavigation.menu.findItem(R.id.tvProfileMenu).setChecked(true)
          //dentifierAction(R.id.tvProfileMenu, 0);

        }
      }

      true
    }
  }

  override fun onClick(p0: View?) {
//        when (p0?.id) {
//            R.id.tvExplore -> {
//                //EventBus.getDefault().postSticky(MapReferesh(true))
////                changeViews(ContainerPagerAdapter.EXPLORE,binding. tvExplore, R.drawable.ic_home_active)
//                changeViews(ContainerPagerAdapter.EXPLORE, (R.id.tvExplore))
//            }
//           /* R.id.tvFeed -> {
//                EventBus.getDefault().postSticky(FeedApi(true))
//                changeViews(ContainerPagerAdapter.FEED, tvFeed, R.drawable.tab_feed_red_icon)
//            }*/
//            R.id.tvAddMenu -> {
//                if(checkAndRequestPermissions() == true){
//                    val addScreen = Intent(this, AddSpotActivity::class.java)
//                    startActivityForResult(addScreen, Constants.REQ_CODE_SPOT_CREATED)
//                }
//                else{
//                    checkAndRequestPermissions()
//                }
//
//              //  changeViews(ContainerPagerAdapter.ADD, tvAddMenu, R.drawable.ic_add_active)
//            }
//
//            R.id.tvChatMenu -> {
////                changeViews(ContainerPagerAdapter.CHAT,binding. tvChatMenu, R.drawable.ic_chat_active)
//                changeViews(ContainerPagerAdapter.CHAT,(R.id.tvChatMenu))
//                //  EventBus.getDefault().postSticky(RefershChatScreen(true))
//
//            }
//            R.id.tvProfileMenu -> {
//                EventBus.getDefault().postSticky(NotificationTab(true))
////                changeViews(ContainerPagerAdapter.PROFILE, binding.tvProfileMenu, R.drawable.ic_profile_active)
//                changeViews(ContainerPagerAdapter.PROFILE,(R.id.tvProfileMenu))
//            }
//        }
  }

  /*  override fun onLocationUpdated(location: Location?) {
        Prefs.with(this).save(Constants.LAT, location?.latitude)
        Prefs.with(this).save(Constants.LNG, location?.longitude)
        stopLocationUpdates()
    }*/

  /* override fun onLocationUpdateFailure() {
   }*/


  override fun onChatMessage(message: String) {
    if (message == "0") {
      // binding. tvChatMenuCount.visibility = View.GONE
      val badgeDrawable = binding.bottomNavigation.getBadge(R.id.tvChatMenu)
      if (badgeDrawable != null) {
        badgeDrawable.isVisible = false
        badgeDrawable.clearNumber()
      }
    } else {
//           binding. tvChatMenuCount.visibility = View.VISIBLE
//           binding. tvChatMenuCount.text = message
      EventBus.getDefault().postSticky(ChatApi(true))

      val badge = binding.bottomNavigation.getOrCreateBadge(R.id.tvChatMenu)
      badge.isVisible = true
      try{
        badge.number = message.toInt()
      }catch (e:Exception){
        val badgeDrawable = binding.bottomNavigation.getBadge(R.id.tvChatMenu)
        if (badgeDrawable != null) {
          badgeDrawable.isVisible = false
          badgeDrawable.clearNumber()
        }
        e.printStackTrace()
        FirebaseCrashlytics.getInstance().recordException(e)
      }

      //EventBus.getDefault().postSticky(RefershChatScreen(true))
    }

  }




  @SuppressLint("NewApi")
  private fun changeViews(pos: Int,  menuItemId: Int/* textView: TextView, drawable: Int*/) {
    //deselectFragment()
    Log.e(TAG, "changeViews: position:: ${pos}", )
    binding.vpContainer.currentItem = pos


    /* textView.setCompoundDrawablesWithIntrinsicBounds(0, drawable, 0, 0)
     val mode = this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
     when (mode) {
         Configuration.UI_MODE_NIGHT_YES->{
             textView.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
             textView.setTextColor(ContextCompat.getColor(this, R.color.white))
         }
         Configuration.UI_MODE_NIGHT_NO->{
             textView.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
             textView.setTextColor(ContextCompat.getColor(this, R.color.black))
         }
     }*/
  }


//    private fun deselectFragment() {
//       binding. tvExplore.setTextColor(ContextCompat.getColor(this, R.color.bottomBarGrey))
//      //  tvFeed.setTextColor(ContextCompat.getColor(this, R.color.bottomBarGrey))
//      binding.  tvAddMenu.setTextColor(ContextCompat.getColor(this, R.color.bottomBarGrey))
//       binding. tvProfileMenu.setTextColor(ContextCompat.getColor(this, R.color.bottomBarGrey))
//       binding. tvChatMenu.setTextColor(ContextCompat.getColor(this, R.color.bottomBarGrey))
//
//       binding. tvExplore.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_home, 0, 0)
//      //  tvFeed.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.tab_feed_icon, 0, 0)
//       binding. tvAddMenu.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_add, 0, 0)
//       binding. tvChatMenu.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_chat, 0, 0)
//        binding.tvProfileMenu.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_profile, 0, 0)
//    }


  override fun attachBaseContext(newBase: Context?) {
    //super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!));
  }

  override fun onMessageReceived(message: String) {
    if (message == "0") {
      // binding. tvCount.visibility = View.GONE
      val badgeDrawable = binding.bottomNavigation.getBadge(R.id.tvProfileMenu)
      if (badgeDrawable != null) {
        badgeDrawable.isVisible = false
        badgeDrawable.clearNumber()
      }
    } else {
//           binding. tvCount.visibility = View.VISIBLE
//           binding. tvCount.text = message
      EventBus.getDefault().postSticky(RefershNotificationApi(true))


      val badge = binding.bottomNavigation.getOrCreateBadge(R.id.tvChatMenu)
      badge.isVisible = true
      try{
        badge.number = message.toInt()
      }catch (e:Exception){
        val badgeDrawable = binding.bottomNavigation.getBadge(R.id.tvProfileMenu)
        if (badgeDrawable != null) {
          badgeDrawable.isVisible = false
          badgeDrawable.clearNumber()
        }
        e.printStackTrace()
        FirebaseCrashlytics.getInstance().recordException(e)
      }

    }
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
      when (requestCode) {
        Constants.REQ_CODE_SPOT_CREATED -> {
//                    changeViews(ContainerPagerAdapter.EXPLORE, binding.tvExplore, R.drawable.ic_home_active)
          changeViews(ContainerPagerAdapter.EXPLORE, (R.id.tvExploreMenu))
        }
      }
      Log.e("null","data null")
    }
  }

  @SuppressLint("ResourceAsColor")
  override fun onBackPressed() {

    if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
      super.onBackPressed()
      finish()
    } else {
      // Toast.makeText(baseContext, getString(R.string.label_back_press), Toast.LENGTH_SHORT).show();

      Toasty.custom(baseContext, getString(R.string.label_back_press), R.drawable.logo_circle, R.color.redVeryPopular, Toast.LENGTH_SHORT, true,
        true).show()

    }
    mBackPressed = System.currentTimeMillis()
  }


  override fun onDestroy() {
    super.onDestroy()
    countSocket.closeSocket()
  }

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onResume() {
    Log.d(TAG, "onCreate:  resume ")
    super.onResume()
    binding.bottomNavigation.menu.findItem(R.id.tvAddMenu).setChecked(false)
    binding.bottomNavigation.menu.findItem(R.id.tvExploreMenu).setChecked(true)
    countSocket.connectSocket(userId, userId)
    countSocket.sendTextMsg(userId)
    countSocket.getchatCount(userId, userId)
    if (latitude==0.0 && longitude==0.0){
      getCurrentlocation()
    }

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll()
  }


  @Subscribe(threadMode = ThreadMode.MAIN, sticky =false)
  fun onRefershMap(refreshExploreApi: ChatSocketReferesh) {

//        binding..isFocusable = false
    EventBus.getDefault().removeStickyEvent<ChatSocketReferesh>(ChatSocketReferesh::class.java)
    if (refreshExploreApi.isReferesh) {
      countSocket.connectSocket(userId, userId)
      countSocket.sendTextMsg(userId)
      countSocket.getchatCount(userId, userId)
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

  private val REQUEST_ID_MULTIPLE_PERMISSIONS = 5

  private fun checkAndRequestPermissions(): Boolean {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      askNotificationPermission()
    }

    val accessFineLocation = ContextCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.ACCESS_FINE_LOCATION)
    val accessCourseLocation = ContextCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
    // val accessCallPhone = ContextCompat.checkSelfPermission(this@HomeActivity,Manifest.permission.CALL_PHONE)
    val readContacts = ContextCompat.checkSelfPermission(this@HomeActivity,Manifest.permission.READ_CONTACTS)
    val listPermissionsNeeded = ArrayList<String>()

    if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (accessCourseLocation != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
//        if(accessCallPhone != PackageManager.PERMISSION_GRANTED){
//            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE)
//        }
    if(readContacts != PackageManager.PERMISSION_GRANTED){
      listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS)
    }

    if (!listPermissionsNeeded.isEmpty()) {
      ActivityCompat.requestPermissions(this@HomeActivity, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
      return false
    } else {
      return true
    }

  }

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    when(requestCode){

      REQUEST_ID_MULTIPLE_PERMISSIONS->{
        val perms = HashMap<String,Int>()

        perms[Manifest.permission.ACCESS_FINE_LOCATION] = PackageManager.PERMISSION_GRANTED
        perms[Manifest.permission.ACCESS_COARSE_LOCATION] = PackageManager.PERMISSION_GRANTED
        //     perms[Manifest.permission.CALL_PHONE] = PackageManager.PERMISSION_GRANTED
        perms[Manifest.permission.READ_CONTACTS] = PackageManager.PERMISSION_GRANTED

        if(grantResults.size > 0){
          for(i in permissions.indices) perms[permissions[i]] = grantResults[i]

          //check for all permissions
          if(perms[Manifest.permission.ACCESS_COARSE_LOCATION] == PackageManager.PERMISSION_GRANTED &&
            perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED
          ){
            if(checkAndRequestPermissions() == true){
              getCurrentlocation()
            }
          }
          else {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this@HomeActivity, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this@HomeActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
//         2nd dialog after login
              //              showDialogOK("Service Permissions are required for this app",
//                DialogInterface.OnClickListener { dialog, which ->
//                  when (which) {
//                    DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
//                    DialogInterface.BUTTON_NEGATIVE ->   explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?", "Settings")
//
//                    // proceed with logic by disabling the related features or quit the app.
//                    //  finish()
//
//                  }
//                })
              Log.d(TAG, "onRequestPermissionsResult: // if ")
            } else {
              Log.d(TAG, "onRequestPermissionsResult: // else ")
//              explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
              explain("Alert",
                "Please switch on location services in settings to get nearby hotspots.",
                "Settings")
              //proceed with logic by disabling the related features or quit the app.
            }
          }

        }


      }
    }
  }

  private fun explain(title : String,msg: String, positiveText : String) {
    var positiveT = "Yes"
    if (positiveText!=null){
      positiveT = positiveText
    }else{
      positiveT="Settings"
    }
//    val dialog = androidx.appcompat.app.AlertDialog.Builder(this@HomeActivity)
//    dialog.setMessage(msg)
//      dialog.setTitle("          "+title)
//      .setPositiveButton(positiveT) { paramDialogInterface, paramInt ->
//        //  permissionsclass.requestPermission(type,code);
//        startActivity(
//          Intent(
//            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//            Uri.parse("package:com.codebrew.whrzat")
//          )
//        )
//      }
//      .setNegativeButton("Cancel") { paramDialogInterface, paramInt ->
//        // Toast.makeText(this@HomeActivity, "Required", Toast.LENGTH_LONG).show()
//      }
//    dialog.show()

    //custom
    val builder = AlertDialog.Builder(this)
    val customView : View= getLayoutInflater().inflate(R.layout.custom_layout2, null);
    builder.setView(customView);

    val textViewTitle = customView.findViewById<TextView>(R.id.heading2)
    val textViewMessage = customView.findViewById<TextView>(R.id.description2)
    textViewTitle.setText(title)
    textViewMessage.setText(msg)
    val okButton = customView.findViewById<TextView>(R.id.noBT)
    val noButton = customView.findViewById<TextView>(R.id.okBT)
    okButton.setText(positiveT)
    noButton.setText("Cancel")

    builder.setView(customView)
    // Create the AlertDialog
    val alertDialog: android.app.AlertDialog = builder.create()
    // Set other dialog properties
    alertDialog.setCancelable(false)
    okButton.setOnClickListener {
      startActivity(
        Intent(
          Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
          Uri.parse("package:com.codebrew.whrzat")
        )
      )
    }
    noButton.setOnClickListener {
      alertDialog.dismiss()
    }
    alertDialog.show()
  }

  private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
    AlertDialog.Builder(this@HomeActivity)
      .setMessage(message)
      .setPositiveButton("OK", okListener)
      .setNegativeButton("Cancel", okListener)
      .create()
      .show()
  }



  fun askNotificationPermission() {
    try {
      if (Build.VERSION.SDK_INT >= 33) {
        if (ContextCompat.checkSelfPermission(
            this@HomeActivity, Manifest.permission.POST_NOTIFICATIONS
          ) == PackageManager.PERMISSION_GRANTED
        ) {
          Log.e(
            TAG,
            "onCreate: PERMISSION GRANTED"
          )
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
          val builder = androidx.appcompat.app.AlertDialog.Builder(this@HomeActivity)
          builder.setTitle(getString(R.string.notification_permission_title))
          builder.setMessage(getString(R.string.notification_permission_description))
          builder.setPositiveButton(
            getString(R.string.str_allow)
          ) { dialog, which ->
            dialog.dismiss()
            val intent =
              Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.fromParts("package", "com.codebrew.whrzat", null)
            intent.data = uri
            startActivity(intent)
          }
          builder.setCancelable(false)
          val dialog = builder.create()
          dialog.show()
        } else {
          requestPermissionLauncher.launch(
            Manifest.permission.POST_NOTIFICATIONS
          )
        }
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
      try {
        FirebaseCrashlytics.getInstance().recordException(e)
      } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
      }
    }
  }


}
