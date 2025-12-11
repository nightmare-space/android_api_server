package android.view;

import android.os.Looper;

public abstract class InputEventReceiver {
    public InputEventReceiver(InputChannel inputChannel, Looper looper) {
        throw new RuntimeException("STUB");
    }

    public void onInputEvent(InputEvent event) {
        throw new RuntimeException("STUB");
    }


    /**
     * Finishes an input event and indicates whether it was handled.
     * Must be called on the same Looper thread to which the receiver is attached.
     *
     * @param event   The input event that was finished.
     * @param handled True if the event was handled.
     */
    public final void finishInputEvent(InputEvent event, boolean handled) {
        throw new RuntimeException("STUB");
    }
}