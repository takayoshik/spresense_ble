package com.example.ble.rohmBleSampleApp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.sony.example.ble.rohm_ble_sample_app.R;

public class MainActivity extends AppCompatActivity implements BLE_Interface {
    private final String  TAG = "SPRESENSE";

    /* MAC Address. Please change here for use your board */
    private final String MAC_ADDRESS = "00:20:8B:AA:55:01";

    /* ROHM_BLE driver instance */
    private ROHM_BLE mBle;

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            /*
             * If button pushed, this method will call
             */
            switch (item.getItemId()) {
                case R.id.navigation_home: /* "home" button */
                    mTextMessage.setText(R.string.title_home);
                    /* Send "HOME" to peer device  */
                    mBle.writeString("HOME");
                    return true;
                case R.id.navigation_dashboard: /* "dashboard" button */
                    mTextMessage.setText(R.string.title_dashboard);
                    /* Send "DASHBOARD" to peer device  */
                    mBle.writeString("DASHBOARD");
                    return true;
                case R.id.navigation_notifications: /* "notification" button */
                    mTextMessage.setText(R.string.title_notifications);
                    /* Send "Z" to peer device  */
                    mBle.writeString("Z");
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /* Get new ROHM_BLE instance for using BLE GATT */
        mBle = new ROHM_BLE(this, MAC_ADDRESS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "StartScanning");

        /* Start to scan peer device */
        mBle.startScan();
    }

    public void onBleReceived(String str) {
        /* Put your code here for receve data */
    }

}
