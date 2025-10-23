package android.app;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.List;

/** @noinspection UnusedReturnValue*/
public interface IActivityManager extends IInterface {

    void forceStopPackage(String packageName, int userId) throws RemoteException;

    //
    int startActivityAsUser(
            IApplicationThread caller,
            String callingPackage,
            Intent intent,
            String resolvedType,
            IBinder resultTo,
            String resultWho,
            int requestCode,
            int flags,
            ProfilerInfo profilerInfo,
            Bundle options,
            int userId
    ) throws RemoteException;

    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException;

    List<ApplicationInfo> getRunningExternalApplications() throws RemoteException;

    boolean removeTask(int taskId) throws RemoteException;

    ApplicationStartInfo getHistoricalProcessStartReasons(String packageName, int maxNum, int userId) throws RemoteException;


    abstract class Stub extends Binder {
        public static IActivityManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
