package com.james.imagereader;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private static final String TELEGRAM_URL = "https://t.me/ImageCoolSupport";
    private static final String GMAIL_ADDRESS = "imagecool.reader@gmail.com";
    private static final String GITHUB_URL = "https://github.com/zhangqi6627/ImageReader";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView tvVersion = (TextView) findViewById(R.id.tv_version);
        tvVersion.setText(getVersionText());

        findViewById(R.id.tv_settings_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.layout_telegram).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrl(TELEGRAM_URL);
            }
        });
        findViewById(R.id.layout_gmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });
        findViewById(R.id.layout_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrl(GITHUB_URL);
            }
        });
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.settings_open_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + GMAIL_ADDRESS));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            openUrl("mailto:" + GMAIL_ADDRESS);
        }
    }

    private String getVersionText() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return getString(
                    R.string.settings_version_value,
                    packageInfo.versionName,
                    packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            return getString(R.string.settings_version_value, "", 0);
        }
    }
}
