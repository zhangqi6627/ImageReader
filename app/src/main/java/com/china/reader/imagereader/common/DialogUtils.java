package com.china.reader.imagereader.common;

import android.content.Context;

import com.china.reader.imagereader.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DialogUtils {
    //A basic message
    public static void showBasicDialog(Context context) {
        new SweetAlertDialog(context)
                .setTitleText("Here's a message!")
                .show();
    }

    //A title with a text under
    public static void showTextDialog(Context context){
        new SweetAlertDialog(context)
                .setTitleText("Here's a message!")
                .setContentText("It's pretty, isn't it?")
                .show();
    }

    //A warning message
    public static void showWarningDialog(Context context){
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Won't be able to recover this file!")
                .setConfirmText("Yes,delete it!")
                .show();
    }

    //A message with a custom icon
    public static void showCustomDialog(Context context){
        new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Sweet!")
                .setContentText("Here's a custom image.")
                .setCustomImage(R.mipmap.ic_launcher)
                .show();
    }

    //A success message
    public static void showSuccessDialog(Context context, String title,String msg){
        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Good job!")
                .setContentText(msg)
                .show();
    }

    //Bind the listener to confirm button
    public static void showDialog(Context context) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Won't be able to recover this file!")
                .setConfirmText("Yes,delete it!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }
}
