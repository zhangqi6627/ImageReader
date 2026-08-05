package com.james.imagereader;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApkUpdateDownloader {
    private static final String TAG = "ApkUpdateDownloader";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Listener {
        void onProgress(int progress);

        void onSuccess(File apkFile);

        void onError(Exception exception);
    }

    public void download(final Context context, final String downloadUrl, final String fileName, final Listener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadOnWorker(context, downloadUrl, fileName, listener);
            }
        }).start();
    }

    private void downloadOnWorker(Context context, String downloadUrl, String fileName, Listener listener) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/octet-stream");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IllegalStateException("APK download failed: " + responseCode);
            }

            File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (downloadDir == null) {
                downloadDir = context.getFilesDir();
            }
            if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                throw new IllegalStateException("Create download dir failed: " + downloadDir.getAbsolutePath());
            }

            File apkFile = new File(downloadDir, fileName);
            inputStream = connection.getInputStream();
            outputStream = new FileOutputStream(apkFile);
            int totalSize = connection.getContentLength();
            int downloadedSize = 0;
            int lastProgress = -1;
            byte[] buffer = new byte[16 * 1024];
            int readSize;
            while ((readSize = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readSize);
                downloadedSize += readSize;
                if (totalSize > 0) {
                    int progress = Math.min(100, downloadedSize * 100 / totalSize);
                    if (progress != lastProgress) {
                        lastProgress = progress;
                        notifyProgress(listener, progress);
                    }
                }
            }
            outputStream.flush();
            notifyProgress(listener, 100);
            notifySuccess(listener, apkFile);
        } catch (final Exception e) {
            LogUtils.e(TAG, e.toString(), e);
            notifyError(listener, e);
        } finally {
            closeQuietly(outputStream);
            closeQuietly(inputStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void notifyProgress(final Listener listener, final int progress) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onProgress(progress);
            }
        });
    }

    private void notifySuccess(final Listener listener, final File apkFile) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess(apkFile);
            }
        });
    }

    private void notifyError(final Listener listener, final Exception exception) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onError(exception);
            }
        });
    }

    private void closeQuietly(java.io.Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            LogUtils.w(TAG, e);
        }
    }
}
