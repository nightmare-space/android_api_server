package android.hardware.display;

import android.view.Surface;

public final class VirtualDisplayConfig {
    public static final class Builder {
        public Builder(
                String name,
                int width,
                int height,
                int densityDpi
        ) {
            throw new RuntimeException("STUB");
        }

        public Builder setFlags(int flags) {
            throw new RuntimeException("STUB");
        }

        public Builder setSurface(Surface surface) {
            throw new RuntimeException("STUB");
        }

        public Builder setDisplayIdToMirror(int displayIdToMirror) {
            throw new RuntimeException("STUB");
        }

        public VirtualDisplayConfig build() {
            throw new RuntimeException("STUB");
        }
    }
}