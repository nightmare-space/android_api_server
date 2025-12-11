package android.content.pm;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * Referenced by {@link android.app.IActivityTaskManager#getRecentTasks}
 */
@SuppressLint("ParcelCreator")
public class ParceledListSlice<T extends Parcelable> extends BaseParceledListSlice<T> {
    @Override
    public int describeContents() {
        throw new RuntimeException("STUB");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("STUB");
    }
}