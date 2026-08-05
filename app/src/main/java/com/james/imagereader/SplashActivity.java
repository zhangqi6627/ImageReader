package com.james.imagereader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends Activity {
    private static final long AD_PLACEHOLDER_DURATION_MS = 1800L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean hasOpenedHome;
    private View logoContainer;
    private View adContainer;

    private final Runnable openHomeRunnable = new Runnable() {
        @Override
        public void run() {
            openHome();
        }
    };

    private final Runnable showAdRunnable = new Runnable() {
        @Override
        public void run() {
            showAdPlaceholder();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoContainer = findViewById(R.id.splash_logo_container);
        adContainer = findViewById(R.id.splash_ad_container);
        ImageView logoView = (ImageView) findViewById(R.id.splash_logo);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_enter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 动画结束后先展示广告占位页，再进入首页。
                handler.postDelayed(showAdRunnable, 150);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        logoView.startAnimation(animation);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(showAdRunnable);
        handler.removeCallbacks(openHomeRunnable);
        super.onDestroy();
    }

    private void showAdPlaceholder() {
        if (isFinishing()) {
            return;
        }
        logoContainer.setVisibility(View.GONE);
        adContainer.setVisibility(View.VISIBLE);
        handler.postDelayed(openHomeRunnable, AD_PLACEHOLDER_DURATION_MS);
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
