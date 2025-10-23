package android.app;

import android.os.RemoteException;

import java.util.List;

public class ActivityTaskManager {

    public static ActivityTaskManager getInstance() {
        throw new RuntimeException("STUB");
    }


    public static IActivityTaskManager getService() {
        throw new RuntimeException("STUB");
    }

    public List<ActivityManager.RecentTaskInfo> getRecentTasks(
            int maxNum,
            int flags,
            int userId
    ) {
        throw new RuntimeException("STUB");
    }

    public void registerTaskStackListener(ITaskStackListener listener) {
        throw new RuntimeException("STUB");
    }

}
