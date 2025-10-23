package android.app;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.app.ActivityManager.RunningTaskInfo;
import android.window.TaskSnapshot;

public interface ITaskStackListener extends IInterface {
    void onTaskStackChanged() throws RemoteException;

    void onActivityPinned(String packageName, int userId, int taskId, int rootTaskId) throws RemoteException;

    public void onActivityUnpinned() throws RemoteException;

    public void onActivityRestartAttempt(
            ActivityManager.RunningTaskInfo task,
            boolean homeTaskVisible,
            boolean clearedTask,
            boolean wasVisible
    ) throws RemoteException;


    void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException;

    void onActivityDismissingDockedTask() throws RemoteException;

    void onActivityLaunchOnSecondaryDisplayFailed(
            RunningTaskInfo taskInfo,
            int requestedDisplayId
    ) throws RemoteException;


    void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException;


    void onActivityLaunchOnSecondaryDisplayRerouted(
            RunningTaskInfo taskInfo,
            int requestedDisplayId
    ) throws RemoteException;

    void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException;

    void onTaskRemoved(int taskId) throws RemoteException;

    void onTaskMovedToFront(RunningTaskInfo taskInfo) throws RemoteException;

    void onTaskMovedToFront(int taskId) throws RemoteException;

    void onTaskRemovalStarted(RunningTaskInfo taskInfo) throws RemoteException;

    void onTaskRemovalStarted(int taskId) throws RemoteException;

    void onTaskDescriptionChanged(RunningTaskInfo taskInfo)
            throws RemoteException;

    void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td)
            throws RemoteException;

    void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation)
            throws RemoteException;

    void onTaskProfileLocked(RunningTaskInfo taskInfo) throws RemoteException;

    void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot) throws RemoteException;

    void onBackPressedOnTaskRoot(RunningTaskInfo taskInfo)
            throws RemoteException;

    void onTaskDisplayChanged(int taskId, int newDisplayId) throws RemoteException;

    void onRecentTaskListUpdated() throws RemoteException;

    void onRecentTaskListFrozenChanged(boolean frozen);

    void onTaskFocusChanged(int taskId, boolean focused);

    void onTaskRequestedOrientationChanged(int taskId, int requestedOrientation);

    void onActivityRotation(int displayId);

    void onTaskMovedToBack(RunningTaskInfo taskInfo);

    void onLockTaskModeChanged(int mode);

    abstract class Stub extends Binder implements ITaskStackListener {
        public static ITaskStackListener asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}