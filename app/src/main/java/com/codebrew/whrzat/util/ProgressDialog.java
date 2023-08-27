package com.codebrew.whrzat.util;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.codebrew.whrzat.R;
import com.wang.avi.AVLoadingIndicatorView;


public class ProgressDialog {
    private Dialog mDialog;


    public ProgressDialog(Context context) {
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.layout_progress_dialog, null, false);

      /*  ProgressBar progressBar = (ProgressBar) dialogView.findViewById(R.id.ivProgressBar);*/
        AVLoadingIndicatorView progressBar= (AVLoadingIndicatorView) dialogView.findViewById(R.id.ivProgressBar);
        /*progressBar
                .setColorFilter(ContextCompat.getColor(context, color),
                        PorterDuff.Mode.MULTIPLY);*/

        mDialog = new Dialog(context, R.style.ProgressDialogTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(dialogView);

        mDialog.setCancelable(false);

    }


    public void show() {
        if (!mDialog.isShowing())
            mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    
}
