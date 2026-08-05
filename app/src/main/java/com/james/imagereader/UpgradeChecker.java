package com.james.imagereader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpgradeChecker {
    public static final String RELEASES_LATEST_URL = "https://api.github.com/repos/zhangqi6627/ImageReader/releases/latest";
    private static final String TAG = "UpgradeChecker";
    private static final Pattern VERSION_CODE_PATTERN = Pattern.compile("vc(\\d+)");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onResult(ReleaseInfo releaseInfo);
    }

    public void checkLatestRelease(final Context context, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ReleaseInfo releaseInfo = requestLatestRelease(context);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(releaseInfo);
                    }
                });
            }
        }).start();
    }

    private ReleaseInfo requestLatestRelease(Context context) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(RELEASES_LATEST_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                LogUtils.w(TAG, "GitHub release request failed: " + responseCode);
                return null;
            }

            JSONObject releaseJson = new JSONObject(readString(connection.getInputStream()));
            String tagName = releaseJson.optString("tag_name");
            int latestVersionCode = parseVersionCode(tagName);
            int currentVersionCode = getCurrentVersionCode(context);
            if (latestVersionCode <= currentVersionCode) {
                return null;
            }

            String downloadUrl = findApkDownloadUrl(releaseJson);
            if (TextUtils.isEmpty(downloadUrl)) {
                downloadUrl = releaseJson.optString("html_url");
            }
            if (TextUtils.isEmpty(downloadUrl)) {
                return null;
            }

            return new ReleaseInfo(
                    tagName,
                    releaseJson.optString("name", tagName),
                    releaseJson.optString("body"),
                    downloadUrl,
                    latestVersionCode,
                    currentVersionCode);
        } catch (Exception e) {
            LogUtils.w(TAG, e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readString(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    private int getCurrentVersionCode(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return packageInfo.versionCode;
    }

    private int parseVersionCode(String tagName) {
        if (TextUtils.isEmpty(tagName)) {
            return 0;
        }
        Matcher matcher = VERSION_CODE_PATTERN.matcher(tagName);
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            LogUtils.w(TAG, e);
            return 0;
        }
    }

    private String findApkDownloadUrl(JSONObject releaseJson) {
        JSONArray assets = releaseJson.optJSONArray("assets");
        if (assets == null) {
            return null;
        }
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.optJSONObject(i);
            if (asset == null) {
                continue;
            }
            String name = asset.optString("name");
            if (name != null && name.endsWith(".apk")) {
                return asset.optString("browser_download_url");
            }
        }
        return null;
    }

    public static class ReleaseInfo {
        public final String tagName;
        public final String releaseName;
        public final String releaseNotes;
        public final String downloadUrl;
        public final int latestVersionCode;
        public final int currentVersionCode;

        public ReleaseInfo(String tagName, String releaseName, String releaseNotes, String downloadUrl, int latestVersionCode, int currentVersionCode) {
            this.tagName = tagName;
            this.releaseName = releaseName;
            this.releaseNotes = releaseNotes;
            this.downloadUrl = downloadUrl;
            this.latestVersionCode = latestVersionCode;
            this.currentVersionCode = currentVersionCode;
        }
    }
}
