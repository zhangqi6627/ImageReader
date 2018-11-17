package com.china.reader.imagereader.common;

import android.util.Log;

public class LogUtils {
    public final static boolean isDebugging = true;
    public final static String LOG_TAG = "kee111";

    public static void e(String msg) {
        if (isDebugging) {
            Log.e(LOG_TAG, msg);
        }
    }

    public static void e(String msg, Exception e) {
        if (isDebugging) {
            Log.e(LOG_TAG, msg, e);
        }
    }

    public static void d(String msg) {
        if (isDebugging) {
            Log.d(LOG_TAG, msg);
        }
    }

    public static void i(String msg) {
        if (isDebugging) {
            Log.i(LOG_TAG, msg);
        }
    }

    public static void v(String msg) {
        if (isDebugging) {
            Log.v(LOG_TAG, msg);
        }
    }

    public static void w(String msg) {
        if (isDebugging) {
            Log.w(LOG_TAG, msg);
        }
    }
}
