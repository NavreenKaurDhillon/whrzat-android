package com.codebrew.whrzat.ui.settings.editProfile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arvind.imageandlocationmanager.activities.ImageGetterActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.codebrew.whrzat.R
import com.codebrew.whrzat.databinding.FragmentEditProfileBinding
import com.codebrew.whrzat.event.EditProfileEvent
import com.codebrew.whrzat.util.*
import com.codebrew.whrzat.webservice.pojo.EditData
import com.codebrew.whrzat.webservice.pojo.login.LoginData
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class EditProfileFragment : Fragment(), EditProfileContractor.View, View.OnClickListener, ImageGetterActivity.OnImageSelectListener, GetSampledImage.OnImageSampledListener {

    private  val TAG = "EditProfileFragment"
    private lateinit var mContext: Context
    private lateinit var presenter: EditProfileContractor.Presenter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var pickerForFragment: ImageGetterActivity.ImagePickerForFragment
    private var profileImage: File? = null
    private lateinit var binding: FragmentEditProfileBinding
    val PICK_IMAGE_CAMERA = 1
    val PICK_IMAGE_GALLERY = 2

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        pickerForFragment = context as ImageGetterActivity.ImagePickerForFragment

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: StartActivity")
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        progressDialog = ProgressDialog(mContext)
        presenter = EditProfilePresenter()
        presenter.attachView(this)

        clickListener()
        getDataFromPrefs()

        // enable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        enableNightmode()
    }

    private fun enableNightmode() {
        val mode = this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tvTitle.setTextColor(Color.WHITE)
                binding.etContact.setTextColor(Color.WHITE)
                binding.llEdit.setBackgroundColor(Color.parseColor("#000000"))
                binding. toolbar.setBackgroundColor(Color.parseColor("#000000"))
                binding. tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_white_24)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding. tvTitle.setTextColor(Color.parseColor("#000000"))
                binding. etContact.setTextColor(Color.parseColor("#979797"))
                binding. llEdit.setBackgroundColor(Color.WHITE)
                binding.toolbar.setBackgroundColor(Color.WHITE)
                binding.  tvBack.setImageResource(R.drawable.ic_baseline_arrow_back_24)
            }
        }
    }

    private fun getDataFromPrefs() {
        val userData = Prefs.with(mContext).getObject(Constants.LOGIN_DATA, LoginData::class.java)

        val req = RequestOptions()
        req.transform(RoundedCorners(resources.getDimensionPixelOffset(R.dimen.dp_16)))
                .placeholder(R.drawable.feed_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)


        Glide.with(requireActivity())
                .load(userData.profilePicURL.original)
                .apply(req)
                .into( binding.ivProfilePhoto)

        binding.etEmail.setText(userData.email)
        binding.etBio.setText(userData.bio)
        binding.etName.setText(userData.name)
        binding. etContact.setText(Prefs.with(activity).getString(Constants.COUNTRY_CODE,"+1")+userData.contact)

    }

    private fun clickListener() {
        binding. tvSave.setOnClickListener(this)
        binding.  tvBack.setOnClickListener(this)
        binding.  ivProfilePhoto.setOnClickListener(this)
        pickerForFragment.setImagePickedListener(this)

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvSave -> {
                val userId = Prefs.with(mContext).getString(Constants.USER_ID, "")
                if (GeneralMethods.isNetworkActive(mContext)) {
                    presenter.apiEdiProfile(EditData(profileImage, userId,                 binding.etName.text.toString(),
                        binding. etContact.text.toString().trim(), "",                binding. etBio.text.toString().trim()))
                } else {
                    GeneralMethods.showToast(mContext, R.string.error_no_connection)
                }

            }
            R.id.tvBack -> {
                activity?.onBackPressed()
            }

            R.id.ivProfilePhoto -> {
              //  pickerForFragment.openImagePickerFromFragment()
                showPictureDialog()
            }
        }
    }

    override fun errorName() {
        binding. etName.requestFocus()
        binding.  etName.error = getString(R.string.error_enter_name)
    }

    override fun errorContact() {
        binding.    etContact.requestFocus()
        binding.  etContact.error = getString(R.string.error_contact_invalid)
    }

    override fun errorBio() {
        binding.  etBio.requestFocus()
        binding.  etBio.error = getString(R.string.error_bio)
    }


    override fun sessionExpire() {
        GeneralMethods.tokenExpired(mContext)
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
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()

    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent,PICK_IMAGE_GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, PICK_IMAGE_CAMERA)
    }

    private var uriUserImage: Uri? = null;
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE_CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap

            //      user_image.setImageBitmap(thumbnail)
            uriUserImage = saveImageNew(thumbnail)
            /* val req = RequestOptions()
             req.transform(CircleCrop())*/

            profileImage = File(uriUserImage.toString())
            binding. ivProfilePhoto.setImageBitmap(thumbnail)
            /*Glide.with(mContext)
                    .load(uriUserImage)
                    .apply(req)
                    .into(ivSpot)*/
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_IMAGE_GALLERY) {

            if (data != null) {
                val contentURI = data!!.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, contentURI)
                    uriUserImage = saveImageNew(bitmap)
                    /*  val req = RequestOptions()
                      req.transform(CircleCrop())*/
                    profileImage = File(uriUserImage.toString())
                    binding. ivProfilePhoto.setImageBitmap(bitmap)
                    Log.e("filepath",""+profileImage.toString())
                    /*Glide.with(mContext)
                            .load(bitmap)
                            .apply(req)
                            .into(ivSpot)*/
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(activity, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }

        }
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
                    requireActivity(),
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

    override fun onImageSelected(imageFile: File) {
        compressImage(imageFile)
    }


    private fun compressImage(imageFile: File) {
        val getSampledImage = GetSampledImage()
        getSampledImage.setListener(this)
        getSampledImage.sampleImage(imageFile.absolutePath, Constants.LOCAL_STORAGE_BASE_PATH_FOR_IMAGES, 1500)

    }

    override fun onImageSampled(imageFile: File) {
        profileImage = imageFile

        val req = RequestOptions()
        req.transform(RoundedCorners(resources.getDimensionPixelOffset(R.dimen.dp_10)))
                .placeholder(R.drawable.feed_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)


        Glide.with(requireActivity())
                .load(profileImage)
                .apply(req)
                .into( binding.ivProfilePhoto)

    }

    override fun apiEditSuccess(data: LoginData) {
        Prefs.with(mContext).save(Constants.LOGIN_DATA, data)
        //  GeneralMethods.showToast(mContext, R.string.label_sucess_edt_profile)
        Toasty.success(mContext, getString(R.string.label_sucess_edt_profile), Toast.LENGTH_SHORT).show()
        EventBus.getDefault().postSticky(EditProfileEvent(true))
        activity?.finish()
    }

    override fun apiFailure() {
        GeneralMethods.showToast(mContext, R.string.error_server_busy)
    }

    override fun apiError(errorBody: ResponseBody) {
        GeneralMethods.showErrorMsg(mContext, errorBody)
    }

    override fun showLoading() {
        progressDialog.show()
    }

    override fun dismissLoading() {
        progressDialog.dismiss()
    }
}