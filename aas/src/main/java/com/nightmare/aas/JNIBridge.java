package com.nightmare.aas;

import com.nightmare.aas.helper.L;

import java.util.Random;

public class JNIBridge {
    static {
        //        L.d("" + System.getProperty("java.library.path"));
        System.loadLibrary("aas");
    }

    public static native long sumSquaresNative(int n);


    public static native long fibNative(int n);


    public static void main(String[] args) {
        int n = 10_000_000;

        // Java 计算
        long startJava = System.nanoTime();
        long sumJava = sumSquaresJava(n);
        long endJava = System.nanoTime();
        System.out.println("Java 计算结果: " + sumJava + " | 耗时: " + (endJava - startJava) / 1e6 + " ms");

        // C 计算
        long startC = System.nanoTime();
        long sumC = sumSquaresNative(n);
        long endC = System.nanoTime();
        System.out.println("C 计算结果: " + sumC + " | 耗时: " + (endC - startC) / 1e6 + " ms");

    }

    public static long fibJava(int n) {
        if (n <= 1) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }

    public static void test() {
        int samples = 100_000_000;

        // Java 计算
        long startJava = System.nanoTime();
        double piJava = monteCarloJava(samples);
        long endJava = System.nanoTime();
        System.out.println("Java 计算 π: " + piJava + " | 耗时: " + (endJava - startJava) / 1e6 + " ms");

        // C 计算
        long startC = System.nanoTime();
        double piC = monteCarloNative(samples);
        long endC = System.nanoTime();
        System.out.println("C 计算 π: " + piC + " | 耗时: " + (endC - startC) / 1e6 + " ms");

    }

    public static void test1() {
        int n = 100;

        long startJava = System.nanoTime();
        long resultJava = fibJava(n);
        long endJava = System.nanoTime();
        System.out.println("Java 结果: " + resultJava + " | 耗时: " + (endJava - startJava) / 1e6 + " ms");

        long startC = System.nanoTime();
        long resultC = fibNative(n);
        long endC = System.nanoTime();
        System.out.println("C 结果: " + resultC + " | 耗时: " + (endC - startC) / 1e6 + " ms");
    }

    public static double monteCarloJava(int samples) {
        Random random = new Random();
        int insideCircle = 0;
        for (int i = 0; i < samples; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            if (x * x + y * y <= 1.0) {
                insideCircle++;
            }
        }
        return 4.0 * insideCircle / samples;
    }

    public static native double monteCarloNative(int samples);

    public static long sumSquaresJava(int n) {
        long sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += (long) i * i;
        }
        return sum;
    }

    public static native boolean setUid(int uid);

    // 打开虚拟鼠标（初始化 /dev/uinput）
    public static native int nativeOpen();

    // 移动鼠标
    public static native void nativeMove(int dx, int dy);

    // 左键点击
    public static native void nativeClickLeft();

    // 关闭并销毁虚拟鼠标设备
    public static native void nativeClose();

    public static native int nativeMoveMouse(int dx, int dy);
}
