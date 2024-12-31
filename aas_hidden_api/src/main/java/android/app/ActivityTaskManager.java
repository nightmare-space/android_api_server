package android.app;

import java.util.List;

public class ActivityTaskManager {

    /**
     * @hide
     */
    public static ActivityTaskManager getInstance() {
        throw new RuntimeException("STUB");
    }


    /**
     * @hide
     */
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

}
