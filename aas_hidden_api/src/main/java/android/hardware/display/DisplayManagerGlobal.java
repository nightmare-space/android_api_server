package android.hardware.display;

import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.view.Display;


import java.util.concurrent.Executor;

public class DisplayManagerGlobal {
    public DisplayManagerGlobal(IDisplayManager dm) {
        throw new RuntimeException("STUB");
    }

    public static DisplayManagerGlobal getInstance() {
        throw new RuntimeException("STUB");
    }

    // Android 11(30)
    // https://cs.android.com/android/platform/superproject/+/android11-release:frameworks/base/core/java/android/hardware/display/DisplayManagerGlobal.java;drc=cf3ad873ddd7f1abf3804936befa511b2fbb12b6;l=477
    // Android 12 without L
    // https://cs.android.com/android/platform/superproject/+/android12-release:frameworks/base/core/java/android/hardware/display/DisplayManagerGlobal.java;drc=71b626759930963ff2a272dcc6ab732a874e1864;l=579
    public VirtualDisplay createVirtualDisplay(
            Context context,
            MediaProjection projection,
            VirtualDisplayConfig virtualDisplayConfig,
            VirtualDisplay.Callback callback,
            Handler handler
    ) {
        throw new RuntimeException("STUB");
    }

    // Android 12 with L(32)
    // https://cs.android.com/android/platform/superproject/+/android12L-release:frameworks/base/core/java/android/hardware/display/DisplayManagerGlobal.java;drc=065f4d876ea5d38eb230868ec92d28cd3501999b;l=584
    public VirtualDisplay createVirtualDisplay(
            Context context,
            MediaProjection projection,
            VirtualDisplayConfig virtualDisplayConfig,
            VirtualDisplay.Callback callback,
            Handler handler,
            Context windowContext
    ) {
        throw new RuntimeException("STUB");
    }


    // Android 13
    public VirtualDisplay createVirtualDisplay(
            Context context,
            MediaProjection projection,
            VirtualDisplayConfig virtualDisplayConfig,
            VirtualDisplay.Callback callback,
            Executor executor,
            Context windowContext
    ) {
        throw new RuntimeException("STUB");
    }

    // Above Android 13
    public VirtualDisplay createVirtualDisplay(
            Context context,
            MediaProjection projection,
            VirtualDisplayConfig virtualDisplayConfig,
            VirtualDisplay.Callback callback,
            Executor executor
    ) {
        throw new RuntimeException("STUB");
    }

    /**
     * formatter 和 pre 标签用于保留代码格式
     * 这个底层要判断当前显示器支不支持VRR，才会调用
     * <a href="https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/services/core/java/com/android/server/display/mode/DisplayModeDirector.java;drc=ee890b38c66c7e32c7c0fa2994c75b4a0a70b539;l=568">...</a>
     * `@formatter:off`
     * <pre>
     * public void requestDisplayModes(IBinder token, int displayId, int[] modeIds) {
     *     if (mSystemRequestObserver != null) {
     *         boolean vrrSupported;
     *         synchronized (mLock) {
     *             vrrSupported = isVrrSupportedLocked(displayId);
     *         }
     *         if (vrrSupported) {
     *             mSystemRequestObserver.requestDisplayModes(token, displayId, modeIds);
     *         }
     *     }
     * }
     * </pre>
     * `@formatter:on`
     */
    public void requestDisplayModes(int displayId, int[] modeIds) {
        throw new RuntimeException("STUB");
    }

    public void requestColorMode(int displayId, int colorMode) {
        throw new RuntimeException("STUB");
    }

    public void setShouldAlwaysRespectAppRequestedMode(boolean enabled) {
        throw new RuntimeException("STUB");
    }

    public boolean shouldAlwaysRespectAppRequestedMode() {
        throw new RuntimeException("STUB");
    }

    public void setRefreshRateSwitchingType(int newValue) {
        throw new RuntimeException("STUB");
    }

    public int getRefreshRateSwitchingType() {
        throw new RuntimeException("STUB");
    }

    public int[] getDisplayIds() {
        throw new RuntimeException("STUB");
    }

    public Display getRealDisplay(int displayId) {
        throw new RuntimeException("STUB");
    }

    public void setUserPreferredDisplayMode(int displayId, Display.Mode mode) {
        throw new RuntimeException("STUB");
    }
}