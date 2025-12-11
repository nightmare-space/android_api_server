package android.app;

import android.content.ComponentName;
import android.os.RemoteException;
import android.app.ActivityManager.RunningTaskInfo;
import android.window.TaskSnapshot;

/**
 * Copy from <a href="https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/core/java/android/app/TaskStackListener.java?q=TaskStackListener">...</a>
 */
public abstract class TaskStackListener extends ITaskStackListener.Stub {

    /** Whether this listener and the callback dispatcher are in different processes. */
    private boolean mIsRemote = true;

    public TaskStackListener() {
    }

    /** Indicates that this listener lives in system server. */
    public void setIsLocal() {
        mIsRemote = false;
    }

    @Override
    public void onTaskStackChanged() throws RemoteException {
    }

    @Override
    public void onActivityPinned(String packageName, int userId, int taskId, int rootTaskId)
            throws RemoteException {
    }

    @Override
    public void onActivityUnpinned() throws RemoteException {
    }

    @Override
    public void onActivityRestartAttempt(RunningTaskInfo task, boolean homeTaskVisible,
                                         boolean clearedTask, boolean wasVisible) throws RemoteException {
    }

    @Override
    public void onActivityForcedResizable(String packageName, int taskId, int reason)
            throws RemoteException {
    }

    @Override
    public void onActivityDismissingDockedTask() throws RemoteException {
    }

    @Override
    public void onActivityLaunchOnSecondaryDisplayFailed(RunningTaskInfo taskInfo,
                                                         int requestedDisplayId) throws RemoteException {
        onActivityLaunchOnSecondaryDisplayFailed();
    }

    /**
     * @deprecated see {@link
     *         #onActivityLaunchOnSecondaryDisplayFailed(RunningTaskInfo, int)}
     */
    @Deprecated
    public void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException {
    }

    @Override
    public void onActivityLaunchOnSecondaryDisplayRerouted(RunningTaskInfo taskInfo,
                                                           int requestedDisplayId) throws RemoteException {
    }

    @Override
    public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
    }

    @Override
    public void onTaskRemoved(int taskId) throws RemoteException {
    }

    @Override
    public void onTaskMovedToFront(RunningTaskInfo taskInfo)
            throws RemoteException {
        onTaskMovedToFront(taskInfo.taskId);
    }

    /**
     * @deprecated see {@link #onTaskMovedToFront(RunningTaskInfo)}
     */
    @Deprecated
    public void onTaskMovedToFront(int taskId) throws RemoteException {
    }

    @Override
    public void onTaskRemovalStarted(RunningTaskInfo taskInfo)
            throws RemoteException {
        onTaskRemovalStarted(taskInfo.taskId);
    }

    /**
     * @deprecated see {@link #onTaskRemovalStarted(RunningTaskInfo)}
     */
    @Deprecated
    public void onTaskRemovalStarted(int taskId) throws RemoteException {
    }

    @Override
    public void onTaskDescriptionChanged(RunningTaskInfo taskInfo)
            throws RemoteException {
        onTaskDescriptionChanged(taskInfo.taskId, taskInfo.taskDescription);
    }

    /**
     * @deprecated see {@link #onTaskDescriptionChanged(RunningTaskInfo)}
     */
    @Deprecated
    public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td)
            throws RemoteException {
    }

    @Override
    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation)
            throws RemoteException {
    }

    // @Override
    // public void onTaskProfileLocked(RunningTaskInfo taskInfo, int userId)
    //         throws RemoteException {
    //     onTaskProfileLocked(taskInfo);
    // }

    /**
     * @deprecated see {link #onTaskProfileLocked(RunningTaskInfo, int)}
     */
    @Deprecated
    public void onTaskProfileLocked(RunningTaskInfo taskInfo)
            throws RemoteException {
    }

    /**
     * @deprecated Use {link android.window.TaskSnapshotManager.TaskSnapshotListener} to receive
     * callback.
     */
    @Deprecated
    @Override
    public void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot) throws RemoteException {
        // if (!mIsRemote || snapshot == null) {
        //     return;
        // }
        // if (com.android.window.flags.Flags.reduceTaskSnapshotMemoryUsage()) {
        //     snapshot.closeBuffer();
        // } else if (snapshot.getHardwareBuffer() != null) {
        //     // Preemptively clear any reference to the buffer
        //     snapshot.getHardwareBuffer().close();
        // }
    }

    /**
     * @deprecated Use {link android.window.SnapshotManager.TaskSnapshotListener} to receive
     * callback.
     */
    // @Deprecated
    // @Override
    // public void onTaskSnapshotInvalidated(int taskId) { }

    @Override
    public void onBackPressedOnTaskRoot(RunningTaskInfo taskInfo)
            throws RemoteException {
    }

    @Override
    public void onTaskDisplayChanged(int taskId, int newDisplayId) throws RemoteException {
    }

    @Override
    public void onRecentTaskListUpdated() throws RemoteException {
    }

    @Override
    public void onRecentTaskListFrozenChanged(boolean frozen) {
    }

    // @Override
    // public void onRecentTaskRemovedForAddTask(int taskId) {
    // }

    @Override
    public void onTaskFocusChanged(int taskId, boolean focused) {
    }

    @Override
    public void onTaskRequestedOrientationChanged(int taskId, int requestedOrientation) {
    }

    @Override
    public void onActivityRotation(int displayId) {
    }

    @Override
    public void onTaskMovedToBack(RunningTaskInfo taskInfo) {
    }

    @Override
    public void onLockTaskModeChanged(int mode) {
    }
}