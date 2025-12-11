package android.view;

import android.os.Parcel;
import android.os.Parcelable;

public final class InputChannel implements Parcelable {
    protected InputChannel(Parcel in) {
    }

    public static final Creator<InputChannel> CREATOR = new Creator<InputChannel>() {
        @Override
        public InputChannel createFromParcel(Parcel in) {
            return new InputChannel(in);
        }

        @Override
        public InputChannel[] newArray(int size) {
            return new InputChannel[size];
        }
    };

    @Override
    public int describeContents() {
        throw new RuntimeException("STUB");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("STUB");
    }

    /**
     * Creates a new input channel pair.  One channel should be provided to the input
     * dispatcher and the other to the application's input queue.
     *
     * @param name The descriptive (non-unique) name of the channel pair.
     * @return A pair of input channels.  The first channel is designated as the
     * server channel and should be used to publish input events.  The second channel
     * is designated as the client channel and should be used to consume input events.
     */
    public static InputChannel[] openInputChannelPair(String name) {
        throw new RuntimeException("STUB");
    }
}