package com.codebrew.whrzat.ui.signup

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.codebrew.tagstrade.adapter.CountryCodeAdapter
import com.codebrew.tagstrade.fragment.CountryCodeFragment
import com.codebrew.tagstrade.util.CountryDb
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentSignupBinding
import com.codebrew.whrzat.ui.login.LoginFragment
import com.codebrew.whrzat.ui.otp.ActivityOtpverify
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import com.codebrew.whrzat.webservice.pojo.signup.SignupSetData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.rilixtech.CountryCodePicker
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class SignupFragment : Fragment(), View.OnClickListener,
        SignupContract.View, CountryCodeAdapter.SetCountryCode, GetSampledImage.OnImageSampledListener {


    private val TAG = "SignupFragment"
    private lateinit var mContext: Context
    private lateinit var presenter: SignupContract.Presenter
    private lateinit var progressDialog: ProgressDialog
   // private lateinit var pickerForFragment: ImageGetterActivity.ImagePickerForFragment
    var userImage: File? = null
    private var fbId: String = ""
    private var fromFacebook = false
    private var profilePic = ""
    private var countryzCode = ""
    private var countryName: String = "United States"
    private var countrydb: CountryDb = CountryDb()
//    val PICK_IMAGE_CAMERA = 1
//    val PICK_IMAGE_GALLERY = 2
    var auth: FirebaseAuth?=null
    var phoneNumber:String?=null
    private lateinit var binding: FragmentSignupBinding
    private var uriUserImage: Uri? = null

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        uriUserImage = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriUserImage)
        cameraActivityResultLauncher.launch(cameraIntent)
    }



    val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d(TAG, "${it.key} = ${it.value}")
            if(it.value == false){

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.CAMERA
                    )
                    || (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)&& (Build.VERSION.SDK_INT<= Build.VERSION_CODES.S )
                            )

                ) {
                    showDialogOK("Service Permissions are required for this app",
                        DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                DialogInterface.BUTTON_NEGATIVE ->
                                    // proceed with logic by disabling the related features or quit the app.
                                    //  finish()

                                    Log.e(TAG,"required")
                                // Toast.makeText(requireActivity(), "Required", Toast.LENGTH_LONG).show()
                            }
                        })
                } else {
                    explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                    //                            //proceed with logic by disabling the related features or quit the app.
                }


            }else{
                showPictureDialog()
            }
        }
    }


    var cameraActivityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.getResultCode() == Activity.RESULT_OK) {
            try {
                Log.e(TAG, "result is ok cameraActivityResultLauncher: ${uriUserImage?.path} ")
                if (uriUserImage != null) {
                    binding.ivCamera.visibility = View.GONE
                    userImage = getRealPathFromURI(uriUserImage)?.let { File(it) }
                    binding.ivSpot.setImageURI(uriUserImage)
                    Log.e(TAG, "File path from camera " + userImage.toString())

                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    var galleryActivityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result->
        if (result.getResultCode() == Activity.RESULT_OK) {
            val image_uri: Uri? = result?.getData()?.getData()
            uriUserImage = image_uri
            binding.ivCamera.visibility = View.GONE
            userImage = getRealPathFromURI(uriUserImage)?.let { File(it) }
            binding.ivSpot.setImageURI(uriUserImage)
            Log.e(TAG,"File path from gallery "+userImage.toString())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
       // pickerForFragment = context as ImageGetterActivity.ImagePickerForFragment

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignupBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: StartActivity")

        progressDialog = ProgressDialog(mContext)

        presenter = SignupPresenter()

        presenter.attachView(this)
        checkAndRequestPermissions()
        clickListeners()
        initCountryPicker()
        


        


        getFbData() //get Data fr
        // om fb login if it is signup

        //setCountryNameCode()
        auth= FirebaseAuth.getInstance()
        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
        setFontType()
    }

    private fun setFontType() {
        val face_bold = Typeface.createFromAsset(requireActivity().assets, "fonts/opensans_bold.ttf")
        val face_regular = Typeface.createFromAsset(requireActivity().assets, "fonts/opensans_regular.ttf")
        binding.txtsignupTitle.typeface = face_bold
        binding.txtsignupmessage.typeface = face_regular
        binding.tvTotalLoves.typeface = face_bold
        binding.tvlogin.typeface = face_bold
    }

    var mCode = "";
    private fun initCountryPicker() {
        val manager = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        mCode = manager.simCountryIso.uppercase(Locale.ROOT)
        if (mCode.equals("", ignoreCase = true)) {
            binding.ccpNumber.setDefaultCountryUsingNameCode("US")
            binding.ccpNumber.resetToDefaultCountry()
        } else {
            binding.ccpNumber.setDefaultCountryUsingNameCode(mCode)
            binding.ccpNumber.resetToDefaultCountry()
        }
        mCode = binding.ccpNumber.getSelectedCountryCodeWithPlus()!!
        binding.tvCountryCode.text=mCode
        Prefs.with(mContext).save(Constants.CODE, mCode)
        binding.ccpNumber.setOnCountryChangeListener(CountryCodePicker.OnCountryChangeListener {
            mCode = binding.ccpNumber.selectedCountryCodeWithPlus!!
        })
    }

    private fun enableNightmode() {
        val mode = this.resources.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.txtsignupTitle.setTextColor(Color.WHITE)
                binding.txtsignupmessage.setTextColor(Color.WHITE)
                binding.tvlogin.setTextColor(Color.WHITE)
                binding.tvCountryCode.setTextColor(Color.WHITE)
                binding.llsignupMain.setBackgroundColor(Color.parseColor("#000000"))
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
                binding.ivCamera.setColorFilter(ContextCompat.getColor(mContext, R.color.light_grey), android.graphics.PorterDuff.Mode.MULTIPLY)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.txtsignupTitle.setTextColor(Color.parseColor("#000000"))
                binding.txtsignupmessage.setTextColor(Color.parseColor("#000000"))
                binding.tvlogin.setTextColor(Color.parseColor("#000000"))
                binding.tvCountryCode.setTextColor(Color.parseColor("#000000"))
                binding.llsignupMain.setBackgroundColor(Color.WHITE)
                binding.tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }
    private fun setCountryNameCode() {
        countryName = Prefs.with(mContext).getString(Constants.COUNTRY_NAME, "")

        countrydb.countryList.indices.forEach { i ->
            if (countrydb.countryList[i].name == countryName) {
                countryzCode = countrydb.countryList[i].dialingCode as String
                Prefs.with(mContext).save(Constants.CODE, countryzCode)

            }
        }
        val code = "$countryName ($countryzCode)"

        // countryzCode = Prefs.with(mContext).getString(Constants.COUNTRY_CODE, "")
        if (Prefs.with(mContext).getString(Constants.CODE, "").isEmpty()) {
            binding.tvCountryCode.text = countryzCode
            Prefs.with(mContext).save(Constants.CODE, code)
        } else {
            binding.tvCountryCode.text = Prefs.with(mContext).getString(Constants.CODE, "")

        }

    }

  //  @SuppressLint("SetTextI18n")
    private fun getFbData() {
        if (this.arguments != null) {

            val bundle = this.arguments

            fromFacebook = true
//            tilPassword.visibility = View.GONE
            // etEmail.setText("ankit")
            fbId = bundle?.getString(Constants.FB_ID)!!
            binding.etFName.setText(bundle.getString(Constants.FIRST_NAME))

            if (bundle.getString(Constants.LAST_NAME) != null) {
//                etLName.setText(bundle.getString(Constants.LAST_NAME))
                val name = "${bundle.getString(Constants.FIRST_NAME)} ${bundle.getString(Constants.LAST_NAME)}"
                binding.etFName.setText(name)
            }

//            if (bundle.getString(Constants.EMAIL) != null) {
//                etEmail.setText(bundle.getString(Constants.EMAIL))
//                etEmail.isFocusable=false
//            }

            profilePic = bundle.getString(Constants.PIC)!!

            val req = RequestOptions()
            req.transform(CircleCrop())

            binding.ivCamera.visibility = View.GONE
            Glide.with(mContext)
                    .load(bundle.getString(Constants.PIC))
                    .apply(req)
                    .into(binding.ivSpot)
        }
    }

    private fun clickListeners() {
        Log.d(TAG, "clickListeners: ")
        binding.tvBack.setOnClickListener(this)
        binding.tvTotalLoves.setOnClickListener(this)
        binding.ivSpot.setOnClickListener(this)
        binding.tvCountryCode.setOnClickListener(this)
        binding.tvlogin.setOnClickListener(this)
//        pickerForFragment.setImagePickedListener(this)
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvBack -> {
               activity?.onBackPressed()

            }
            R.id.tvTotalLoves -> {
               val signupData = getData()
               if (fromFacebook) {
                    Log.d(TAG, "onClick: from facebook login ")
                  //  presenter.performFbSignup(signupData)
                } else {
                    phoneNumber=binding.tvCountryCode.text.toString().trim()+binding.etPhone.text.toString().trim()
                    presenter.performSignup(signupData)
                }

            }
            R.id.ivSpot -> {
               //pickerForFragment.openImagePickerFromFragment()
              //  checkAndRequestPermissions()
                if(checkAndRequestPermissions()){
                    Log.e(TAG, "onClick: all permission checked " )
                    showPictureDialog()
                }
                return
            }

            R.id.tvCountryCode -> {
                Log.d(TAG, "onClick:  R.id.tvCountryCode -> ")
                val countryCode = CountryCodeFragment()
                countryCode.setCountryCodeListener(this)
                if (activity?.supportFragmentManager?.findFragmentByTag("countryCode_fragment") == null) {
                    fragmentManager?.inTransaction {
                        add(android.R.id.content, countryCode, "countryCode_fragment")
                    }
                    GeneralMethods.hideSoftKeyboard(activity as FragmentActivity, binding.etPhone)
                }

            }
            R.id.tvlogin->{
               if (activity?.supportFragmentManager?.findFragmentByTag("login_fragment") == null) {
                    activity?.supportFragmentManager?.inTransaction {
                        add(android.R.id.content, LoginFragment(), "login_fragment")
                    }
                }
            }
        }

    }


    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(activity)
        pictureDialog.setTitle("Select Option")
        val pictureDialogItems = arrayOf("Gallery", "Camera")
        pictureDialog.setItems(
                pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> openCamera()
            }
        }
        pictureDialog.show()

    }


    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(galleryIntent)

