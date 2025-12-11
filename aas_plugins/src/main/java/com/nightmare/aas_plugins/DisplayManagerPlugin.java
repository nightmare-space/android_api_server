package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.hardware.display.VirtualDisplay;
import android.hardware.display.VirtualDisplayConfig;
import android.media.MediaCodec;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.system.Os;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.android.internal.os.ClassLoaderFactory;
import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.foundation.FakeContext;
import com.nightmare.aas.helper.L;
import com.nightmare.aas.helper.RH;
import com.nightmare.aas.helper.ReflectionHelper;
import com.nightmare.aas_plugins.helper.DisplayHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import fi.iki.elonen.NanoHTTPD;

import android.os.Process;
import android.view.SurfaceControl;

public class DisplayManagerPlugin extends AndroidAPIPlugin {
    public static void printInheritanceChain(Class<?> clazz) {
        System.out.println("继承链:");
        Class<?> current = clazz;

        while (current != null) {
            System.out.println("  " + current.getName());
            current = current.getSuperclass();
        }
    }

    public DisplayManagerPlugin() {
        DisplayManagerGlobal dmg = DisplayManagerGlobal.getInstance();
        // dmg.noSuchMethod();
        // Test.noSuchMethod();
        // RH.l(dmg);;
        // RH.l(DisplayManager.class);
        // printInheritanceChain(FakeContext.class);
        // RH.l(dmg);
        // testChangeRefreshRate20251009();
        // reflect get com/android/server/display/mode/DisplayModeDirector.java

        // IBinder binder = ServiceManager.getService(Context.WINDOW_SERVICE);
        // IWindowManager iwm = IWindowManager.Stub.asInterface(binder);
        // L.d("iwm -> " + iwm);
        //
        // try {
        //     Method registerTaskFpsCallbackMethod = iwm.getClass().getDeclaredMethod(
        //             "registerTaskFpsCallback", int.class, ITaskFpsCallback.class
        //     );
        //     ITaskFpsCallback callback = new ITaskFpsCallback() {
        //         @Override
        //         public void onFpsReported(float fps) {
        //             L.d("onFpsReported -> " + fps);
        //         }
        //
        //     };
        //     registerTaskFpsCallbackMethod.setAccessible(true);
        //     registerTaskFpsCallbackMethod.invoke(iwm, 57, callback);
        //
        // } catch (NoSuchMethodException e) {
        //     throw new RuntimeException(e);
        // } catch (InvocationTargetException e) {
        //     throw new RuntimeException(e);
        // } catch (IllegalAccessException e) {
        //     throw new RuntimeException(e);
        // }

        // void registerTaskFpsCallback( int taskId, in ITaskFpsCallback callback);
        // try {
        //     // 创建服务端套接字
        //     LocalServerSocket serverSocket = new LocalServerSocket("example_socket");
        //
        //     System.out.println("Server is waiting for a connection...");
        //
        //     // 接受客户端连接
        //     LocalSocket clientSocket = serverSocket.accept();
        //     System.out.println("Client connected!");
        //
        //     // 处理数据
        //     DataInputStream input = new DataInputStream(clientSocket.getInputStream());
        //     DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
        //
        //     String message = input.readUTF();
        //     System.out.println("Received from client: " + message);
        //
        //     output.writeUTF("Hello from server!");
        //     output.flush();
        //
        //     // 关闭连接
        //     clientSocket.close();
        //     serverSocket.close();
        // } catch (IOException e) {
        //     throw new RuntimeException(e);
        // }
        // try {
        //     Class clazz = Class.forName("android.hardware.input.InputManagerGlobal");
        //     Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
        //     getInstanceMethod.setAccessible(true);
        //     Object instance = getInstanceMethod.invoke(null);
        //     L.d("InputManagerGlobal -> " + instance);
        // } catch (ClassNotFoundException e) {
        //     throw new RuntimeException(e);
        // } catch (InvocationTargetException e) {
        //     throw new RuntimeException(e);
        // } catch (NoSuchMethodException e) {
        //     throw new RuntimeException(e);
        // } catch (IllegalAccessException e) {
        //     throw new RuntimeException(e);
        // }
        // testChangeRefreshRate();
        // testChangeRefreshRate20250714();
        // ReflectionHelper.listAllObject(DisplayManagerGlobal.class);
        // ReflectionHelper.listAllObject(VirtualDisplayConfig.Builder.class);
        // VirtualDisplayConfig.Builder builder = new VirtualDisplayConfig.Builder(
        //         "applib-vd",
        //         1080,
        //         1920,
        //         320
        // );
        // ReflectionHelper.listAllObject(builder);
        // testChangeRefreshRate20251009();
    }

