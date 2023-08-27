package com.codebrew.whrzat.ui.addhotspot

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.ImagePickerActivity
import com.codebrew.whrzat.databinding.ActivityAddHotspotBinding
import com.codebrew.whrzat.event.MapReferesh
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.createHotspot.SupplyHotspotData
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import es.dmoral.toasty.Toasty
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import repository.GPSTracker
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class AddSpotActivity : AppCompatActivity(), View.OnClickListener, AddContract.View,
  GetSampledImage.OnImageSampledListener {

  private val TAG = "AddSpotActivity"
  private lateinit var presenter: AddContract.Presenter
  private var spotImage: File? = null
  private lateinit var progressDialog: ProgressDialog
  var gps: GPSTracker? = null
  var latitude: Double? = 0.0
  var longitude: Double? = 0.0
  private lateinit var binding: ActivityAddHotspotBinding

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_add_hotspot)
    Log.d(TAG, "onCreate: StartActivity")

    progressDialog = ProgressDialog(this)
    presenter = AddPresenter()
    presenter.attachView(this)

    enableNightmode()

    binding.etTags.setOnFocusChangeListener { v, hasFocus ->
      Log.d(TAG, "onCreate: // focus et")
      binding.tvCreate.requestFocus()
      binding.nestedSV.smoothScrollTo(0, 400)
    }
    binding.etTags.setOnClickListener {
      Log.d(TAG, "onCreate: //et taf")
    }
    getCurrentlocation()
    clickListeners()
    GeneralMethods.hideSoftKeyboard(this, binding.tvAddress)
    binding.etDescription.imeOptions = EditorInfo.IME_ACTION_DONE
    binding.etDescription.setRawInputType(InputType.TYPE_CLASS_TEXT)
    setFontType()
  }

  private fun setFontType() {
    val face_bold = Typeface.createFromAsset(assets, "fonts/opensans_bold.ttf")
    val face_regular = Typeface.createFromAsset(assets, "fonts/opensans_regular.ttf")
    val face_semi = Typeface.createFromAsset(assets, "fonts/opensans_semibold.ttf")
    binding.tvaddTitle.setTypeface(face_semi)
    binding.tvAddress.setTypeface(face_regular)
    binding.tvCreate.setTypeface(face_semi)
  }

  @SuppressLint("NewApi")
  private fun enableNightmode() {
    val mode = this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
    when (mode) {
      Configuration.UI_MODE_NIGHT_YES -> {
        binding.tvaddTitle.setTextColor(Color.WHITE)
        binding.tvAddress.setTextColor(Color.WHITE)
        binding.llAddhotsot.setBackgroundColor(Color.parseColor("#000000"))
        binding.tvCancel.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
        binding.toolbarSpot.setBackgroundColor(Color.parseColor("#000000"))
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
      }

      Configuration.UI_MODE_NIGHT_NO -> {
        binding.tvaddTitle.setTextColor(Color.parseColor("#000000"))
        binding.tvAddress.setTextColor(Color.parseColor("#000000"))
        binding.llAddhotsot.setBackgroundColor(Color.WHITE)
        binding.tvCancel.setImageResource(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarSpot.setBackgroundColor(Color.WHITE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        //window.navigationBarColor=ContextCompat.getColor(this, R.color.white)
      }

      Configuration.UI_MODE_NIGHT_UNDEFINED -> {
      }
    }
  }


  private fun clickListeners() {
    binding.tvCreate.setOnClickListener(this)
    binding.tvCancel.setOnClickListener(this)
    binding.ivSpot.setOnClickListener(this)
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.tvCreate -> {
        if (GeneralMethods.isNetworkActive(this)) {
          val spotData = supplyHotspotData()
          presenter.performCreateHotspot(spotData)
        } else {
          GeneralMethods.showToast(this, R.string.error_no_connection)
        }

      }

      R.id.ivSpot -> {
        //openImageSelector()
        onProfileImageClick()
      }

      R.id.tvCancel -> {
        finish()
      }

    }
  }

  private fun supplyHotspotData(): SupplyHotspotData {
    val loginData = Prefs.with(this).getObject(Constants.LOGIN_DATA, LoginData::class.java)
    val spotData = SupplyHotspotData(
      binding.etHotspotName.text.toString().trim(),
      binding.etTags.text.toString().trim(),
      binding.etDescription.text.toString().trim(),
      spotImage,
      loginData._id,
      latitude.toString(),
      longitude.toString(),
      binding.tvAddress.text.toString()
    )
    return spotData
  }


  fun onProfileImageClick() {
    Dexter.withActivity(this)
      .withPermissions(
        Manifest.permission.CAMERA,
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
          Log.e(
            TAG,
            "onProfileImageClick:  Build.VERSION.SDK_INT <= Build.VERSION_CODES.S"
          )
          Manifest.permission.WRITE_EXTERNAL_STORAGE
        } else Manifest.permission.CAMERA
      )
      .withListener(object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
          if (report.areAllPermissionsGranted()) {
            showImagePickerOptions()
          }
          if (report.isAnyPermissionPermanentlyDenied) {
            showSettingsDialog()
          }
        }

        override fun onPermissionRationaleShouldBeShown(
          permissions: List<PermissionRequest>,
          token: PermissionToken
        ) {
          token.continuePermissionRequest()
        }
      }).check()
  }

  private fun showImagePickerOptions() {

    ImagePickerActivity.showImagePickerOptions(
      this,
      object : ImagePickerActivity.PickerOptionListener {
        override fun onTakeCameraSelected() {
          launchCameraIntent()
        }

        override fun onChooseGallerySelected() {
          launchGalleryIntent()
        }
      })
  }

  val REQUEST_IMAGE = 100
  private fun launchCameraIntent() {
    val intent = Intent(this, ImagePickerActivity::class.java)
    intent.putExtra(
      ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
      ImagePickerActivity.REQUEST_IMAGE_CAPTURE
    )
    // setting aspect ratio
    intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
    intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
    intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1)
    // setting maximum bitmap width and height
    intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true)
    intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000)
    intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000)
    startActivityForResult(intent, REQUEST_IMAGE)
  }

  private fun launchGalleryIntent() {
    val intent = Intent(this, ImagePickerActivity::class.java)
    intent.putExtra(
      ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
      ImagePickerActivity.REQUEST_GALLERY_IMAGE
    )
    // setting aspect ratio
    intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
    intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
    intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1)
    startActivityForResult(intent, REQUEST_IMAGE)
  }

  private fun showSettingsDialog() {
    val builder =
      AlertDialog.Builder(this)
    builder.setTitle(getString(R.string.dialog_permission_title))
    builder.setMessage(getString(R.string.dialog_permission_message))
    builder.setPositiveButton(
      getString(R.string.go_to_settings)
    ) { dialog, which ->
      dialog.cancel()
      openSettings()
    }
    builder.setNegativeButton(
      getString(android.R.string.cancel)
    ) { dialog, which -> dialog.cancel() }
    builder.show()
  }

  // navigating user to app settings
  private fun openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri =
      Uri.fromParts("package", this!!.packageName, null)
    intent.data = uri
    startActivityForResult(intent, 101)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_IMAGE) {
      if (resultCode == Activity.RESULT_OK) {
        val uri = data!!.getParcelableExtra<Uri>("path")
        try { // You can update this bitmap to your server
          val bitmap = MediaStore.Images.Media.getBitmap(this!!.contentResolver, uri)
          val tempUri = getImageUri(this, bitmap)
          spotImage = File(getRealPathFromURI(tempUri))

          // loading profile image from local cache
          val req = RequestOptions()
            .placeholder(R.drawable.feed_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

          Glide.with(this)
            .load(spotImage!!.absolutePath)
            .apply(req)
            .into(binding.ivSpot)
        } catch (e: IOException) {
          e.printStackTrace()
        }
      }
    }
  }


  fun getImageUri(inContext: Context?, inImage: Bitmap): Uri {
    val bytes = ByteArrayOutputStream()
    inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
    val pathone = MediaStore.Images.Media.insertImage(
      inContext!!.contentResolver,
      inImage,
      "Title",
      null
    )
    // compressImage(pathone);
    return Uri.parse(pathone)
  }

  fun getRealPathFromURI(uri: Uri?): String? {
    val cursor =
      this!!.contentResolver.query(uri!!, null, null, null, null)
    cursor!!.moveToFirst()
    val idx = cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA)
    // video_path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
    return cursor.getString(idx)
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

  @SuppressLint("CheckResult")
  override fun onImageSampled(imageFile: File) {
    spotImage = imageFile

    val req = RequestOptions()
    req.transform(RoundedCorners(resources.getDimensionPixelOffset(R.dimen.dp_16)))
      .placeholder(R.drawable.feed_placeholder)
      .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    /*  Glide.with(this)
              .load(imageFile.absolutePath)
              .apply(req)
              .into(binding.ivSpot)*/
  }


  override fun emptyHotspotName() {
    binding.etHotspotName.requestFocus()
    binding.etHotspotName.error = getString(R.string.error_empty_spot_name)
  }

  override fun emptyTags() {

  }

  override fun emptyDescription() {
    binding.etDescription.requestFocus()
    binding.etDescription.error = getString(R.string.error_empty_description)
  }

  override fun selectImage() {
    //   GeneralMethods.showToast(this, R.string.error_image)
    Toasty.error(this, getString(R.string.error_image), Toast.LENGTH_SHORT, true).show()

  }

  override fun noAddress() {
    GeneralMethods.showToast(this, R.string.error_no_address)
  }

  override fun successCreateHotspot() {
    EventBus.getDefault().postSticky(MapReferesh(true))
    //GeneralMethods.showToast(this, R.string.success_created)
    Toasty.success(this, getString(R.string.success_created), Toast.LENGTH_SHORT, true).show()
    setResult(Activity.RESULT_OK)
    finish()
  }

  override fun failureCreateHotspot() {
    GeneralMethods.showToast(this, R.string.error_no_connection)
  }

  override fun errorCreateHotspot(errorBody: ResponseBody) {
    GeneralMethods.showErrorMsg(this, errorBody)
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

  override fun attachBaseContext(newBase: Context?) {
    // super.attachBaseContext(newBase);
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!));
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.detachView()
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
          )
            ?: ArrayList<Address>() // Here 1 represent max location result to returned, by documents it recommended 1 to 5
          returnAddress = addresses[0]
          Log.e("returnAddress", addresses.toString())
          var address = returnAddress.maxAddressLineIndex
          val city = returnAddress.locality
          val name = returnAddress.featureName
          val subLocality = returnAddress.subThoroughfare
          val country = returnAddress.countryName
          val region_code = returnAddress.countryCode
          val zipcode = returnAddress.postalCode
          val state = returnAddress.adminArea

          Log.e(
            "current address",
            name + ", " + city + ", " + state + ", " + country + ", " + zipcode
          )

          if (returnAddress.getThoroughfare() != null) {
            binding.tvAddress.setText(returnAddress.getThoroughfare() + ", " + returnAddress.subAdminArea + ", " + returnAddress.postalCode + ", " + returnAddress.adminArea + ", " + returnAddress.getCountryName())
          } else {
            //tvAddAddress.text=returnAddress.getAddressLine(0)
            binding.tvAddress.setText(name + ", " + returnAddress.subAdminArea + ", " + returnAddress.postalCode + ", " + returnAddress.adminArea + ", " + returnAddress.getCountryName())

          }


          val mlongitude: String = longitude.toString()
          val mlatitude: String = latitude.toString()
          Prefs.with(this).save(Constants.LAT, mlatitude)
          Prefs.with(this).save(Constants.LNG, mlongitude)
          /*"29.5791961","-90.6913207"*/
        }

      } catch (e: IOException) {
        Log.e("error message", e.message.toString())
        e.printStackTrace()
      }
    } else {
      gps!!.showSettingsAlert()
    }
  }

}
