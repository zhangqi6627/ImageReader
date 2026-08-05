package com.james.imagereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends Activity {
    private static final long AD_PLACEHOLDER_DURATION_MS = 1800L;
    private static final long UPGRADE_CHECK_WAIT_MS = 1500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean hasOpenedHome;
    private boolean hasShownUpgradeDialog;
    private boolean hasUpgradeCheckFinished;
    private boolean isWaitingUpgradeCheck;
    private UpgradeChecker.ReleaseInfo pendingReleaseInfo;
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

    private final Runnable upgradeCheckTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            isWaitingUpgradeCheck = false;
            openHome();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoContainer = findViewById(R.id.splash_logo_container);
        adContainer = findViewById(R.id.splash_ad_container);
        checkUpgrade();
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
        handler.removeCallbacks(upgradeCheckTimeoutRunnable);
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
        if (pendingReleaseInfo != null && !hasShownUpgradeDialog) {
            showUpgradeDialog(pendingReleaseInfo);
            return;
        }
        if (!hasUpgradeCheckFinished && !isWaitingUpgradeCheck) {
            isWaitingUpgradeCheck = true;
            handler.postDelayed(upgradeCheckTimeoutRunnable, UPGRADE_CHECK_WAIT_MS);
            return;
        }
        hasOpenedHome = true;
        startActivity(new Intent(this, AssetsActivity.class));
        finish();
    }

    private void checkUpgrade() {
        new UpgradeChecker().checkLatestRelease(this, new UpgradeChecker.Callback() {
            @Override
            public void onResult(UpgradeChecker.ReleaseInfo releaseInfo) {
                hasUpgradeCheckFinished = true;
                pendingReleaseInfo = releaseInfo;
                if (isWaitingUpgradeCheck) {
                    isWaitingUpgradeCheck = false;
                    handler.removeCallbacks(upgradeCheckTimeoutRunnable);
                    openHome();
                }
            }
        });
    }

    private void showUpgradeDialog(final UpgradeChecker.ReleaseInfo releaseInfo) {
        hasShownUpgradeDialog = true;
        handler.removeCallbacks(openHomeRunnable);
        String message = getString(R.string.upgrade_message, releaseInfo.currentVersionCode, releaseInfo.latestVersionCode);
        if (!TextUtils.isEmpty(releaseInfo.releaseNotes)) {
            message = message + "\n\n" + releaseInfo.releaseNotes;
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.upgrade_title))
                .setMessage(message)
                .setPositiveButton(R.string.upgrade_now, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(releaseInfo.downloadUrl)));
                        openHome();
                    }
                })
                .setNegativeButton(R.string.upgrade_later, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        openHome();
                    }
                })
                .setOnCancelListener(new android.content.DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(android.content.DialogInterface dialog) {
                        openHome();
                    }
                })
                .show();
    }
}
