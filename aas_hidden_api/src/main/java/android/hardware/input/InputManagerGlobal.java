package android.hardware.input;

import android.view.InputDevice;
import android.view.InputEvent;

public class InputManagerGlobal {
    public InputManagerGlobal(IInputManager im) {
        throw new RuntimeException("STUB");
    }

    /**
     * Gets an instance of the input manager global singleton.
     *
     * @return The input manager instance, may be null early in system startup
     * before the input manager has been fully initialized.
     */
    public static InputManagerGlobal getInstance() {
        throw new RuntimeException("STUB");
    }

    public IInputManager getInputManagerService() {
        throw new RuntimeException("STUB");
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        throw new RuntimeException("STUB");
    }

    public int[] getInputDeviceIds() {
        throw new RuntimeException("STUB");
    }

    public InputDevice getInputDevice(int id) {
        throw new RuntimeException("STUB");
    }

    public void addUniqueIdAssociationByPort(String inputPort,
                                             String displayUniqueId) {
        throw new RuntimeException("STUB");
    }

    public void removeUniqueIdAssociationByPort(String inputPort) {
        throw new RuntimeException("STUB");
    }

    public void addUniqueIdAssociationByDescriptor(String inputDeviceDescriptor,
                                                   String displayUniqueId) {
        throw new RuntimeException("STUB");
    }

    public void removeUniqueIdAssociationByDescriptor(String inputDeviceDescriptor) {
        throw new RuntimeException("STUB");
    }
}