//        val galleryIntent = Intent(
//                Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        )
//        startActivityForResult(galleryIntent,PICK_IMAGE_GALLERY)
    }

  /*  private fun takePhotoFromCamera() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultLauncher.launch(intent)
       // startActivityForResult(intent, PICK_IMAGE_CAMERA)
    }*/






/*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE_CAMERA) {
          //  val thumbnail = data!!.extras!!.get("data") as Bitmap
            val uri = data!!.getParcelableExtra<Uri>("path")
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            //      user_image.setImageBitmap(thumbnail)

           // uriUserImage = saveImageNew(thumbnail)
            uriUserImage = getImageUri(requireContext(),bitmap)
           */
/* val req = RequestOptions()
            req.transform(CircleCrop())*//*

            binding.ivCamera.visibility = View.GONE

            userImage = getRealPathFromURI(uriUserImage)?.let { File(it) }

            binding.ivSpot.setImageBitmap(bitmap)
            */
/*Glide.with(mContext)
                    .load(uriUserImage)
                    .apply(req)
                    .into(binding.ivSpot)*//*

        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE_GALLERY) {

            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, contentURI)
                  //  uriUserImage = saveImageNew(bitmap)
                    uriUserImage = getImageUri(requireContext(),bitmap)
                  */
/*  val req = RequestOptions()
                    req.transform(CircleCrop())*//*

                    binding.ivCamera.visibility = View.GONE
                    userImage = getRealPathFromURI(uriUserImage)?.let { File(it) }
                    binding.ivSpot.setImageBitmap(bitmap)
                    Log.e("filepath",""+userImage.toString())
                    */
