package de.testo.demo.t300interface;

import de.testo.demo.t300interface.device.TestoDevice;

public class DeviceHandler {
    private static TestoDevice activeDevice = null;

    public static TestoDevice getActiveDevice() {
        return activeDevice;
    }

    public static void setActiveDevice(TestoDevice activeDevice) {
        DeviceHandler.activeDevice = activeDevice;
    }
}
