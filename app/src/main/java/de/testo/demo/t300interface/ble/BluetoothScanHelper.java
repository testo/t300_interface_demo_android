package de.testo.demo.t300interface.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public abstract class BluetoothScanHelper {
    private static final String TAG = "BluetoothScanHelper";

    protected BluetoothAdapter mBluetoothAdapter = null;
    protected BluetoothLeScanner mBluetoothScanner = null;
    protected String mNameFilter = null;
    private boolean mScanRunning = false;

    private Handler handler = new Handler();

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "onScanResult: " + callbackType + " result: " + result.toString());
            if(callbackType != ScanSettings.CALLBACK_TYPE_MATCH_LOST) {
                handleScanResult(result);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "onBatchScanResults: " + results.size());
            for (ScanResult result : results) {
                handleScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed:" + errorCode);
        }
    };

    public BluetoothScanHelper(Context context) {
        this("testo 300", context);
    }

    public BluetoothScanHelper(String nameFilter, Context context) {
        Log.d(TAG, "Initializing BluetoothScanHelper");
        mNameFilter = nameFilter;
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if(manager != null) {
            mBluetoothAdapter = manager.getAdapter();
            mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
    }

    @SuppressLint("MissingPermission")
    public void startScan() {
        if(mBluetoothScanner != null) {
            this.mScanRunning = true;
            Log.d(TAG, "Starting Bluetooth scan");
            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(TestoGattDefinitions.UUID_MEASDATA_SERVICE)).build());
            filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(TestoGattDefinitions.UUID_CONTROL_SERVICE)).build());

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .setReportDelay(0)
                    .build();

            mBluetoothScanner.startScan(filters, settings, scanCallback);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, 10000);
        }
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if(mScanRunning) {
            Log.d(TAG, "Stopping Bluetooth scan");
            mBluetoothScanner.stopScan(scanCallback);
            mScanRunning = false;
        }
    }
    @SuppressLint("MissingPermission")
    protected void handleScanResult(ScanResult scanResult) {
        Log.d(TAG, "handleScanResult: " + scanResult.toString());
        if(scanResult.isConnectable()) {
            Log.d(TAG, "handleScanResult: isConnectable() true");
            BluetoothDevice device = scanResult.getDevice();
            if (device != null) {
                Log.d(TAG, "+--------------------------------------------------");
                Log.d(TAG, "| Device name:    " + device.getName());
                Log.d(TAG, "| Device address: " + device.getAddress());
                Log.d(TAG, "| Device type:    " + device.getType());
                Log.d(TAG, "| Bond state:     " + device.getBondState());
                Log.d(TAG, "+--------------------------------------------------");
                if (device.getName().startsWith(mNameFilter)) {
                    Log.d(TAG, "Found device with name: " + device.getName());
                    bleDeviceFound(device);
                }
            }
        }
    }

    abstract public void bleDeviceFound(BluetoothDevice device);
}
