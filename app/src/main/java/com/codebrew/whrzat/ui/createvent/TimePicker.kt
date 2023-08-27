package com.codebrew.whrzat.ui.createvent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import android.text.format.DateFormat
import android.util.Log
import java.util.*


class TimePicker : DialogFragment() {

    private  val TAG = "TimePicker"

    private lateinit var listener: TimePickerDialog.OnTimeSetListener


    fun setListener(@NonNull listener: TimePickerDialog.OnTimeSetListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog: StartActivity")
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)


        return TimePickerDialog(activity, listener, hour, minute,
                DateFormat.is24HourFormat(activity))
    }


}
