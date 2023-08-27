package com.codebrew.whrzat.util


import android.R.id
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.codebrew.tagstrade.webservices.pojo.ErrorBodyPojo
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.SplashActivity
import com.codebrew.whrzat.ui.fblogin.FbLoginActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import es.dmoral.toasty.Toasty
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import android.R.id.message
import android.content.ContentValues.TAG
import android.os.Build


object GeneralMethods {

    val JPEG_FILE_PREFIX = "IMG_"
    val JPEG_FILE_SUFFIX = ".jpg"

    fun replaceFragment(fragmentManager: FragmentManager, containerID: Int, fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(containerID, fragment, tag).addToBackStack(null)
                .commit()
    }

    fun addFragment(fragmentManager: FragmentManager, containerID: Int, fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .add(containerID, fragment, tag).addToBackStack(null)
                .commit()
    }

    fun replaceFragmentWithoutBackStack(fragmentManager: FragmentManager, containerID: Int, fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)

                .replace(containerID, fragment, tag)
                .commit()
    }

    fun replaceFragmentWithoutAnimation(fragmentManager: FragmentManager, containerID: Int, fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction()
                .replace(containerID, fragment, tag).addToBackStack(null)
                .commit()
    }

    fun replaceFragmentWithoutAnimBackStack(fragmentManager: FragmentManager, containerID: Int, fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction()
                .replace(containerID, fragment, tag).addToBackStack(null)
                .commit()
    }

    fun getArrayFromJsonArray(jsonString: String?): List<String> {
        var jsonArray: JSONArray? = null
        val list = ArrayList<String>()
        if (jsonString != null) {
            try {
                jsonArray = JSONArray(jsonString)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            if (jsonArray != null) {
                for (i in 0..jsonArray.length() - 1) {
                    try {
                        list.add(jsonArray.getString(i))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
            return list
        } else
            list.add("")
        return list
    }

    fun showToolbar(context: Context, toolbar: Toolbar, val1: Boolean, val2: Boolean) {

        (context as AppCompatActivity).setSupportActionBar(toolbar)
        context.supportActionBar!!.show()
        context.supportActionBar!!.setDisplayShowTitleEnabled(val1)
        context.supportActionBar!!.setDisplayHomeAsUpEnabled(val2)

    }

    fun timeZone():Int {
        val tz = TimeZone.getDefault()
        val now = Date()
        val offsetFromUtc = tz.getOffset(now.time) / 1000
        return offsetFromUtc
    }

    fun navigationBackPress(toolbar: Toolbar, context: Context) {
        toolbar.setNavigationOnClickListener { (context as AppCompatActivity).onBackPressed() }
    }

    fun stringToRequestBody(string: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), string)
    }

    fun imageToRequestBody(imageUri: String): RequestBody {
        return RequestBody.create(MediaType.parse("image/*"), File(imageUri))
    }

    fun imageToRequestBodyKey(parameterName: String, fileName: String): String {
        return parameterName + "\"; filename=\"" + fileName
    }

        fun isNetworkActive(context: Context?): Boolean {
            if (context != null) {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting
            }
            return false
        }

    fun showToast(context: Context?, message: String) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showToast(context: Context?, @StringRes resId: Int) {
        if (context != null) {
            Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
        }
    }

    fun showKeyboard(view: View, mContext: Context) {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }


    /* public static void dialogWindow(@StringRes int textId, final Context mContext) {
        new AlertDialog.Builder(mContext, R.style.MyDialog)
                .setMessage(textId)
                .setNegativeButton(android.R.string.ok, null)
                .setCancelable(true)
                .show();
    }*/


    /* public static void displayErrorMessage(@NonNull Context context, @NonNull ResponseBody errorBody, int statusCode)
    {
        if (statusCode == StatusCode.UNAUTHORIZED)
        {
            showAccessTokenExpiredDialog(context);
            } else
        {
            OtherUserProfilePojo otherUserProfilePojo = null;
            try
            {
                otherUserProfilePojo = new GsonBuilder()
                .create()
                .fromJson(errorBody.string(), OtherUserProfilePojo.class);
                } catch (IOException e)
            {
                e.printStackTrace();
                }

            if (otherUserProfilePojo != null && !otherUserProfilePojo.message.isEmpty())
            Toast.makeText(context, otherUserProfilePojo.message, Toast.LENGTH_SHORT).show();
            }
        }*/

    fun showErrorMsg(context: Context, responseBody: ResponseBody): Void? {
        try {
            val errorBody = Gson().fromJson(responseBody.string(), ErrorBodyPojo::class.java)
            //  Toast.makeText(context, errorBody.message, Toast.LENGTH_SHORT).show()
            Toasty.error(context, errorBody.message.toString(), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
    fun showErrorMsgLong(context: Context, responseBody: ResponseBody): Void? {
        try {
            /*val errorBody = Gson().fromJson(responseBody.string(), ErrorBodyPojo::class.java)*/
            val jsonObj:JSONObject = JSONObject(responseBody.string())
            //  Toast.makeText(context, errorBody.message, Toast.LENGTH_SHORT).show()

            Toasty.error(context, jsonObj.optString("message"), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun errorRemove(tilEmail: TextInputLayout?, etEmail: TextInputEditText?) {
        etEmail?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilEmail?.isErrorEnabled = false
            }
        })
    }

    fun tokenExpired(mContext: Context) {
        Prefs.with(mContext).save(Constants.LOGIN_STATUS, false)
      Log.d(TAG, "tokenExpired: //s ession expired")
//        AlertDialog.Builder(mContext)
//                .setMessage(R.string.dialog_session_has_been_expired)
//                .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
//                    val intent = Intent(mContext, FbLoginActivity::class.java)
//                    mContext.startActivity(intent)
//                    (mContext as AppCompatActivity).finishAffinity()
//                    mContext.overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
//                }
//                .setCancelable(false)
//                .show()
    }
    fun deleteProfile(mContext: Context) {
        Prefs.with(mContext).save(Constants.LOGIN_STATUS, false)
        AlertDialog.Builder(mContext)
                .setMessage(R.string.dialog_delete_profile)
                .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                    val intent = Intent(mContext, FbLoginActivity::class.java)
                    mContext.startActivity(intent)
                    (mContext as AppCompatActivity).finishAffinity()
                    mContext.overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
                }
                .setCancelable(false)
                .show()
    }

    /* public static void displayImagePicker(final Fragment fragment) {
        final Context context = fragment.getActivity();

        String[] pickerItems = {
                context.getString(R.string.dialog_camera),
                context.getString(R.string.dialog_choose_from_gallery),
                context.getString(R.string.dialog_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.dialog_select_your_choice));
        builder.setItems(pickerItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0:
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        try {
                            Constants.IMAGE_FILE = setUpImageFile(Constants.LOCAL_STORAGE_BASE_PATH_FOR_IMAGES);
                            Uri photoURI = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", Constants.IMAGE_FILE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                            *//*imageIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    FileProvider.getUriForFile(fragment.getContext(), BuildConfig.APPLICATION_ID + ".provider", Constants.IMAGE_FILE));
*//*
                           *//* List<ResolveInfo> resolvedIntentActivities = fragment.getContext().getPackageManager().queryIntentActivities(imageIntent, PackageManager.MATCH_DEFAULT_ONLY);
                            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                                String packageName = resolvedIntentInfo.activityInfo.packageName;

                                fragment.getContext()
                                        .grantUriPermission(packageName,
                                                FileProvider.getUriForFile(fragment.getContext(), BuildConfig.APPLICATION_ID + ".provider", Constants.IMAGE_FILE),
                                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }*//*

                            fragment.startActivityForResult(intent,
                                    Constants.REQ_CODE_CAMERA_PICTURE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case 1:
                        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        fragment.startActivityForResult(intent, Constants.REQ_CODE_GALLERY_PICTURE);
                        break;
                }
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
*/
    @Throws(IOException::class)
    fun setUpImageFile(directory: String): File? {
        var imageFile: File? = null
        if (Environment.MEDIA_MOUNTED == Environment
                .getExternalStorageState()) {
            val storageDir = File(directory)
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.d("Camera", "failed to create directory")
                    return null
                }
            }
            imageFile = File.createTempFile(JPEG_FILE_PREFIX + System.currentTimeMillis() + "_", JPEG_FILE_SUFFIX, storageDir)
        }
        return imageFile
    }

    fun saveImageToExternalStorage(context: Context, finalBitmap: Bitmap): File? {
        val file2: File
        val mediaStorageDir = File(context.externalCacheDir!!.path)
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        file2 = File(mediaStorageDir.path + File.separator
                + "IMG_" + timeStamp + ".jpg")

        try {
            val out: FileOutputStream
            out = FileOutputStream(file2)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            return file2
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file2
    }

    /* public static void showRationaleDialog(Context context, @StringRes int messageResId, final PermissionRequest request) {
         new AlertDialog.Builder(context, R.style.MyDialog)
                 .setPositiveButton(R.string.dialog_allow, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(@NonNull DialogInterface dialog, int which) {
                         request.proceed();

                     }
                 })
                 .setNegativeButton(R.string.dialog_deny, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(@NonNull DialogInterface dialog, int which) {
                         request.cancel();
                     }
                 })
                 .setCancelable(false)
                 .setMessage(messageResId)
                 .show();
     }
 */
    /* public static void permissionEnableDialog(final Context context, @StringRes int messageResId) {


        new AlertDialog.Builder(context)
                .setPositiveButton(R.string.dialog_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        startInstalledAppDetailsActivity(context);

                    }
                })
                .setNegativeButton(R.string.dialog_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        ((AppCompatActivity) context).finish();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }*/

    fun startInstalledAppDetailsActivity(context: Context?) {
        if (context == null) {
            return
        }
        val i = Intent()
        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:" + context.packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        context.startActivity(i)
    }

    fun hideSoftKeyboard(activity: Activity, view: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setToolbarIcons(isHomeIcon: Boolean, isTitle: Boolean, context: Context, toolbar: Toolbar) {
        (context as AppCompatActivity).setSupportActionBar(toolbar)
        context.supportActionBar!!.setDisplayHomeAsUpEnabled(isHomeIcon)
        context.supportActionBar!!.setDisplayShowTitleEnabled(isTitle)
    }

    /*public static void showRationaleDialog(Context context, @StringRes int messageResId) {
        new AlertDialog.Builder(context)
                .setPositiveButton(R.string.dialog_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        // request.proceed();

                    }
                })
                .setNegativeButton(R.string.dialog_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        // request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }*/

    /*
    private static Bitmap getFile(String imgPath) {
        Bitmap bMapRotate=null;
        try {

            if (imgPath != null) {
                ExifInterface exif = new ExifInterface(imgPath);

                int mOrientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 1);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgPath, options);
                options.inSampleSize = calculateInSampleSize(options,400,400);
                options.inJustDecodeBounds = false;

                bMapRotate = BitmapFactory.decodeFile(imgPath, options);
                if (mOrientation == 6) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bMapRotate = Bitmap.createBitmap(bMapRotate, 0, 0,
                            bMapRotate.getWidth(), bMapRotate.getHeight(),
                            matrix, true);
                } else if (mOrientation == 2) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
                    bMapRotate = Bitmap.createBitmap(bMapRotate, 0, 0,
                            bMapRotate.getWidth(), bMapRotate.getHeight(),
                            matrix, true);
                } else if (mOrientation == 3) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    bMapRotate = Bitmap.createBitmap(bMapRotate, 0, 0,
                            bMapRotate.getWidth(), bMapRotate.getHeight(),
                            matrix, true);
                }
            }
        } catch (OutOfMemoryError e) {
            bMapRotate = null;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            bMapRotate = null;
            e.printStackTrace();
        }
        return bMapRotate;
    }
*/

    /*  public static Void showErrorMsg(@NonNull Context context, @NonNull ResponseBody responseBody, int statusCode) {

        if (statusCode == 401 ||statusCode==403) {
            tokenExpired(context);
        } else {
            try {
                ErrorBodyPojo errorBody = new Gson().fromJson(responseBody.string(), ErrorBodyPojo.class);
                Toast.makeText(context, errorBody.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }


    private static void tokenExpired(final Context mContext) {
        Prefs.with(mContext).save(Constants.LOGIN_STATUS, false);
        new AlertDialog.Builder(mContext)
                .setMessage(R.string.dialog_session_has_been_expired)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent = new Intent(mContext, FbLoginActivity.class);
                        mContext.startActivity(intent);
                        ((AppCompatActivity) mContext).finishAffinity();
                        ((AppCompatActivity) mContext).overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);

                    }
                })
                .setCancelable(false)
                .show();
    }*/

    fun showSnackbar(view: View, @StringRes resId: Int) {
        val bar = Snackbar.make(view, resId, Snackbar.LENGTH_LONG)
                .setAction("Dismiss") {
                    // Handle user action
                }

        bar.show()
    }


    fun isValidEmail(target: CharSequence?): Boolean {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target)
                .matches()
    }

    fun getDateFromFormat(date: String, format: String): Date {
        val sdf = SimpleDateFormat(format, Locale.US)
        try {
            return sdf.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return Calendar.getInstance().time
        }

    }

    fun convertDate(dateInMilliseconds: String, dateFormat: String): String {
        return DateFormat.format(dateFormat, java.lang.Long.parseLong(dateInMilliseconds)).toString()
    }

    fun getFormatFromDate(date: Date, format: String): String {
        val sdf = SimpleDateFormat(format, Locale.US)
        try {
            return sdf.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }


    }

    fun getAppVersion(context: Context):String{
        var version=" ";
        try {
            val pInfo: PackageInfo =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
            version = pInfo.versionName

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return version
    }
}
