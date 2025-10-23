package android.app;

import android.content.ComponentName;
import android.os.RemoteException;
import android.app.ActivityManager.RunningTaskInfo;
import android.window.TaskSnapshot;

public abstract class TaskStackListener extends ITaskStackListener.Stub {

    // @UnsupportedAppUsage
    public TaskStackListener() {
    }


    @Override
    public void onTaskStackChanged() throws RemoteException {
    }

    @Override
    public void onActivityPinned(String packageName, int userId, int taskId, int rootTaskId) throws RemoteException {
    }

    @Override
    public void onActivityUnpinned() throws RemoteException {
    }

    @Override
    public void onActivityRestartAttempt(
            ActivityManager.RunningTaskInfo task,
            boolean homeTaskVisible,
            boolean clearedTask,
            boolean wasVisible
    ) {
    }

    @Override
    public void onActivityForcedResizable(String packageName, int taskId, int reason) {
    }

    @Override
    public void onActivityDismissingDockedTask() {
    }

    @Override
    public void onActivityLaunchOnSecondaryDisplayFailed(
            ActivityManager.RunningTaskInfo taskInfo,
            int requestedDisplayId
    ) {
        onActivityLaunchOnSecondaryDisplayFailed();
    }

    @Override
    public void onActivityLaunchOnSecondaryDisplayFailed() {
    }

    @Override
    public void onActivityLaunchOnSecondaryDisplayRerouted(
            ActivityManager.RunningTaskInfo taskInfo,
            int requestedDisplayId
    ) {
    }

    @Override
    public void onTaskCreated(int taskId, ComponentName componentName) {
    }

    @Override
    public void onTaskRemoved(int taskId) {
    }

    @Override
    public void onTaskMovedToFront(RunningTaskInfo taskInfo) {
    }

    @Deprecated
    public void onTaskMovedToFront(int taskId) {
    }

    @Override
    public void onTaskRemovalStarted(RunningTaskInfo taskInfo) {
    }

    /**
     * @deprecated see {@link #onTaskRemovalStarted(RunningTaskInfo)}
     */
    @Deprecated
    public void onTaskRemovalStarted(int taskId) {
    }

    @Override
    public void onTaskDescriptionChanged(RunningTaskInfo taskInfo)
            throws RemoteException {
    }

    /**
     * @deprecated see {@link #onTaskDescriptionChanged(RunningTaskInfo)}
     */
    @Deprecated
    public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td) {
    }

    @Override
    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) {
    }

    @Override
    public void onTaskProfileLocked(RunningTaskInfo taskInfo) {
    }

    @Override
    public void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot) {
    }

    @Override
    public void onBackPressedOnTaskRoot(RunningTaskInfo taskInfo) {
    }

    @Override
    public void onTaskDisplayChanged(int taskId, int newDisplayId) {
    }

    @Override
    public void onRecentTaskListUpdated() {
    }

    @Override
    public void onRecentTaskListFrozenChanged(boolean frozen) {
    }

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