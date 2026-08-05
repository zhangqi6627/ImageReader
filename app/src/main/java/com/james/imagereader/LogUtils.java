package com.james.imagereader;

import android.util.Log;

public final class LogUtils {
    private static final int LOG_DISABLED = 0;
    private static volatile boolean logEnabled = true;

    private LogUtils() {
    }

    public static void setLogEnabled(boolean enabled) {
        logEnabled = enabled;
    }

    public static boolean isLogEnabled() {
        return logEnabled;
    }

    public static int v(String tag, String msg) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (!logEnabled) {
            return LOG_DISABLED;
        }
        return Log.e(tag, msg, tr);
    }
}
