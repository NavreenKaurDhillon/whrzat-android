package com.codebrew.whrzat.ui.detailhotspot

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codebrew.tagstrade.adapter.ImagesAdapter
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.ActvityHotspotDetailBinding
import com.codebrew.whrzat.event.MapReferesh
import com.codebrew.whrzat.event.RefreshExploreApi
import com.codebrew.whrzat.ui.allevents.AllEventsActivity
import com.codebrew.whrzat.ui.chat.userchat.ChatActivity
import com.codebrew.whrzat.ui.createvent.EventActivity
import com.codebrew.whrzat.ui.otherprofile.ProfileOtherActivity
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.HotspotDetail.DetailData
import com.codebrew.whrzat.webservice.pojo.HotspotDetail.HotSpotDetail
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import es.dmoral.toasty.Toasty
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.BranchShortLinkBuilder
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class DetailActivity : AppCompatActivity(), DetailContract.View, View.OnClickListener,
  ImagesAdapter.Love, GetSampledImage.OnImageSampledListener {
  companion object {
    const val PREFIX = "https://whrzat.page.link"
    var REQ_IMAGE_UPLOADED = 10
  }

  private val TAG = "DetailActivity"
  private lateinit var feedAdapter: ImagesAdapter
  private lateinit var presenter: DetailContract.Presenter
  private lateinit var progressDialog: ProgressDialog
  var hotspotId = ""
  var hotspotName = ""
  var hotspotCreater = ""
  private var createdBy = ""
  private var title = ""
  private var spotImages: File? = null
  private var canCheckIn = false
  private var isCheckIn = false
  private var isImageClicked = false
  private var userId = ""
  private var isAddEvent = false
  private var color = ""
  private var spotName = ""
  private var isFavClick = 0
  private var favoriteCount = 0
  private var isFavCheck = false
  private var favouriteColor = ""
  var hotspotData: HotSpotDetail? = null
  private lateinit var binding: ActvityHotspotDetailBinding
//    val PICK_IMAGE_CAMERA = 1
//    val PICK_IMAGE_GALLERY = 2

  var cameraActivityResultLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.getResultCode() == Activity.RESULT_OK) {
      try {
        Log.e(TAG, "result is ok cameraActivityResultLauncher: ${uriUserImage?.path} ")
        if (uriUserImage != null) {
          /* val req = RequestOptions()
           req.transform(CircleCrop())*/
          isImageClicked = true
          spotImages = getRealPathFromURI(uriUserImage)?.let { File(it) }

          Log.e(TAG, "onActivityResult: URI USER IMAGE :: ${uriUserImage.toString()}")

          Log.d("TokenData", Prefs.with(this).getString(Constants.ACCESS_TOKEN, ""))
          presenter.addImages(spotImages as File, userId, hotspotId)

        }
      } catch (ex: Exception) {
        ex.printStackTrace()
      }
    }
  }

  fun getRealPathFromURI(uri: Uri?): String? {
    val cursor = contentResolver.query(uri!!, null, null, null, null)
    cursor!!.moveToFirst()
    val idx = cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA)
    // video_path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
    return cursor.getString(idx)
  }

  var galleryActivityResultLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.getResultCode() == Activity.RESULT_OK) {
      val image_uri: Uri? = result?.getData()?.getData()
      uriUserImage = image_uri


      Log.e(TAG, "File path from gallery " + uriUserImage.toString())


//            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
//            uriUserImage = saveImageNew(bitmap)
      /*  val req = RequestOptions()
        req.transform(CircleCrop())*/
      isImageClicked = true
      spotImages = getRealPathFromURI(uriUserImage)?.let { File(it) }//File(uriUserImage.toString())
      Log.d("TokenData", Prefs.with(this).getString(Constants.ACCESS_TOKEN, ""))
      presenter.addImages(spotImages as File, userId, hotspotId)
      Log.e("filepath", "" + spotImages.toString())

    }
  }


  val requestMultiplePermissions = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    permissions.entries.forEach {
      Log.d(TAG, "${it.key} = ${it.value}")
      if (it.value == false) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
            this@DetailActivity,
            Manifest.permission.CAMERA
          )
          || (ActivityCompat.shouldShowRequestPermissionRationale(
            this@DetailActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
          ) && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S))

        ) {
          showDialogOK("Service Permissions are required for this app",
            DialogInterface.OnClickListener { dialog, which ->
              when (which) {
                DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                DialogInterface.BUTTON_NEGATIVE ->
                  // proceed with logic by disabling the related features or quit the app.
                  //  finish()

                  Log.e(TAG, "required")
                // Toast.makeText(requireActivity(), "Required", Toast.LENGTH_LONG).show()
              }
            })
        } else {
          explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
          //                            //proceed with logic by disabling the related features or quit the app.
        }


      } else {
        showPictureDialog()
      }
    }
  }


  @SuppressLint("NewApi")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.actvity_hotspot_detail)
    // setContentView(R.layout.actvity_hotspot_detail)
    Log.d(TAG, "onCreate: StartActivity")

    // setCamera()
    setAdapter()
    setClickListener()
    progressDialog = ProgressDialog(this)
    presenter = DetailPresenter()
    presenter.attachView(this)

    userId = Prefs.with(this).getString(Constants.USER_ID, "")
    Log.d(TAG, "onCreate: // hotspot id = " + intent.getStringExtra(Constants.HOTSPOT_ID))
    if (intent.getStringExtra(Constants.HOTSPOT_ID) != null) {
      hotspotId = intent.getStringExtra(Constants.HOTSPOT_ID)!!
      Log.e("detail activity get hotspot id = ", hotspotId)
      presenter.getHotspotDetail(hotspotId, userId)
    } else {
      GeneralMethods.showToast(this, "Unable to get hotspot id")
    }
    checkAndRequestPermissions()
    val window = window
    // enable night mode
    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
    enableNightmode()
    setFontType()
  }

  private fun setFontType() {
    val face_bold = Typeface.createFromAsset(assets, "fonts/opensans_bold.ttf")
    val face_regular = Typeface.createFromAsset(assets, "fonts/opensans_regular.ttf")
    val face_semi = Typeface.createFromAsset(assets, "fonts/opensans_semibold.ttf")
    binding.tvSpotTitle.setTypeface(face_semi)
    binding.tvPopularityType.setTypeface(face_regular)
    binding.tvTotalEvents.setTypeface(face_regular)
  }

  @SuppressLint("NewApi")
  private fun enableNightmode() {
    val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
    when (mode) {
      Configuration.UI_MODE_NIGHT_YES -> {
        binding.tvSpotTitle.setTextColor(Color.WHITE)
        binding.tvTotalEvents.setTextColor(Color.WHITE)
        binding.detailLayout.setBackgroundColor(Color.parseColor("#000000"))
        binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        binding.tvCheckIn.compoundDrawableTintList =
          ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
//                binding.tvCheckIn.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.tvCheckIn.setTextColor(ContextCompat.getColor(this, R.color.transparent))
        binding.tvAddPhoto.compoundDrawableTintList =
          ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
//                binding.tvAddPhoto.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.tvAddPhoto.setTextColor(ContextCompat.getColor(this, R.color.transparent))
        binding.tvAddEvent.compoundDrawableTintList =
          ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
//                binding.tvAddEvent.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.tvAddEvent.setTextColor(ContextCompat.getColor(this, R.color.transparent))
        binding.tvFavEvent.compoundDrawableTintList =
          ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
//                binding.tvFavEvent.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.tvFavEvent.setTextColor(ContextCompat.getColor(this, R.color.transparent))
        binding.Ivinfo.setColorFilter(getResources().getColor(R.color.white))
      }

      Configuration.UI_MODE_NIGHT_NO -> {
        binding.tvSpotTitle.setTextColor(Color.parseColor("#000000"))
        binding.tvTotalEvents.setTextColor(Color.parseColor("#000000"))
        binding.detailLayout.setBackgroundColor(Color.WHITE)
        binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        //    window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
      }

      Configuration.UI_MODE_NIGHT_UNDEFINED -> {
      }
    }
  }

  /*   private fun setCamera() {
         setAuthorityForURI(BuildConfig.APPLICATION_ID)
         setImageLocation(ImageGetterActivity.EXTERNAL_STORAGE_APP_DIRECTORY_DEFAULT, getString(R.string.app_name))
         setCropperEnabled(true, ImageGetterActivity.SHAPE_RECT)
         setMinCropSize(700, 700)

     }*/

  private fun setClickListener() {
    binding.tvAddEvent.setOnClickListener(this)
    binding.tvAddPhoto.setOnClickListener(this)
    binding.tvCheckIn.setOnClickListener(this)
    binding.tvBack.setOnClickListener(this)
    binding.tvTotalEvents.setOnClickListener(this)
    binding.tvFavEvent.setOnClickListener(this)
    binding.Ivinfo.setOnClickListener(this)
    binding.ivShare.setOnClickListener(this)
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  override fun onClick(v: View) {
    when (v.id) {
      R.id.tvAddEvent -> {
        if (canCheckIn) {
          startAddEventActivity()
          isAddEvent = true

        } else {
          //  GeneralMethods.showToast(this, R.string.error_checkin_area)
          Toasty.error(this, getString(R.string.error_checkin_area), Toast.LENGTH_SHORT, true)
            .show()
        }
      }

      R.id.tvAddPhoto -> {

        if (canCheckIn) {
          //openImageSelector()
          if (checkAndRequestPermissions()) {
            showPictureDialog()
          }

        } else {
          // GeneralMethods.showToast(this, R.string.error_checkin_area)
          Toasty.error(this, getString(R.string.error_checkin_area), Toast.LENGTH_SHORT, true)
            .show();

        }
      }

      R.id.tvCheckIn -> {
        if (canCheckIn) {
          Log.d(TAG, "onClick: // check in = " + canCheckIn)
          if (GeneralMethods.isNetworkActive(this)) {
            if (binding.tvCheckIn.text == getString(R.string.label_check_in)) {
              Log.d(TAG, "onClick: // checkked  in = " + canCheckIn)
              presenter.checkIn(hotspotId, Prefs.with(this).getString(Constants.USER_ID, ""))
            } else {
              Log.d(TAG, "onClick: // not checkked in = " + canCheckIn)

              presenter.checkOut(hotspotId, Prefs.with(this).getString(Constants.USER_ID, ""))
            }
          } else {
            GeneralMethods.showToast(this, R.string.error_no_connection)
          }
        } else {
          Toasty.error(this, getString(R.string.error_checkin_area), Toast.LENGTH_SHORT, true)
            .show()

        }

      }

      R.id.tvBack -> {
        finish()
      }

      R.id.tvFavEvent -> {
        if (GeneralMethods.isNetworkActive(this)) {
          changeIconCheckFavClick(binding.tvFavEvent)
        } else {
          GeneralMethods.showToast(this, R.string.error_no_connection)
        }
      }

      R.id.tvTotalEvents -> {
        val eventsIntent = Intent(this, AllEventsActivity::class.java)
        eventsIntent.putExtra(Constants.HOTSPOT_ID, hotspotId)
        eventsIntent.putExtra(Constants.CREATED_BY, createdBy)
        eventsIntent.putExtra(Constants.IS_CHECK_IN, isCheckIn)
        eventsIntent.putExtra(Constants.COLOR, color)
        eventsIntent.putExtra(Constants.SPOT_NAME, spotName)
        startActivityForResult(eventsIntent, Constants.REQ_CODE_EVENT_COUNT)
      }

      R.id.Ivinfo -> {
        showBottomSeetPage()
      }

      R.id.ivShare -> {
//        shareHotspotUsingBranch()
        shareHotspot()
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun shareHotspotUsingBranch() {
    Log.e(TAG, "shareHotspotUsingBranch: //")
    // Initialize Branch
    Branch.getAutoInstance(this)
    generateAndShareDynamicLink(hotspotId)
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun generateAndShareDynamicLink(hotspotID: String) {
//    val shortLinkBuilder = BranchShortLinkBuilder(this)
//      .addParameters("hotspotId", hotspotID) // Replace with your actual hotspot ID
//
//    shortLinkBuilder.generateShortUrl { url: String, error: BranchError? ->
//      if (error == null) {
//        val shortLink = url
//        // Use the short link, e.g., share it with users
//        Log.d(TAG, "generateAndShareDynamicLink: /// link = "+shortLink)
//      }
//
//
//      else{
//        Log.d(TAG, "generateAndShareDynamicLink: /// link = "+error.message)
//
//      }
//    }

    // Create a BranchUniversalObject
    val buo = BranchUniversalObject()
      .setContentMetadata(
        ContentMetadata()
          .addCustomMetadata("hotspotId", hotspotId)
      )

    // Create a LinkProperties object with your preferred settings
    val lp = LinkProperties()
      .setFeature("sharing")
      .setChannel("any")

    // Generate the short URL
    buo.generateShortUrl(this, lp, object : Branch.BranchLinkCreateListener {
      override fun onLinkCreate(url: String, error: BranchError?) {
        if (error == null) {
          // The generated short URL is available in the 'url' parameter
          // You can now use this 'url' to share the content with others
          // For example, you can send it via an Intent, share dialog, etc.

          Log.d(TAG, "onLinkCreate: /// " + url)
          shareDynamicLink(url)
        } else {
          // Handle error
          // For example, log the error or show an error message
          Log.e("BranchError", error.message)
        }
      }
    })
  }

  // Function to share the dynamic link
  private fun shareDynamicLink(url: String) {
    if (hotspotData != null) {
      hotspotName = hotspotData?.name.toString()
      hotspotCreater =
        "Tap to check out this hotspot created by " + hotspotData?.createdBy.toString() + " at " + hotspotData?.area.toString()
    } else {
      hotspotName = "New hotspot created"
      hotspotCreater = "Tap to check out this hotspot created near you"
    }
    // Function to share the dynamic link
    // Use an Intent to share the URL
    val input = hotspotCreater
    val parts = input.split(" ")
    val createrFirstName = parts.firstOrNull() ?: ""
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(
      Intent.EXTRA_TEXT,
      "Check out this hotspot (" + createrFirstName + ") shared with you " + url
    )
    startActivity(Intent.createChooser(shareIntent, "Share this hotspot"))
  }

  private fun shareHotspot() {
    Log.d(
      TAG, "shareHotspot: // hotspot id intent = " + intent.getStringExtra(Constants.HOTSPOT_ID) +
        " var = " + hotspotId
    )
    val hotspotId = intent.getStringExtra(Constants.HOTSPOT_ID)

    // Generate the dynamic link
    Log.d(TAG, "shareHotspot: /// hotspot data = " + hotspotData?.name)
    if (hotspotData != null) {
      hotspotName = hotspotData?.name.toString()
      val input = hotspotName
      val parts = input.split(" ")
      val createrFirstName = parts.firstOrNull() ?: ""
      hotspotCreater =
        "Tap to check out this hotspot created by " + createrFirstName + " at " + hotspotData?.area.toString()
    } else {
      hotspotName = "New hotspot created"
      hotspotCreater = "Tap to check out this hotspot created near you"
    }

    //firebase dynamic link
//    val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
//      .setLink(Uri.parse(PREFIX + "/?hotspotId=" + hotspotId))
//      .setDomainUriPrefix(PREFIX)
//      .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
//      .setSocialMetaTagParameters(
//        DynamicLink.SocialMetaTagParameters.Builder()
//          .setTitle(hotspotName)
//          .setDescription(hotspotCreater)
//          .setImageUrl("https://img.freepik.com/premium-vector/fire-logo-flame-vector-icon_393879-437.jpg?w=740".toUri())
//          .build()
//      )
//      .buildDynamicLink()

    //firebase short dynamic link
    FirebaseDynamicLinks.getInstance().createDynamicLink()
      .setLink(Uri.parse(PREFIX + "/?hotspotId=" + hotspotId))
      .setDomainUriPrefix(PREFIX)
      .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
      .setIosParameters(DynamicLink.IosParameters.Builder("com.sam.erminesoft.whrzat").build())
      .setSocialMetaTagParameters(
        DynamicLink.SocialMetaTagParameters.Builder()
          .setTitle("WhrzAt")
//          .setDescription(hotspotCreater)
//          .setImageUrl("https://bitbucket.org/ritesh03/whrzat-android/raw/02e41e6d13136918194884be89a180f547331b81/app/src/main/res/drawable/splash.png".toUri())
//          .setImageUrl("https://www.whrzat.com/images/ic_logo.png".toUri())
//          .setImageUrl("https://img.freepik.com/premium-vector/fire-logo-flame-vector-icon_393879-437.jpg?w=740".toUri())
          .build()
      )
      .buildShortDynamicLink()
      .addOnSuccessListener { result ->

        //Short link created
        val flowchartLink = result.previewLink
        Log.d(TAG, "shareHotspot: /// short link= " + result.shortLink)
        val dynamicLinkUri = result.shortLink.toString()
        // Share the link using any sharing method you prefer
        val shareIntent = Intent(Intent.ACTION_SEND)

        Log.d(TAG, "shareHotspot: /// name = " + hotspotData?.createdBy)
        Log.d(TAG, "shareHotspot: /// name = " + hotspotData?.createdBy)
        shareIntent.type = "text/plain"
        val input = hotspotData?.name.toString()
        val parts = input.split(" ")
        val createrFirstName = parts.firstOrNull() ?: ""
        shareIntent.putExtra(
          Intent.EXTRA_TEXT,
          "Check out this hotspot " + createrFirstName + " shared with you " + dynamicLinkUri
        )
//    shareIntent.putExtra(Intent.EXTRA_SUBJECT, hotspotName);
        startActivity(Intent.createChooser(shareIntent, "Share this hotspot"))
      }
      .addOnFailureListener {
        //Error
        //…
      }


    // Get the generated link as a string
//    val dynamicLinkUri = shortLink.result.shortLink
////    val dynamicLinkUri = dynamicLink.uri.toString()
//    // Share the link using any sharing method you prefer
//    val shareIntent = Intent(Intent.ACTION_SEND)
//    shareIntent.type = "text/plain"
//    shareIntent.putExtra(Intent.EXTRA_TEXT, dynamicLinkUri)
//    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hotspot created by "+hotspotData?.createdBy.toString()+" at "+ hotspotData?.area.toString());
//    startActivity(Intent.createChooser(shareIntent, "Open this hotspot"))
  }

  private fun showBottomSeetPage() {
    var dialog = this?.let { BottomSheetDialog(it, R.style.BottomSheetDialog) }
    val inflater = this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val dialogView = inflater.inflate(R.layout.detail_info_dialog, null)
    dialog?.setContentView(dialogView)
    dialog?.setCancelable(true)
    dialog?.show()
    val face_bold = Typeface.createFromAsset(assets, "fonts/opensans_bold.ttf")
    val face_regular = Typeface.createFromAsset(assets, "fonts/opensans_regular.ttf")
    dialogView.findViewById<TextView>(R.id.tvHotspotName).setTypeface(face_bold)
    dialogView.findViewById<TextView>(R.id.tvHotspotFlame).setTypeface(face_regular)
    dialogView.findViewById<TextView>(R.id.tvAbout).setTypeface(face_bold)
    dialogView.findViewById<TextView>(R.id.tvSpotDescription).setTypeface(face_regular)
    dialogView.findViewById<TextView>(R.id.tvTag).setTypeface(face_bold)
    dialogView.findViewById<TextView>(R.id.tvTags).setTypeface(face_regular)
    dialogView.findViewById<TextView>(R.id.txtAddress).setTypeface(face_bold)
    dialogView.findViewById<TextView>(R.id.tvAddress).setTypeface(face_regular)

    val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
    when (mode) {
      Configuration.UI_MODE_NIGHT_YES -> {
        dialogView.setBackgroundResource(R.drawable.rounded_corners_shape_dark)
        dialogView.findViewById<TextView>(R.id.tvHotspotName).setTextColor(Color.WHITE)
        dialogView.findViewById<TextView>(R.id.fav_count).setTextColor(Color.WHITE)
        dialogView.findViewById<TextView>(R.id.tvAbout).setTextColor(Color.WHITE)
        dialogView.findViewById<TextView>(R.id.tvSpotDescription).setTextColor(Color.WHITE)
        dialogView.findViewById<TextView>(R.id.tvTag).setTextColor(Color.WHITE)
        dialogView.findViewById<TextView>(R.id.tvTags).setTextColor(Color.WHITE)
        dialogView.findViewById<TextView>(R.id.txtAddress).setTextColor(Color.WHITE)
        dialogView.findViewById<TextView>(R.id.tvAddress).setTextColor(Color.WHITE)
      }

      Configuration.UI_MODE_NIGHT_NO -> {
        dialogView.setBackgroundResource(R.drawable.rounded_corners_shape)
        dialogView.findViewById<TextView>(R.id.tvHotspotName).setTextColor(Color.BLACK)
        dialogView.findViewById<TextView>(R.id.fav_count).setTextColor(Color.BLACK)
        dialogView.findViewById<TextView>(R.id.tvAbout).setTextColor(Color.BLACK)
        dialogView.findViewById<TextView>(R.id.tvSpotDescription).setTextColor(Color.BLACK)
        dialogView.findViewById<TextView>(R.id.tvTag).setTextColor(Color.BLACK)
        dialogView.findViewById<TextView>(R.id.tvTags).setTextColor(Color.BLACK)
        dialogView.findViewById<TextView>(R.id.txtAddress).setTextColor(Color.BLACK)
        dialogView.findViewById<TextView>(R.id.tvAddress).setTextColor(Color.BLACK)
      }
    }

    dialogView.findViewById<TextView>(R.id.tvHotspotName).text = hotspotData?.name
    dialogView.findViewById<TextView>(R.id.tvSpotDescription).text = hotspotData?.description
    dialogView.findViewById<TextView>(R.id.tvAddress).text = hotspotData?.area
    val reqs = RequestOptions()
      .placeholder(R.drawable.profile_avatar_placeholder_large)
    if (hotspotData != null) {
      Glide.with(this)
        .asBitmap()
        .apply(reqs)
        .load(hotspotData?.images?.get(0)?.picture?.original)
        .into(dialogView.findViewById(R.id.ivHotspot))
    }
    var tags: String = ""
    for (i in hotspotData?.tags?.indices!!) {
      if (i == 0) {
        tags = hotspotData?.tags?.get(i).toString()
      } else {
        tags = tags + " #" + hotspotData?.tags?.get(i)?.trim()
      }

    }
    dialogView.findViewById<TextView>(R.id.tvTags).text = "#" + tags

    if (hotspotData?.isFavouriteCount == 0) {
      dialogView.findViewById<TextView>(R.id.fav_count).visibility = View.GONE
    } else {
      dialogView.findViewById<TextView>(R.id.fav_count).visibility = View.VISIBLE
      dialogView.findViewById<TextView>(R.id.fav_count).text =
        hotspotData?.isFavouriteCount.toString()
    }

    when {
      hotspotData?.isFavouriteCount!! <= 10 -> {
        var color = Color.rgb(95, 171, 255)
        val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
        val bitmap = (myDrawable as BitmapDrawable).bitmap
        val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 70, 70, true))
        d?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        dialogView.findViewById<TextView>(R.id.fav_count)
          .setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
        // dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
      }

      hotspotData?.isFavouriteCount!! <= 20 -> {
        var color = Color.rgb(253, 216, 53)
        val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
        val bitmap = (myDrawable as BitmapDrawable).bitmap
        val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 70, 70, true))
        d?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        dialogView.findViewById<TextView>(R.id.fav_count)
          .setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
        //dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
      }

      hotspotData?.isFavouriteCount!! <= 30 -> {
        var color = Color.rgb(251, 114, 0)
        val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
        val bitmap = (myDrawable as BitmapDrawable).bitmap
        val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 70, 70, true))
        d?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        dialogView.findViewById<TextView>(R.id.fav_count)
          .setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
        //dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
      }

      else -> {
        var color = Color.rgb(251, 114, 0)
        val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
        myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        dialogView.findViewById<TextView>(R.id.fav_count)
          .setCompoundDrawablesWithIntrinsicBounds(null, myDrawable, null, null);
        //  dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
      }
    }



    when {
      hotspotData?.hotness == Constants.BLUE -> {
        dialog?.findViewById<TextView>(R.id.tvHotspotFlame)?.text = getString(R.string.label_chill)
        dialog?.findViewById<TextView>(R.id.tvHotspotFlame)
          ?.setTextColor(ContextCompat.getColor(this, R.color.blueChill))
//                val dr = resources.getDrawable(R.drawable.hotspot_detail_blue_icon)
//                val bitmap = (dr as BitmapDrawable).bitmap
////                val bitmap = (dr as BitmapDrawable).bitmap
//                val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 38, 38, true))
//                dialog?.findViewById<TextView>(R.id.tvHotspotFlame)?.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)

        //xml code
        setDrawableIntoTV(
          dialog?.findViewById<TextView>(R.id.tvHotspotFlame),
          R.drawable.hotspot_detail_blue_icon
        )
      }

      hotspotData?.hotness == Constants.RED -> {
        dialog?.findViewById<TextView>(R.id.tvHotspotFlame)?.text =
          getString(R.string.label_very_popular)
        dialog?.findViewById<TextView>(R.id.tvHotspotFlame)
          ?.setTextColor(ContextCompat.getColor(this, R.color.redVeryPopular))
//                val dr = resources.getDrawable(R.drawable.hotspot_detail_red_icon)
//                val bitmap = (dr as BitmapDrawable).bitmap
//                val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 38, 38, true))
//                dialog?.findViewById<TextView>(R.id.tvHotspotFlame)?.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)

        //xml code
        setDrawableIntoTV(
          dialog?.findViewById<TextView>(R.id.tvHotspotFlame),
          R.drawable.hotspot_detail_red_icon
        )
      }

      hotspotData?.hotness == Constants.YELLOW -> {
        dialog?.findViewById<TextView>(R.id.tvHotspotFlame)?.text =
          getString(R.string.label_Just_in)
        dialog?.findViewById<TextView>(R.id.tvHotspotFlame)
          ?.setTextColor(ContextCompat.getColor(this, R.color.yellow))
//                val dr = resources.getDrawable(R.drawable.hotspot_detail_yellow_icon)
//                val bitmap = (dr as BitmapDrawable).bitmap
//                val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 38, 38, true))
//                dialog?.findViewById<TextView>(R.id.tvHotspotFlame)?.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)

        setDrawableIntoTV(
          dialog?.findViewById<TextView>(R.id.tvHotspotFlame), R.drawable.hotspot_detail_yellow_icon
        )
      }

      hotspotData?.hotness == Constants.ORANGE -> {
        dialog?.findViewById<TextView>(R.id.tvHotspotFlame)?.text =
          getString(R.string.label_popular)
        dialog?.findViewById<TextView>(R.id.tvHotspotFlame)
          ?.setTextColor(ContextCompat.getColor(this, R.color.orangePopular))
//                val dr = ContextCompat.getDrawable(this@DetailActivity,R.drawable.hotspot_detail_orange_icon)//resources.getDrawable(R.drawable.hotspot_detail_orange_icon)
//                val bitmap = (dr as BitmapDrawable).bitmap
//                val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 38, 38, true))
//                dialog?.findViewById<TextView>(R.id.tvHotspotFlame)?.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)

        setDrawableIntoTV(
          dialog?.findViewById<TextView>(R.id.tvHotspotFlame),
          R.drawable.hotspot_detail_orange_icon
        )
      }
    }


    // var btn_continue = dialogView.findViewById<Button>(R.id.btn_continue)

  }

  private fun setDrawableIntoTV(textView: TextView?, icon: Int) {
    val db: Drawable? = ContextCompat.getDrawable(this@DetailActivity, icon)
    binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
      db,
      null,
      null,
      null
    )
    textView?.setCompoundDrawablesWithIntrinsicBounds(null, db, null, null)
  }

  fun changeIconCheckFavClick(view: TextView) {

    Log.d("Token", Prefs.with(this).getString(Constants.ACCESS_TOKEN, ""))
    presenter?.favoriteHotSpot(
      hotspotId,
      Prefs.with(this).getString(Constants.USER_ID, ""),
      !isFavCheck
    )

  }

  private fun startAddEventActivity() {
    val intent = Intent(this, EventActivity::class.java)
    intent.putExtra(Constants.HOTSPOT_ID, hotspotId)
    intent.putExtra(Constants.CREATED_BY, createdBy)
    intent.putExtra(Constants.COLOR, color)
    intent.putExtra(Constants.IS_CHECK_IN, isCheckIn)
    intent.putExtra(Constants.SPOT_NAME, spotName)
    startActivityForResult(intent, Constants.REQ_CODE_EVENT_CREATED)
  }

  override fun successDeleteApi() {
    //  GeneralMethods.showToast(this, R.string.success_delete_api)
    Toasty.success(this, getString(R.string.success_delete_api), Toast.LENGTH_SHORT, true).show()
    EventBus.getDefault().postSticky(RefreshExploreApi(true))
  }


  override fun clickLove(imageId: String, isLike: Boolean, likeCount: String, position: Int) {
    presenter.love(Prefs.with(this).getString(Constants.USER_ID, ""), imageId, isLike)
  }

  override fun delete(imageId: String) {
    presenter.apiDelete(imageId, hotspotId)
  }

  override fun report(imageId: String) {
    presenter.apiReport(imageId, userId)
  }

  private fun setAdapter() {
    feedAdapter = ImagesAdapter(this)
    binding.rvHotspotDetail.layoutManager = LinearLayoutManager(this)
    binding.rvHotspotDetail.isNestedScrollingEnabled = false
    binding.rvHotspotDetail.adapter = feedAdapter
    binding.rvHotspotDetail.addItemDecoration(
      DividerItemDecoration(
        this,
        LinearLayoutManager.VERTICAL
      )
    )
    feedAdapter.setListener(this)
  }

  override fun successReportApi() {
    Toasty.success(this, getString(R.string.success_reported), Toast.LENGTH_SHORT, true).show()
    //GeneralMethods.showToast(this, R.string.success_reported)
  }

  override fun successFavoriteApi() {

    if (isFavCheck) {
      isFavCheck = false
    } else {
      isFavCheck = true
    }

    if (!isFavCheck) {
      binding.tvFavEvent.text = "Favorite"

      if (favoriteCount != null) {
        favoriteCount = --favoriteCount

        // val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_empty)
        // val bitmap = (myDrawable as BitmapDrawable).bitmap
        // val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 60, 60, true))
        binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(
          0,
          R.drawable.ic_favorite,
          0,
          0
        );
        when {
          favoriteCount <= 10 -> {
            var color = Color.rgb(95, 171, 255)
            val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
            myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            // dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
          }

          favoriteCount <= 20 -> {
            var color = Color.rgb(253, 216, 53)
            val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
            myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            // dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
          }

          favoriteCount <= 30 -> {
            var color = Color.rgb(251, 114, 0)
            val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
            myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            // dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
          }

          else -> {
            var color = Color.rgb(251, 114, 0)
            val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
            myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            //  dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
          }
        }
      }
    } else {
      binding.tvFavEvent.text = "Unfavorite"

      if (favoriteCount != null) {
        favoriteCount = ++favoriteCount
        when {
          favoriteCount <= 10 -> {
            var color = Color.rgb(95, 171, 255)
            val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_fill)
            // val bitmap = (myDrawable as BitmapDrawable).bitmap
            // val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 60, 60, true))
            myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(
              null,
              myDrawable,
              null,
              null
            );
            // dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
          }

          favoriteCount <= 20 -> {
            var color = Color.rgb(253, 216, 53)
            val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_fill)
            //  val bitmap = (myDrawable as BitmapDrawable).bitmap
            //   val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 60, 60, true))
            myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(
              null,
              myDrawable,
              null,
              null
            );
            //dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
          }

          favoriteCount <= 30 -> {
            var color = Color.rgb(251, 114, 0)
            val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_fill)
            // val bitmap = (myDrawable as BitmapDrawable).bitmap
            // val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 60, 60, true))
            myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(
              null,
              myDrawable,
              null,
              null
            );
            //dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
          }

          else -> {
            var color = Color.rgb(251, 114, 0)
            val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_fill)
            myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(
              null,
              myDrawable,
              null,
              null
            );
            //  dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
          }
        }
      }
    }
    /*if (favoriteCount > 0) {
//            tv_fav_count.visibility=View.VISIBLE
        dialogView.findViewById<TextView>(R.id.favCount).visibility = View.VISIBLE
        if (favoriteCount == 1) {
            tv_fav_count.text = favoriteCount.toString() + " user marked this place as favorite"
        } else {
            tv_fav_count.text = favoriteCount.toString() + " users marked this place as favorite"
        }
//            dialogView.findViewById<TextView>(R.id.favCount).text=favoriteCount.toString()
    } else {
        tv_fav_count.visibility = View.GONE
        dialogView.findViewById<TextView>(R.id.favCount).visibility = View.GONE
    }*/
  }


  /*  override fun file(imageFile: File) {
        //get the image File
        isImageClicked = true
        //call the func to compress the imageFile
        compressImage(imageFile)
    }*/

  private fun showPictureDialog() {
    val pictureDialog = AlertDialog.Builder(this)
    pictureDialog.setTitle("Select Option")
    val pictureDialogItems = arrayOf("Gallery", "Camera")
    pictureDialog.setItems(
      pictureDialogItems
    ) { dialog, which ->
      when (which) {
        0 -> choosePhotoFromGallary()
        1 -> takePhotoFromCamera()
      }
    }
    pictureDialog.show()

  }

  fun choosePhotoFromGallary() {
    /* val galleryIntent = Intent(
             Intent.ACTION_PICK,
             MediaStore.Images.Media.EXTERNAL_CONTENT_URI
     )
     startActivityForResult(galleryIntent,PICK_IMAGE_GALLERY)*/

    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    galleryActivityResultLauncher.launch(galleryIntent)
  }

  private fun takePhotoFromCamera() {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.TITLE, "New Picture")
    values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
    uriUserImage = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriUserImage)
    cameraActivityResultLauncher.launch(cameraIntent)

  }

  private var uriUserImage: Uri? = null;

  private val REQUEST_ID_MULTIPLE_PERMISSIONS = 3

  private fun checkAndRequestPermissions(): Boolean {
    val camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    val writepermission =
      ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val listPermissionsNeeded = ArrayList<String>()

    if (camerapermission != PackageManager.PERMISSION_GRANTED) {
      listPermissionsNeeded.add(Manifest.permission.CAMERA)
    }
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
      if (writepermission != PackageManager.PERMISSION_GRANTED) {
        listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      }
    }
    if (!listPermissionsNeeded.isEmpty()) {
      requestMultiplePermissions.launch(
        listPermissionsNeeded.toTypedArray()
      )
      ActivityCompat.requestPermissions(
        this,
        listPermissionsNeeded.toTypedArray(),
        REQUEST_ID_MULTIPLE_PERMISSIONS
      )
      return false
    } else {

    }
    return true
  }


  /*  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = java.util.HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.size > 0) {
                    for (i in permissions.indices) perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (checkAndRequestPermissions()) {
                            showPictureDialog()
                        }
                    } else {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                        this,
                                        Manifest.permission.CAMERA
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                        this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                        ) {
                            showDialogOK("Service Permissions are required for this app",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                            DialogInterface.BUTTON_NEGATIVE ->
                                                // proceed with logic by disabling the related features or quit the app.
                                                //  finish()

                                                Toast.makeText(this, "Required", Toast.LENGTH_LONG).show()
                                        }
                                    })
                        } else {
                         //  explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                } else {
                    if (checkAndRequestPermissions()) {
                    }
                }
            }

        }
    }*/

  private fun explain(msg: String) {
    val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
    dialog.setMessage(msg)
      .setPositiveButton("Yes") { paramDialogInterface, paramInt ->
        //  permissionsclass.requestPermission(type,code);
        startActivity(
          Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:com.codebrew.whrzat")
          )
        )
      }
      .setNegativeButton("Cancel") { paramDialogInterface, paramInt ->
        // Toast.makeText(this, "Required", Toast.LENGTH_LONG).show()
      }
    dialog.show()
  }

  private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
    AlertDialog.Builder(this)
      .setMessage(message)
      .setPositiveButton("OK", okListener)
      .setNegativeButton("Cancel", okListener)
      .create()
      .show()
  }

  private val IMAGE_DIRECTORY = "/whrzat"

  /** Used to save image and return uri
   *
   *@param myBitmap
   */
  fun saveImageNew(myBitmap: Bitmap): Uri {
    val bytes = ByteArrayOutputStream()
    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val wallpaperDirectory = File(
      (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
    )
    // have the object build the directory structure, if needed.
    Log.d("fee", wallpaperDirectory.toString())
    if (!wallpaperDirectory.exists()) {

      wallpaperDirectory.mkdirs()
    }

    try {
      Log.d("heel", wallpaperDirectory.toString())
      val f = File(
        wallpaperDirectory, ((Calendar.getInstance()
          .getTimeInMillis()).toString() + ".jpg")
      )
      f.createNewFile()
      val fo = FileOutputStream(f)
      fo.write(bytes.toByteArray())
      MediaScannerConnection.scanFile(
        this,
        arrayOf(f.getPath()),
        arrayOf("image/jpeg"), null
      )
      fo.close()
      Log.d("TAG", "File Saved::--->" + f.getAbsolutePath())

      return Uri.parse(f.getAbsolutePath())
    } catch (e1: IOException) {
      e1.printStackTrace()
    }

    return Uri.parse("")
  }


  private fun compressImage(imageFile: File) {
    val getSampledImage = GetSampledImage()
    getSampledImage.setListener(this)
    getSampledImage.sampleImage(
      imageFile.absolutePath,
      Constants.LOCAL_STORAGE_BASE_PATH_FOR_IMAGES,
      1500
    )
  }

  override fun onImageSampled(imageFile: File) {
    //Receive the compressed Image here
    spotImages = imageFile

    Log.d("TokenData", Prefs.with(this).getString(Constants.ACCESS_TOKEN, ""))
    presenter.addImages(spotImages as File, userId, hotspotId)

  }

  override fun sessionExpire() {
    GeneralMethods.tokenExpired(this)
  }

  override fun showLoading() {
    progressDialog.show()
  }

  override fun successLoveApi() {
  }

  override fun errorApi(errorBody: ResponseBody) {
    Log.e("error", errorBody.string())
    GeneralMethods.showErrorMsgLong(this, errorBody)
  }

  override fun errorMassage(it: ResponseBody) {
    Log.e("error", it.string())
    Toasty.error(
      this,
      "You have used all your favorites, please unfavorite a place to favorite current hotspot",
      Toast.LENGTH_LONG
    ).show()
  }

  override fun Apimesaage(message: String?) {
    Toasty.error(this, message!!, Toast.LENGTH_LONG).show()

  }

  override fun dismissLoading() {
    progressDialog.dismiss()
  }


  override fun failureApi() {
    GeneralMethods.showToast(this, R.string.error_server_busy)
  }


  override fun successCheckInApi(data: DetailData?) {
    binding.tvCheckIn.text = getString(R.string.label_check_out)
    isCheckIn = true
    Log.d(TAG, "successCheckInApi: ///data = " + data)
    data?.data?.hotness?.let {
      Log.e(TAG, "successCheckInApi: hotness ${it}")
      setHotness(it)
    } ?: presenter.getHotspotDetail(hotspotId, userId)
    //  binding.tvCheckIn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_checkout, 0, 0);
    when {
      isImageClicked -> isImageClicked = false
      isAddEvent -> // startAddEventActivity()
        isAddEvent = false

//      else -> Toasty.success(this, getString(R.string.success_check_in), Toast.LENGTH_SHORT, true)
//        .show()
    }
    ;
    EventBus.getDefault().postSticky(MapReferesh(true))
    // EventBus.getDefault().postSticky(RefreshExploreApi(true))
  }


  override fun successCheckOutApi(data: DetailData?) {
    binding.tvCheckIn.text = getString(R.string.label_check_in)
    isCheckIn = false
    Log.e(TAG, "successCheckOutApi: ${data?.data?.hotness}")
    data?.data?.hotness?.let {
      Log.e(TAG, "successCheckOutApi: hotness ${it}")
      setHotness(it)
    } ?: presenter.getHotspotDetail(hotspotId, userId);
    // binding.tvCheckIn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_checkin, 0, 0);
    //   GeneralMethods.showToast(this, R.string.success_check_out)
    EventBus.getDefault().postSticky(MapReferesh(true))

    //EventBus.getDefault().postSticky(RefreshExploreApi(true))

  }

  override fun successAddEvent() {
    // GeneralMethods.showToast(this, R.string.success_created_event)
    Toasty.success(this, getString(R.string.success_created_event), Toast.LENGTH_SHORT, true).show()
    presenter.getHotspotDetail(hotspotId, userId) //to refersh event count
    EventBus.getDefault().postSticky(RefreshExploreApi(true))

  }

  override fun successAddImagesApi() {
    Toasty.success(this, getString(R.string.success_images), Toast.LENGTH_SHORT, true).show()
    // GeneralMethods.showToast(this, R.string.success_images)
    presenter.getHotspotDetail(hotspotId, userId) //to refersh images
    EventBus.getDefault().postSticky(RefreshExploreApi(true))
    //   setResult(Activity.RESULT_OK)
  }

  override fun onChatClick(id: String, ivProfilePic: ImageView, original: String) {
    val intent = Intent(this, ChatActivity::class.java)
    intent.putExtra(Constants.OTHER_USER_ID, id)
    intent.putExtra(Constants.PIC, original)
    //intent.putExtra(Constants.FIRST_NAME,name)
    val p1 = Pair.create(ivProfilePic as View, "pic")
    // val p2 = Pair.create(tvName as View, "name")
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1)
    startActivity(intent, options.toBundle())
  }

  override fun successHotspotDetailApi(hotspotData: HotSpotDetail) {
    this.hotspotData = hotspotData;
    binding.detailLayout.visibility = View.VISIBLE
    color = hotspotData.hotness
    favouriteColor = hotspotData.isFavouriteColor
    spotName = hotspotData.name
    isFavCheck = hotspotData.isFavourite
    favoriteCount = hotspotData.isFavouriteCount!!
    if (favoriteCount > 0) {
//            tv_fav_count.visibility=View.VISIBLE
      // dialogView.findViewById<TextView>(R.id.favCount).visibility = View.VISIBLE
      if (favoriteCount == 1) {
        //     tv_fav_count.text = favoriteCount.toString() + " user marked this place as favorite"
      } else {
        //        tv_fav_count.text = favoriteCount.toString() + " users marked this place as favorite"
      }
//            dialogView.findViewById<TextView>(R.id.favCount).text=favoriteCount.toString()
    } else {
      //     tv_fav_count.visibility = View.GONE
      //    dialogView.findViewById<TextView>(R.id.favCount).visibility = View.GONE
    }
    binding.tvPopularityType.visibility = View.VISIBLE

    if (!hotspotData.checkedIn.isEmpty()) {
      for (id in hotspotData.checkedIn) {
        if (id == Prefs.with(this).getString(Constants.USER_ID, "")) {
          binding.tvCheckIn.text = getString(R.string.label_check_out)
          isCheckIn = true
          ////   binding.tvCheckIn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_checkout, 0, 0);
        } else {
          binding.tvCheckIn.text = getString(R.string.label_check_in)
          isCheckIn = false
          //   binding.tvCheckIn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_checkin, 0, 0);

        }
      }
    } else {
      binding.tvCheckIn.text = getString(R.string.label_check_in)
      isCheckIn = false
    }
    setHotness(hotspotData.hotness);
    /*  {
          binding.tvFavEvent.text = "Unfavorite"
          when {
              hotspotData.isFavouriteColor == Constants.BLUE -> {
                  var color = Color.rgb(95, 171, 255)
                  val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
                  myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                  binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(null, myDrawable, null, null);
              //    dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
              }

              hotspotData.isFavouriteColor == Constants.YELLOW -> {

                  var color = Color.rgb(253, 216, 53)
                  val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
                  myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                  binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(null, myDrawable, null, null);
               //   dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
              }
              hotspotData.isFavouriteColor == Constants.ORANGE -> {

                  var color = Color.rgb(251, 114, 0)
                  val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
                  myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                  binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(null, myDrawable, null, null);
               //   dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
              }
              hotspotData.isFavouriteColor == Constants.RED -> {
                  var color = Color.rgb(251, 114, 0)
                  val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_yellow)
                  myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                  binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(null, myDrawable, null, null);
             //     dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(myDrawable, null, null, null);
              }
              else -> {
                  binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_empty, 0, 0);
               //   dialogView.findViewById<TextView>(R.id.favCount).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_empty, 0, 0, 0);
              }
          }
      }*/
    if (isFavCheck) {
      binding.tvFavEvent.text = "Unfavorite"
      var color = Color.rgb(95, 171, 255)
      val myDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star_fill)
      //val bitmap = (myDrawable as BitmapDrawable).bitmap
      // val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 60, 60, true))
      myDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
      binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(null, myDrawable, null, null);
    } else {
      binding.tvFavEvent.text = "Favorite"
      binding.tvFavEvent.setCompoundDrawablesWithIntrinsicBounds(
        0,
        R.drawable.ic_favorite,
        0,
        0
      );
    }


    val latlngSpot = Location("hotspot")
    val latlngCurrent = Location("current")
    latlngSpot.latitude = hotspotData.location[1]
    latlngSpot.longitude = hotspotData.location[0]
    hotspotId = hotspotData._id
    createdBy = hotspotData.createdBy

    binding.tvSpotTitle.text = hotspotData.name

    if (hotspotData.tags.isNotEmpty()) {
      //    tvSpotSubtitle.text = hotspotData.tags[0]
    }



    latlngCurrent.latitude = Prefs.with(this).getString(Constants.LAT, "").toDouble()
    latlngCurrent.longitude = Prefs.with(this).getString(Constants.LNG, "").toDouble()

    // fetch distance  radius
    val distance = latlngSpot.distanceTo(latlngCurrent)
    val distanceInMiles = distance / 1609.344

    canCheckIn = distanceInMiles <= 0.5  // distance in miles


    /*    if(!Prefs.with(this).getString(Constants.RADIUS,"").equals("") && Prefs.with(this).getString(Constants.RADIUS,"").toInt()!=0){
            Log.e("distance",""+distance+" "+Prefs.with(this).getString(Constants.RADIUS,"").toInt()*1000)
            canCheckIn = distance <=Prefs.with(this).getString(Constants.RADIUS,"").toInt()*1000  // distance in meeter
        }else{
            Log.e("distance",""+distance)
            canCheckIn = distance <=200000  // distance in meeter
        }*/

    isCheckIn = canCheckIn

