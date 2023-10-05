package de.testo.demo.t300interface.json;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.testo.demo.t300interface.DeviceHandler;
import de.testo.demo.t300interface.device.TestoDevice;

/**
 * Works in the background
 * Read the TJF and parse it
 */
public class JsonProcessor {

    final static String TAG = JsonProcessor.class.getSimpleName();

    private String mJsonInput = null;
    private String mTitle = null;
    private ArrayList<HashMap<String, String>> mMeasurementList = null;

    static String sTimeStamp = null;
    static HashMap<String, HashMap<String, String>> sAdditionalInfoList = new HashMap<String, HashMap<String, String>>();

    public void setJsonInput(String mJsonInput) {
        this.mJsonInput = mJsonInput;
    }

    public void parseJson() {
        mMeasurementList = new ArrayList<>();

        if (mJsonInput != null) {
            try {
                JSONObject jsonObject = new JSONObject(mJsonInput);
                TestoDevice device = DeviceHandler.getActiveDevice();

                //Getting JSON Array node
                //1st level additionalMeasurementInformation --> not important to read out
                //1st level channels
                JSONArray channels = jsonObject.getJSONArray("channels");

                //2nd level type name measurement --> Input for ListView Title
                JSONObject typeNameMeasurement = jsonObject.getJSONObject("type");
                mTitle = typeNameMeasurement.getString("name");

                //looping through All channels
                //looping for channels
                for (int i = 0; i < channels.length(); i++) {

                    //3rd level type/unit/values
                    JSONObject channels2 = channels.getJSONObject(i);

                    //Adresse type description object
                    JSONObject type = channels2.getJSONObject("type");

                    String description = type.getString("name");


                    //Adress unit name object
                    JSONObject unit = channels2.getJSONObject("unit");
                    String name = unit.getString("name");


                    //Adress values description object
                    JSONArray values = channels2.getJSONArray("values");
                    ArrayList<String> descriptionList = new ArrayList<>();
                    if (device == null ) {

                        for (int k = 0; k < values.length(); k++) {
                            JSONObject valuesInput = values.getJSONObject(k);
                            if (valuesInput.has("status")) {
                                String vStatus = "----";
                                descriptionList.add(vStatus);
                            }

                            if (valuesInput.has("value")) {
                                String vValue = valuesInput.getString("value");
                                descriptionList.add(vValue);
                            }

                        }
                    } else {
                        for (int k = 0; k < values.length(); k++) {
                            JSONObject valuesInput = values.getJSONObject(k);
                            if (valuesInput.has("description")) {
                                String vValue = valuesInput.getString("description");
                                descriptionList.add(vValue);
                            }

                        }

                    }
                    //tmp hash map for single measurements
                    HashMap<String, String> channel = new HashMap<>();


                    //adding each child node to HashMap key --> value
                    channel.put("description", description);
                    channel.put("name", name);
                    for (String s : descriptionList) {
                        channel.put("vDescription", s);
                        //adding channel to list
                        mMeasurementList.add(channel);
                    }

                }
                try {

                    if (device == null) {
                        parseJsonAdditionalInfo();
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, e.toString());
                }

            } catch (final JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
    }


    public String getTitle() {
        return mTitle;
    }

    public String getTimeStamp(){
        return sTimeStamp;
    }

    public ArrayList<HashMap<String, String>> getMeasurementList() {
        return mMeasurementList;
    }

    public static HashMap<String, HashMap<String, String>> getAdditionalInfoList() {
        return sAdditionalInfoList;
    }


    private void parseJsonAdditionalInfo() throws JSONException {

        JSONObject jsonObject = new JSONObject(mJsonInput);
        JSONArray devices = jsonObject.getJSONArray("device");
        HashMap<String, String> customerList = new HashMap<String, String>();
        HashMap<String, String> deviceList = new HashMap<String, String>();
        HashMap<String, String> measuringPointList = new HashMap<String, String>();
        HashMap<String, String> organisationList = new HashMap<String, String>();
        HashMap<String, String> propertiesList = new HashMap<String, String>();

        //adding Customer
        JSONObject customer = jsonObject.getJSONObject("customer");
        String[] contentsCustomer = {"city", "email", "name", "personOfContact", "phone", "referenceNumber", "street"};

        for (String content : contentsCustomer) {
            if (content != null) {
                String value = customer.getString(content);
                customerList.put(content, value);
            }
        }

        sAdditionalInfoList.put("Customer", customerList);

        //adding devices
        //looping through All devices
        for (int l = 0; l < devices.length(); l++) {

            JSONObject devices2 = devices.getJSONObject(l);
            String[] contentsDevice = {"deviceIdentifier", "lastServiceDate", "name", "serial"};
            for (String content : contentsDevice) {
                if (content != null) {
                    String value = devices2.getString(content);
                    deviceList.put(content, value);
                }
            }
        }
        sAdditionalInfoList.put("Device", deviceList);

        //adding measuringPoints
        JSONObject measuringPoint = jsonObject.getJSONObject("measuringPoint");
        String[] contentsMeasuringPoints = {"name", "systemManufacturer", "systemType", "yearOfConstruction"};

        for (String content : contentsMeasuringPoints) {
            if (content != null) {
                String value = measuringPoint.getString(content);
                measuringPointList.put(content, value);
            }
        }
        sAdditionalInfoList.put("Measuring point", measuringPointList);

        //adding organisation
        JSONObject organisation = jsonObject.getJSONObject("organisation");
        String[] contentsOrganisation = {"city", "email", "name", "personOfContact", "phone", "street"};

        for (String content : contentsOrganisation) {
            if (content != null) {
                String value = organisation.getString(content);
                organisationList.put(content, value);
            }
        }
        sAdditionalInfoList.put("Organisation", organisationList);

        //adding properties
        JSONObject properties = jsonObject.getJSONObject("properties");
        String[] contentsProperties = {"co2MaxValue", "country", "fuelId", "fuelName", "fuelType", "o2FuelValuesValid", "o2Value"};

        for (String content : contentsProperties) {
            if (content != null) {
                String value = properties.getString(content);
                propertiesList.put(content, value);
            }
        }
        sAdditionalInfoList.put("Properties", propertiesList);
        if (sTimeStamp == null) {
            sTimeStamp = jsonObject.getString("timeStamp");
        }
    }

}


