package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.foundation.FakeContext;
import com.nightmare.aas.helper.L;

import org.json.JSONException;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;

// 2025.12.06 on Android14 tested all passed.
public class ActivityManagerPlugin extends AndroidAPIPlugin {
    public ActivityManagerPlugin() {
        IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
        ams = IActivityManager.Stub.asInterface(binder);
    }

    IActivityManager ams;

    @Override
    public String route() {
        return "/activity_manager";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        String action = session.getParms().get("action");
        assert action != null;
        switch (action) {
            case "start_activity": {
                String activity = session.getParms().get("activity");
                String id = session.getParms().get("displayId");
                String userIdStr = session.getParms().get("userId");
                int userId = -2;
                if (userIdStr != null) {
                    userId = Integer.parseInt(userIdStr);
                }
                L.d("user id -> " + userId);
                startActivity(packageName, activity, id, userId);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("result", "success");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
            }
            case "stop_activity": {
                JSONObject jsonObject = new JSONObject();
                try {
                    try {
                        ams.forceStopPackage(packageName, -2);
                        jsonObject.put("result", "success");
                    } catch (RemoteException | JSONException e) {
                        jsonObject.put("result", "failed");
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
            }
            case "remove_task": {
                JSONObject jsonObject = new JSONObject();
                // noinspection DataFlowIssue,deprecation
                int id = Integer.parseInt(session.getParms().get("id"));
                try {
                    try {
                        ams.removeTask(id);
                        jsonObject.put("result", "success");
                    } catch (RemoteException | JSONException e) {
                        jsonObject.put("result", "failed");
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
            }
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{}");
    }

    public void startActivity(String packageName, String activity, String displayId, int userId) {
        Intent launchIntent = new Intent();
        launchIntent.setClassName(packageName, activity);

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle options = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityOptions launchOptions = ActivityOptions.makeBasic();
            launchOptions.setLaunchDisplayId(Integer.parseInt(displayId));

            options = launchOptions.toBundle();
        }
        try {
            IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
            IActivityManager activityManagerServices = IActivityManager.Stub.asInterface(binder);
            activityManagerServices.startActivityAsUser(
                    /* caller */null,
                    /* callingPackage */ FakeContext.PACKAGE_NAME,
                    /* intent */ launchIntent,
                    /* resolvedType */ null,
                    /* resultTo */ null,
                    /* resultWho */ null,
                    /* requestCode */ 0,
                    /* startFlags */ 0,
                    /* profilerInfo */ null,
                    /* bOptions */ options,
                    /* userId */ /* UserHandle.USER_CURRENT */ userId
            );
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