    DisplayManager displayManager = createDisplayManager();

    WindowManager windowManager = (WindowManager) ContextStore.getContext().getSystemService(Context.WINDOW_SERVICE);
    int VANILLA_ICE_CREAM = 35;

    @Override
    public String route() {
        return "/display_manager";
    }

    public static Map<Integer, VirtualDisplay> cache = new HashMap<>();


    private static final int VIRTUAL_DISPLAY_FLAG_PUBLIC = 1;
    private static final int VIRTUAL_DISPLAY_FLAG_PRESENTATION = 1 << 1;
    private static final int VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY = 1 << 3;
    private static final int VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH = 1 << 6;
    private static final int VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT = 1 << 7;
    private static final int VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL = 1 << 8;
    private static final int VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS = 1 << 9;
    private static final int VIRTUAL_DISPLAY_FLAG_TRUSTED = 1 << 10;
    private static final int VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP = 1 << 11;
    private static final int VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED = 1 << 12;
    private static final int VIRTUAL_DISPLAY_FLAG_TOUCH_FEEDBACK_DISABLED = 1 << 13;
    private static final int VIRTUAL_DISPLAY_FLAG_OWN_FOCUS = 1 << 14;
    private static final int VIRTUAL_DISPLAY_FLAG_DEVICE_DISPLAY_GROUP = 1 << 15;

