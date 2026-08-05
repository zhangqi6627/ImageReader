package com.james.imagereader;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssetCoverLoader {
    private final LruCache<String, Bitmap> coverCache;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AssetCoverLoader() {
        int maxCacheSize = (int) (Runtime.getRuntime().maxMemory() / 8);
        coverCache = new LruCache<String, Bitmap>(maxCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value == null ? 0 : value.getByteCount();
            }
        };
    }

    public void loadCover(final String apkPath, final ImageView imageView) {
        imageView.setTag(apkPath);
        imageView.setImageDrawable(null);
        Bitmap cachedCover = coverCache.get(apkPath);
        if (cachedCover != null) {
            imageView.setImageBitmap(cachedCover);
            return;
        }

        final int reqWidth = imageView.getWidth();
        final int reqHeight = imageView.getHeight();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap cover = decodeFirstCover(apkPath, reqWidth, reqHeight);
                if (cover != null) {
                    coverCache.put(apkPath, cover);
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (apkPath.equals(imageView.getTag())) {
                            imageView.setImageBitmap(cover);
                        }
                    }
                });
            }
        });
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    private Bitmap decodeFirstCover(String apkPath, int reqWidth, int reqHeight) {
        AssetManager assetManager = createAssetManager(apkPath);
        if (assetManager == null) {
            return null;
        }
        try {
            String coverPath = findFirstImagePath(assetManager);
            if (coverPath == null) {
                return null;
            }
            return decodeAssetImage(assetManager, coverPath, reqWidth, reqHeight);
        } finally {
            assetManager.close();
        }
    }

    private AssetManager createAssetManager(String apkPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, apkPath);
            return assetManager;
        } catch (Exception e) {
            LogUtils.e("AssetCoverLoader", e.toString());
            return null;
        }
    }

    private String findFirstImagePath(AssetManager assetManager) {
        String imagePath = findFirstImagePath(assetManager, "imgs");
        if (imagePath != null) {
            return imagePath;
        }
        return findFirstImagePath(assetManager, "");
    }

    private String findFirstImagePath(AssetManager assetManager, String dir) {
        try {
            String[] names = assetManager.list(dir);
            if (names == null || names.length == 0) {
                return null;
            }
            Arrays.sort(names);
            for (String name : names) {
                String assetPath = dir.length() == 0 ? name : dir + "/" + name;
                if (isSupportedImage(name)) {
                    return assetPath;
                }
            }
        } catch (IOException e) {
            LogUtils.e("AssetCoverLoader", e.toString());
        }
        return null;
    }

    private boolean isSupportedImage(String name) {
        String lowerName = name.toLowerCase(Locale.US);
        return lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".webp")
                || lowerName.endsWith(".avif");
    }

    private Bitmap decodeAssetImage(AssetManager assetManager, String assetPath, int reqWidth, int reqHeight) {
        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        InputStream boundsStream = null;
        try {
            boundsStream = assetManager.open(assetPath);
            BitmapFactory.decodeStream(boundsStream, null, boundsOptions);
        } catch (IOException e) {
            LogUtils.e("AssetCoverLoader", e.toString());
            return null;
        } finally {
            closeStream(boundsStream);
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight);
        InputStream imageStream = null;
        try {
            imageStream = assetManager.open(assetPath);
            return BitmapFactory.decodeStream(imageStream, null, decodeOptions);
        } catch (IOException e) {
            LogUtils.e("AssetCoverLoader", e.toString());
            return null;
        } finally {
            closeStream(imageStream);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int targetWidth = reqWidth > 0 ? reqWidth : 360;
        int targetHeight = reqHeight > 0 ? reqHeight : 480;
        int inSampleSize = 1;
        int height = options.outHeight;
        int width = options.outWidth;

        while ((height / inSampleSize) > targetHeight || (width / inSampleSize) > targetWidth) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    private void closeStream(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            LogUtils.e("AssetCoverLoader", e.toString());
        }
    }
}
