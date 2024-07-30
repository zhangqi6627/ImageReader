package com.james.imagereader;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

public class AssetsIntentService extends JobIntentService {
    private static final int JOB_ID = 10111;
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AssetsIntentService.class, JOB_ID, work);
    }
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AssetsProvider.getInstance(AssetsIntentService.this).getAssetPackages("");
            }
        }).start();
    }
}
