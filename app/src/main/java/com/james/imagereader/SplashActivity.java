package com.james.imagereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class SplashActivity extends Activity {
    private static final long AD_PLACEHOLDER_DURATION_MS = 1800L;
    private static final long UPGRADE_CHECK_WAIT_MS = 1500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean hasOpenedHome;
    private boolean hasShownUpgradeDialog;
    private boolean hasUpgradeCheckFinished;
    private boolean isWaitingUpgradeCheck;
    private boolean isDownloadingUpdate;
    private AlertDialog upgradeDialog;
    private ProgressBar upgradeProgressBar;
    private TextView upgradeProgressText;
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
        if (upgradeDialog != null && upgradeDialog.isShowing()) {
            upgradeDialog.dismiss();
        }
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
        LinearLayout upgradeLayout = new LinearLayout(this);
        upgradeLayout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        upgradeLayout.setPadding(padding, 0, padding, 0);

        TextView messageView = new TextView(this);
        messageView.setText(message);
        upgradeLayout.addView(messageView);

        upgradeProgressText = new TextView(this);
        upgradeProgressText.setText(R.string.upgrade_downloading);
        upgradeProgressText.setVisibility(View.GONE);
        upgradeLayout.addView(upgradeProgressText);

        upgradeProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        upgradeProgressBar.setMax(100);
        upgradeProgressBar.setProgress(0);
        upgradeProgressBar.setVisibility(View.GONE);
        upgradeLayout.addView(upgradeProgressBar);

        upgradeDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.upgrade_title))
                .setView(upgradeLayout)
                .setPositiveButton(R.string.upgrade_now, null)
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
                .create();
        upgradeDialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
            @Override
            public void onShow(android.content.DialogInterface dialog) {
                upgradeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startApkDownload(releaseInfo);
                    }
                });
            }
        });
        upgradeDialog.show();
    }

    private void startApkDownload(UpgradeChecker.ReleaseInfo releaseInfo) {
        if (isDownloadingUpdate) {
            return;
        }
        isDownloadingUpdate = true;
        upgradeProgressText.setVisibility(View.VISIBLE);
        upgradeProgressBar.setVisibility(View.VISIBLE);
        upgradeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        upgradeDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        String apkName = "ImageReader-" + releaseInfo.tagName + ".apk";
        new ApkUpdateDownloader().download(this, releaseInfo.downloadUrl, apkName, new ApkUpdateDownloader.Listener() {
            @Override
            public void onProgress(int progress) {
                upgradeProgressBar.setProgress(progress);
                upgradeProgressText.setText(getString(R.string.upgrade_download_progress, progress));
            }

            @Override
            public void onSuccess(File apkFile) {
                upgradeProgressText.setText(R.string.upgrade_download_done);
                installApk(apkFile);
                openHome();
            }

            @Override
            public void onError(Exception exception) {
                isDownloadingUpdate = false;
                upgradeProgressText.setText(R.string.upgrade_download_failed);
                upgradeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                upgradeDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
            }
        });
    }

    private void installApk(File apkFile) {
        Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", apkFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
