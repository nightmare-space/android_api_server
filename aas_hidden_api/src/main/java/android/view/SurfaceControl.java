package android.view;

import android.os.IBinder;
import android.os.Parcelable;

public class SurfaceControl {
    public static void setBootDisplayMode(IBinder displayToken, int displayModeId) {
        throw new RuntimeException("Stub!");
    }

    public static final class RefreshRateRange {
        public static final String TAG = "RefreshRateRange";

        // The tolerance within which we consider something approximately equals.
        public static final float FLOAT_TOLERANCE = 0.01f;

        /**
         * The lowest desired refresh rate.
         */
        public float min;

        /**
         * The highest desired refresh rate.
         */
        public float max;

        public RefreshRateRange() {
        }

        public RefreshRateRange(float min, float max) {
        }

    }

    public static final class RefreshRateRanges {
        public static final String TAG = "RefreshRateRanges";

        /**
         * The range of refresh rates that the display should run at.
         */
        public final RefreshRateRange physical;

        /**
         * The range of refresh rates that apps should render at.
         */
        public final RefreshRateRange render;

        public RefreshRateRanges() {
            physical = new RefreshRateRange();
            render = new RefreshRateRange();
        }

        public RefreshRateRanges(RefreshRateRange physical, RefreshRateRange render) {
            this.physical = new RefreshRateRange(physical.min, physical.max);
            this.render = new RefreshRateRange(render.min, render.max);
        }
    }
}
