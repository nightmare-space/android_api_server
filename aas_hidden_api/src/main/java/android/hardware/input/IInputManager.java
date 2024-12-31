package android.hardware.input;

import android.os.Binder;
import android.os.IBinder;

public interface IInputManager {

    abstract class Stub extends Binder implements IInputManager {

        public static IInputManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}