/*Glide.with(mContext)
                            .load(bitmap)
                            .apply(req)
                            .into(binding.ivSpot)*//*

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(activity, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
*/

    fun getRealPathFromURI(uri: Uri?): String? {
        val cursor =
            requireActivity().contentResolver.query(uri!!, null, null, null, null)
        cursor!!.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA)
        // video_path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
        return cursor.getString(idx)
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

    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 3
    private fun checkAndRequestPermissions(): Boolean {
        val camerapermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
        val writepermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
       val listPermissionsNeeded = ArrayList<String>()

        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            if (writepermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
         if (!listPermissionsNeeded.isEmpty()) {
             requestMultiplePermissions.launch(
                 listPermissionsNeeded.toTypedArray()
             )
             // ActivityCompat.requestPermissions(requireActivity(), listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        } else {
             return true
        }

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
                    }
                    else {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                        requireActivity(),
                                        Manifest.permission.CAMERA
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                        requireActivity(),
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

                                            Log.e(TAG,"required")
                                               // Toast.makeText(requireActivity(), "Required", Toast.LENGTH_LONG).show()
                                        }
                                    })
                        } else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                } else {

                }
            }
        }
    }
*/
    private fun explain(msg: String) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
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
                  //  Toast.makeText(requireActivity(), "Required", Toast.LENGTH_LONG).show()
                }
        dialog.show()
    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(requireActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
    }
