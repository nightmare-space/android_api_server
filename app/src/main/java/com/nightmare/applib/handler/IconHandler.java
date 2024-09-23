package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;

import com.nightmare.applib.FakeContext;
import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.BitmapHelper;
import com.nightmare.applib.utils.L;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class IconHandler implements IHTTPHandler {
    public IconHandler() {
        displayMetrics = new DisplayMetrics();
        displayMetrics.setToDefaults();
        configuration = new Configuration();
        configuration.setToDefaults();
    }

    private final String TAG = "IconHandler";

    DisplayMetrics displayMetrics;
    Configuration configuration;

    @Override
    public String route() {
        return "/icon";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
//        String url = session.getUri();
        String path = session.getParms().get("path");
        L.d("icon get path -> " + path);
        byte[] bytes = null;
        if (path != null) {
            try {
                bytes = appChannel.getApkBitmapBytes(path);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            String packageName = session.getParms().get("package");
            assert packageName != null;
            if (packageName.contains(".png")) {
                int dotIndex = packageName.lastIndexOf('.'); // 找到 '.' 的位置
                String result;
                if (dotIndex != -1) {
                    result = packageName.substring(0, dotIndex); // 获取 '.' 前的部分
                } else {
                    result = packageName; // 如果没有 '.'，返回原字符串
                }
                packageName = result;
            }
            L.d("package -> " + packageName);
            bytes = BitmapHelper.bitmap2Bytes(getBitmap(packageName));
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/png", new ByteArrayInputStream(bytes), bytes.length);
    }

    /**
     * @noinspection CallToPrintStackTrace
     */
    static public PackageInfo getPackageInfo(String packageName, int flag) {
        PackageManager pm = FakeContext.get().getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(packageName, flag);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    static public  PackageInfo getPackageInfo(String packageName) {
        return getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
    }

    /**
     * @param packageName: App package name
     * @return Bitmap
     * @noinspection CallToPrintStackTrace
     */
    public synchronized Bitmap getBitmap(String packageName) {
        Drawable icon = null;
        PackageInfo packageInfo = getPackageInfo(packageName);
        L.d("package info -> " + packageInfo);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (applicationInfo == null) {
            L.d("applicationInfo is null");
            return null;
        }
        AssetManager assetManager = null;
        try {
            assetManager = AssetManager.class.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        try {
            assert assetManager != null;
            //noinspection JavaReflectionMemberAccess
            assetManager.getClass().getMethod("addAssetPath", String.class).invoke(assetManager, applicationInfo.sourceDir);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        Resources resources = new Resources(assetManager, displayMetrics, configuration);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon = resources.getDrawable(applicationInfo.icon, null);
            }
        } catch (Exception e) {
            L.d("getBitmap package error:" + applicationInfo.packageName);
            return null;
        }
        try {
            if (icon == null) {
                return null;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && icon instanceof AdaptiveIconDrawable) {
                Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
                return bitmap;
            } else {
                int w = icon.getIntrinsicWidth();
                int h = icon.getIntrinsicHeight();
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                //设置画布的范围
                icon.setBounds(0, 0, w, h);
                icon.draw(canvas);
                return bitmap;
            }
        } catch (Exception e) {
            L.d(TAG + " Exception:" + e);
            return null;
        }
    }
}