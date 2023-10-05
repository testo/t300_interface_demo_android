package de.testo.demo.t300interface;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.testo.demo.t300interface.qrcode.QRScanner;
import de.testo.demo.t300interface.ble.BluetoothScanHelper;
import de.testo.demo.t300interface.ble.TestoBluetoothDevice;
import de.testo.demo.t300interface.http.TestoHttpDevice;
import de.testo.demo.t300interface.device.TestoDevice;
import de.testo.demo.t300interface.udp.UdpBroadcastReceiver;

import static de.testo.demo.t300interface.util.TextViewHelper.GenerateDefaultTextView;

/**
 * First Activity
 * Display all the found devices in the same network
 */
public class MainActivity extends AppCompatActivity {
    UdpBroadcastReceiver udpBroadcastReceiver = new UdpBroadcastReceiver() {
        @Override
        public void onBroadcastReceived(final String message, final String sender) {
            // Because there is an UI update happening this has to be executed in the main thread
            mainActivity.runOnUiThread(new Runnable() {
                public void run() {

                    // Create a new TestoDevice from the json that was sent with the udp broadcast
                    TestoDevice d = TestoHttpDevice.fromJson(message);

                    // if the device is new, add it to the table
                    if (!devicesFoundByReceiver.contains(d) && d != null) {
                        devicesFoundByReceiver.add(d);
                    }

                    initializeTable();
                }
            });
        }
    };

    private BluetoothScanHelper bluetoothScanHelper;

    private ImageButton mQrCodeBtn;

    private Handler handler = new Handler();

    public TableLayout tableLayout = null;
    public ArrayList<TestoDevice> devicesFoundByReceiver;
    public Map<TestoDevice, View> devicesShownInTable;
    private Activity mainActivity = null;

    private String TAG = MainActivity.class.getSimpleName();

    /**
     * OnCreate method
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize members
        mainActivity = this;
        devicesFoundByReceiver = new ArrayList<TestoDevice>();
        devicesShownInTable = new HashMap<TestoDevice, View>();

        bluetoothScanHelper = new BluetoothScanHelper(this) {
            @Override
            public void bleDeviceFound(BluetoothDevice bluetoothDevice) {
                TestoDevice testoDevice = TestoBluetoothDevice.fromBluetoothDevice(bluetoothDevice);
                devicesFoundByReceiver.add(testoDevice);
                initializeTable();
            }
        };

        // initialize UI
        setTheme(R.style.AppTheme);
        setContentView(R.layout.device_list);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        }, 0);


        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_ADMIN
        }, 0);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH
        }, 0);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_PRIVILEGED
        }, 0);


        /**
         * start the udp listener and initialize the table
         */
        startUdpListener();
        bluetoothScanHelper.startScan();
        initializeTableLayout();
        initializeTable();
        initializeTableHeader();


        //refresh the list (looking for new devices)
        final ImageButton research = findViewById(R.id.button2);
        research.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tableLayout != null) {
                    tableLayout.removeAllViewsInLayout();

                    devicesFoundByReceiver.clear();
                    devicesShownInTable.clear();
                    Log.d(TAG, "Refresh View!");
                }
                initializeTable();
                bluetoothScanHelper.startScan();
            }
        });

        mQrCodeBtn = (ImageButton) findViewById(R.id.button3);
        mQrCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBarcodeScanner();
                bluetoothScanHelper.stopScan();
            }
        });
    }// onCreate


    private void openBarcodeScanner() {
        Intent intent = new Intent(this, QRScanner.class);
        startActivity(intent);

    }


    /**
     * Initializes the UdpBroadcastReceiver
     */
    protected void startUdpListener() {
        udpBroadcastReceiver.startUdpListener();
    }

    /**
     * Creates the table layout for the found devices
     */
    protected void initializeTableLayout() {
        if (tableLayout == null) {
            tableLayout = findViewById(R.id.table_layout);
        }
    }

    /**
     * Initializes the table to display all the found testo devicesFoundByReceiver and remove all
     * doublets
     */
    protected void initializeTable() {
        initializeTableLayout();

        for (final TestoDevice device : devicesFoundByReceiver) {
            if (!devicesShownInTable.keySet().contains(device)) {
                addDeviceToTable(device);
            }
        }

        for (final TestoDevice device : devicesShownInTable.keySet()) {
            if (!devicesFoundByReceiver.contains(device)) {
                devicesShownInTable.remove(device);
                tableLayout.removeView(devicesShownInTable.get(device));
            }
        }
    }

    private void initializeTableHeader() {

        TableRow blankTableHead = new TableRow(this);

        TextView name = GenerateDefaultTextView(this, "Device");
        TextView serialNumber = GenerateDefaultTextView(this, "Serial-NR");
        TextView version = GenerateDefaultTextView(this, "FW-V");
        TextView ipv4 = GenerateDefaultTextView(this, "IPv4");
        TextView port = GenerateDefaultTextView(this, "Port");
        TextView type = GenerateDefaultTextView(this, "Type");

        blankTableHead.addView(name);
        blankTableHead.addView(serialNumber);
        blankTableHead.addView(version);
        blankTableHead.addView(ipv4);
        blankTableHead.addView(port);
        blankTableHead.addView(type);

        blankTableHead.setBackgroundColor(Color.LTGRAY);

        tableLayout.addView(blankTableHead);

    }

    /**
     * Fills the table row with the date from the JSON.
     * --> definitely upgradable
     *
     * @param device
     */

    private void addDeviceToTable(final TestoDevice device) {
        TableRow row = new TableRow(this);

        TextView name = GenerateDefaultTextView(this, device.getDeviceName());
        TextView serialNumber = GenerateDefaultTextView(this, device.getSerialNumber());
        TextView version = GenerateDefaultTextView(this, device.getFirmwareVersion());
        TextView ipv4 = GenerateDefaultTextView(this, device.getIpv4Address());
        TextView port = GenerateDefaultTextView(this, device.getHttpServerPort());
        TextView type = GenerateDefaultTextView(this, device.getType());

        row.addView(name);
        row.addView(serialNumber);
        row.addView(version);
        row.addView(ipv4);
        row.addView(port);
        row.addView(type);

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                for (int i = 0; i < tableLayout.getChildCount(); ++i) {
                    tableLayout.getChildAt(i).setBackgroundColor(Color.WHITE);
                }

                final TableRow thisRow = (TableRow) view;
                thisRow.setBackgroundColor(Color.LTGRAY);
                openMeasurementListActivity(device, view);
            }
        });

        tableLayout.addView(row);
        devicesShownInTable.put(device, row);
    }

    /**
     * Change the Activity and gives the chosen URL to the JSON Task
     *
     * @param view
     */
    public void openMeasurementListActivity(TestoDevice device, View view) {
        Intent intent = new Intent(MainActivity.this, MeasurementListActivity.class);

        device.connect(this);
        DeviceHandler.setActiveDevice(device);
        startActivity(intent);

        udpBroadcastReceiver.stopUdpListener(false);
        bluetoothScanHelper.stopScan();

        //provides an empty scan overview when using the toolbar back button
        if (tableLayout != null) {
            tableLayout.removeAllViews();
            initializeTable();
        }
    }


    /**
     * Hardware back button for leaving the application with a query
     */
    public void onBackPressed() {
        backButtonHandler();
        return;
    }

    public void backButtonHandler() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Leave application?");

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to leave the application?");


        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


}
