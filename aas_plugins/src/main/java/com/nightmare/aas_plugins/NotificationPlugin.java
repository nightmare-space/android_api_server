package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.app.INotificationManager;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;
import android.service.notification.StatusBarNotification;

import com.nightmare.aas.ContextStore;
import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.helper.L;
import com.nightmare.aas.helper.ReflectionHelper;
import com.nightmare.aas.helper.ReflectionPrinter;

import fi.iki.elonen.NanoHTTPD;

public class NotificationPlugin extends AndroidAPIPlugin {

    private final android.app.INotificationManager nm;
    private final android.content.Context context;
    private final android.content.pm.PackageManager pm;

    public NotificationPlugin() {
        context = com.nightmare.aas.ContextStore.getContext();
        pm = context.getPackageManager();

        android.os.IBinder binder =
                android.os.ServiceManager.getService(
                        android.content.Context.NOTIFICATION_SERVICE
                );

        nm = android.app.INotificationManager.Stub.asInterface(binder);
    }

    @Override
    public String route() {
        return "/notification_manager";
    }

    @Override
    public fi.iki.elonen.NanoHTTPD.Response handle(
            fi.iki.elonen.NanoHTTPD.IHTTPSession session
    ) {

        try {
            android.service.notification.StatusBarNotification[] list =
                    nm.getActiveNotifications(context.getPackageName());

            StringBuilder json = new StringBuilder();
            json.append("[");

            for (int i = 0; i < list.length; i++) {
                android.service.notification.StatusBarNotification sbn = list[i];
                android.app.Notification n = sbn.getNotification();

                CharSequence title = null;
                CharSequence text = null;
                CharSequence subText = null;
                CharSequence bigText = null;

                if (n.extras != null) {
                    title = n.extras.getCharSequence(
                            android.app.Notification.EXTRA_TITLE
                    );
                    text = n.extras.getCharSequence(
                            android.app.Notification.EXTRA_TEXT
                    );
                    subText = n.extras.getCharSequence(
                            android.app.Notification.EXTRA_SUB_TEXT
                    );
                    bigText = n.extras.getCharSequence(
                            android.app.Notification.EXTRA_BIG_TEXT
                    );
                }

                String appName = sbn.getPackageName();
                try {
                    android.content.pm.ApplicationInfo ai =
                            pm.getApplicationInfo(appName, 0);
                    appName = pm.getApplicationLabel(ai).toString();
                } catch (Throwable ignored) {}

                json.append("{")
                        .append("\"package\":\"").append(sbn.getPackageName()).append("\",")
                        .append("\"appName\":\"").append(appName).append("\",")
                        .append("\"id\":").append(sbn.getId()).append(",")
                        .append("\"tag\":")
                        .append(sbn.getTag() == null
                                ? "null"
                                : "\"" + sbn.getTag() + "\"")
                        .append(",")
                        .append("\"title\":")
                        .append(title == null
                                ? "null"
                                : "\"" + escape(title) + "\"")
                        .append(",")
                        .append("\"text\":")
                        .append(text == null
                                ? "null"
                                : "\"" + escape(text) + "\"")
                        .append(",")
                        .append("\"subText\":")
                        .append(subText == null
                                ? "null"
                                : "\"" + escape(subText) + "\"")
                        .append(",")
                        .append("\"bigText\":")
                        .append(bigText == null
                                ? "null"
                                : "\"" + escape(bigText) + "\"")
                        .append(",")
                        .append("\"when\":").append(n.when).append(",")
                        .append("\"ongoing\":").append(sbn.isOngoing()).append(",")
                        .append("\"clearable\":").append(sbn.isClearable())
                        .append("}");

                if (i != list.length - 1) {
                    json.append(",");
                }
            }

            json.append("]");

            return fi.iki.elonen.NanoHTTPD.newFixedLengthResponse(
                    fi.iki.elonen.NanoHTTPD.Response.Status.OK,
                    "application/json",
                    json.toString()
            );

        } catch (Throwable e) {
            return fi.iki.elonen.NanoHTTPD.newFixedLengthResponse(
                    fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR,
                    "text/plain",
                    e.toString()
            );
        }
    }

    private static String escape(CharSequence cs) {
        if (cs == null) return "";
        return cs.toString()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}