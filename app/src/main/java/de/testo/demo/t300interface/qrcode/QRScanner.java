package de.testo.demo.t300interface.qrcode;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import de.testo.demo.t300interface.DeviceHandler;
import de.testo.demo.t300interface.MeasurementListActivity;
import de.testo.demo.t300interface.R;
import de.testo.demo.t300interface.json.JsonProcessor;

public class QRScanner extends AppCompatActivity {

    private Button mCameraBtn;
    private Button mScanBtn;
    private Button mReturnToListBtn;
    private ImageView mImageIv;
    private TextView mResultTv;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    private String[] mCameraPermission;

    private Uri mImageUri = null;

    private BarcodeScannerOptions qrScannerOptions;
    private BarcodeScanner qrCodeScanner;

    private static final String TAG = "MAIN_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner_view);

        mCameraBtn = findViewById(R.id.cameraBtn);
        mImageIv = findViewById(R.id.imageIv);
        mScanBtn = findViewById(R.id.scanBtn);
        mResultTv = findViewById(R.id.resultTv);

        mCameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        qrScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        qrCodeScanner = BarcodeScanning.getClient(qrScannerOptions);

        if (checkCameraPermission()){
            pickImageCamera();
        }
        else{
            requestCameraPermission();
        }

        mCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkCameraPermission()){
                    mResultTv.setText("");
                    pickImageCamera();
                }
                else{
                    requestCameraPermission();
                }
            }
        });

        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mImageUri == null){
                    Toast.makeText(QRScanner.this, "Using dummy image", Toast.LENGTH_SHORT).show();
                }
                mResultTv.setText("");
                DeviceHandler.setActiveDevice(null);
                detectResultFromImage();
            }
        });

        mReturnToListBtn = (Button) findViewById(R.id.returntolist);
        mReturnToListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToList();
            }
        });
    }

    private void returnToList() {
        finish();
    }

    private void detectResultFromImage() {
        try {
            InputImage inputImage = null;
            if (mImageUri == null){
                inputImage = InputImage.fromFilePath(this, Uri.parse("android.resource://de.testo.demo.t300interface/drawable/exampleqr"));
            }
            else {
                inputImage = InputImage.fromFilePath( this, mImageUri);
            }

            Task<List<Barcode>> qrCodeResult = qrCodeScanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> qrCodes) {
                            if (qrCodes.isEmpty()) {
                                Toast.makeText(QRScanner.this, "No Qr-code found, please try again", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                extractQRCodeInfo(qrCodes);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Task failed with an exception, we can't get any detail
                            Toast.makeText(QRScanner.this, "Failed scanning due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        catch (Exception e){
            Toast.makeText(this, "Failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    String decodedString;
    private void extractQRCodeInfo(List<Barcode> qrCodes) {

        for (Barcode qrcode : qrCodes) {
            String mRawValue = qrcode.getRawValue();

            Log.d(TAG, "extractQRCodeInfo: " + mRawValue);
            int valueType = qrcode.getValueType();
            switch (valueType) {
                case Barcode.TYPE_TEXT: {
                    try {
                        // Annahme: Der 'compressedData' ist der komprimierte Binärcode aus dem QR-Code
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(qrcode.getRawBytes());
                        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(inputStream);

                        ByteArrayOutputStream decompressedBytes = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int n;
                        while ((n = gzipIn.read(buffer)) >= 0) {
                            decompressedBytes.write(buffer, 0, n);
                        }
                        decodedString = new String(decompressedBytes.toByteArray());

                    } catch (IOException e) {
                        // Behandeln Sie etwaige Fehler
                    }

                    ArrayList<String> decodedStrings = new ArrayList<>();

                    int startIndex = 0;
                    int endIndex = decodedString.indexOf("}\n{") + 1;
                    while (endIndex != 0) {
                        decodedStrings.add(decodedString.substring(startIndex, endIndex));
                        decodedString = decodedString.substring(endIndex + 1, decodedString.length());
                        endIndex = decodedString.indexOf("}\n{") + 1;
                    }
                    decodedStrings.add(decodedString.substring(startIndex, decodedString.length()));

                    ArrayList<ArrayList<HashMap<String, String>>> allMeasurementLists = new ArrayList<>();
                    ArrayList<String> allTitles = new ArrayList<>();

                    for (int i = 0; i < decodedStrings.size(); i++) {
                        String jsonInput = decodedStrings.get(i);
                        JsonProcessor jsonProcessor = new JsonProcessor();
                        jsonProcessor.setJsonInput(jsonInput);
                        jsonProcessor.parseJson();

                        // Get measurement list and title for the current JSON
                        ArrayList<HashMap<String, String>> measurementList = jsonProcessor.getMeasurementList();
                        String title = jsonProcessor.getTitle();

                        // Add measurement list and title to the lists
                        allMeasurementLists.add(measurementList);
                        allTitles.add(title);
                    }

                    if (allMeasurementLists != null) {
                        TestoQRCodeDevice device = new TestoQRCodeDevice(allMeasurementLists, allTitles);
                        Intent intent = new Intent(QRScanner.this, MeasurementListActivity.class);

                        DeviceHandler.setActiveDevice(device);
                        startActivity(intent);
                    }
                }
                break;
                case Barcode.TYPE_WIFI: {

                    Barcode.WiFi typeWifi = qrcode.getWifi();

                    String ssid = "" + typeWifi.getSsid();
                    String password = "" + typeWifi.getPassword();
                    String encryptionType = "" + typeWifi.getEncryptionType();

                    Log.d(TAG, "extractQRCodeInfo: TYPE_WIFI: ");
                    Log.d(TAG, "extractQRCodeInfo: ssid: " + ssid);
                    Log.d(TAG, "extractQRCodeInfo: password: " + password);
                    Log.d(TAG, "extractQRCodeInfo: encryptionType: " + encryptionType);

                    mResultTv.setText("TYPE: TYPE_WIFI \nssid: " + ssid + "\npassword: " + password + "\nencryptionType: " + encryptionType);
                    mResultTv.setTextIsSelectable(true);
                }
                break;
                default: {
                    Toast.makeText(QRScanner.this, "No supported format found, please try again", Toast.LENGTH_SHORT).show();
                    mResultTv.setTextIsSelectable(true);
                }
            }
        }
    }

    private void pickImageCamera(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");

        mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();

                        Log.d(TAG, "onActivityResult: imageUri"+ mImageUri);
                        mImageIv.setImageURI(mImageUri);
                    }
                    else {

                        Toast.makeText(QRScanner.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkCameraPermission(){

        boolean resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return  resultCamera && resultStorage;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, mCameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case  CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted){
                        pickImageCamera();
                    }
                    else {
                        Toast.makeText(this, "Camera & Storage permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                //function disabled
            }
            break;
        }
    }
}