    private static int getVirtualDisplayFlags() {
        int flags = VIRTUAL_DISPLAY_FLAG_PUBLIC
                | VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                | VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH
                | VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT
                // Check 这个 flag 移除后，关闭虚拟显示器，app会不会退出
                //                | VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL
                // 这行能让魅族直接把 Launcher 启动到这个虚拟显示器上
                //                | VIRTUAL_DISPLAY_FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS
                | VIRTUAL_DISPLAY_FLAG_TOUCH_FEEDBACK_DISABLED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            flags |= VIRTUAL_DISPLAY_FLAG_TRUSTED
                    | VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP
                    | VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED
                    | VIRTUAL_DISPLAY_FLAG_TOUCH_FEEDBACK_DISABLED;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                flags |= VIRTUAL_DISPLAY_FLAG_OWN_FOCUS
                        | VIRTUAL_DISPLAY_FLAG_DEVICE_DISPLAY_GROUP;
            }
        }
        return flags;
    }

    /**
     * createVirtualDisplay with VirtualDisplayConfig
     * TODO 这个应该会影响无界的应用流转
     * 需要根据不同的安卓版本做适配
     *
     * @param name
     * @param width
     * @param height
     * @param densityDpi
     * @param surface
     * @param flags
     * @param callback
     * @return
     */
    @SuppressLint({"NewApi", "LocalSuppress"})
    public VirtualDisplay createVirtualDisplay(
            String name,
            int width,
            int height,
            int densityDpi,
            Surface surface,
            int flags,
            VirtualDisplay.Callback callback,
            Executor executor
    ) {
        final VirtualDisplayConfig.Builder builder = new VirtualDisplayConfig.Builder(name, width, height, densityDpi);
        builder.setFlags(flags);
        if (surface != null) {
            builder.setSurface(surface);
        }
        DisplayManagerGlobal dmg = DisplayManagerGlobal.getInstance();
        return dmg.createVirtualDisplay(ContextStore.getContext(), null, builder.build(), callback, executor);
    }

    DisplayManager createDisplayManager() {
        // Android 11/12/13/14/15 (test on 2024.09.17) is ok
        // TODO: 测一下直接用 getSystemService 在这几个安卓版本是否 OK
        DisplayManager displayManager = null;
        // final int callingUid = Binder.getCallingUid();
        // L.d("callingUid -> " + callingUid);
        Context context = ContextStore.getContext();
        if (context instanceof FakeContext) {
            try {
                // noinspection JavaReflectionMemberAccess
                displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(context);
            } catch (IllegalAccessException |
                     InstantiationException |
                     InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

        } else {
            displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        }
        return displayManager;
    }

    @SuppressLint("UseRequiresApi")
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        assert action != null;
        switch (action) {
            case "getDisplays": {
                Display[] displays = displayManager.getDisplays();
                JSONObject jsonObjectResult = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                try {
                    for (Display display : displays) {
                        JSONObject jsonObject = DisplayHelper.getDisplayInfo(display);
                        // get display modes
                        Display.Mode[] modes = display.getSupportedModes();
                        // log it
                        JSONArray jsonArrayModes = new JSONArray();
                        for (Display.Mode mode : modes) {
                            // L.d("mode: " + mode);
                            JSONObject jsonObjectMode = new JSONObject();
                            jsonObjectMode.put("modeId", mode.getModeId());
                            jsonObjectMode.put("width", mode.getPhysicalWidth());
                            jsonObjectMode.put("height", mode.getPhysicalHeight());
                            jsonObjectMode.put("refreshRate", mode.getRefreshRate());
                            jsonArrayModes.put(jsonObjectMode);
                        }
                        jsonObject.put("modes", jsonArrayModes);
                        jsonArray.put(jsonObject);
                    }
                    jsonObjectResult.put("datas", jsonArray);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObjectResult.toString());
            }
            case "createVirtualDisplay": {
                L.d("createVirtualDisplayWithSurfaceView invoke");
                int uid = Binder.getCallingUid();
                Map<String, String> params = session.getParms();
                String useDeviceConfig = params.get("useDeviceConfig");
                String displayName = params.get("displayName");
                String width, height, density;
                boolean useDeviceConfigBool = Boolean.parseBoolean(useDeviceConfig);
                if (useDeviceConfigBool) {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
                    width = displayMetrics.widthPixels + "";
                    height = displayMetrics.heightPixels + "";
                    density = displayMetrics.densityDpi + "";
                } else {
                    width = params.get("width");
                    height = params.get("height");
                    density = params.get("density");
                }
                assert width != null;
                assert height != null;
                assert density != null;
                VirtualDisplay display = null;
                DisplayManager displayManager = createDisplayManager();
                if (displayName == null) {
                    displayName = "applib-vd";
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 之前用这种方式获取 Surface，但是安卓12上会抛异常
                    // java.lang.SecurityException: Given calling package android does not match caller's uid 2000
                    // SurfaceView surfaceView = new SurfaceView(FakeContext.get());
                    // surfaceView.getHolder().getSurface();
                    Surface surface = MediaCodec.createPersistentInputSurface();
                    display = createVirtualDisplay(
                            displayName,
                            Integer.parseInt(width),
                            Integer.parseInt(height),
                            Integer.parseInt(density),
                            surface,
                            getVirtualDisplayFlags(),
                            null,
                            null
                    );
                    // display = displayManager.createVirtualDisplay(
                    //         displayName,
                    //         Integer.parseInt(width),
                    //         Integer.parseInt(height),
                    //         Integer.parseInt(density),
                    //         surface,
                    //         getVirtualDisplayFlags()
                    // );
                }
                assert display != null;
                cache.put(display.getDisplay().getDisplayId(), display);
                JSONObject json = null;
                try {
                    json = DisplayHelper.getDisplayInfo(display.getDisplay());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(
                        NanoHTTPD.Response.Status.OK,
                        "application/json",
                        json.toString()
                );
            }
        }
        String displayId = session.getParms().get("id");
        assert displayId != null;
        if (cache.containsKey(Integer.parseInt(displayId))) {
            Objects.requireNonNull(cache.get(Integer.parseInt(displayId))).release();
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "success");
    }


    ClassLoader getServerClassLoader() {
        String systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH");
        ClassLoader loader = ClassLoaderFactory.createClassLoader(
                systemServerClasspath,
                null,
                null,
                ClassLoader.getSystemClassLoader(),
                0,
                true,
                null
        );
        assert loader != null;
        return loader;
    }

    void setConfig() {
        DisplayManagerGlobal dmg = DisplayManagerGlobal.getInstance();
        boolean shouldAlwaysRespectAppRequestedMode = dmg.shouldAlwaysRespectAppRequestedMode();
        L.d("shouldAlwaysRespectAppRequestedMode -> " + shouldAlwaysRespectAppRequestedMode);
        dmg.setShouldAlwaysRespectAppRequestedMode(true);
        shouldAlwaysRespectAppRequestedMode = dmg.shouldAlwaysRespectAppRequestedMode();
        L.d("shouldAlwaysRespectAppRequestedMode -> " + shouldAlwaysRespectAppRequestedMode);

        int refreshRateSwitchingType = dmg.getRefreshRateSwitchingType();
        L.d("refreshRateSwitchingType -> " + refreshRateSwitchingType);
        dmg.setRefreshRateSwitchingType(2);
        refreshRateSwitchingType = dmg.getRefreshRateSwitchingType();
        L.d("refreshRateSwitchingType -> " + refreshRateSwitchingType);
        // float hdrSdrRatio = RH.iHM(dmg, "getHighestHdrSdrRatio", 0);
        // L.d("hdrSdrRatio -> " + hdrSdrRatio);
    }

    ClassLoader serverClassLoader = getServerClassLoader();

    @SuppressLint("PrivateApi")
    Class<?> getDisplayManagerServiceFromFramework() {
        try {
            Class<?> clazz = serverClassLoader.loadClass("com.android.server.display.DisplayManagerService");
            RH.iM(Runtime.getRuntime(), "loadLibrary0", clazz, "android_servers");
            return clazz;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("PrivateApi")
    Class<?> getDisplayControlFromFramework() {
        try {
            Class<?> clazz = serverClassLoader.loadClass("com.android.server.display.DisplayControl");
            try {
                RH.iM(Runtime.getRuntime(), "loadLibrary0", clazz, "android_servers");
            } catch (Throwable ignored) {
                L.e("loadLibrary0 failed -> " + ignored);
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    void logDisplayModeDirectorField(Object director) {
        try {
            L.d("director -> " + director);
            boolean vrrSupported = RH.iM(director, "isVrrSupportedLocked", 0);
            L.d("vrrSupported -> " + vrrSupported);
            // get hidden field mSystemRequestObserver;
            Object systemRequestObserver = RH.gF(director, "mSystemRequestObserver");
            L.d("systemRequestObserver -> " + systemRequestObserver);
            // 下面两个打印的内容和 DMG 中的不一致是正常的，因为是直接 new 的 DisplayManagerService
            // DisplayManagerService 内部也是新 new 的 DisplayModeDirector
            // 而 DisplayManagerGlobal -> IDisplayManager - > DisplayManagerService 是通过 Binder 跨进程调用的
            // 获取到的是系统框架中的实力
            boolean shouldAlwaysRespectAppRequestedMode = RH.iM(director, "shouldAlwaysRespectAppRequestedMode");
            L.d("shouldAlwaysRespectAppRequestedMode -> " + shouldAlwaysRespectAppRequestedMode);
            int modeSwitchingType = RH.iM(director, "getModeSwitchingType");
            L.d("modeSwitchingType -> " + modeSwitchingType);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    int defaultDisplayId = 0;
    int externalDisplayId = 3;
    String internalViewportTag = "local:4630946983774026899";
    String externalViewportTag = "local:4632669072017659157";
    private static final float REFRESH_RATE = 60f;

    // private static final RefreshRateRange REFRESH_RATE_RANGE =
    //         new RefreshRateRange(REFRESH_RATE, REFRESH_RATE);
    // private static final RefreshRateRanges REFRESH_RATE_RANGES =
    //         new RefreshRateRanges(REFRESH_RATE_RANGE, REFRESH_RATE_RANGE);
    void logDMS() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        // PathClassLoader pathClassLoader = (PathClassLoader) ClassLoaderFactory.createClassLoader(
        //         path, /*librarySearchPath=*/null, /*libraryPermittedPath=*/null, this.getClass().getClassLoader(),
        //         Build.VERSION.SDK_INT, /*isNamespaceShared=*/true , /*classLoaderName=*/null);
        Class<?> dmsClass = null;
        try {
            dmsClass = serverClassLoader.loadClass("com.android.server.display.DisplayManagerService");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        RH.iM(Runtime.getRuntime(), "loadLibrary0", dmsClass, "android_servers");
        Class<?> ldaClass = null;
        try {
            ldaClass = serverClassLoader.loadClass("com.android.server.display.LocalDisplayAdapter");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        RH.iM(Runtime.getRuntime(), "loadLibrary0", ldaClass, "android_servers");
        Class<?> ddmsClass = null;
        try {
            ddmsClass = Class.forName("android.view.SurfaceControl$DesiredDisplayModeSpecs");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Class<?> rrrs;
        try {
            rrrs = Class.forName("android.view.SurfaceControl$RefreshRateRanges");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Class<?> rrr;
        try {
            rrr = Class.forName("android.view.SurfaceControl$RefreshRateRange");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Object rrrO = RH.cI(rrr, 60.000004F, 60.000004F);
        L.d("rrrO -> " + rrrO);
        Object rrrsO = RH.cI(rrrs, rrrO, rrrO);
        L.d("rrrsO -> " + rrrsO);

        // 报错 java.lang.SecurityException: SELinux denied for service.
        try {
            Object dms = dmsClass.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
            Object test = RH.iM(dms, "getActiveDisplayModeAtStart", externalDisplayId);
            L.d("getActiveDisplayModeAtStart -> " + test);
            Object DisplayInfo = RH.iM(dms, "getDisplayInfoInternal", externalDisplayId, Process.myUid());
            L.d("DisplayInfo -> " + DisplayInfo);
            Point stableDisplaySize = RH.iM(dms, "getStableDisplaySizeInternal");
            L.d("stableDisplaySize -> " + stableDisplaySize);
            Object dvp = RH.iM(dms, "getViewportLocked", 1, internalViewportTag);
            L.d("dvp -> " + dvp);
            // getDisplayToken
            IBinder displayToken = RH.iM(dms, "getDisplayToken", 0);
            L.d("displayToken -> " + displayToken);

            Class<?> mDCC = getDisplayControlFromFramework();
            long[] ids = RH.iM(mDCC, "getPhysicalDisplayIds");
            for (long id : ids) {
                L.d("Physical Display Id -> " + id);
                // token
                IBinder token = RH.iM(mDCC, "getPhysicalDisplayToken", id);
                L.d("token -> " + token);
                Object ddms = RH.iMWP(SurfaceControl.class, "getDesiredDisplayModeSpecs", new Class[]{IBinder.class}, token);
                L.d("ddms -> " + ddms);
                Object mDDI = RH.iM(
                        SurfaceControl.class,
                        "getDynamicDisplayInfo",
                        id
                );
                RH.sF(ddms, "defaultMode", 1);
                RH.sF(ddms, "primaryRanges", rrrsO);
                RH.sF(ddms, "appRequestRanges", rrrsO);

                L.d("ddms -> " + ddms);
                //  public static boolean setDesiredDisplayModeSpecs(IBinder displayToken,
                //             DesiredDisplayModeSpecs desiredDisplayModeSpecs) {

                if (id == 4632669072017659157L) {
                    L.d("setDesiredDisplayModeSpecs for external display");
                    RH.iMWP(
                            SurfaceControl.class,
                            "setDesiredDisplayModeSpecs",
                            new Class[]{IBinder.class, ddmsClass},
                            token,
                            ddms
                    );
                }
                int activeColorMode = RH.gF(mDDI, "activeColorMode");
                int activeDisplayModeId = RH.gF(mDDI, "activeDisplayModeId");
                boolean autoLowLatencyModeSupported = RH.gF(mDDI, "autoLowLatencyModeSupported");
                boolean gameContentTypeSupported = RH.gF(mDDI, "gameContentTypeSupported");
                Object hdrCapabilities = RH.gF(mDDI, "hdrCapabilities");
                int preferredBootDisplayMode = RH.gF(mDDI, "preferredBootDisplayMode");
                float renderFrameRate = RH.gF(mDDI, "renderFrameRate");
                int[] supportedColorModes = RH.gF(mDDI, "supportedColorModes");
                // boolean hasArrSupport = RH.gHF(mDDI, "hasArrSupport");
                // Object frameRateCategoryRate = RH.gHF(mDDI, "frameRateCategoryRate");
                // float[] supportedRefreshRates = RH.gHF(mDDI, "supportedRefreshRates");
                L.d("-> activeDisplayModeId -> " + activeDisplayModeId);
                L.d("-> renderFrameRate -> " + renderFrameRate);
                L.d("-> supportedColorModes -> " + Arrays.toString(supportedColorModes));
                L.d("-> activeColorMode -> " + activeColorMode);
                L.d("-> autoLowLatencyModeSupported -> " + autoLowLatencyModeSupported);
                L.d("-> gameContentTypeSupported -> " + gameContentTypeSupported);
                L.d("-> hdrCapabilities -> " + hdrCapabilities);
                L.d("-> preferredBootDisplayMode -> " + preferredBootDisplayMode);
                Object[] supportedDisplayModes = (Object[]) RH.gF(mDDI, "supportedDisplayModes");
                // log in for
                for (Object mode : supportedDisplayModes) {
                    L.d("mode -> " + mode);
                }

            }
            // private java.util.Optional getViewportType(com.android.server.display.DisplayDeviceInfo arg0)

            // invoke dms getDisplayHandler
            Object director = RH.gF(dms, "mDisplayModeDirector");
            Object systemRequestObserver = RH.gF(director, "mSystemRequestObserver");
            logDisplayModeDirectorField(director);

            DisplayManagerGlobal dmg = DisplayManagerGlobal.getInstance();
            IBinder dmgToken = RH.gF(dmg, "mToken");
            // RH.iHMWT(
            //         systemRequestObserver,
            //         "requestDisplayModes",
            //         new Class[]{IBinder.class, int.class, int[].class},
            //         dmgToken,
            //         // 下面这个id就是普通的 Display.getDisplayId() 得到的值
            //         externalDisplayId,
            //         new int[]{1, 2}
            // );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    void testChangeRefreshRate20251009() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        DisplayManagerGlobal dmg = DisplayManagerGlobal.getInstance();
        // getHighestHdrSdrRatio
        IDisplayManager idm = RH.gF(dmg, "mDm");
        int[] displayIds = dmg.getDisplayIds();
        // for (int id : displayIds) {
        //     L.d("displayId -> " + id);
        //     Display display = dmg.getRealDisplay(id);
        //     Display.Mode[] modes = display.getSupportedModes();
        //     for (Display.Mode mode : modes) {
        //         L.d("mode -> " + mode);
        //     }
        //     if (id == externalDisplayId) {
        //         L.d("Try to set display mode to modeId 1");
        //         dmg.setUserPreferredDisplayMode(id, display.getSupportedModes()[1]);
        //     }
        // }


        // frameworks/base/services/core/java/com/android/server/display/mode/DisplayModeDirector.java
        // 2025-10-09 04:53:26.862  3201-4722  DisplayModeDirector     system_server                        D  setAppRequest displayId : 85  modeId: 0  requestedRefreshRate: 0.0  requestedMinRefreshRateRange: 0.0  requestedMaxRefreshRateRange: 0.0
        // 2025-10-09 04:53:26.863  3201-4722  DisplayModeDirector     system_server                        D  setAppRequest displayId : 0  modeId: 1  requestedRefreshRate: 0.0  requestedMinRefreshRateRange: 0.0  requestedMaxRefreshRateRange: 120.00001
        setConfig();
        try {
            logDMS();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (true) {
            return;
        }

        // invoke  void requestDisplayModes(IBinder token, int displayId, @Nullable int[] modeIds)  from SystemRequestObserver

        IBinder dmgToken = RH.gF(dmg, "mToken");
        L.d("dmgToken -> " + dmgToken);

        // if (true) return;
        L.d("com.android.server.display.DisplayControl");
        // invoke public void requestDisplayModes(IBinder token, int displayId, int[] modeIds) from DisplayModeDirector

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //     RH.l(SurfaceControl.class);
        // }
        Class<?> mDCC = getDisplayControlFromFramework();
        long[] ids = RH.iM(mDCC, "getPhysicalDisplayIds");
        L.d("ids -> " + Arrays.toString(ids));
        // frameworks/base/services/core/java/com/android/server/display/mode/DisplayModeDirector.java
        for (long id : ids) {
            L.d("Display Id -> " + id);
            IBinder displayToken = RH.iM(mDCC, "getPhysicalDisplayToken", id);
            L.d("displayToken -> " + displayToken);
            Object mDDI = RH.iM(
                    SurfaceControl.class,
                    "getDynamicDisplayInfo",
                    id
            );
            int activeColorMode = RH.gF(mDDI, "activeColorMode");
            int activeDisplayModeId = RH.gF(mDDI, "activeDisplayModeId");
            boolean autoLowLatencyModeSupported = RH.gF(mDDI, "autoLowLatencyModeSupported");
            boolean gameContentTypeSupported = RH.gF(mDDI, "gameContentTypeSupported");
            Object hdrCapabilities = RH.gF(mDDI, "hdrCapabilities");
            int preferredBootDisplayMode = RH.gF(mDDI, "preferredBootDisplayMode");
            float renderFrameRate = RH.gF(mDDI, "renderFrameRate");
            int[] supportedColorModes = RH.gF(mDDI, "supportedColorModes");
            // boolean hasArrSupport = RH.gHF(mDDI, "hasArrSupport");
            // Object frameRateCategoryRate = RH.gHF(mDDI, "frameRateCategoryRate");
            // float[] supportedRefreshRates = RH.gHF(mDDI, "supportedRefreshRates");
            L.d("-> activeDisplayModeId -> " + activeDisplayModeId);
            L.d("-> renderFrameRate -> " + renderFrameRate);
            L.d("-> supportedColorModes -> " + Arrays.toString(supportedColorModes));
            L.d("-> activeColorMode -> " + activeColorMode);
            L.d("-> autoLowLatencyModeSupported -> " + autoLowLatencyModeSupported);
            L.d("-> gameContentTypeSupported -> " + gameContentTypeSupported);
            L.d("-> hdrCapabilities -> " + hdrCapabilities);
            L.d("-> preferredBootDisplayMode -> " + preferredBootDisplayMode);
            // L.d("-> hasArrSupport -> " + hasArrSupport);
            // L.d("-> frameRateCategoryRate -> " + frameRateCategoryRate);
            // L.d("-> supportedRefreshRates -> " + Arrays.toString(supportedRefreshRates));
            Object[] supportedDisplayModes = (Object[]) RH.gF(mDDI, "supportedDisplayModes");
            // log in for
            for (Object mode : supportedDisplayModes) {
                L.d("mode -> " + mode);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Object staticInfo = RH.iMWP(
                        SurfaceControl.class,
                        "getStaticDisplayInfo",
                        new Class[]{long.class},
                        id
                );
                L.d("staticInfo -> " + staticInfo);
                // setBootDisplayMode: Invalid mode 733 for display 21
                // 可以确认这个 mode 不是来自 Display.Mode
                // if (id == 4632669072017659157L) {
                // 2025-11-02 23:16:59.985  2174-2174  HWComposer              surfaceflinger                       E  setBootDisplayMode: setBootDisplayMode failed for display 4632669072017659157: UNSUPPORTED (8)
                // getActiveDisplayModeAtStart for cmd display -> DisplayManagerService
                L.d("Try to requestDisplayModes to 30 for external display");
                // RH.iSMWT(
                //         SurfaceControl.class,
                //         "setBootDisplayMode",
                //         new Class[]{IBinder.class, int.class},
                //         displayToken,
                //         9
                // );
                dmg.requestDisplayModes(externalDisplayId, new int[]{8});
                dmg.requestColorMode(externalDisplayId, 7);
                // }

                // RH.l(dCS);
                // getDynamicDisplayInfo

                // get DisplayMode[] supportedDisplayModes

                // RESTRICT_DISPLAY_MODES
                RH.iM(
                        dmg,
                        "requestDisplayModes",
                        new Class[]{int.class, int[].class},
                        // 下面这个id就是普通的 Display.getDisplayId() 得到的值
                        externalDisplayId,
                        new int[]{2, 3}
                );
            }
        }
        // setDisplayOffsetsInternal
        // setDisplayPropertiesInternal
        // setRefreshRateSwitchingTypeInternal
        // setShouldAlwaysRespectAppRequestedModeInternal
        // InputManagerInternal.java getCursorPosition setMousePointerAccelerationEnabled
    }


}

