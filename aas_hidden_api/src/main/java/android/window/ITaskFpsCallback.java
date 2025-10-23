package android.window;

public interface ITaskFpsCallback {

    /**
     * Reports the fps from the registered task
     *
     * @param fps The frame rate per second of the task that has the registered task id
     *            and its children.
     */
    void onFpsReported(float fps);
}