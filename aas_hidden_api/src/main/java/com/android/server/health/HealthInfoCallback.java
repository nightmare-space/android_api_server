package com.android.server.health;

public interface HealthInfoCallback {
    /**
     * Signals to the client that health info is changed.
     *
     * @param props the new health info.
     */
    void update(android.hardware.health.HealthInfo props);
}