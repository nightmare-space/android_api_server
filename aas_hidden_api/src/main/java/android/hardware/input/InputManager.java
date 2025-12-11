package android.hardware.input;

import android.view.InputDevice;

public class InputManager {
    static InputManager getInstance() {
        throw new RuntimeException("STUB");
    }

    public void addUniqueIdAssociationByDescriptor(
            String inputDeviceDescriptor,
            String displayUniqueId
    ) {
        throw new RuntimeException("STUB");
    }

    public void removeUniqueIdAssociationByDescriptor(String inputDeviceDescriptor) {
        throw new RuntimeException("STUB");
    }

    public InputDevice getInputDevice(int id) {
        throw new RuntimeException("STUB");
    }

    public int[] getInputDeviceIds() {
        throw new RuntimeException("STUB");
    }
}
