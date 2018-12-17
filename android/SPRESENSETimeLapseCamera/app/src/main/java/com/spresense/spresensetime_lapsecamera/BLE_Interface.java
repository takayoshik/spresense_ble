package com.spresense.spresensetime_lapsecamera;

/*
 * BLE interface for receive data
 */
public interface BLE_Interface {

    /*
     * BLE GATT characteristic callback.
     * If data receive from peer device, this method will call.
     */
    public void onBleReceived(byte[] data);
    public void onBleConnected();
    public void onBleDisconnected();
}
