package com.james.imagereader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Window;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Utils {

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,###.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 设置状态栏字体图标为深色，需要 MIUIV6 以上
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回 true
     */
    public static boolean MIUISetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 根据包名判断apk是否已安装
     *
     * @param context
     * @param packageName
     * @return true if app installed
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    public static boolean isAppExists(String apkPath) {
        return false;
    }

    public static int getStatusBarHeight(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return statusHeight;
    }

    public static void unZip(String zipFilePath, String unzipPath) throws IOException {
        ZipFile zipFile = new ZipFile(zipFilePath);
        // 创建解压目标目录
        File unzipFolder = new File(unzipPath);
        // 如果目标目录不存在，则创建
        if (!unzipFolder.exists()) {
            unzipFolder.mkdirs();
        }
        // 删除之前解压过的文件
        for (File file : Objects.requireNonNull(unzipFolder.listFiles())) {
            file.delete();
        }
        ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)));
        // 读取一个进入点
        ZipEntry nextEntry = zipInputStream.getNextEntry();
        byte[] buffer = new byte[1024 * 1024];
        int count = 0;
        // 如果进入点为空说明已经遍历完所有压缩包中文件和目录
        while (nextEntry != null) {
            unzipFolder = new File(unzipPath + File.separator + nextEntry.getName());
            if (nextEntry.isDirectory()) {
                // 如果是一个文件夹
                if (!unzipFolder.exists()) {
                    unzipFolder.mkdir();
                }
            } else {
                // 如果是文件那就保存,则解压文件
                if (!unzipFolder.exists()) {
                    unzipFolder.createNewFile();
                    FileOutputStream fos = new FileOutputStream(unzipFolder);
                    while ((count = zipInputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, count);
                    }
                    fos.close();
                }
            }
            //这里很关键循环解读下一个文件
            nextEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }
}
