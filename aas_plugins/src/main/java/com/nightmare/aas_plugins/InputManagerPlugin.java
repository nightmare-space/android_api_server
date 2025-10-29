package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.health.HealthInfo;
import android.hardware.input.InputManager;
import android.hardware.input.InputManagerGlobal;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Looper;
import android.os.ServiceManager;
import android.system.Os;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.TextView;

import com.android.internal.os.ClassLoaderFactory;
import com.android.server.health.HealthInfoCallback;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.foundation.FakeContext;
import com.nightmare.aas.helper.L;
import com.nightmare.aas.helper.RH;
import com.nightmare.aas.helper.ReflectionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Ref;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;

public class InputManagerPlugin extends AndroidAPIPlugin {
    /**
     * @noinspection JavaReflectionMemberAccess
     */
    @SuppressLint("WrongConstant")
    public InputManagerPlugin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            im = ContextStore.getContext().getSystemService(InputManager.class);
            // reflect get mIm
            Object mim = ReflectionHelper.getHiddenField(im, "mIm");
            // ReflectionHelper.listAllObject(im);
            // ReflectionHelper.listAllObject(mim);
            InputManagerGlobal img = InputManagerGlobal.getInstance();
            WindowManager wm = (WindowManager) ContextStore.getContext().getSystemService(Context.WINDOW_SERVICE);
            // WindowManager 没有暴露 getDisplayImePolicy、setDisplayImePolicy
            // 反射调用也比较麻烦
            // ReflectionHelper.listAllObject(wm);
            WindowManagerGlobal wmg = WindowManagerGlobal.getInstance();
            IWindowManager iwm = WindowManagerGlobal.getWindowManagerService();
            // int current = iwm.getDisplayImePolicy(10);
            // iwm.setDisplayImePolicy(Display.DEFAULT_DISPLAY,0);
            // iwm.setDisplayImePolicy(10,1);
            // iwm.setDisplayImePolicy(14,1);
            // L.d("Current Display IME Policy for display 10: " + current);
            // ReflectionHelper.listAllObject(iwm);
            DisplayManager dm = (DisplayManager) ContextStore.getContext().getSystemService(Context.DISPLAY_SERVICE);
            Display[] displays = dm.getDisplays();
            for (Display display : displays) {
                int policy = iwm.getDisplayImePolicy(display.getDisplayId());
                L.d("Display " + display.getDisplayId() + " (" + display.getName() + ") IME Policy: " + policy);
            }
            // test();
            BatteryManager bm = (BatteryManager) ContextStore.getContext().getSystemService(Context.BATTERY_SERVICE);
            int currentNow = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            float currentNowMa = Math.abs(currentNow) / 1000.0f; // 转换为毫安
            L.d("当前电流: " + currentNowMa + " mA");

            int voltage = getBatteryVoltageFromCmd();
            L.d("当前电压 (from cmd): " + voltage + " mV");

