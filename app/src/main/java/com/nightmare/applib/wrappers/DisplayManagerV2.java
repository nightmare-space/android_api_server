package com.nightmare.applib.wrappers;


import com.nightmare.applib.*;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.ReflectUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.hardware.display.VirtualDisplay;
import android.os.Build;
import android.view.Display;
import android.view.Surface;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TargetApi(Build.VERSION_CODES.KITKAT)
@SuppressLint("PrivateApi,DiscouragedPrivateApi")
public final class DisplayManagerV2 {
    private final Object manager; // instance of hidden class android.hardware.display.DisplayManagerGlobal
    private Method createVirtualDisplayMethod;

    static public DisplayManagerV2 create() {
        try {
            Class<?> clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
            Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
            Object dmg = getInstanceMethod.invoke(null);
            return new DisplayManagerV2(dmg);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private DisplayManagerV2(Object manager) {
        this.manager = manager;
    }

    // public to call it from unit tests
//    public static DisplayInfo parseDisplayInfo(String dumpsysDisplayOutput, int displayId) {
//        Pattern regex = Pattern.compile(
//                "^    mOverrideDisplayInfo=DisplayInfo\\{\".*?, displayId " + displayId + ".*?(, FLAG_.*)?, real ([0-9]+) x ([0-9]+).*?, "
//                        + "rotation ([0-9]+).*?, layerStack ([0-9]+)",
//                Pattern.MULTILINE);
//        Matcher m = regex.matcher(dumpsysDisplayOutput);
//        if (!m.find()) {
//            return null;
//        }
//        int flags = parseDisplayFlags(m.group(1));
//        int width = Integer.parseInt(m.group(2));
//        int height = Integer.parseInt(m.group(3));
//        int rotation = Integer.parseInt(m.group(4));
//        int layerStack = Integer.parseInt(m.group(5));
//
//        return new DisplayInfo(displayId, new Size(width, height), rotation, layerStack, flags);
//    }

//    private static DisplayInfo getDisplayInfoFromDumpsysDisplay(int displayId) {
//        try {
//            String dumpsysDisplayOutput = Command.execReadOutput("dumpsys", "display");
//            return parseDisplayInfo(dumpsysDisplayOutput, displayId);
//        } catch (Exception e) {
//            Ln.e("Could not get display info from \"dumpsys display\" output", e);
//            return null;
//        }
//    }

    private static int parseDisplayFlags(String text) {
        Pattern regex = Pattern.compile("FLAG_[A-Z_]+");
        if (text == null) {
            return 0;
        }

        int flags = 0;
        Matcher m = regex.matcher(text);
        while (m.find()) {
            String flagString = m.group();
            try {
                Field filed = Display.class.getDeclaredField(flagString);
                flags |= filed.getInt(null);
            } catch (ReflectiveOperationException e) {
                // Silently ignore, some flags reported by "dumpsys display" are @TestApi
            }
        }
        return flags;
    }

    public DisplayInfo getDisplayInfo(int displayId) {
        try {
//            ReflectUtil.listAllObject(manager);
            // setUserPreferredDisplayMode 测试是否可以更改帧率
            Object displayInfo = manager.getClass().getMethod("getDisplayInfo", int.class).invoke(manager, displayId);
            assert displayInfo != null;
            Class<?> cls = displayInfo.getClass();
            // width and height already take the rotation into account
            int width = cls.getDeclaredField("logicalWidth").getInt(displayInfo);
            int height = cls.getDeclaredField("logicalHeight").getInt(displayInfo);
            int rotation = cls.getDeclaredField("rotation").getInt(displayInfo);
            int layerStack = cls.getDeclaredField("layerStack").getInt(displayInfo);
            int flags = cls.getDeclaredField("flags").getInt(displayInfo);
            String name = (String) cls.getDeclaredField("name").get(displayInfo);
            return new DisplayInfo(displayId, new Size(width, height), rotation, layerStack, flags, name, displayInfo.toString());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public int[] getDisplayIds() {
        try {
            return (int[]) manager.getClass().getMethod("getDisplayIds").invoke(manager);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private Method getCreateVirtualDisplayMethod() throws NoSuchMethodException {
        if (createVirtualDisplayMethod == null) {
            createVirtualDisplayMethod = android.hardware.display.DisplayManager.class
                    .getMethod("createVirtualDisplay", String.class, int.class, int.class, int.class, Surface.class);
        }
        return createVirtualDisplayMethod;
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int displayIdToMirror, Surface surface) throws Exception {
        Method method = getCreateVirtualDisplayMethod();
        return (VirtualDisplay) method.invoke(null, name, width, height, displayIdToMirror, surface);
    }
}
