package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.GraphicBuffer;
import android.graphics.PixelFormat;
import android.hardware.HardwareBuffer;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.helper.L;
import com.nightmare.aas.helper.ReflectionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

/**
 * ActivityTaskManager Plugin
 */
public class ActivityTaskManagerPlugin extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/activity_task_manager";
    }

    public Bitmap graphicBufferToBitmap(GraphicBuffer graphicBuffer) {
        int width = graphicBuffer.getWidth();
        int height = graphicBuffer.getHeight();
        int format = graphicBuffer.getFormat();

        Bitmap.Config config;
        if (format == PixelFormat.RGBA_8888) {
            config = Bitmap.Config.ARGB_8888;
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        graphicBuffer.lockCanvas().drawBitmap(bitmap, 0, 0, null);
        graphicBuffer.unlockCanvasAndPost(graphicBuffer.lockCanvas());

        return bitmap;
    }


    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        String id = session.getParms().get("id");
        L.d("id -> " + id);
        assert action != null;
        switch (action) {
            case "get_task_snapshot":
                byte[] bytes = null;
                try {
                    long start = System.currentTimeMillis();
                    IActivityTaskManager activityTaskManager = ActivityTaskManager.getService();
                    ReflectionHelper.listAllObject(activityTaskManager);
                    Object snapshot = null;
                    // Android 12/Android 15
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == 35) {
                        L.d("S or VANILLA_ICE_CREAM");
                        // TODO use hidden api
                        snapshot = ReflectionHelper.invokeHiddenMethod(activityTaskManager, "getTaskSnapshot", Integer.parseInt(id), false);
                        L.d("snapshot -> " + snapshot);
                    }
                    // Android 13/Android 14
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU || Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        L.d("TIRAMISU or UPSIDE_DOWN_CAKE");
                        snapshot = ReflectionHelper.invokeHiddenMethod(activityTaskManager, "getTaskSnapshot", Integer.parseInt(id), false, false);
                        L.d("snapshot -> " + snapshot);
                    }
                    Object hardBuffer = ReflectionHelper.getHiddenField(snapshot, "mSnapshot");
                    L.d("hardBuffer -> " + hardBuffer);
                    Object colorSpace = ReflectionHelper.getHiddenField(snapshot, "mColorSpace");
                    //
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HardwareBuffer hardwareBuffer = (HardwareBuffer) hardBuffer;
                        Bitmap bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, (ColorSpace) colorSpace);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        bytes = baos.toByteArray();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/png", new ByteArrayInputStream(bytes), bytes.length);
            case "move_task":
                int idInt = Integer.parseInt(id);
                int displayId = Integer.parseInt(session.getParms().get("display_id"));
                try {
                    ActivityTaskManager.getService().moveRootTaskToDisplay(idInt, displayId);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{}");
            case "get_running_apps":
                JSONObject result = new JSONObject();
                JSONArray datas = new JSONArray();
                IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                IActivityManager activityManagerService = ActivityManagerNative.asInterface(binder);
                try {
                    List<ActivityManager.RunningAppProcessInfo> infos = activityManagerService.getRunningAppProcesses();
                    L.d("len -> " + infos.size());
                    for (ActivityManager.RunningAppProcessInfo info : infos) {
                        JSONObject item = new JSONObject();
                        item.put("processName", info.processName);
                        item.put("importance", info.importance);
                        item.put("importanceReasonCode", info.importanceReasonCode);
                        item.put("importanceReasonPid", info.importanceReasonPid);
                        item.put("lru", info.lru);
                        item.put("pid", info.pid);
                        item.put("uid", info.uid);
                        datas.put(item);
                    }
                } catch (RemoteException | JSONException e) {
                    throw new RuntimeException(e);
                }
                try {
                    result.put("datas", datas);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", result.toString());
            case "get_tasks":
                try {
                    return newFixedLengthResponse(
                            NanoHTTPD.Response.Status.OK,
                            "application/json",
                            getRecentTasks()
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{}");
    }

    String getRecentTasks() {
        List<ActivityManager.RecentTaskInfo> recentTaskInfos = ActivityTaskManager.getInstance().getRecentTasks(100, 0, -2);
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (ActivityManager.RecentTaskInfo taskInfo : recentTaskInfos) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", taskInfo.id);
                // above Android 3.1
                jsonObject.put("persistentId", taskInfo.persistentId);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        int displayId = ReflectionHelper.getHiddenField(taskInfo, "displayId");
                        jsonObject.put("displayId", displayId);
                        boolean isVisible = ReflectionHelper.getHiddenField(taskInfo, "isVisible");
                        jsonObject.put("isVisible", isVisible);
                        boolean isRunning = ReflectionHelper.getHiddenField(taskInfo, "isRunning");
                        jsonObject.put("isRunning", isRunning);
                        boolean isFocused = ReflectionHelper.getHiddenField(taskInfo, "isFocused");
                        jsonObject.put("isFocused", isFocused);
                    }
                    // 有的任务后台久了，会拿不到topActivity
                    if (taskInfo.topActivity != null) {
                        jsonObject.put("topPackage", taskInfo.topActivity.getPackageName());
                        jsonObject.put("topActivity", taskInfo.topActivity.getClassName());
                        PackageInfo packageInfo = PackageManagerPlugin.getPackageInfo(taskInfo.topActivity.getPackageName());
                        jsonObject.put("label", PackageManagerPlugin.getLabel(packageInfo.applicationInfo));
                    } else {
                        jsonObject.put("topPackage", "");
                        jsonObject.put("topActivity", "");
                        jsonObject.put("label", "");
                    }
                }
                jsonArray.put(jsonObject);
            }
            jsonObjectResult.put("datas", jsonArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return jsonObjectResult.toString();
    }


    // TODO
    public static int getChargingCurrent(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (batteryManager != null) {
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        }
        return 0;
    }

}
