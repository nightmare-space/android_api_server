package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserManager;

import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.helper.L;
import com.nightmare.aas.helper.RH;
import com.nightmare.aas.helper.ReflectionHelper;
import com.nightmare.aas_plugins.helper.BitmapHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import fi.iki.elonen.NanoHTTPD;

public class PackageManagerPlugin extends AndroidAPIPlugin {
    public PackageManagerPlugin() {
    }

    PackageManager pm = ContextStore.getContext().getPackageManager();

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
        String prefix;
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
                    Bitmap bitmap = getBitmap(packageName, false);
                    bytes = BitmapHelper.bitmap2Bytes(bitmap);
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
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            // 获取当前用户的应用
            @SuppressLint("QueryPermissionsNeeded")
            List<PackageInfo> packageInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            processPackageInfos(packageInfos, jsonArray, getSystemApp);

            // 获取所有用户ID
            int[] userIds = getUserIds();
            if (userIds != null) {
                for (int userId : userIds) {
                    L.d("Processing user ID: " + userId + "current user id -> " + android.os.Process.myUserHandle().hashCode());
                    // 跳过当前用户，因为已经处理过了
                    if (userId == android.os.Process.myUserHandle().hashCode()) {
                        continue;
                    }

                    // 使用反射调用getInstalledPackagesAsUser
                    try {
                        Method method = PackageManager.class.getMethod("getInstalledPackagesAsUser",
                                int.class, int.class);
                        List<PackageInfo> userPackages = (List<PackageInfo>) method.invoke(pm,
                                PackageManager.GET_UNINSTALLED_PACKAGES, userId);
                        // log userPackages length
                        L.d("User " + userId + " installed packages count: " + (userPackages != null ? userPackages.size() : 0));
                        if (userPackages != null) {
                            processPackageInfos(userPackages, jsonArray, getSystemApp);
                        }
                    } catch (Exception e) {
                        L.e("获取用户 " + userId + " 的应用列表失败: " + e.getMessage());
                    }
                }
            }

            jsonObjectResult.put("datas", jsonArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObjectResult.toString();
    }

    private void processPackageInfos(List<PackageInfo> packageInfos, JSONArray jsonArray, boolean getSystemApp) throws JSONException {
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo == null || packageInfo.applicationInfo == null) {
                continue;
            }

            int flags = packageInfo.applicationInfo.flags;
            boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isInstalled = (flags & ApplicationInfo.FLAG_INSTALLED) != 0;

            if (!getSystemApp && isSystemApp) {
                continue;
            }
            if (getSystemApp && !isSystemApp) {
                continue;
            }

            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            // 获取应用的用户ID
            int userId = 0;
            try {
                userId = getUserIdForPackage(applicationInfo);
            } catch (Exception e) {
                L.e("获取应用用户ID失败: " + e.getMessage());
            }

            // 过滤条件：用户ID为0的应用保留；非0用户ID的应用必须是已安装的才保留
            if (userId != 0 && !isInstalled) {
                // L.d("跳过用户 " + userId + " 下未安装的应用: " + applicationInfo.packageName);
                continue;
            }

            JSONObject appInfoJson = new JSONObject();
            appInfoJson.put("package", applicationInfo.packageName);
            appInfoJson.put("label", getLabel(applicationInfo));
            appInfoJson.put("userId", userId);

            // 其他代码保持不变...
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
            appInfoJson.put("is_install", isInstalled);
            appInfoJson.put("hide", false);
            appInfoJson.put("uid", applicationInfo.uid);
            appInfoJson.put("sourceDir", applicationInfo.sourceDir);

