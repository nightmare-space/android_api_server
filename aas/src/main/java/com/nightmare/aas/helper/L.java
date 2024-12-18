package com.nightmare.aas.helper;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


public class L {
    private static PrintStream fileOut;
    @SuppressLint("SdCardPath")
    static public String serverLogPath = "/sdcard/app_server_log";
    static String TAG = "applib";
    static public boolean enableTerminalLog = true;

    public static void d(Object object) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) 0x1b + "[38;5;42m·");
        sb.append((char) 0x1b + "[0m ");
        sb.append((char) 0x1b + "[38;5;38m");
        sb.append(object.toString());
        sb.append((char) 0x1b + "[0m");
        Log.d(TAG, object.toString());
        if(enableTerminalLog) {
            System.out.println(sb);
            System.out.flush();
        }
        initFileOutStream();
        fileOut.println(sb);
    }


    public static void e(Object object) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) 0x1b + "[38;5;42m·");
        sb.append((char) 0x1b + "[0m ");
        sb.append((char) 0x1b + "[38;5;196m");
        sb.append(object.toString());
        sb.append((char) 0x1b + "[0m");
        Log.d(TAG, object.toString());
        if(enableTerminalLog) {
            System.out.println(sb);
            System.out.flush();
        }
        initFileOutStream();
        fileOut.println(sb);
    }

    private static void initFileOutStream() {
        if (fileOut == null) {
            try {
                File logFile = new File(serverLogPath);
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                fileOut = new PrintStream(new FileOutputStream(logFile, false));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}