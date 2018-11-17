package com.china.reader.imagereader;

import android.app.Application;
import android.content.Context;

import com.avos.avoscloud.AVOSCloud;
import com.china.reader.imagereader.common.GreenDaoManager;

public class ConfigApplication extends Application {

    public final static String YOUMI_APPID = "d036c71a709c815f";
    public final static String YOUMI_APPSECRET = "1f7a299f2dbe83f0";

    public final static String GOOGLE_ADS_ID = "ca-app-pub-5774850723915613~4851963479";
    public final static String GOOGLE_BANNER_ID = "ca-app-pub-5774850723915613/2813366767";

    private static Context mContext;

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //AVOS
        AVOSCloud.initialize(this, "DtbSJBl8laAg9yQGexYyuiS2-gzGzoHsz", "X9MgaVvqb6XbIJrsnubba7fk");
        AVOSCloud.setDebugLogEnabled(true);
        GreenDaoManager.getInstance();

    }

    public static String username;

    public static String getLoginAccount() {
        return username;
    }
}