            double powerMw = (voltage * currentNowMa) / 1000.0; // 转换为毫瓦
            L.d("当前功率: " + powerMw + " mW");
            // convert W
            double powerW = powerMw / 1000.0;
            L.d("当前功率: " + powerW + " W");


        } else {
            L.e("InputManagerPlugin requires Android M or higher");
        }
    }

    // InputManagerGlobal iMG = InputManagerGlobal.getInstance();
    // IInputManager iIM = iMG.getInputManagerService();

    private Integer parseBatteryVoltageFromDump(String dump) {
        if (dump == null || dump.isEmpty()) return null;
        // 优先匹配 "voltage: 4480"
        Pattern pVoltage = Pattern.compile("\\bvoltage:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m = pVoltage.matcher(dump);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        // 其次匹配 "Charger voltage : 4785"
        Pattern pCharger = Pattern.compile("Charger\\s+voltage\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        m = pCharger.matcher(dump);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Integer getBatteryVoltageFromCmd() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "battery", "dump");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            process.waitFor();
            String output = sb.toString();
            Integer voltage = parseBatteryVoltageFromDump(output);
            if (voltage == null) {
                L.d("Battery dump parse failed, output:\n" + output);
            } else {
                L.d("Parsed battery voltage: " + voltage + " mV");
            }
            return voltage;
        } catch (IOException | InterruptedException e) {
            L.e("getBatteryVoltageFromCmd error: " + e.getMessage());
            return null;
        }
    }

    InputManager im;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            L.d("当前电压: " + voltage + " mV");
        }
    };

    void test() {

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        FakeContext.get().registerReceiver(batteryReceiver, filter);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = FakeContext.get().registerReceiver(batteryReceiver, ifilter);

        // EXTRA_VOLTAGE returns the current battery voltage in millivolts.
        // int a = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        // L.d(a);
        BatteryManager batteryManager = (BatteryManager) ContextStore.getContext().getSystemService(Context.BATTERY_SERVICE);
        // RH.l(batteryManager);
        // int voltage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_VOLTAGE);

        // Log.d("Battery", "Voltage: " + voltage + " mV");
        // private IBatteryPropertiesRegistrar mBatteryPropertiesRegistrar
        // private IBatteryStats mBatteryStats
        Object ooo = RH.gHF(batteryManager, "mBatteryPropertiesRegistrar");
        RH.l(ooo);
        // scheduleUpdate
        RH.iHM(ooo, "scheduleUpdate");
        Object oooo = RH.gHF(batteryManager, "mBatteryStats");
        RH.l(oooo);

        // 获取当前电流 (微安 μA)
        int currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        float currentNowMa = Math.abs(currentNow) / 1000.0f; // 转换为毫安
        L.d("当前电流: " + currentNowMa + " mA");
        // long l = RH.iHM(batteryManager, "queryProperty", BatteryManager.EXTRA_VOLTAGE);
        // L.d("voltage -> " + l);
        // 获取平均电流 (微安 μA)
        int currentAverage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
        float currentAverageMa = Math.abs(currentAverage) / 1000.0f; // 转换为毫安

        L.d("当前电流: " + currentNowMa + " mA");
        L.d("平均电流: " + currentAverageMa + " mA");
        L.d("Battery Current Now: " + currentNow + " uA, Average: " + currentAverage + " uA");
        Class<?> serviceManagerClass = ServiceManager.class;
        Method listServicesMethod;
        try {
            listServicesMethod = serviceManagerClass.getDeclaredMethod("listServices");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        listServicesMethod.setAccessible(true);
        String[] data;
        try {
            data = (String[]) listServicesMethod.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        Object am = ContextStore.getContext().getSystemService("activity");
        ClassLoader serverClassLoader = getServerClassLoader();
        // create com.android.server.BatteryService
        try {
            // com/android/server/health/HealthServiceWrapper.java
            Class<?> healthServiceWrapperClass = serverClassLoader.loadClass("com.android.server.health.HealthServiceWrapper");
            // com.android.server.health.HealthInfoCallback
            Class<?> healthInfoCallbackClass = serverClassLoader.loadClass("com.android.server.health.HealthInfoCallback");
            Method method = healthServiceWrapperClass.getDeclaredMethod("create", healthInfoCallbackClass);
            Object o = Proxy.newProxyInstance(
                    serverClassLoader,
                    new Class<?>[]{healthInfoCallbackClass},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            L.d("HealthInfoCallback method invoked: " + method.getName() + ", args: " + Arrays.toString(args));
                            return null;
                        }
                    });
            method.setAccessible(true);
            Object o1 = method.invoke(null, o);
            // RH.l(o);
            // RH.l(o1);
            // com/android/server/SystemServiceManager.java
            Class<?> systemServiceManagerClass = serverClassLoader.loadClass("com.android.server.SystemServiceManager");
            // RH.l(systemServiceManagerClass);
            Object ob = ReflectionHelper.createInstanceWithType(systemServiceManagerClass, new Class[]{Context.class}, FakeContext.get());
            // RH.l(ob);
            // com/android/server/LocalServices.java
            Class<?> localServicesClass = serverClassLoader.loadClass("com.android.server.LocalServices");
            RH.l(localServicesClass);
            Field field = localServicesClass.getDeclaredField("sLocalServiceObjects");
            field.setAccessible(true);
            ArrayMap ssss = (ArrayMap) field.get(null);
            L.d("ssss.size() = " + ssss.size());
            // log all ssss data
            for (Object key : ssss.keySet()) {
                Object value = ssss.get(key);
                L.d("LocalService key: " + key + ", value: " + value);
            }
            // Object obj = FakeContext.get().getSystemService("batteryproperties");
            // RH.l(obj);
            // RH.iHM(o1, "scheduleUpdate");
            // Object info = RH.iHM(o1, "getHealthInfo");

            // RH.l(info);
            // Class<?> adbServiceClass = serverClassLoader.loadClass("com.android.server.LocalServices");
            // RH.l(adbServiceClass);
            // // getService
            // Object o = RH.iSM(adbServiceClass, "getService", battery);
            // create instance
            // Constructor<?> adbServiceConstructor = adbServiceClass.getDeclaredConstructor(Context.class);
            // adbServiceConstructor.setAccessible(true);
            // Object adbServiceInstance = adbServiceConstructor.newInstance(ContextStore.getContext().getApplicationContext());
            // L.d("AdbService instance created: " + adbServiceInstance);
            // ReflectionHelper.listAllObject(adbServiceInstance);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create BatteryService instance", e);
        }
    }

    @Override
    public String route() {
        return "/input_manager";
    }

    @SuppressLint("BlockedPrivateApi")
    Method getLoadMethod() {
        Method loadMethod = null;
        try {
            loadMethod = Runtime.class.getDeclaredMethod("loadLibrary0", Class.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        // 加载所需的本地库
        loadMethod.setAccessible(true);
        return loadMethod;
    }


    /**
     * @noinspection JavaReflectionMemberAccess
     */
    ClassLoader getServerClassLoader() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> classLoaderFactoryClass = Class.forName("com.android.internal.os.ClassLoaderFactory");

            @SuppressLint({"PrivateApi", "BlockedPrivateApi"})
            Method createClassLoaderMethod = classLoaderFactoryClass.getDeclaredMethod(
                    "createClassLoader", String.class, String.class, String.class, ClassLoader.class, int.class, boolean.class, String.class
            );
            String systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH");
            ClassLoader classLoader = (ClassLoader) createClassLoaderMethod.invoke(
                    null,
                    systemServerClasspath,
                    null,
                    null,
                    ClassLoader.getSystemClassLoader(),
                    0,
                    true,
                    null
            );
            assert classLoader != null;
            return classLoader;
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Map<String, String>> getDetailedDeviceInfo() {
        Map<String, Map<String, String>> deviceInfoMap = new HashMap<>();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("getevent", "-i");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Map<String, String> currentDeviceInfo = null;
            String currentDeviceName = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("add device")) {
                    // 开始新设备，保存之前的设备信息
                    if (currentDeviceName != null && currentDeviceInfo != null) {
                        deviceInfoMap.put(currentDeviceName, currentDeviceInfo);
                    }

                    currentDeviceInfo = new HashMap<>();
                    currentDeviceName = null;
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        String devicePath = parts[1].trim();
                        currentDeviceInfo.put("path", devicePath);
                    }
                } else if (currentDeviceInfo != null) {
                    // 使用正则表达式提取键值对
                    if (line.startsWith("bus:")) {
                        // 提取形如 "bus:      0003" 的值
                        String[] parts = line.split("\\s+", 2);
                        if (parts.length > 1) {
                            currentDeviceInfo.put("bus", parts[1].trim());
                        }
                    } else if (line.startsWith("vendor")) {
                        // 提取形如 "vendor    24ae" 的值
                        String[] parts = line.split("\\s+", 2);
                        if (parts.length > 1) {
                            currentDeviceInfo.put("vendor", parts[1].trim());
                        }
                    } else if (line.startsWith("product")) {
                        // 提取形如 "product   1235" 的值
                        String[] parts = line.split("\\s+", 2);
                        if (parts.length > 1) {
                            currentDeviceInfo.put("product", parts[1].trim());
                        }
                    } else if (line.startsWith("version") && !line.startsWith("version:")) {
                        // 处理 "version   0111" 的情况
                        String[] parts = line.split("\\s+", 2);
                        if (parts.length > 1) {
                            currentDeviceInfo.put("version", parts[1].trim());
                        }
                    } else if (line.startsWith("name:")) {
                        // 提取形如 "name:     "Rapoo Rapoo Gaming Device"" 的值
                        String name = line.substring(line.indexOf(":") + 1).trim();
                        name = name.replaceAll("^\"|\"$", ""); // 去除引号
                        currentDeviceName = name;
                        currentDeviceInfo.put("name", name);
                    } else if (line.startsWith("location:")) {
                        // 提取形如 "location: "usb-xhci-hcd.1.auto-1/input1"" 的值
                        String location = line.substring(line.indexOf(":") + 1).trim();
                        location = location.replaceAll("^\"|\"$", ""); // 去除引号
                        currentDeviceInfo.put("location", location);
                    } else if (line.startsWith("id:")) {
                        // 提取形如 "id:       "DONGLE_RTL_B-CUT_20240408"" 的值
                        String id = line.substring(line.indexOf(":") + 1).trim();
                        id = id.replaceAll("^\"|\"$", ""); // 去除引号
                        currentDeviceInfo.put("id", id);
                    } else if (line.startsWith("version:")) {
                        // 提取形如 "version:  1.0.1" 的值
                        String fwVersion = line.substring(line.indexOf(":") + 1).trim();
                        currentDeviceInfo.put("fw_version", fwVersion);
                    }
                }
            }

            // 保存最后一个设备的信息
            if (currentDeviceName != null) {
                deviceInfoMap.put(currentDeviceName, currentDeviceInfo);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            L.e("Error executing getevent: " + e.getMessage());
        }

        return deviceInfoMap;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint({"WrongConstant", "UseRequiresApi"})
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        //  TODO android 12 also have
        //  void removePortAssociation(String arg0)
        //  void removeUniqueIdAssociation(String arg0)
        //  TODO 用模拟器测试安卓12能不能绑定输入设备
        String action = session.getParms().get("action");
        assert action != null;
        switch (action) {
            case "bind_device_to_display": {
                String descriptor = session.getParms().get("descriptor");
                String targetDisplay = session.getParms().get("display");
                if (descriptor == null || targetDisplay == null) {
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing parameters");
                }
                ReflectionHelper.invokeHiddenMethod(im, "removeUniqueIdAssociationByDescriptor", descriptor);
                L.d("Remove: " + descriptor);
                ReflectionHelper.invokeHiddenMethod(im, "addUniqueIdAssociationByDescriptor", descriptor, targetDisplay);
                // img.removeUniqueIdAssociationByPort("usb-xhci-hcd.1.auto-1/input1");
                // img.addUniqueIdAssociationByPort("usb-xhci-hcd.1.auto-1/input1", targetDisplay);
                JSONObject responseJson = new JSONObject();
                try {
                    responseJson.put("result", "success");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", responseJson.toString());
            }
            case "move_ime_to_display": {
                // set display 0 policy to 0z
                // set display targetDisplay policy to 1
                // WindowManager.DISPLAY_IME_POLICY_LOCAL;
                String targetDisplay = session.getParms().get("display");
                if (targetDisplay == null) {
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing parameters");
                }
                IWindowManager iwm = WindowManagerGlobal.getWindowManagerService();
                iwm.setDisplayImePolicy(Display.DEFAULT_DISPLAY, 1);
                // iwm.setDisplayImePolicy(10,0);
                iwm.setDisplayImePolicy(Integer.parseInt(targetDisplay), 0);
                L.d("Move IME to display: " + targetDisplay);
                // get all display ime policy
                int displayCount = iwm.getDisplayImePolicy(-1);
                L.d("Display IME Policy count: " + displayCount);
                DisplayManager dm = (DisplayManager) ContextStore.getContext().getSystemService(Context.DISPLAY_SERVICE);
                Display[] displays = dm.getDisplays();
                for (Display display : displays) {
                    int policy = iwm.getDisplayImePolicy(display.getDisplayId());
                    L.d("Display " + display.getDisplayId() + " (" + display.getName() + ") IME Policy: " + policy);
                }
                JSONObject responseJson = new JSONObject();
                try {
                    responseJson.put("result", "success");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", responseJson.toString());
            }
            case "remove_device": {
                String descriptor = session.getParms().get("descriptor");
                if (descriptor == null) {
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain", "Missing parameters");
                }
                ReflectionHelper.invokeHiddenMethod(im, "removeUniqueIdAssociationByDescriptor", descriptor);
                L.d("Remove: " + descriptor);
                JSONObject responseJson = new JSONObject();
                try {
                    responseJson.put("result", "success");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", responseJson.toString());
            }
            case "get_input_devices":
                // reflect get IInputManager
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //     Object overlay = null;
                //     overlay = ContextStore.getContext().getSystemService("overlay");
                //     // Context c = ReflectionHelper.getHiddenField(overlay, "mContext");
                //     L.d("Overlay Context: " + overlay);
                //     RH.l(overlay);
                //     // ReflectionHelper.listAllObject(overlay);
                //
                //     // ReflectionHelper.listAllObject(imsClass);
                //     // 构造 InputManagerService 实例
                // }
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //     InputManager im = ContextStore.getContext().getSystemService(InputManager.class);
                //     ReflectionHelper.listAllObject(im);
                //     // reflect getMousePointerSpeed
                //     int mousePointerSpeed = ReflectionHelper.invokeHiddenMethod(im, "getMousePointerSpeed");
                //     L.d("Mouse Pointer Speed: " + mousePointerSpeed);
                // }
                // 获取详细的设备信息
                Map<String, Map<String, String>> detailedDeviceInfo = getDetailedDeviceInfo();

                int[] deviceIds = im.getInputDeviceIds();
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                try {
                    for (int deviceId : deviceIds) {
                        InputDevice device = im.getInputDevice(deviceId);
                        JSONObject deviceJson = new JSONObject();
                        if (device != null) {
                            try {
                                deviceJson.put("device_bus", ReflectionHelper.invokeHiddenMethod(device, "getDeviceBus"));
                                int associatedDisplayId = ReflectionHelper.invokeHiddenMethod(device, "getAssociatedDisplayId");
                                deviceJson.put("associatedDisplayId", associatedDisplayId);
                            } catch (RuntimeException e) {
                                L.d("Illegal access to getDeviceBus: " + e.getMessage());
                            }
                            try {
                                int associatedDisplayId = ReflectionHelper.invokeHiddenMethod(device, "getAssociatedDisplayId");
                                deviceJson.put("associatedDisplayId", associatedDisplayId);
                            } catch (RuntimeException e) {
                                L.d("Illegal access to associatedDisplayId: " + e.getMessage());
                            }
                            // 添加从 getevent -i 获取的详细信息
                            String deviceName = device.getName();
                            Map<String, String> details = detailedDeviceInfo.get(deviceName);
                            if (details != null) {
                                // 将获取到的详细信息添加到 JSON 对象
                                deviceJson.put("detailed_bus", details.get("bus"));
                                deviceJson.put("detailed_vendor", details.get("vendor"));
                                deviceJson.put("detailed_product", details.get("product"));
                                deviceJson.put("detailed_version", details.get("version"));
                                deviceJson.put("detailed_location", details.get("location"));
                                deviceJson.put("detailed_id", details.get("id"));
                                deviceJson.put("detailed_fw_version", details.get("fw_version"));
                                deviceJson.put("detailed_path", details.get("path"));
                            }

                            // 其他已有的信息
                            int controllerNumber = ReflectionHelper.invokeHiddenMethod(device, "getControllerNumber");
                            deviceJson.put("controllerNumber", controllerNumber);

                            // 添加运动范围信息...
                            int generation = ReflectionHelper.invokeHiddenMethod(device, "getGeneration");
                            deviceJson.put("generation", generation);
                            deviceJson.put("name", device.getName());
                            deviceJson.put("id", device.getId());
                            deviceJson.put("vendorId", device.getVendorId());
                            deviceJson.put("productId", device.getProductId());
                            deviceJson.put("sources", device.getSources());
                            deviceJson.put("keyboardType", device.getKeyboardType());
                            deviceJson.put("isVirtual", device.isVirtual());
                            deviceJson.put("descriptor", device.getDescriptor());
                            jsonArray.put(deviceJson);
                        }
                    }
                    jsonObject.put("devices", jsonArray);
                } catch (JSONException e) {
                    L.e("JSON Exception: " + e.getMessage());
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
        }
        return null;
    }
}
