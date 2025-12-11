package com.nightmare.aas_plugins.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.display.DeviceProductInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayCutout;

import com.nightmare.aas.ContextStore;
import com.nightmare.aas.helper.L;
import com.nightmare.aas.helper.RH;
import com.nightmare.aas.helper.ReflectionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class DisplayHelper {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static public JSONObject getDisplayInfo(Display display) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        Point realSize = new Point();
        display.getRealSize(realSize);
        // getRealMetrics getRealSize 在一加上获取到的大小总是不对的，会多85，但是
        // 经过一些研究，也不知道这个85是哪儿来的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DeviceProductInfo deviceProductInfo = display.getDeviceProductInfo();
            if (deviceProductInfo != null) {
                jsonObject.put("productName", deviceProductInfo.getName());
                jsonObject.put("productId", deviceProductInfo.getProductId());
                jsonObject.put("productManufacture", deviceProductInfo.getManufacturerPnpId());
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 这里尝试从当前的 Mode 中获取宽高
            Display.Mode mode = display.getMode();
            jsonObject.put("width", mode.getPhysicalWidth());
            jsonObject.put("height", mode.getPhysicalHeight());
        }
        jsonObject.put("id", display.getDisplayId());
        try {
            jsonObject.put("uniqueId", RH.iM(display, "getUniqueId"));
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            L.d("Cannot get uniqueId: " + e.getMessage());
        }
        jsonObject.put("name", display.getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            DisplayCutout cutout = display.getCutout();
            jsonObject.put("cutout", cutout != null ? cutout.toString() : "null");
        }
        jsonObject.put("rotation", display.getRotation());
        jsonObject.put("refreshRate", display.getRefreshRate());
        jsonObject.put("densityDpi", metrics.densityDpi);
        jsonObject.put("density", metrics.density);

        // 尝试获取其他可能的系统元素
        int captionBarHeight = getCaptionBarHeight(ContextStore.getContext());
        int displayCutoutHeight = getDisplayCutoutHeight(ContextStore.getContext());
        int actionBarHeight = getActionBarHeight(ContextStore.getContext());

        jsonObject.put("captionBarHeight", captionBarHeight);
        jsonObject.put("displayCutoutHeight", displayCutoutHeight);
        jsonObject.put("actionBarHeight", actionBarHeight);
        // ReflectionHelper.listAllObject(display);
        return jsonObject;
    }

    @TargetApi(Build.VERSION_CODES.R)
    public static int getDisplayCutoutHeight(Context context, Display display) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                // 获取WindowManager
                android.view.WindowManager windowManager = (android.view.WindowManager)
                        context.getSystemService(Context.WINDOW_SERVICE);

                // 获取默认显示的DisplayCutout
                android.view.WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
                android.view.WindowInsets windowInsets = windowMetrics.getWindowInsets();
                android.view.DisplayCutout cutout = windowInsets.getDisplayCutout();

                if (cutout != null) {
                    // 获取安全区域的顶部间距，即刘海高度
                    return cutout.getSafeInsetTop();
                }

                // 或者通过特定Display获取
                if (display != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10及以上可以直接从Display获取信息
                    Object displayInfo = ReflectionHelper.invokeMethod(display, "getDisplayInfo");
                    if (displayInfo != null) {
                        Object cutoutObject = ReflectionHelper.getField(displayInfo, "displayCutout");
                        if (cutoutObject != null) {
                            return (int) ReflectionHelper.getField(cutoutObject, "safeInsetTop");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    // 获取ActionBar高度
    public static int getActionBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("action_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }

        // 尝试获取默认ActionBar高度
        resourceId = resources.getIdentifier("action_bar_default_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    // 获取标题栏高度
    public static int getCaptionBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("caption_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    // 获取显示缺口(刘海屏/挖孔屏)高度
    public static int getDisplayCutoutHeight(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                // 尝试通过反射获取窗口装饰的相关信息
                Class<?> windowClass = Class.forName("android.view.WindowInsets");
                if (windowClass != null) {
                    Resources resources = context.getResources();
                    int resourceId = resources.getIdentifier("display_cutout_height", "dimen", "android");
                    if (resourceId > 0) {
                        return resources.getDimensionPixelSize(resourceId);
                    }
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
        return 0;
    }

}
