package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.content.ComponentName;
import android.content.Context;
import android.app.ITaskStackListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.GraphicBuffer;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Printer;
import android.window.TaskSnapshot;

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
    public ActivityTaskManagerPlugin() {
        IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
        iATM = ActivityTaskManager.getService();
        iAM = ActivityManagerNative.asInterface(binder);
    }


    public ITaskStackListener iTaskStackListener = new ITaskStackListener.Stub() {
        @Override
        public void onTaskStackChanged() {
            L.d("onTaskStackChanged");
        }

        @Override
        public void onActivityPinned(String packageName, int userId, int taskId, int rootTaskId) {
            L.d("onActivityPinned: packageName=" + packageName + ", userId=" + userId + ", taskId=" + taskId + ", rootTaskId=" + rootTaskId);
        }

        @Override
        public void onActivityUnpinned() {
            L.d("onActivityUnpinned");
        }

        @Override
        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo task, boolean homeTaskVisible, boolean clearedTask, boolean wasVisible) throws RemoteException {
            // L.d("onActivityRestartAttempt: task=" + task + ", homeTaskVisible=" + homeTaskVisible + ", clearedTask=" + clearedTask + ", wasVisible=" + wasVisible);
            L.d("onActivityRestartAttempt: " + " homeTaskVisible=" + homeTaskVisible + ", clearedTask=" + clearedTask + ", wasVisible=" + wasVisible);
        }

        @Override
        public void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException {
            L.d("onActivityForcedResizable: packageName=" + packageName + ", taskId=" + taskId + ", reason=" + reason);
        }

        @Override
        public void onActivityDismissingDockedTask() throws RemoteException {
            L.d("onActivityDismissingDockedTask");

        }

        @Override
        public void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) throws RemoteException {

            L.d("onActivityLaunchOnSecondaryDisplayFailed: taskInfo=" + taskInfo + ", requestedDisplayId=" + requestedDisplayId);
        }

        @Override
        public void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException {

            L.d("onActivityLaunchOnSecondaryDisplayFailed");
        }

        @Override
        public void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) throws RemoteException {
            L.d("onActivityLaunchOnSecondaryDisplayRerouted: taskInfo=" + taskInfo + ", requestedDisplayId=" + requestedDisplayId);
        }

        @Override
        public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
            L.d("onTaskCreated: taskId=" + taskId + ", componentName=" + componentName);
        }

        @Override
        public void onTaskRemoved(int taskId) throws RemoteException {
            L.d("onTaskRemoved: taskId=" + taskId);
        }

        @Override
        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException {
            // L.d("onTaskMovedToFront: taskId=" + taskInfo.taskId);
        }

        @Override
        public void onTaskMovedToFront(int taskId) throws RemoteException {

            L.d("onTaskMovedToFront: taskId=" + taskId);
        }

        @Override
        public void onTaskRemovalStarted(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException {
            L.d("onTaskRemovalStarted: taskInfo=" + taskInfo);
        }

        @Override
        public void onTaskRemovalStarted(int taskId) throws RemoteException {
            L.d("onTaskRemovalStarted: taskId=" + taskId);
        }

        @Override
        public void onTaskDescriptionChanged(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException {
            // L.d("onTaskDescriptionChanged: taskInfo=" + taskInfo);
            L.d("onTaskDescriptionChanged");
        }

        @Override
        public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td) throws RemoteException {
            L.d("onTaskDescriptionChanged: taskId=" + taskId + ", td=" + td);
        }

        @Override
        public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException {
            L.d("onActivityRequestedOrientationChanged: taskId=" + taskId + ", requestedOrientation=" + requestedOrientation);
        }

        @Override
        public void onTaskProfileLocked(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException {
            L.d("onTaskProfileLocked: taskInfo=" + taskInfo);
        }

        @Override
        public void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot) throws RemoteException {
            // L.d("onTaskSnapshotChanged: taskId=" + taskId + ", snapshot=" + snapshot);
            L.d("onTaskSnapshotChanged: taskId=" + taskId);

        }

        @Override
        public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException {
            L.d("onBackPressedOnTaskRoot: taskInfo=" + taskInfo);
        }

        @Override
        public void onTaskDisplayChanged(int taskId, int newDisplayId) throws RemoteException {
            L.d("onTaskDisplayChanged: taskId=" + taskId + ", newDisplayId=" + newDisplayId);
        }

        @Override
        public void onRecentTaskListUpdated() throws RemoteException {
            L.d("onRecentTaskListUpdated");
        }

        @Override
        public void onRecentTaskListFrozenChanged(boolean frozen) {
            L.d("onRecentTaskListFrozenChanged: frozen=" + frozen);
        }

        // @Override
        // public void onRecentTaskRemovedForAddTask(int taskId) {
        //     L.d("onRecentTaskRemovedForAddTask: taskId=" + taskId);
        // }

        @Override
        public void onTaskFocusChanged(int taskId, boolean focused) {
            L.d("onTaskFocusChanged: taskId=" + taskId + ", focused=" + focused);
        }

        @Override
        public void onTaskRequestedOrientationChanged(int taskId, int requestedOrientation) {
            L.d("onTaskRequestedOrientationChanged: taskId=" + taskId + ", requestedOrientation=" + requestedOrientation);
        }

        @Override
        public void onActivityRotation(int displayId) {
            L.d("onActivityRotation : displayId=" + displayId);
        }

        @Override
        public void onTaskMovedToBack(ActivityManager.RunningTaskInfo taskInfo) {
            L.d("onTaskMovedToBack: taskInfo=" + taskInfo);
        }

        @Override
        public void onLockTaskModeChanged(int mode) {
            L.d("onLockTaskModeChanged: mode=" + mode);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }
    };

    IActivityTaskManager iATM;
    IActivityManager iAM;


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


    /**
     * @noinspection DataFlowIssue, deprecation
     */
    @SuppressLint("NewApi")
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        String id = session.getParms().get("id");
        L.d("id -> " + id);
        assert action != null;
        switch (action) {
            case "get_task_snapshot":
                // TODO 这个是不是最多测到 Android15
                byte[] bytes = null;
                try {
                    ReflectionHelper.listAllObject(iATM);
                    Object snapshot = null;
                    int taskId = Integer.parseInt(id);
                    int SDK_INT = Build.VERSION.SDK_INT;
                    // Android 12/13/15
                    if (SDK_INT == Build.VERSION_CODES.S ||
                            SDK_INT == Build.VERSION_CODES.S_V2 ||
                            SDK_INT == Build.VERSION_CODES.TIRAMISU |
                                    SDK_INT == Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        L.d("S/S_V2 or TIRAMISU or VANILLA_ICE_CREAM");
                        snapshot = iATM.getTaskSnapshot(taskId, false);
                        L.d("snapshot -> " + snapshot);
                    }
                    // Android 14
                    if (SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        L.d("UPSIDE_DOWN_CAKE");
                        snapshot = iATM.getTaskSnapshot(taskId, false, false);
                        L.d("snapshot -> " + snapshot);
                    }
                    Object hardBuffer = ReflectionHelper.getHiddenField(snapshot, "mSnapshot");
                    L.d("hardBuffer -> " + hardBuffer);
                    Object colorSpace = ReflectionHelper.getHiddenField(snapshot, "mColorSpace");
                    //
                    if (SDK_INT >= Build.VERSION_CODES.S) {
                        HardwareBuffer hardwareBuffer = (HardwareBuffer) hardBuffer;
                        Bitmap bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, (ColorSpace) colorSpace);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        assert bitmap != null;
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        bytes = baos.toByteArray();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/png", new ByteArrayInputStream(bytes), bytes.length);
            case "move_task": {
                assert id != null;
                int idInt = Integer.parseInt(id);
                int displayId = Integer.parseInt(session.getParms().get("display_id"));
                try {
                    ActivityTaskManager.getService().moveRootTaskToDisplay(idInt, displayId);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{}");
            }
            case "set_focused_task":
                int taskId = Integer.parseInt(id);
                int displayId = Integer.parseInt(session.getParms().get("display_id"));
                ReflectionHelper.listAllObject(iATM);
                try {
                    iATM.setFocusedTask(taskId);
                    iATM.setFocusedRootTask(taskId);
                    // iATM.focusTopTask(displayId);
                    // iATM.resizeTask(taskId, new Rect(0, 0, 1080, 2400), 1);
                    // iATM.moveTaskToRootTask(taskId, taskId, false);
                    // iATM.moveTaskToRootTask(taskId, taskId, true);
                    // iATM.moveRootTaskToDisplay(taskId, displayId);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("result", "success");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
            case "get_running_apps":
                JSONObject result = new JSONObject();
                JSONArray datas = new JSONArray();
                try {
                    List<ActivityManager.RunningAppProcessInfo> infos = iAM.getRunningAppProcesses();
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
        // Android 11 haven't getRecentTasks
        Object recentTaskRaw = ReflectionHelper.invokeHiddenMethod(iATM, "getRecentTasks", 100, 0, -2);
        List<ActivityManager.RecentTaskInfo> recentTaskInfos = ReflectionHelper.invokeHiddenMethod(recentTaskRaw, "getList");
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (ActivityManager.RecentTaskInfo taskInfo : recentTaskInfos) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", taskInfo.id);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    int taskId = taskInfo.taskId;
                    jsonObject.put("taskId", taskId);
                }
                try {
                    int stackId = ReflectionHelper.getHiddenField(taskInfo, "stackId");
                    jsonObject.put("stackId", stackId);
                } catch (Exception e) {
                    // Android 15 没有 stackId 字段
                    // L.d("stackId get error -> " + e);
                }
                // above Android 3.1
                jsonObject.put("persistentId", taskInfo.persistentId);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // https://cs.android.com/android/platform/superproject/+/android11-release:frameworks/base/core/java/android/app/TaskInfo.java
                    // R is Android 11
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        int displayId = ReflectionHelper.getHiddenField(taskInfo, "displayId");
                        jsonObject.put("displayId", displayId);
                    }
                    int affiliatedTaskId = ReflectionHelper.getHiddenField(taskInfo, "affiliatedTaskId");
                    jsonObject.put("affiliatedTaskId", affiliatedTaskId);
                    ActivityInfo topActivityInfo = null;
                    // Q is Android 10
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        topActivityInfo = ReflectionHelper.getHiddenField(taskInfo, "topActivityInfo");
                        // L.d("topActivityInfo -> " + topActivityInfo);
                        int topActivityType = ReflectionHelper.getHiddenField(taskInfo, "topActivityType");
                        // L.d("topActivityType -> " + topActivityType);
                        jsonObject.put("topActivityType", topActivityType);
                    }
                    if (topActivityInfo != null) {
                        Printer printWriter = new LogPrinter(Log.DEBUG, "StandardOutput");
                        topActivityInfo.dump(printWriter, "  ");
                        jsonObject.put("topActivityInfo", topActivityInfo + "");
                    } else {
                        jsonObject.put("topActivityInfo", "");
                    }
                    // TODO resizeMode 在 android 11 就有，Sula 能不能借助这个来判断
                    // S is Android 12
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
}
