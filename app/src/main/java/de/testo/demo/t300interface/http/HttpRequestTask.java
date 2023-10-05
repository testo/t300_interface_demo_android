package de.testo.demo.t300interface.http;

import android.os.AsyncTask;

import java.io.IOException;

public class HttpRequestTask extends AsyncTask<Void, Void, String> {
    @Override
    protected String doInBackground(Void... args0) {
        String retVal = null;
        int tries = 0;
        int maxTries = 3;
        while(retVal == null && tries < maxTries) {
            try {
               retVal = HttpHandler.getJson();
            } catch (IOException e) {
                e.printStackTrace();
            }
            tries++;
        }
        return retVal;
    }
}