//        tvAddress.text = hotspotData.area
    if (hotspotData.count != 0) {
      binding.tvTotalEvents.visibility = View.VISIBLE
      binding.view2.visibility = View.VISIBLE
      binding.tvTotalEvents.setOnClickListener(this)

      if (hotspotData.count!! > 1) {
        binding.tvTotalEvents.text = (hotspotData.count?.toString()).plus(" ")
          .plus(getString(R.string.label_Events_at_hotspot))
      } else {

        binding.tvTotalEvents.text = (hotspotData.count?.toString()).plus(" ")
          .plus(getString(R.string.label_Events_at_hotspot_1))
      }
    } else {
      //     view1.visibility = View.GONE
      binding.view2.visibility = View.GONE
      binding.tvTotalEvents.visibility = View.GONE
      binding.tvTotalEvents.text = (getString(R.string.labl_no_events_spot))
    }

    if (hotspotData.images.size != 0 && hotspotData.images.get(0) != null) {
      feedAdapter.addList(hotspotData.images)
    }

  }

  private fun setHotness(hotness: String) {
    Log.e(TAG, "setHotness: ${hotness}")
    Log.d(TAG, "setHotness: /// check in value = " + binding.tvCheckIn.text.toString())
    if (binding.tvCheckIn.text.toString().equals(getString(R.string.label_check_in)) == false) {

      //not checked in can check in
      when (hotness) {

        Constants.BLUE -> {

          binding.tvPopularityType.setText(getString(R.string.label_chill))
          binding.tvPopularityType.setTextColor(
            ContextCompat.getColor(
              this,
              R.color.blueChill
            )
          )
          val db: Drawable? =
            ContextCompat.getDrawable(this@DetailActivity, R.drawable.hotspot_detail_blue_icon)
          binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
            db,
            null,
            null,
            null
          )
          setBlackDrawable(binding.tvCheckIn)

        }

        Constants.RED -> {
          binding.tvPopularityType.text = getString(R.string.label_very_popular)
          binding.tvPopularityType.setTextColor(
            ContextCompat.getColor(
              this,
              R.color.redVeryPopular
            )
          )
//                    val dr = resources.getDrawable(R.drawable.hotspot_detail_red_icon)
//                    val bitmap = (dr as BitmapDrawable).bitmap
//                    val d: Drawable =
//                        BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 38, 38, true))
//                    val db: Drawable =
//                        BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 90, 90, true))
//
//                    binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
//                        d,
//                        null,
//                        null,
//                        null
//                    )
//                    binding.tvCheckIn.setCompoundDrawablesWithIntrinsicBounds(null, db, null, null)

          //xml code
          val db: Drawable? =
            ContextCompat.getDrawable(this@DetailActivity, R.drawable.hotspot_detail_red_icon)
          binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
            db,
            null,
            null,
            null
          )
          setBlackDrawable(binding.tvCheckIn)

        }

        Constants.YELLOW -> {
          Log.d(TAG, "setHotness: /// her yell")
          binding.tvPopularityType.text = getString(R.string.label_Just_in)
          binding.tvPopularityType.setTextColor(
            ContextCompat.getColor(
              this,
              R.color.yellow
            )
          )
//                    val dr = resources.getDrawable(R.drawable.hotspot_detail_yellow_icon)
//                    val bitmap = (dr as BitmapDrawable).bitmap
//                    val d: Drawable =
//                        BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 38, 38, true))
////                    val db: Drawable =
////                        BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 90, 90, true))
//
//                    binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
//                        d,
//                        null,
//                        null,
//                        null
//                    )
////                    binding.tvCheckIn.setCompoundDrawablesWithIntrinsicBounds(null, db, null, null)
//

          //xml codde
          val db: Drawable? =
            ContextCompat.getDrawable(this@DetailActivity, R.drawable.hotspot_detail_yellow_icon)
          binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
            db,
            null,
            null,
            null
          )
          setDrawableIntoTV(binding.tvCheckIn, R.drawable.hotspot_detail_yellow_icon)
        }

        Constants.ORANGE -> {
          binding.tvPopularityType.text = getString(R.string.label_popular)
          binding.tvPopularityType.setTextColor(
            ContextCompat.getColor(
              this,
              R.color.orangePopular
            )
          )
//                    val dr = ContextCompat.getDrawable(this@DetailActivity,R.drawable.hotspot_detail_orange_icon)//resources.getDrawable(R.drawable.hotspot_detail_orange_icon)
//
//                    val bitmap = (dr as BitmapDrawable).bitmap
//                    val d: Drawable =
//                        BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 38, 38, true))
//                    val db: Drawable =
//                        BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 90, 90, true))
//                    binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
//                        d,
//                        null,
//                        null,
//                        null
//                    )
//                    binding.tvCheckIn.setCompoundDrawablesWithIntrinsicBounds(null, db, null, null)

          //xml code
          val db: Drawable? =
            ContextCompat.getDrawable(this@DetailActivity, R.drawable.hotspot_detail_orange_icon)
          binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
            db,
            null,
            null,
            null
          )
          setBlackDrawable(binding.tvCheckIn)
        }
      }
    } else {
      //already checked in
      when (hotness) {

        Constants.BLUE -> {

          binding.tvPopularityType.setText(getString(R.string.label_chill))
          binding.tvPopularityType.setTextColor(
            ContextCompat.getColor(
              this,
              R.color.blueChill
            )
          )
          val db: Drawable? =
            ContextCompat.getDrawable(this@DetailActivity, R.drawable.hotspot_detail_blue_icon)
          binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
            db,
            null,
            null,
            null
          )
          setBlackDrawable(binding.tvCheckIn)

        }

        Constants.RED -> {
          binding.tvPopularityType.text = getString(R.string.label_very_popular)
          binding.tvPopularityType.setTextColor(
            ContextCompat.getColor(
              this,
              R.color.redVeryPopular
            )
          )
          val db: Drawable? =
            ContextCompat.getDrawable(this@DetailActivity, R.drawable.hotspot_detail_red_icon)
          binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
            db,
            null,
            null,
            null
          )
          setBlackDrawable(binding.tvCheckIn)

        }

        Constants.YELLOW -> {
          Log.d(TAG, "setHotness: /// her yell")
          binding.tvPopularityType.text = getString(R.string.label_Just_in)
          binding.tvPopularityType.setTextColor(
            ContextCompat.getColor(
              this,
              R.color.yellow
            )
          )
          val db: Drawable? =
            ContextCompat.getDrawable(this@DetailActivity, R.drawable.hotspot_detail_yellow_icon)
          binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
            db,
            null,
            null,
            null
          )
          setBlackDrawable(binding.tvCheckIn)
        }

        Constants.ORANGE -> {
          binding.tvPopularityType.text = getString(R.string.label_popular)
          binding.tvPopularityType.setTextColor(
            ContextCompat.getColor(
              this,
              R.color.orangePopular
            )
          )
          val db: Drawable? =
            ContextCompat.getDrawable(this@DetailActivity, R.drawable.hotspot_detail_orange_icon)
          binding.tvPopularityType.setCompoundDrawablesWithIntrinsicBounds(
            db,
            null,
            null,
            null
          )
          setBlackDrawable(binding.tvCheckIn)
        }
      }
    }
  }

  private fun setBlackDrawable(tv: TextView) {
    val db: Drawable? =
      ContextCompat.getDrawable(this@DetailActivity, R.drawable.ic_flame_bottom)
    tv.setCompoundDrawablesWithIntrinsicBounds(
      db,
      null,
      null,
      null
    )
    tv.setCompoundDrawablesWithIntrinsicBounds(null, db, null, null)
  }

  private fun setHotnessDrawableIntoTV(icon: Int, tv1: TextView, tv2: TextView) {
    val db: Drawable? =
      ContextCompat.getDrawable(this@DetailActivity, icon)
    tv1.setCompoundDrawablesWithIntrinsicBounds(
      db,
      null,
      null,
      null
    )
    tv2.setCompoundDrawablesWithIntrinsicBounds(null, db, null, null)
  }

  override fun attachBaseContext(newBase: Context?) {
    // super.attachBaseContext(newBase);
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!));
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      Constants.REQ_CODE_EVENT_CREATED -> {
        if (resultCode == Activity.RESULT_OK) {
          presenter.getHotspotDetail(hotspotId, userId)
        }
      }

      Constants.REQ_CODE_EVENT_COUNT -> {
        if (resultCode == Activity.RESULT_OK) {
          presenter.getHotspotDetail(hotspotId, userId)
        }
      }

    }
