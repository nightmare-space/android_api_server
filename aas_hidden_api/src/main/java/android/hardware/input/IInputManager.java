package android.hardware.input;

import android.os.Binder;
import android.os.IBinder;
import android.view.InputEvent;

public interface IInputManager {

    void addUniqueIdAssociationByDescriptor(
            String inputDeviceDescriptor,
            String displayUniqueId
    );

    void removeUniqueIdAssociationByDescriptor(String inputDeviceDescriptor);

    abstract class Stub extends Binder implements IInputManager {
        public static IInputManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}