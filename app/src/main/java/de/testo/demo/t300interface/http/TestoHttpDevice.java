package de.testo.demo.t300interface.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.testo.demo.t300interface.json.JsonProcessor;
import de.testo.demo.t300interface.device.TestoDevice;
import de.testo.demo.t300interface.util.URLBuilder;

public class TestoHttpDevice extends TestoDevice {
    private static final String TAG = TestoHttpDevice.class.getSimpleName();

    /**
     * Creates a new instance from a given JSON as it would be received from the UDP broadcast
     *
     * @param json a json object
     * @return a new instance of TestoDevice which was derived from the given json
     */
    public static TestoDevice fromJson(String json) {
        try {
            JSONObject data = new JSONObject(json);
            return new TestoHttpDevice(
                    data.getString("DeviceName"),
                    data.getString("FirmwareVersion"),
                    data.getString("HttpServerIPv4"),
                    data.getString("HttpServerIPv6"),
                    data.getString("HttpServerPort"),
                    data.getString("SerialNumber")
                    );
        } catch (JSONException e) {
            Log.e(TAG, "Could not deserialize JSON");
        }
        return null;
    }

    private TestoHttpDevice(String deviceName, String firmwareVersion, String ipv4Address, String ipv6Address, String httpServerPort, String serialNumber) {
        this.mDeviceName = deviceName;
        this.mFirmwareVersion = firmwareVersion;
        this.mIpv4Address = ipv4Address;
        this.mIpv6Address = ipv6Address;
        this.mHttpServerPort = httpServerPort;
        this.mSerialNumber = serialNumber;
        this.mType = TYPE_HTTP;
    }

    @Override
    public void connect(Context context) {
        Log.d(TAG, "HTTP device does not have to be connected explicitly");
    }
    @SuppressLint("StaticFieldLeak")
    @Override
    public void fetchData() {
        URLBuilder.setActiveUrl(mIpv4Address, mHttpServerPort);
        HttpRequestTask httpRequestTask = new HttpRequestTask() {
            @Override
            protected void onPostExecute(String jsonInput) {
                if(jsonInput != null) {
                    JsonProcessor jsonProcessor = new JsonProcessor();
                    jsonProcessor.setJsonInput(jsonInput);
                    jsonProcessor.parseJson();
                    mAllMeasurementLists.clear();
                    mAllMeasurementLists.add(jsonProcessor.getMeasurementList());
                    mTitle = jsonProcessor.getTitle();
                }

                if(deviceCallback != null) {
                    deviceCallback.onDataFetchedFromDevice(TestoHttpDevice.this);
                }
            }
        };
        httpRequestTask.execute();
    }

    @Override
    public void toggleMeasurement() {
        Log.d(TAG, "NOT YET IMPLEMENTED");
    }
}
