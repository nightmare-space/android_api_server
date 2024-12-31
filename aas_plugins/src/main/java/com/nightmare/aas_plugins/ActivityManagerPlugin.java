package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.foundation.FakeContext;
import com.nightmare.aas.helper.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class ActivityManagerPlugin extends AndroidAPIPlugin {

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
                startActivity(packageName, activity, id);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("result", "success");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
            }
            case "stop_activity": {
                IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                IActivityManager activityManagerServices = IActivityManager.Stub.asInterface(binder);
                JSONObject jsonObject = new JSONObject();
                try {
                    try {
                        activityManagerServices.forceStopPackage(packageName, -2);
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

    // TODO Sula里面的没有用过ShizkuBinderWrapper包装
    public void startActivity(String packageName, String activity, String displayId) {
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
                    /* caller */ null,
                    /* callingPackage */ FakeContext.PACKAGE_NAME,
                    /* intent */ launchIntent,
                    /* resolvedType */ null,
                    /* resultTo */ null,
                    /* resultWho */ null,
                    /* requestCode */ 0,
                    /* startFlags */ 0,
                    /* profilerInfo */ null,
                    /* bOptions */ options,
                    /* userId */ /* UserHandle.USER_CURRENT */ -2
            );
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


}
