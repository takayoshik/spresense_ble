package com.example.ble.rohmBleSampleApp;

import android.content.Context;

/*
 * BLE interface for receive data
 */
public interface BLE_Interface {

    /*
     * BLE GATT characteristic callback.
     * If data receive from peer device, this method will call.
     */
    public void onBleReceived(String str);
}
