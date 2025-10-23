package android.app;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.window.TaskSnapshot;


public interface IActivityTaskManager extends IInterface {
    TaskSnapshot getTaskSnapshot(int taskId, boolean isLowResolution) throws RemoteException;

    TaskSnapshot getTaskSnapshot(int taskId, boolean what, boolean isLowResolution) throws RemoteException;

    TaskSnapshot takeTaskSnapshot(int taskId) throws RemoteException;

    //    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    TaskSnapshot takeTaskSnapshot(int taskId, boolean isLowResolution) throws RemoteException;

    // TODO 搞懂这个token是啥
    boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException;

    void registerTaskStackListener(ITaskStackListener listener) throws RemoteException;

    void unregisterTaskStackListener(ITaskStackListener listener) throws RemoteException;

    void moveRootTaskToDisplay(int taskId, int displayId) throws RemoteException;

    void setFocusedTask(int taskId) throws RemoteException;

    // setFocusedRootTask
    void setFocusedRootTask(int arg0) throws RemoteException;

    void focusTopTask(int displayId) throws RemoteException;

    void resizeTask(int taskId, Rect bounds, int resizeMode) throws RemoteException;

    void moveTaskToRootTask(int taskId, int rootTaskId, boolean toTop) throws RemoteException;

    abstract class Stub extends Binder implements IActivityTaskManager {
        public static IActivityTaskManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
