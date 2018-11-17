package com.china.reader.imagereader.common;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import com.google.android.gms.ads.AdRequest;

public class GoogleAdListener extends AdListener implements UnifiedNativeAd.OnUnifiedNativeAdLoadedListener, RewardedVideoAdListener {

    private Context mContext;

    private GoogleAdListener(Context context) {
        this.mContext = context;
    }

    private static GoogleAdListener googleAdListener;

    public static GoogleAdListener getInstance(Context context) {
        if (googleAdListener == null) {
            googleAdListener = new GoogleAdListener(context);
        }
        return googleAdListener;
    }

    @Override
    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
        LogUtils.e( "GoogleAdListener->onUnifiedNativeAdLoaded()");
    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
        LogUtils.e( "GoogleAdListener->onAdLoaded()");


    }

    @Override
    public void onAdOpened() {
        super.onAdOpened();
        LogUtils.e( "GoogleAdListener->onAdOpened()");
        DialogUtils.showSuccessDialog(mContext, "恭喜你！", "积分+1");
    }

    @Override
    public void onAdClosed() {
        super.onAdClosed();
        LogUtils.e( "GoogleAdListener->onAdClosed()");
    }

    @Override
    public void onAdClicked() {
        super.onAdClicked();
        LogUtils.e( "GoogleAdListener->onAdClicked()");
    }

    @Override
    public void onAdImpression() {
        super.onAdImpression();
        LogUtils.e( "GoogleAdListener->onAdImpression()");
    }

    @Override
    public void onAdLeftApplication() {
        super.onAdLeftApplication();
        LogUtils.e( "GoogleAdListener->onAdLeftApplication()");
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        super.onAdFailedToLoad(errorCode);
        LogUtils.e( "GoogleAdListener->onAdFailedToLoad() errorCode:" + errorCode);
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                break;
        }
        //ERROR_CODE_INTERNAL_ERROR - Something happened internally; for instance, an invalid response was received from the ad server.
        //ERROR_CODE_INVALID_REQUEST - The ad request was invalid; for instance, the ad unit ID was incorrect.
        //ERROR_CODE_NETWORK_ERROR - The ad request was unsuccessful due to network connectivity.
        //ERROR_CODE_NO_FILL - The ad request was successful, but no ad was returned due to lack of ad inventory.
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        LogUtils.e( "GoogleAdListener->onRewardedVideoAdLoaded()");
    }

    @Override
    public void onRewardedVideoAdOpened() {
        LogUtils.e( "GoogleAdListener->onRewardedVideoAdOpened()");
    }

    @Override
    public void onRewardedVideoStarted() {
        LogUtils.e( "GoogleAdListener->onRewardedVideoStarted()");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        LogUtils.e( "GoogleAdListener->onRewardedVideoAdClosed()");
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        LogUtils.e( "GoogleAdListener->onRewarded()");
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        LogUtils.e( "GoogleAdListener->onRewardedVideoAdLeftApplication()");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        LogUtils.e( "GoogleAdListener->onRewardedVideoAdFailedToLoad()");
    }

    @Override
    public void onRewardedVideoCompleted() {
        LogUtils.e("GoogleAdListener->onRewardedVideoCompleted()");
    }

    /* getAdMobRequest */
    public static AdRequest getAdMobRequest() {
        return new AdRequest.Builder().build();
    }
}
