IActivityManager setFocusedRootTask

试试switch user

moveActivityTaskToBack 是不是能将activity暂停

ActivityManager 这个也是调用的 IActivityManager
    @RequiresPermission(android.Manifest.permission.REORDER_TASKS)
    public void moveTaskToFront(int taskId, @MoveTaskFlags int flags, Bundle options) {
        try {
            ActivityThread thread = ActivityThread.currentActivityThread();
            IApplicationThread appThread = thread.getApplicationThread();
            String packageName = mContext.getOpPackageName();
            getTaskService().moveTaskToFront(appThread, packageName, taskId, flags, options);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }


    试试 
WindowManagerGlobal.java
 的 addview



 IActivityTaskManager setFocusedTask


     @Override
    public void setFocusedTask(int taskId) {
        enforceTaskPermission("setFocusedTask()");
        final long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (mGlobalLock) {
                setFocusedTask(taskId, null /* touchedActivity */);
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    @Override
    public void focusTopTask(int displayId) {
        enforceTaskPermission("focusTopTask()");
        final long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (mGlobalLock) {
                final DisplayContent dc = mRootWindowContainer.getDisplayContent(displayId);
                if (dc == null) return;
                final Task task = dc.getTask((t) -> t.isLeafTask() && t.isTopActivityFocusable(),
                        true /*  traverseTopToBottom */);
                if (task == null) return;
                setFocusedTask(task.mTaskId, null /* touchedActivity */);
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }
