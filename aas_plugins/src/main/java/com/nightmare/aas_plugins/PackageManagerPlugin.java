package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.helper.L;
import com.nightmare.aas_plugins.util.BitmapHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class PackageManagerPlugin extends AndroidAPIPlugin {
    public PackageManagerPlugin() {
    }

    public static String calculateHash(File file, String algorithm) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String calculateHash(byte[] bytes, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getMD5(byte[] bytes) {
        return calculateHash(bytes, "MD5");
    }

    public static String getSHA1(byte[] bytes) {
        return calculateHash(bytes, "SHA-1");
    }

    public static String getSHA256(byte[] bytes) {
        return calculateHash(bytes, "SHA-256");
    }

    @Override
    public String route() {
        return "/package_manager";
    }

    Map<Integer, Boolean> getFlagsStatus(int flag, boolean privateFlags) {
        String prefix = null;
        if (privateFlags) {
            prefix = "PRIVATE_FLAG_";
        } else {
            prefix = "FLAG_";
        }
        Map<Integer, Boolean> result = new HashMap<>();
        List<Integer> flagValues = new ArrayList<>();
        Field[] fields = ApplicationInfo.class.getFields();
        for (Field field : fields) {
            if (field.getName().startsWith(prefix)) {
                try {
                    int value = field.getInt(null);
                    L.d("value -> " + value + " name -> " + field.getName());
                    flagValues.add(value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < flagValues.size(); i++) {
            int key = flagValues.get(i);
            if (key == Integer.MIN_VALUE) {
                key = 1 << 31;
            }
            boolean value = ((flag & flagValues.get(i)) != 0);
            result.put(key, value);
        }
        return result;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        assert action != null;
        switch (action) {
            case "get_permissions": {
                String packageName = session.getParms().get("package");
                String permissions = getAppPermissions(packageName);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", permissions);
            }
            case "get_app_flags": {
                String packageName = session.getParms().get("package");
                String privateFlags = session.getParms().get("private");
                boolean isPrivate = Boolean.parseBoolean(privateFlags == null ? "false" : privateFlags);
                PackageInfo packageInfo = getPackageInfo(packageName);
                JSONObject flags = new JSONObject();
                Map<Integer, Boolean> status = getFlagsStatus(packageInfo.applicationInfo.flags, isPrivate);
                for (Map.Entry<Integer, Boolean> entry : status.entrySet()) {
                    try {
                        flags.put(entry.getKey() + "", entry.getValue());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("flags", flags);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
            }
            case "get_app_details": {
                String packageName = session.getParms().get("package");
                String details = getAppDetail(packageName);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", details);
            }
            case "get_icon":
                String path = session.getParms().get("path");
                L.d("icon get path -> " + path);
                byte[] bytes = null;
                if (path != null) {
                    try {
                        bytes = getApkBitmapBytes(path);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String packageName = session.getParms().get("package");
                    L.d("icon get package -> " + packageName);
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
            case "get_app_activities": {
                String packageName = session.getParms().get("package");
                String activitys = getAppActivities(packageName);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", activitys);
            }
            case "get_all_app_info":
                String line = session.getParms().get("is_system_app");
                boolean isSystemApp = Boolean.parseBoolean(line);
                String apps = getAllAppInfo(isSystemApp);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", apps);
            case "app_main_activity": {
                String packageName = session.getParms().get("package");
                String mainActivity = getAppMainActivity(packageName);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("activity", mainActivity);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
            }
            case "pm_cmd": {
                Runtime runtime = Runtime.getRuntime();
                String cmd = "pm " + session.getParms().get("cmd");
                JSONObject jsonObject = new JSONObject();
                try {
                    Process process = runtime.exec(cmd);
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    String s;
                    StringBuilder output = new StringBuilder();
                    StringBuilder error = new StringBuilder();

                    while ((s = stdInput.readLine()) != null) {
                        output.append(s).append("\n");
                    }

                    while ((s = stdError.readLine()) != null) {
                        error.append(s).append("\n");
                    }
                    jsonObject.put("stdout", output.toString());
                    jsonObject.put("stderr", error.toString());
                    L.d("Command output: " + output.toString());
                    L.d("Command error: " + error.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
            }

        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{}");
    }

    /**
     * 通过包名获取Main Activity
     *
     * @param packageName: Android Package Name
     * @return Main Activity Plain Text
     */
    public String getAppMainActivity(String packageName) {
        StringBuilder builder = new StringBuilder();
        PackageManager pm = ContextStore.getContext().getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            builder.append(launchIntent.getComponent().getClassName());
        } else {
            L.d(packageName + "获取启动Activity失败");
        }
        return builder.toString();
//        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, null, 0, 0);
//        for (int i = 0; i < appList.size(); i++) {
//            ResolveInfo resolveInfo = appList.get(i);
//            String packageStr = resolveInfo.activityInfo.packageName;
//            if (packageStr.equals(packageName)) {
//                builder.append(resolveInfo.activityInfo.name).append("\n");
//                break;
//            }
//        }
//        return builder.toString();
    }

    public static String getLabel(ApplicationInfo info) {
        PackageManager pm = ContextStore.getContext().getPackageManager();
        return (String) info.loadLabel(pm);
        // TODO(lin) 下面是个啥
//        int res = info.labelRes;
//        if (info.nonLocalizedLabel != null) {
//            return (String) info.nonLocalizedLabel;
//        }
//        if (res != 0) {
//            AssetManager assetManager = getAssetManagerFromPath(info.sourceDir);
//            Resources resources = new Resources(assetManager, displayMetrics, configuration);
//            return (String) resources.getText(res);
//        }
//        return null;
    }


    public String getAllAppInfo(boolean getSystemApp) {
        PackageManager pm = ContextStore.getContext().getPackageManager();
        @SuppressLint("QueryPermissionsNeeded")
        List<PackageInfo> packageInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (PackageInfo packageInfo : packageInfos) {
                JSONObject appInfoJson = new JSONObject();
                if (packageInfo == null) {
                    continue;
                }
                int flags = packageInfo.applicationInfo.flags;
                boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                L.d("package " + packageInfo.packageName + " isSystemApp -> " + isSystemApp);
                if (!getSystemApp && isSystemApp) {
                    continue;
                }
                if (getSystemApp && !isSystemApp) {
                    continue;
                }
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                appInfoJson.put("package", applicationInfo.packageName);
                appInfoJson.put("label", getLabel(applicationInfo));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    appInfoJson.put("minSdk", packageInfo.applicationInfo.minSdkVersion);
                } else {
                    appInfoJson.put("minSdk", 0);
                }
                appInfoJson.put("targetSdk", applicationInfo.targetSdkVersion);
                appInfoJson.put("versionName", packageInfo.versionName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    appInfoJson.put("versionCode", packageInfo.getLongVersionCode());
                } else {
                    appInfoJson.put("versionCode", packageInfo.versionCode);
                }
                appInfoJson.put("enabled", applicationInfo.enabled);
                boolean isSuspend = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    isSuspend = (flags & ApplicationInfo.FLAG_SUSPENDED) != 0;
                }
                appInfoJson.put("is_suspend", isSuspend);
                boolean isUninstall = (flags & ApplicationInfo.FLAG_INSTALLED) != 0;
                appInfoJson.put("is_install", isUninstall);
                // TODO 判断 Apk 是否隐藏需要重新实现
//                PackageInfo withoutHidePackage = PackageManagerPlugin.getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
                appInfoJson.put("hide", false);
                appInfoJson.put("uid", applicationInfo.uid);
                appInfoJson.put("sourceDir", applicationInfo.sourceDir);
                jsonArray.put(appInfoJson);
            }
            jsonObjectResult.put("datas", jsonArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObjectResult.toString();
    }


    public String getAppActivities(String packageName) {
        PackageManager packageManager = ContextStore.getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activities = packageInfo.activities;
            if (activities == null) {
                return "[]";
            }
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("package", packageName);
            jsonObject.put("activitys", jsonArray);
            for (ActivityInfo activityInfo : activities) {
                jsonArray.put(activityInfo.name);
            }
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }


    /**
     * 获取应用详情
     *
     * @param packageName: 包名
     * @return json map
     */
    public String getAppDetail(String packageName) {
        JSONObject jsonObject = new JSONObject();
        try {
            int flag = PackageManager.GET_UNINSTALLED_PACKAGES;
            PackageInfo packageInfo = PackageManagerPlugin.getPackageInfo(packageName, flag);
            jsonObject.put("first_install_time", packageInfo.firstInstallTime);
            jsonObject.put("last_update_time", packageInfo.lastUpdateTime);
            jsonObject.put("data_dir", packageInfo.applicationInfo.dataDir);
            jsonObject.put("native_library_dir", packageInfo.applicationInfo.nativeLibraryDir);
            String apkPath = packageInfo.applicationInfo.sourceDir;
            int size = (int) new File(apkPath).length();
            jsonObject.put("apk_size", size);
            PackageInfo packageInfoWithSign = PackageManagerPlugin.getPackageInfo(packageName, flag | PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfoWithSign.signatures) {
                L.d("signature -> " + signature.toCharsString());
                // toByteArray
                byte[] bytes = signature.toByteArray();
                String md5 = getMD5(bytes);
                String sha1 = getSHA1(bytes);
                String sha256 = getSHA256(bytes);
                jsonObject.put("md5", md5);
                jsonObject.put("sha1", sha1);
                jsonObject.put("sha256", sha256);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }


    public String getAppPermissions(String packageName) {
        PackageManager packageManager = ContextStore.getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] permissions = packageInfo.requestedPermissions;
//            L.d("permissions: " + permissions);
            if (permissions == null) {
                return "[]";
            }
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("datas", jsonArray);
            for (String permission : permissions) {
                JSONObject object = new JSONObject();
                object.put("name", permission);
                try {
                    PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);
//                    L.d("permissionInfo: " + permissionInfo);
                    if (permissionInfo != null) {
                        CharSequence description = permissionInfo.loadDescription(packageManager);
                        if (description != null) {
                            object.put("description", description.toString());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                jsonArray.put(object);
            }
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }


    static public PackageInfo getPackageInfo(String packageName, int flag) {
        PackageManager pm = ContextStore.getContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(packageName, flag);
        } catch (PackageManager.NameNotFoundException e) {
            L.e(packageName + "not found");
        }
        return info;
    }

    static public PackageInfo getPackageInfo(String packageName) {
        return getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
    }

    public Bitmap getBitmap(String packageName) {
        return getBitmap(packageName, false);
    }


    public byte[] getApkBitmapBytes(String path) throws
            InvocationTargetException, IllegalAccessException {
        return BitmapHelper.bitmap2Bytes(getUninstallAPKIcon(path));
    }


    AssetManager getAssetManagerFromPath(String path) {
        AssetManager assetManager = null;
        try {
            assetManager = AssetManager.class.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        try {
            assert assetManager != null;
            assetManager.getClass().getMethod("addAssetPath", String.class).invoke(assetManager, path);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return assetManager;
    }

    /**
     * @param packageName: App package name
     * @return Bitmap
     */
    public synchronized Bitmap getBitmap(String packageName, boolean useDesperateWay) {
        Drawable icon = null;
        PackageInfo packageInfo = getPackageInfo(packageName);
        if (packageInfo == null) {

            L.d(packageName + " packageInfo is null");
            return null;
        }
        L.d("package info -> " + packageInfo);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (applicationInfo == null) {
            L.d("applicationInfo is null");
            return null;
        }
        try {
            // this way will crash on meizu 21
            // 而且很奇怪，icon 在 crash 的时候，并不为 null，所以还不能直接通过异常处理的方式来切换方案
            // 所以先用之前的方案
            // java.lang.NullPointerException: Attempt to invoke virtual method 'boolean java.lang.String.equals(java.lang.Object)' on a null object reference
            //        at android.content.res.flymetheme.FlymeThemeHelper.makeThemeIcon(FlymeThemeHelper.java:837)
            //        at android.content.res.flymetheme.FlymeThemeHelper.makeThemeIcon(FlymeThemeHelper.java:810)
            //        at android.app.ApplicationPackageManager$Injector.makeThemeIcon(ApplicationPackageManager.java:4035)
            //        at android.app.ApplicationPackageManager.getDrawable(ApplicationPackageManager.java:1817)
            //        at android.app.ApplicationPackageManager.loadUnbadgedItemIcon(ApplicationPackageManager.java:3397)
            //        at android.app.ApplicationPackageManager.loadItemIcon(ApplicationPackageManager.java:3376)
            //        at android.content.pm.PackageItemInfo.loadIcon(PackageItemInfo.java:273)
            //        at com.nightmare.aas_plugins.IconHandler.getBitmap(IconHandler.java:107)
            //        at com.nightmare.aas_plugins.IconHandler.getBitmap(IconHandler.java:83)
            //        at com.nightmare.aas_plugins.IconHandler.handle(IconHandler.java:59)
            //        at com.nightmare.applib.AppServer.serve(AppServer.java:388)
            //        at fi.iki.elonen.NanoHTTPD$HTTPSession.execute(NanoHTTPD.java:945)
            //        at fi.iki.elonen.NanoHTTPD$ClientHandler.run(NanoHTTPD.java:192)
            //        at java.lang.Thread.run(Thread.java:1012)
//            icon = applicationInfo.loadIcon(FakeContext.get().getPackageManager());
//            L.e("applicationInfo.loadIcon failed for " + packageName + " use the second way");
            AssetManager assetManager = AssetManager.class.newInstance();
            //noinspection JavaReflectionMemberAccess
            assetManager.getClass().getMethod("addAssetPath", String.class).invoke(assetManager, applicationInfo.sourceDir);
            Resources resources = new Resources(assetManager, null, null);
            icon = resources.getDrawable(applicationInfo.icon, null);
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
        } catch (Throwable t) {
            L.e("IconHandler getBitmap Exception:" + t);
            return null;
        }
    }


    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo info = packageInfo.applicationInfo;
            info.sourceDir = apkPath;
            info.publicSourceDir = apkPath;
            try {
                return info.loadIcon(packageManager);
            } catch (Exception e) {

            }
        }
        return null;
    }

    //
    public Bitmap getUninstallAPKIcon(String apkPath) {
        Drawable icon = getApkIcon(ContextStore.getContext(), apkPath);
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
                return ((BitmapDrawable) icon).getBitmap();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
