package com.james.imagereader;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.widget.Toast;

public class AssetsIntentService extends JobIntentService {
    private static final int JOB_ID = 10111;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AssetsIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
//        long timeBegin = System.currentTimeMillis();
//        AssetsProvider.getInstance(AssetsIntentService.this).getAssetsInfoFromApk();
//        long timeSecond = System.currentTimeMillis();
//        mHandler.sendMessage(mHandler.obtainMessage(0, (int) ((timeSecond - timeBegin) / 1000), 0));
//        // 扫描数据库中的记录，如果应用不存在就删除记录，并且更新RecyclerView
//        AssetsProvider.getInstance(AssetsIntentService.this).deleteItemIfNotExist();
//        long timeEnd = System.currentTimeMillis();
//        mHandler.sendMessage(mHandler.obtainMessage(1, (int) ((timeEnd - timeSecond) / 1000), 0));
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Toast.makeText(getApplication(), "插件扫描完成:" + msg.arg1 + "s", Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Toast.makeText(getApplication(), "数据库更新完毕:" + msg.arg1 + "s", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
}
