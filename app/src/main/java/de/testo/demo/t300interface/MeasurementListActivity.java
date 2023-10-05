package de.testo.demo.t300interface;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import de.testo.demo.t300interface.qrcode.QRScanner;
import de.testo.demo.t300interface.qrcode.TestoQRCodeDevice;
import de.testo.demo.t300interface.device.TestoDevice;
import de.testo.demo.t300interface.device.TestoDeviceCallback;
import de.testo.demo.t300interface.json.JsonProcessor;


/**
 * Second Activity
 * Display the TJF from the selected device
 */
public class MeasurementListActivity extends AppCompatActivity {

    public  Activity activity = null;
    private String TAG = MeasurementListActivity.class.getSimpleName();

    private ArrayList<HashMap<String,String>> mMeasurementListInput;

    private ListView mMeasurementLv;
    private AlphaAnimation mInAnimation;
    private AlphaAnimation mOutAnimation;
    private FrameLayout mProgressBarHolder = null;
    private ImageButton mRefreshBtn = null;
    private ImageButton mStartStopBtn = null;
    private ImageButton mAdditionalInfoBtn = null;
    private ImageButton mCloseAdditionalInfoBtn = null;
    private ImageButton mQrCodeBtn = null;
    private ImageButton mListLeftBtn = null;
    private ImageButton mListRightBtn = null;
    private TextView mCloseAdditionalInfoTv = null;
    private String mTimeStamp = null;

    ExpendableListViewAdapter listViewAdapter;
    ExpandableListView expandableListView;

    private boolean mIsQrCode = false;
    public int measurementNr = -1;

    public void setAdapterMeasLv(ListAdapter adapter){
        mMeasurementLv.setAdapter(adapter);
    }

    private void setMeasurementTitle(String title) {
        TextView titleView = (TextView) findViewById(R.id.measurementTitle);
        if(mIsQrCode) {
            titleView.setText(title + "   " + (measurementNr+1) + "/" + (DeviceHandler.getActiveDevice().getMeasurementLength())); //Hier Titel übergeben als String
        }
        else{
            titleView.setText(title); //Hier Titel übergeben als String
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        setTheme(R.style.AppTheme);
        setContentView(R.layout.measurement_list);




        if(DeviceHandler.getActiveDevice() != null) {
            String device = DeviceHandler.getActiveDevice().getClass().getSimpleName();
            if (device.equals(TestoQRCodeDevice.class.getSimpleName())){
                setContentView(R.layout.measurement_list_qr);
                mIsQrCode = true;
                measurementNr = 0;
            }

            DeviceHandler.getActiveDevice().setDeviceCallback(new TestoDeviceCallback() {
                @Override
                public void onDataFetchedFromDevice(final TestoDevice device) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateListView(device);
                            hideProgressBar();
                        }
                    });
                }
            });
        }



        showProgressBar();

        //initialize measurement List and title of measurement
        mMeasurementListInput = new ArrayList<>();
        mMeasurementLv = (ListView) findViewById(R.id.list);

        initializeProgressBar();
        initializeButtons();

        updateData();


    }// onCreate

    private void updateListView(TestoDevice device) {
            ListAdapter adapter = new SimpleAdapter(activity, device.getMeasurementList(measurementNr), R.layout.measurement_list_entry, new String[]{ "name","description", "vDescription"},
                    new int[]{R.id.tx_description, R.id.tx_name, R.id.tx_vDescription});
            setAdapterMeasLv(adapter);
            setMeasurementTitle(device.getMeasurementTitle(measurementNr));
    }


    private void openQRScanner() {
        Intent intent = new Intent(this, QRScanner.class);
        startActivity(intent);

    }

    private void updateData() {
        DeviceHandler.getActiveDevice().fetchData();
    }

    private void enableButtons() {
        initializeButtons();
        if (mIsQrCode){

        }
        else {
            mRefreshBtn.setEnabled(true);
            mStartStopBtn.setEnabled(true);
        }
    }

    private void disableButtons() {
        initializeButtons();
        if (mIsQrCode){

        }
        else {
            mRefreshBtn.setEnabled(false);
            mStartStopBtn.setEnabled(false);
        }
    }

    private void initializeButtons() {
        if (mIsQrCode) {

            mAdditionalInfoBtn = findViewById(R.id.additionalInfoBtn);
            mAdditionalInfoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAdditionalInfo();
                }
            });

            mQrCodeBtn = findViewById(R.id.QrCodeBtn);
            mQrCodeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openQRScanner();
                }
            });

            mListLeftBtn = findViewById(R.id.listLeftBtn);
            mListLeftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPreviousMeasurement();
                }
            });

            mListRightBtn = findViewById(R.id.listRightBtn);
            mListRightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showNextMeasurement();
                }
            });
        }
        else {
            if (mStartStopBtn == null) {
                mStartStopBtn = (ImageButton) findViewById(R.id.startStop);
                mStartStopBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DeviceHandler.getActiveDevice().toggleMeasurement();
                    }
                });
            }


            if (mRefreshBtn == null) {
                mRefreshBtn = findViewById(R.id.refreshMeasurement);
                mRefreshBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showProgressBar();
                        updateData();
                    }
                });
            }
        }
    }

    private void showPreviousMeasurement(){
        TestoDevice device = DeviceHandler.getActiveDevice();
        if(measurementNr > 0) {
            measurementNr -= 1;
            updateListView(device);
        }
        else{
            Toast.makeText(MeasurementListActivity.this, "Already the first measurement...", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNextMeasurement(){
        TestoDevice device = DeviceHandler.getActiveDevice();
        if(measurementNr +1 < device.getMeasurementLength()) {
            measurementNr += 1;
            updateListView(device);
        }
        else{
            Toast.makeText(MeasurementListActivity.this, "Already the last measurement...", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAdditionalInfo() {
        final Dialog dialog = new Dialog(this);
        JsonProcessor jsonProcessor = new JsonProcessor();

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.additional_info_list);
        expandableListView = dialog.findViewById(R.id.additionalListView);

        mCloseAdditionalInfoBtn = dialog.findViewById(R.id.closeAdditionalInfoBtn);
        mCloseAdditionalInfoTv = dialog.findViewById(R.id.closeAdditionalInfoTv);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);


        listViewAdapter = new ExpendableListViewAdapter(this, JsonProcessor.getAdditionalInfoList());
        expandableListView.setAdapter(listViewAdapter);

        mTimeStamp = jsonProcessor.getTimeStamp();
        TextView timeStampTv = dialog.findViewById(R.id.timeStampTv);
        timeStampTv.setText(mTimeStamp);

        mCloseAdditionalInfoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        mCloseAdditionalInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void showProgressBar() {
        Log.d(TAG, "Show progress bar");
        disableButtons();
        initializeProgressBar();
        mInAnimation = new AlphaAnimation(0f, 1f);
        mInAnimation.setDuration(200);
        mProgressBarHolder.setAnimation(mInAnimation);
        mProgressBarHolder.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        Log.d(TAG, "Hide progress bar");
        enableButtons();
        initializeProgressBar();
        mOutAnimation = new AlphaAnimation(1f, 0f);
        mOutAnimation.setDuration(200);
        mProgressBarHolder.setAnimation(mOutAnimation);
        mProgressBarHolder.setVisibility(View.GONE);
    }

    private void initializeProgressBar() {
        if(mProgressBarHolder == null) {
            mProgressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        }
    }

}
