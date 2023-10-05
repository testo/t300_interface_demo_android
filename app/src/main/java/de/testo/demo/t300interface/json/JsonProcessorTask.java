package de.testo.demo.t300interface.json;

import android.os.AsyncTask;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Work in background
 * Get the information from JsonProcessor-class
 */

public class JsonProcessorTask extends AsyncTask<Void, Void, Pair<String, ArrayList<HashMap<String, String>>>> {


    /**
     * JSON Parser Version MeasurementListActivity (Second Activity)
     * @param args0
     */

    private String TAG = JsonProcessorTask.class.getSimpleName();

    @Override
    protected Pair<String, ArrayList<HashMap<String, String>>> doInBackground(Void... args0) {
        JsonProcessor jsonBuilder = new JsonProcessor();

        jsonBuilder.parseJson();

        return new Pair<String, ArrayList<HashMap<String, String>>>(jsonBuilder.getTitle(), jsonBuilder.getMeasurementList());
    }



}
