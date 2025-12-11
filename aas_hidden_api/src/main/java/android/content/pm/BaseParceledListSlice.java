package android.content.pm;

import android.os.Parcelable;

import java.util.List;
/**
 * Referenced by {@link android.app.IActivityTaskManager#getRecentTasks}
 */
abstract class BaseParceledListSlice<T> implements Parcelable {
    public List<T> getList() {
        throw new RuntimeException("STUB");
    }
}