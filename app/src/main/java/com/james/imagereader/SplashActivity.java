package com.james.imagereader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean hasOpenedHome;

    private final Runnable openHomeRunnable = new Runnable() {
        @Override
        public void run() {
            openHome();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logoView = (ImageView) findViewById(R.id.splash_logo);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_enter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 动画结束后再进入首页，避免启动页与权限流程互相打断。
                handler.postDelayed(openHomeRunnable, 150);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        logoView.startAnimation(animation);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openHomeRunnable);
        super.onDestroy();
    }

    private void openHome() {
        if (hasOpenedHome || isFinishing()) {
            return;
        }
        hasOpenedHome = true;
        startActivity(new Intent(this, AssetsActivity.class));
        finish();
    }
}
