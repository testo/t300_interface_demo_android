package de.testo.demo.t300interface.device;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class TestoDevice {
    private static final String TAG = TestoDevice.class.getSimpleName();

    public static final String TYPE_BLE = "BLE";
    public static final String TYPE_HTTP = "HTTP";

    protected String mDeviceName = "";
    protected String mFirmwareVersion = "";
    protected String mIpv4Address = "";
    protected String mIpv6Address = "";
    protected String mHttpServerPort = "";
    protected String mSerialNumber = "";
    protected String mType = "";
    protected BluetoothDevice bleDevice = null;

    protected TestoDeviceCallback deviceCallback;

    protected String mTitle;

    protected TestoDevice() {
    }

    protected ArrayList<ArrayList<HashMap<String, String>>> mAllMeasurementLists = new ArrayList<>();
    protected ArrayList<String> mAllTitles = new ArrayList<>();

    abstract public void connect(Context context);
    abstract public void fetchData();

    abstract public void toggleMeasurement();

    public TestoDeviceCallback getDeviceCallback() {
        return deviceCallback;
    }

    public void setDeviceCallback(TestoDeviceCallback deviceCallback) {
        this.deviceCallback = deviceCallback;
    }

    public int getMeasurementLength(){return mAllMeasurementLists.size();}

    public String getMeasurementTitle(int measurementNr) {
        if(measurementNr == -1){
            return mTitle;
        }
        else{
            return mAllTitles.get(measurementNr);
        }
    }

    public List<HashMap<String, String>> getMeasurementList(int measurementNr) {
        if(measurementNr == -1){
            return mAllMeasurementLists.get(0);
        }
        else{
            return mAllMeasurementLists.get(measurementNr);
        }
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    public String getIpv4Address() {
        return mIpv4Address;
    }

    public String getIpv6Address() {
        return mIpv6Address;
    }

    public String getHttpServerPort() {
        return mHttpServerPort;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    public String getType() {
        return mType;
    }

    @Override
    public boolean equals(Object object) {
        boolean same = false;
        if (object != null && object instanceof TestoDevice) {
            TestoDevice other = (TestoDevice) object;
            same = this.mDeviceName.equals(other.mDeviceName)
                    && this.mFirmwareVersion.equals(other.mFirmwareVersion)
                    && this.mIpv4Address.equals(other.mIpv4Address)
                    && this.mIpv6Address.equals(other.mIpv6Address)
                    && this.mHttpServerPort.equals(other.mHttpServerPort)
                    && this.mSerialNumber.equals(other.mSerialNumber);
        }
        return same;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mDeviceName, mFirmwareVersion, mIpv4Address, mIpv6Address, mHttpServerPort, mSerialNumber);
    }

}
