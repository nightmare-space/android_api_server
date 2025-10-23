package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.window.ITaskFpsCallback;

public interface IWindowManager extends IInterface {

    /**
     * Registers the frame rate per second count callback for one given task ID.
     * Each callback can only register for receiving FPS callback for one task id until unregister
     * is called. If there's no task associated with the given task id,
     * {@link IllegalArgumentException} will be thrown. If a task id destroyed after a callback is
     * registered, the registered callback will not be unregistered until
     * {@link unregisterTaskFpsCallback()} is called
     *
     * @param taskId   task id of the task.
     * @param callback callback to be registered.
     * @hide
     */
    void registerTaskFpsCallback(int taskId, ITaskFpsCallback callback);


    /**
     * Watch the rotation of the specified screen.  Returns the current rotation,
     * calls back when it changes.
     */
    int watchRotation(IRotationWatcher watcher, int displayId);


    public void setWindowingMode(int displayId, int mode);

    void setDisplayImePolicy(int displayId, int imePolicy);

    int getDisplayImePolicy(int displayId);

    abstract class Stub extends Binder implements IWindowManager {

        public static IWindowManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
