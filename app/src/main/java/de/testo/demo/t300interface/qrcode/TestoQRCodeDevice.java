package de.testo.demo.t300interface.qrcode;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.testo.demo.t300interface.device.TestoDevice;


public class TestoQRCodeDevice extends TestoDevice {

    public TestoQRCodeDevice(ArrayList<ArrayList<HashMap<String, String>>> allMeasurementLists,
    ArrayList<String> allTitles){
        mAllMeasurementLists = allMeasurementLists;
        mAllTitles = allTitles;
    }


    @Override
    public void connect(Context context) {
    }

    @Override
    public void fetchData() {
        if(deviceCallback != null) {
            deviceCallback.onDataFetchedFromDevice(TestoQRCodeDevice.this);
        }
    }

    @Override
    public void toggleMeasurement() {
    }
}