//        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE_CAMERA) {
//            val thumbnail = data!!.extras!!.get("data") as Bitmap
//
//            //      user_image.setImageBitmap(thumbnail)
//            uriUserImage = saveImageNew(thumbnail)
//            /* val req = RequestOptions()
//             req.transform(CircleCrop())*/
//            isImageClicked = true
//            spotImages = File(uriUserImage.toString())
//
//            Log.e(TAG, "onActivityResult: URI USER IMAGE :: ${uriUserImage.toString()}", )

    //  Log.d("TokenData", Prefs.with(this).getString(Constants.ACCESS_TOKEN, ""))
    //    presenter.addImages(spotImages as File, userId, hotspotId)
    /*Glide.with(mContext)
            .load(uriUserImage)
            .apply(req)
            .into(ivSpot)*/
    //  }


//         if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE_GALLERY) {
//
//            if (data != null) {
//                val contentURI = data!!.data
//                try {
//                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
//                    uriUserImage = saveImageNew(bitmap)
//                    /*  val req = RequestOptions()
//                      req.transform(CircleCrop())*/
//                    isImageClicked = true
//                    spotImages = File(uriUserImage.toString())
//
//                    Log.d("TokenData", Prefs.with(this).getString(Constants.ACCESS_TOKEN, ""))
//                    presenter.addImages(spotImages as File, userId, hotspotId)
//                    Log.e("filepath",""+spotImages.toString())
//                    /*Glide.with(mContext)
//                            .load(bitmap)
//                            .apply(req)
//                            .into(ivSpot)*/
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//        }
  }

  override fun viewProfile(
    otherUserid: String,
    ivProfilePic: ImageView,
    tvUserName: TextView,
    original: String,
    name: String
  ) {

    val intent = Intent(this, ProfileOtherActivity::class.java)
    intent.putExtra(Constants.OTHER_USER_ID, otherUserid)

    intent.putExtra(ProfileOtherActivity.PIC_URL, original)
    intent.putExtra(ProfileOtherActivity.USERNAME, name)
    val p1 = Pair.create(ivProfilePic as View, "pic")
    //   val p3 = Pair.create(tvUserName as View, "name")

    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1)
    startActivity(intent, options.toBundle())
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.detachView()
  }
}
