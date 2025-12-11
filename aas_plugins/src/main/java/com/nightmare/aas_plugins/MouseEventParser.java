package com.nightmare.aas_plugins;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MouseEventParser {

    private static final String TAG = "MouseEventParser";

    // Linux input event constants
    // EV_MSC
    public static final int EV_MSC = 0x04;
    public static final int EV_REL = 0x02;    // 相对坐标事件
    public static final int EV_ABS = 0x03;    // 绝对坐标事件
    public static final int EV_KEY = 0x01;    // 按键事件
    public static final int EV_SYN = 0x00;    // 同步事件

    // 相对坐标轴
    public static final int REL_X = 0x00;     // X轴移动
    public static final int REL_Y = 0x01;     // Y轴移动
    public static final int REL_WHEEL = 0x08; // 滚轮

    // 鼠标按键
    public static final int BTN_LEFT = 0x110;   // 左键
    public static final int BTN_RIGHT = 0x111;  // 右键
    public static final int BTN_MIDDLE = 0x112; // 中键

    // BTN_SIDE
    public static final int BTN_SIDE = 0x113; // 侧键
    // BTN_EXTRA
    public static final int BTN_EXTRA = 0x114; // 额外键

    // MSC_SCAN
    public static final int MSC_SCAN = 0x04;

    private static final int INPUT_EVENT_SIZE = 24; // sizeof(struct input_event) on 64-bit

    private FileDescriptor fd;
    private ByteBuffer buffer;

    public MouseEventParser(String devicePath) throws IOException {
        try {
            fd = Os.open(devicePath, OsConstants.O_RDONLY, 0);
            // 这里不再使用反射获取描述符
            buffer = ByteBuffer.allocate(INPUT_EVENT_SIZE).order(ByteOrder.nativeOrder());
            Log.d(TAG, "Successfully opened device: " + devicePath);
        } catch (ErrnoException e) {
            throw new IOException("Failed to open device: " + devicePath, e);
        }
    }

    public MouseEvent readEvent() throws IOException {
        try {
            buffer.clear();
            int bytesRead = Os.read(fd, buffer);
            if (bytesRead < INPUT_EVENT_SIZE) {
                return null;
            }

            buffer.flip();

            // struct input_event {
            //     struct timeval time;  // 16 bytes on 64-bit
            //     __u16 type;          // 2 bytes
            //     __u16 code;          // 2 bytes
            //     __s32 value;         // 4 bytes
            // };

            long timeSeconds = buffer.getLong();
            long timeMicroseconds = buffer.getLong();
            int type = buffer.getShort() & 0xFFFF;
            int code = buffer.getShort() & 0xFFFF;
            int value = buffer.getInt();

            return new MouseEvent(timeSeconds, timeMicroseconds, type, code, value);

        } catch (ErrnoException e) {
            throw new IOException("Failed to read event", e);
        }
    }

    public void close() {
        if (fd != null) {
            try {
                Os.close(fd);
                Log.d(TAG, "Device closed");
            } catch (ErrnoException e) {
                Log.e(TAG, "Error closing device", e);
            }
        }
    }

    public static class MouseEvent {
        public final long timeSeconds;
        public final long timeMicroseconds;
        public final int type;
        public final int code;
        public final int value;

        public MouseEvent(long timeSeconds, long timeMicroseconds, int type, int code, int value) {
            this.timeSeconds = timeSeconds;
            this.timeMicroseconds = timeMicroseconds;
            this.type = type;
            this.code = code;
            this.value = value;
        }

        @Override
        public String toString() {
            String eventType = "";
            switch (type) {
                case EV_MSC:
                    if (code == MSC_SCAN) eventType = "MSC_SCAN: " + value;
                    else eventType = "MSC_" + code + ": " + value;
                    break;
                case EV_REL:
                    if (code == REL_X) eventType = "Mouse X: " + value;
                    else if (code == REL_Y) eventType = "Mouse Y: " + value;
                    else if (code == REL_WHEEL) eventType = "Scroll: " + value;
                    else eventType = "REL_" + code + ": " + value;
                    break;
                case EV_KEY:
                    String button = "";
                    if (code == BTN_LEFT) button = "LEFT";
                    else if (code == BTN_RIGHT) button = "RIGHT";
                    else if (code == BTN_MIDDLE) button = "MIDDLE";
                    else if (code == BTN_SIDE) button = "SIDE";
                    else if (code == BTN_EXTRA) button = "EXTRA";
                    else button = "BTN_" + code;
                    eventType = button + " " + (value == 1 ? "PRESS" : "RELEASE");
                    break;
                case EV_SYN:
                    eventType = "SYNC";
                    break;
                default:
                    eventType = "TYPE_" + type + "_CODE_" + code + ": " + value;
            }
            return String.format("MouseEvent{time=%d.%06d, %s}", timeSeconds, timeMicroseconds, eventType);
        }
    }
}