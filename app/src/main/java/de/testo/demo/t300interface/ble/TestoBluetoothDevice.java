package de.testo.demo.t300interface.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import de.testo.demo.t300interface.json.JsonProcessor;
import de.testo.demo.t300interface.device.TestoDevice;
@SuppressLint("MissingPermission")
public class TestoBluetoothDevice extends TestoDevice {
    private static final String TAG = TestoBluetoothDevice.class.getSimpleName();

    private BluetoothGatt bluetoothGatt = null;
    private BluetoothGattService measDataService = null;

    private List<UUID> mJsonDataCharacteristics = new ArrayList<>(
            Arrays.asList(
                    TestoGattDefinitions.UUID_JSON_MEASDATA01_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA02_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA03_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA04_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA05_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA06_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA07_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA08_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA09_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA10_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA11_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA12_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA13_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA14_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA15_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA16_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA17_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA18_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA19_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA20_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA21_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA22_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA23_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA24_CHARACTERISTIC,
                    TestoGattDefinitions.UUID_JSON_MEASDATA25_CHARACTERISTIC
            )
    );

    private SparseArray<String> jsonResponseArray = new SparseArray<>();
    private int mActiveJsonCharacteristics = 0;

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.requestMtu(512);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            measDataService = gatt.getService(TestoGattDefinitions.UUID_MEASDATA_SERVICE);
            gatt.readCharacteristic(measDataService.getCharacteristic(TestoGattDefinitions.UUID_JSON_MEASDATA_META_CHARACTERISTIC));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(characteristic.getUuid().equals(TestoGattDefinitions.UUID_JSON_MEASDATA_META_CHARACTERISTIC)) {
                mActiveJsonCharacteristics = Integer.parseInt(characteristic.getStringValue(0));
                Log.d(TAG, "activeJsonCharacteristics: " + mActiveJsonCharacteristics);
                Log.d(TAG, "Reading characteristic: " + mJsonDataCharacteristics.get(0).toString());
                gatt.readCharacteristic(measDataService.getCharacteristic(mJsonDataCharacteristics.get(0)));
            }
            else if(mJsonDataCharacteristics.contains(characteristic.getUuid())) {
                jsonResponseArray.put(mJsonDataCharacteristics.indexOf(characteristic.getUuid()), new String(characteristic.getValue(), StandardCharsets.UTF_8));
                Log.d(TAG, "Successful Read for characteristic " + characteristic.getUuid().toString());
                int nextIndex = mJsonDataCharacteristics.indexOf(characteristic.getUuid()) + 1;
                if(nextIndex < mActiveJsonCharacteristics) {
                    gatt.readCharacteristic(measDataService.getCharacteristic(mJsonDataCharacteristics.get(nextIndex)));
                    Log.d(TAG, "Reading characteristic: " + mJsonDataCharacteristics.get(nextIndex).toString());
                }
            }
            Log.d(TAG, jsonResponseArray.toString()+"jsonResponseArray");
            Log.d(TAG, String.valueOf(jsonResponseArray.size())+ String.valueOf(deviceCallback));
            if(jsonResponseArray.size() != 0
                    && mActiveJsonCharacteristics != 0
                    && jsonResponseArray.size() == mActiveJsonCharacteristics
                    && deviceCallback != null) {
                String jsonInput;
                StringBuilder jsonInputBuilder = new StringBuilder();
                for(int i = 0; i < jsonResponseArray.size(); i++) {
                    jsonInputBuilder.append(jsonResponseArray.get(i));
                }

                jsonInput = jsonInputBuilder.toString();
                JsonProcessor jsonProcessor = new JsonProcessor();
                jsonProcessor.setJsonInput(jsonInput.toString());
                jsonProcessor.parseJson();
                mAllMeasurementLists.clear();
                mAllMeasurementLists.add(jsonProcessor.getMeasurementList());
                mTitle = jsonProcessor.getTitle();

                if(deviceCallback != null) {
                    deviceCallback.onDataFetchedFromDevice(TestoBluetoothDevice.this);
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d(TAG, "MTU changed: " + mtu + " (Status: " + status + ")");
            gatt.discoverServices();
        }
    };

    /**
     * Creates a new instance from a given BluetoothDevice
     *
     * @param bluetoothDevice
     * @return
     */

    public static TestoDevice fromBluetoothDevice(BluetoothDevice bluetoothDevice) {
        String serial = bluetoothDevice.getName().replace("testo 300 ", "");
        return new TestoBluetoothDevice("testo 300", serial, bluetoothDevice);
    }

    private TestoBluetoothDevice(String deviceName, String serialNumber, BluetoothDevice device) {
        this.mDeviceName = deviceName;
        this.mSerialNumber = serialNumber;
        this.bleDevice = device;
        this.mType = TYPE_BLE;
    }

    @Override
    public void connect(Context context) {
        this.bluetoothGatt = this.bleDevice.connectGatt(context, false, bluetoothGattCallback);
    }

    @Override
    public void fetchData() {
        if(measDataService != null) {
            jsonResponseArray.clear();
            mActiveJsonCharacteristics = 0;
            bluetoothGatt.readCharacteristic(measDataService.getCharacteristic(TestoGattDefinitions.UUID_JSON_MEASDATA_META_CHARACTERISTIC));
        }
    }

    @Override
    public void toggleMeasurement() {
        if(bluetoothGatt != null) {
            BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(TestoGattDefinitions.UUID_CONTROL_SERVICE)
                    .getCharacteristic(TestoGattDefinitions.UUID_CONTROL_COMMAND_CHARACTERISTIC);
            characteristic.setValue("TOGGLE_MEASUREMENT");
            bluetoothGatt.writeCharacteristic(characteristic);
            Log.d(TAG, "Write value \"TOGGLE_MEASUREMENT\" to " + TestoGattDefinitions.UUID_CONTROL_STATUS_CHARACTERISTIC.toString());
        }
    }
}