//    private val IMAGE_DIRECTORY = "/whrzat"
//    fun saveImageNew(myBitmap: Bitmap): Uri {
//        val bytes = ByteArrayOutputStream()
//        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//        val wallpaperDirectory = File(
//            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
//        )
//        // have the object build the directory structure, if needed.
//        Log.d("fee", wallpaperDirectory.toString())
//        if (!wallpaperDirectory.exists()) {
//            wallpaperDirectory.mkdirs()
//        }
//
//        try {
//            Log.d("heel", wallpaperDirectory.toString())
//            val f = File(
//                wallpaperDirectory, ((Calendar.getInstance()
//                    .getTimeInMillis()).toString() + ".jpg")
//            )
//            f.createNewFile()
//            val fo = FileOutputStream(f)
//            fo.write(bytes.toByteArray())
//            MediaScannerConnection.scanFile(
//                requireActivity(),
//                arrayOf(f.getPath()),
//                arrayOf("image/jpeg"), null
//            )
//            fo.close()
//            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath())
//
//            return Uri.parse(f.getAbsolutePath())
//        } catch (e1: IOException) {
//            e1.printStackTrace()
//        }
//
//        return Uri.parse("")
//    }
    override fun onImageSampled(imageFile: File) {
        val req = RequestOptions()
        req.transform(CircleCrop())
        binding.ivCamera.visibility = View.GONE
        userImage = imageFile
        Glide.with(mContext)
                .load(imageFile)
                .apply(req)
                .into(binding.ivSpot)
    }

   /* override fun onImageSelected(imageFile: File) {
        compressImage(imageFile)

    }*/

    override fun code(countryCode: String, country: String) {
        binding.tvCountryCode.text = countryCode
        countryzCode = countryCode
        countryName = country
    }

    private fun compressImage(imageFile: File) {
        val getSampledImage = GetSampledImage()
        getSampledImage.setListener(this)
        getSampledImage.sampleImage(imageFile.absolutePath, Constants.LOCAL_STORAGE_BASE_PATH_FOR_IMAGES, 1500)

    }

    private fun getData(): SignupSetData {
        Log.d(TAG, "getData:image sent ${userImage?.name}")

        val signupData = SignupSetData(userImage,
                binding.etFName.text.toString().trim(),
                "",
                binding.etBio.text.toString().trim(),
                fbId,
                "",
                binding.etPhone.text.toString().trim(),
                fromFacebook,
                "",
                userImage,
                profilePic,
                binding.tvCountryCode.text.toString(),
               binding. etreferralCode.text.toString().trim())


        return signupData
    }

    override fun emptyContact() {
       // tilPhone.error = getString(R.string.error_empty_contact)
       // GeneralMethods.errorRemove(tilPhone, etPhone)
        Toasty.error(mContext,getString(R.string.error_empty_contact), Toast.LENGTH_SHORT, true).show()
    }


    override fun invalidUserName() {

    }

    override fun errorImage() {
        Toasty.error(mContext,getString(R.string.error_upload_image), Toast.LENGTH_SHORT, true).show()
        //  GeneralMethods.showToast(mContext, R.string.error_upload_image)
    }

    override fun invalidFirstName() {
       // tilFName.error = "Full name is empty"
       // GeneralMethods.errorRemove(tilFName, binding.etFName)
        Toasty.error(mContext,"Full name is empty", Toast.LENGTH_SHORT, true).show()
    }

    override fun invalidLastName() {
//        tilLName.error = getString(R.string.error_empty_lname)
//        GeneralMethods.errorRemove(tilLName, etLName)
    }

    override fun invalidContact() {
       /* tilPhone.error = getString(R.string.error_invalid_contact)
        GeneralMethods.errorRemove(tilPhone, etPhone)*/
    }

    override fun invalidPassword() {
//        tilPassword.error = getString(R.string.error_empty_password)
//        GeneralMethods.errorRemove(tilPassword, etPassword)
    }

    override fun errorBio() {
        /*tilBio.error = getString(R.string.error_empty_bio)
        GeneralMethods.errorRemove(tilBio, etBio)*/
        Toasty.error(mContext,getString(R.string.error_empty_bio), Toast.LENGTH_SHORT, true).show()
    }

    override fun errorPasswordLimit() {
//        tilPassword.error = getString(R.string.error_limit_password)
//        GeneralMethods.errorRemove(tilPassword, etPassword)
    }


    override fun invalidEmail() {
//        tilEmail.error = getString(R.string.error_invalid_email)
//        GeneralMethods.errorRemove(tilEmail, etEmail)

    }
    var rewardStatus:Boolean?=null
    override fun signupSuccess(data: LoginData?) {
        Log.e("success",data.toString())
        Prefs.with(mContext).save(Constants.LOGIN_DATA, data)
//        Prefs.with(mContext).save(Constants.LOGIN_STATUS, true)
        Prefs.with(mContext).save(Constants.ACCESS_TOKEN, data?.accessToken)
        Prefs.with(mContext).save(Constants.COUNTRY_CODE,binding.tvCountryCode.text.toString().trim())
        Prefs.with(mContext).save(Constants.USER_ID, data?._id)
        Prefs.with(mContext).save(Constants.ISFEEDONBOOL, data?.feedsOn ?: false)
        Prefs.with(mContext).save(Constants.FACEBOOK_LOGIN, data?.fromFacebook ?: false)
        Prefs.with(mContext).save(Constants.Referralcode, data?.referralCode)
//        val intent = Intent(mContext, HomeActivity::class.java)
        rewardStatus=data?.rewardStatus
       /* val intent = Intent(mContext, ActivityOtpverify::class.java)
        intent.putExtra("rewardStatus",data?.rewardStatus)
        mContext.startActivity(intent)*/
//        activity?.finishAffinity()
        //Firebase phone number verification
        phoneNumberVarification()

    }
    private fun phoneNumberVarification() {

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.e("firebae phone", "onVerificationCompleted:$credential")

                //signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e("firebae phone", "onVerificationFailed", e)
                GeneralMethods.showToast(mContext,e.message.toString())
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
                // Show a message and update the UI
            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("firebae phone", "onCodeSent:$verificationId")

                dismissLoading();
                // Save verification ID and resending token so we can use them later
                var storedVerificationId = verificationId
                var resendToken = token
                Prefs.with(mContext).save("verificationId",storedVerificationId)
                Prefs.with(mContext).save("token",resendToken)

                val intent = Intent(mContext, ActivityOtpverify::class.java)//8283093315
                intent.putExtra("rewardStatus",rewardStatus)
                startActivity(intent)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth!!)
                .setPhoneNumber(phoneNumber!!)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity())                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun fbNewUser() {
    }

    override fun signupError(errorMessage: ResponseBody) {
        GeneralMethods.showErrorMsg(mContext, errorMessage)
    }

    override fun signupFailure() {
        GeneralMethods.showToast(mContext, R.string.error_server_busy)

    }

    override fun showLoading() {
        progressDialog.show()

    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }

    override fun sessionExpired() {
        GeneralMethods.tokenExpired(mContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

}
