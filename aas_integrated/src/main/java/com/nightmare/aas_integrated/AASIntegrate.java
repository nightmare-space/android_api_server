package com.nightmare.aas_integrated;

import android.content.Context;
import android.os.Looper;

import com.nightmare.aas.AndroidAPIServer;
import com.nightmare.aas.helper.L;
import com.nightmare.aas_plugins.ActivityManagerPlugin;
import com.nightmare.aas_plugins.ActivityTaskManagerPlugin;
import com.nightmare.aas_plugins.CodecPlugin;
import com.nightmare.aas_plugins.DeviceInfoPlugin;
import com.nightmare.aas_plugins.DisplayManagerPlugin;
import com.nightmare.aas_plugins.FilePlugin;
import com.nightmare.aas_plugins.InputManagerPlugin;
import com.nightmare.aas_plugins.NotificationPlugin;
import com.nightmare.aas_plugins.PackageManagerPlugin;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class AASIntegrate {
    private static final PrintStream CONSOLE_OUT = new PrintStream(new FileOutputStream(FileDescriptor.out));
    private static final PrintStream CONSOLE_ERR = new PrintStream(new FileOutputStream(FileDescriptor.err));

    public static void main(String... args) {
        int status = 0;
        try {
            internalMain(args);
        } catch (Throwable throwable) {
            L.e("Uncaught exception: " + throwable.getMessage());
            StackTraceElement[] stacks = throwable.getStackTrace();
            for (StackTraceElement stack : stacks) {
                L.e("\tat " + stack.toString());
            }
            // throwable.printStackTrace(CONSOLE_ERR);
            status = 1;
        } finally {
            // By default, the Java process exits when all non-daemon threads are terminated.
            // The Android SDK might start some non-daemon threads internally, preventing the scrcpy server to exit.
            // So force the process to exit explicitly.
            System.exit(status);
        }
    }

    static public void internalMain(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            L.e("Exception on thread: " + t);
            L.e("Message: " + e.getMessage());
            StackTraceElement[] stacks = e.getStackTrace();
            for (StackTraceElement stack : stacks) {
                L.e("\tat " + stack.toString());
            }
        });
        int status = 0;
        try {
            AndroidAPIServer server = new AndroidAPIServer();
            server.startServerForShell(args);
            registerRoutes(server);
            L.d("success start port -> " + server.serverHTTPD.getListeningPort() + ".");
            Looper.loop();
        } catch (Throwable t) {
            L.e(t.getMessage());
            status = 1;
        } finally {
            // By default, the Java process exits when all non-daemon threads are terminated.
            // The Android SDK might start some non-daemon threads internally, preventing the scrcpy server to exit.
            // So force the process to exit explicitly.
            System.exit(status);
        }
    }

    public static int startServerFromActivity(Context context) {
        AndroidAPIServer server = new AndroidAPIServer();
        int port = server.startServerFromActivity(context);
        registerRoutes(server);
        return port;
    }

    private static void registerRoutes(AndroidAPIServer server) {
        try {
            server.registerPlugin(new ActivityManagerPlugin());
            server.registerPlugin(new ActivityTaskManagerPlugin());
            server.registerPlugin(new CodecPlugin());
            server.registerPlugin(new DeviceInfoPlugin());
            server.registerPlugin(new DisplayManagerPlugin());
            server.registerPlugin(new FilePlugin());
            server.registerPlugin(new InputManagerPlugin());
            server.registerPlugin(new NotificationPlugin());
            server.registerPlugin(new PackageManagerPlugin());
        } catch (NoClassDefFoundError e) {
            // some android versions may not have these classes
            // noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
