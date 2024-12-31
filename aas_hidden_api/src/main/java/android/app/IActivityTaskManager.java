package android.app;

import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.window.TaskSnapshot;

import java.util.List;

//import androidx.annotation.RequiresApi;

public interface IActivityTaskManager extends IInterface {
    //    @RequiresApi(Build.VERSION_CODES.S)
    public TaskSnapshot getTaskSnapshot(int taskId, boolean isLowResolution) throws RemoteException;

    //    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public TaskSnapshot getTaskSnapshot(int taskId, boolean what, boolean isLowResolution) throws RemoteException;

    public TaskSnapshot takeTaskSnapshot(int taskId) throws RemoteException;

    //    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public TaskSnapshot takeTaskSnapshot(int taskId, boolean isLowResolution) throws RemoteException;

    // TODO 搞懂这个token是啥
    public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException;


    void moveRootTaskToDisplay(int taskId, int displayId) throws RemoteException;

    abstract class Stub extends Binder implements IActivityTaskManager {

        public static IActivityTaskManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
