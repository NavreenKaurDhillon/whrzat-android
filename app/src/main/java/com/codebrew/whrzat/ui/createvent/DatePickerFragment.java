package com.codebrew.whrzat.ui.createvent;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
{

    private static final String TAG = "DatePickerFragment";
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_MIN_TODAY = 1;
    public static final int TYPE_MAX_TODAY = 2;

    private DatePickerDialog.OnDateSetListener listener;
    private long dateInMillis;
    private int calenderType = TYPE_DEFAULT;

    public DatePickerFragment()
    {
    }

    public void setListener(@NonNull OnDateSetListener listener)
    {
        this.listener = listener;
    }

    public void setDate(long dateInMillis)
    {
        this.dateInMillis = dateInMillis;
    }

    public void setCalenderType(@CalenderType int type)
    {
        this.calenderType = type;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateDialog: StartActivity");
        // Use the current date as the default date in the picker
        final Calendar calendar = Calendar.getInstance();
        long currentMillis = calendar.getTimeInMillis();

        // Set the calender time if the provided millis is non-zero
        if (dateInMillis != 0)
            calendar.setTime(new Date(dateInMillis));

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), listener, year, month, day);

        // Set the calender type for the date picker
        switch (calenderType)
        {
            case TYPE_MIN_TODAY:
                pickerDialog.getDatePicker().setMinDate(currentMillis - 1000);
                break;

            case TYPE_MAX_TODAY:
                pickerDialog.getDatePicker().setMaxDate(currentMillis - 1000);
                break;
        }

        // For device below lollipop, this will disable the white background visible behind the date picker
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            if (pickerDialog.getWindow() != null)
                pickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // In some devices, title is visible for the dialog. This will remove it.
        pickerDialog.setTitle("");

        // return the instance of datePickerDialog
        return pickerDialog;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_DEFAULT, TYPE_MIN_TODAY, TYPE_MAX_TODAY})
    private @interface CalenderType
    {
    }
}