            jsonArray.put(appInfoJson);
        }
    }

    // 获取所有用户ID
    private int[] getUserIds() {
        UserManager um = (UserManager) ContextStore.getContext().getSystemService(Context.USER_SERVICE);
        try {

            Method getUsersMethod = um.getClass().getMethod("getUsers");
            List<?> users = (List<?>) getUsersMethod.invoke(um);
            L.d("users -> " + users);

            if (users != null) {
                int[] userIds = new int[users.size()];
                for (int i = 0; i < users.size(); i++) {
                    Object user = users.get(i);
                    userIds[i] = RH.gF(user, "id");
                }
                return userIds;
            }
        } catch (Exception e) {
            L.e("获取用户列表失败: " + e.getMessage());
        }
        return null;
    }

    // 获取应用所属的用户ID
    private int getUserIdForPackage(ApplicationInfo applicationInfo) throws Exception {
        int uid = applicationInfo.uid;
        // 用户ID存储在UID的高位
        return uid / 100000;
    }

    public String getAppActivities(String packageName) {
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
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


    public byte[] getApkBitmapBytes(String path) throws InvocationTargetException, IllegalAccessException {
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
        try {
            // 如果获取失败，尝试从资源中加载
            AssetManager assetManager = AssetManager.class.newInstance();
            ReflectionHelper.invokeMethod(assetManager, "addAssetPath", applicationInfo.sourceDir);
            // 后面两个不能传 null，不然金铲铲这类获取不到图标
            Resources resources = new Resources(
                    assetManager,
                    ContextStore.getContext().getResources().getDisplayMetrics(),
                    ContextStore.getContext().getResources().getConfiguration()
            );
            icon = resources.getDrawable(applicationInfo.icon, null);
            if (icon == null) {
                return null;
            }
            // 将 Drawable 转换为 Bitmap
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && icon instanceof AdaptiveIconDrawable) {
                Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
                return bitmap;
            } else if (icon instanceof BitmapDrawable) {
                return ((BitmapDrawable) icon).getBitmap();
            }
        } catch (Exception e) {
            L.e("getBitmap Exception: " + e);
        }
        return null;
    }


    public Bitmap getBitmap(String packageName) {
        try {
            PackageInfo packageInfo = getPackageInfo(packageName);
            if (packageInfo == null || packageInfo.applicationInfo == null) {
                L.d(packageName + " packageInfo or applicationInfo is null");
                return null;
            }

            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            Drawable icon = null;

            // 方法2: 尝试从资源文件加载图标
            try {
                // 先尝试从公共源目录加载
                String resourcePath = applicationInfo.publicSourceDir != null ?
                        applicationInfo.publicSourceDir : applicationInfo.sourceDir;

                L.d("publicSourceDir -> " + applicationInfo.publicSourceDir);
                L.d("sourceDir -> " + applicationInfo.sourceDir);

                AssetManager assetManager = AssetManager.class.newInstance();
                RH.iM(assetManager, "addAssetPath", applicationInfo.sourceDir);
                Resources resources = new Resources(assetManager,
                        ContextStore.getContext().getResources().getDisplayMetrics(),
                        ContextStore.getContext().getResources().getConfiguration());

                if (applicationInfo.icon != 0) {
                    icon = resources.getDrawable(applicationInfo.icon, null);
                    if (icon != null) {
                        L.d("获取图标成功: 使用resources.getDrawable从" + resourcePath);
                        return drawableToBitmap(icon);
                    }
                }

                // 如果资源ID为0或加载失败，尝试加载默认图标
                icon = resources.getDrawable(android.R.drawable.sym_def_app_icon, null);
                if (icon != null) {
                    L.d("获取默认图标成功");
                    return drawableToBitmap(icon);
                }
            } catch (Exception e) {
                L.e("通过resources.getDrawable获取图标失败: " + e.getMessage());
            }

            // 方法3: 对于Unity应用，尝试查找特殊路径下的图标
            try {
                // 检查是否是Unity应用 (可以通过特征检测)
                if (isUnityApp(applicationInfo)) {
                    // 尝试从Unity特定路径加载图标
                    String[] possiblePaths = {
                            "assets/bin/Data/splash.png",
                            "assets/icon.png",
                            "res/drawable/app_icon.png",
                            "res/mipmap/app_icon.png"
                    };

                    for (String path : possiblePaths) {
                        try {
                            AssetManager am = getAssetManagerFromPath(applicationInfo.sourceDir);
                            InputStream is = am.open(path);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            is.close();
                            if (bitmap != null) {
                                L.d("从Unity特定路径加载图标成功: " + path);
                                return bitmap;
                            }
                        } catch (Exception ignored) {
                            // 忽略单个路径的错误，继续尝试下一个
                        }
                    }
                }
            } catch (Exception e) {
                L.e("尝试加载Unity特定图标失败: " + e.getMessage());
            }

            L.e("无法获取应用图标: " + packageName);
            return null;
        } catch (Exception e) {
            L.e("getBitmap总体异常: " + e.getMessage());
            return null;
        }
    }

    // 辅助方法：判断是否为Unity应用
    private boolean isUnityApp(ApplicationInfo appInfo) {
        try {
            // 检查是否包含Unity特征文件或目录
            String[] unityMarkers = {
                    "assets/bin/Data",
                    "lib/libunity.so",
                    "assets/bin/Data/Managed/UnityEngine.dll"
            };

            for (String marker : unityMarkers) {
                File file = new File(appInfo.sourceDir);
                if (file.exists()) {
                    ZipFile zipFile = new ZipFile(file);
                    if (zipFile.getEntry(marker) != null) {
                        zipFile.close();
                        return true;
                    }
                    zipFile.close();
                }
            }
        } catch (Exception ignored) {
            // 忽略检测错误
        }
        return false;
    }

    // 辅助方法：Drawable转Bitmap
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) return null;

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        // 确保宽高有效
        width = width > 0 ? width : 256;
        height = height > 0 ? height : 256;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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
