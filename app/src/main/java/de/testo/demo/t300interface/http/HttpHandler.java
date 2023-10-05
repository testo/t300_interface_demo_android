package de.testo.demo.t300interface.http;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.testo.demo.t300interface.util.URLBuilder;


/**
 * Relevant class. Belongs to the JsonProcessorTask-/ZivTask- class
 */
public class HttpHandler {
    public enum Method {
        GET,
        POST
    }
    private static final String TAG = HttpHandler.class.getSimpleName();

    public static String getJson() throws IOException, RuntimeException {
        return makeHttpRequest(URLBuilder.getTjfUrl(), Method.GET);
    }

    public static String getZiv() throws IOException, RuntimeException {
        return makeHttpRequest(URLBuilder.getZivUrl(), Method.GET);
    }

    public static String postZiv(String payload) throws IOException, RuntimeException {
        return makeHttpRequest(URLBuilder.getZivUrl(), Method.POST, payload);
    }

    private static String makeHttpRequest(String address, Method method) throws IOException, RuntimeException {
        return makeHttpRequest(address, method, null);
    }

    private static String makeHttpRequest(String address, Method method, String payload) throws IOException, RuntimeException {
        String response = null;
        try {
            URL url = new URL(address);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(10000);

            switch(method) {
                case GET:
                    httpConn.setRequestMethod("GET");
                    break;
                case POST:
                    httpConn.setRequestMethod("POST");
                    if(payload != null) {
                        httpConn.setDoOutput(true);
                        OutputStream os = httpConn.getOutputStream();
                        os.write(payload.getBytes());
                        os.flush();
                    }
                    break;
            }

            int responseCode = httpConn.getResponseCode();
            if(responseCode == 200) {
                Map<String, List<String>> headerMap = httpConn.getHeaderFields();
                Iterator<Entry<String,List<String>>> it = headerMap.entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry<String,List<String>> pair = it.next();
                    Log.d(TAG, pair.getKey() + ": " + pair.getValue());
                }

                if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + httpConn.getResponseCode());
                }
                response = convertStreamToString(new BufferedInputStream(httpConn.getInputStream()));
            }
            httpConn.disconnect();
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException during http request");
        }
        return response;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

}
