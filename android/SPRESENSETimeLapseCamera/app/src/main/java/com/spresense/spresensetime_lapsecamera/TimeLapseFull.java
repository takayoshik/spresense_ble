package com.spresense.spresensetime_lapsecamera;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class TimeLapseFull extends AppCompatActivity implements BLE_Interface{

    private ROHM_BLE mBle = null;
    private final String MAC_ADDRESS = "00:20:8B:AA:55:01";
    private final String TAG = "SPRESENSE TIME LAPSE";

    private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_lapse_full);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        mBle = new ROHM_BLE(this, MAC_ADDRESS);
        TimeLapseAnimation timeLapseAnimation = new TimeLapseAnimation(bitmaps, this);
        timeLapseAnimation.start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "StartScanning");

        /* Start to scan peer device */
        mBle.startScan();
    }

    public void setImage(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView i_view = findViewById(R.id.main_view);
                i_view.setImageBitmap(bitmap);
                i_view.invalidate();
            }
        });
    }

    static byte pre = 0x00;
    static int length = 0;
    static byte jpeg_d[] = new byte[100 * 1024];
    private void pop_data(byte val) {
        jpeg_d[length] = val;
        length ++;
    }

    private final int MAX_IMAGES = 600;
    private void pop(byte val) {
        if (val != 0x2C) {
            if (pre == 0x2C) {
                if (val == 0x00) {
                    /* 0x2C */
                    pop_data((byte)0x2C);
                } else if (val == 0x01) {
                    /* 0x2B */
                    pop_data((byte)0x2B);
                } else if (val == 0x02) {
                    /* File start */
                    length = 0;
                } else if (val == 0x03) {
                    /* File end */
                    Log.e(TAG, String.format("Jpeg size: %08d", length));
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg_d, 0, length);

                    if(bitmap != null) {
                        if (bitmaps.size() < MAX_IMAGES) {
                            bitmaps.add(bitmap);
                        } else {
                            for (int i = 0; i < MAX_IMAGES / 2; i ++) {
                                bitmaps.remove(MAX_IMAGES - 1 - 2 * i);
                            }
                            bitmaps.add(bitmap);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageView view[] = {findViewById(R.id.sub_view_1), findViewById(R.id.sub_view_2), findViewById(R.id.sub_view_3), findViewById(R.id.sub_view_4)};
                                for (int v = 0; v < 4 && v < bitmaps.size(); v ++) {
                                    view[v].setImageBitmap(bitmaps.get(bitmaps.size() - 1 - v));
                                }
                            }
                        });
                    }
                }
            } else {
                pop_data(val);
            }
        }
        pre = val;
    }
    public void onBleConnected() {

    }

    public void onBleDisconnected() {
        pre = 0x00;
        length = 0;
        jpeg_d = new byte[100 * 1024];
    }

    public void onBleReceived(byte[] data) {
        /* Put your code here for receve data */
        //Log.e(TAG, String.format("Data length = %d", data.length));
        for (int i = 0; i < data.length; i ++) {
            pop(data[i]);
        }
    }
}
