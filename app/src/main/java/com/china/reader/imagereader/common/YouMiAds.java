package com.china.reader.imagereader.common;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.china.reader.imagereader.ConfigApplication;
import com.china.reader.imagereader.activity.ImagesActivity;

import wer.xds.fds.AdManager;
import wer.xds.fds.nm.sp.SplashViewSettings;
import wer.xds.fds.nm.sp.SpotManager;
import wer.xds.fds.nm.sp.SpotRequestListener;

public class YouMiAds extends BaseActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // youmi ads
        AdManager.getInstance(mContext).init(ConfigApplication.YOUMI_APPID, ConfigApplication.YOUMI_APPSECRET, true);
        //OffersManager.getInstance(mContext).showOffersWallDialog(this);
        SpotManager.getInstance(mContext).requestSpot(new SpotRequestListener() {
            @Override
            public void onRequestSuccess() {
                LogUtils.e( "onRequestSuccess");
            }

            @Override
            public void onRequestFailed(int i) {
                LogUtils.e( "onRequestFailed:" + i);
            }
        });
        SplashViewSettings splashViewSettings = new SplashViewSettings();
        splashViewSettings.setAutoJumpToTargetWhenShowFailed(true);
        splashViewSettings.setTargetClass(ImagesActivity.class);
    }
}
