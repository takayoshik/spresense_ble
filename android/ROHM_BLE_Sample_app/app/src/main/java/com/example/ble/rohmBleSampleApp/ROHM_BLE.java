/*
 * ROHM MK7125-002 Android driver
 */

package com.example.ble.rohmBleSampleApp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class ROHM_BLE implements BluetoothAdapter.LeScanCallback {

    /* Device name shown by scan result */
    private final String  TGT_DEVICE_NAME = "LapisDev";

    /* Log TAG */
    private final String  TAG = "SPRESENSE_BLE";

    /* GATT Service UUID for MK7125-002 */
    private final String ROHM_BLE_SERVICE_UUID = "0179BBD0-5351-48B5-BF6D-2167639BC867";

    /* GATT Characteristic UUID for MK7125-002 Serial port profile */
    private final String ROHM_BLE_CHAR_UUID    = "0179BBD1-5351-48B5-BF6D-2167639BC867";

    /* Descriptor UUID for MK7125-002 Serial port profile */
    private final String ROHM_BLE_DESCRIPTOR   = "00002902-0000-1000-8000-00805F9B34FB";

    /* BT/BLE common instances */
    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice mDevice;
    private BluetoothGatt mConnGatt;
    private BluetoothGattCharacteristic mCharacter;

    private int mStatus;

    // Additional BLE State.
    private final int STATE_SCANNING        = 100;  // Scanning the Device.
    private final int STATE_FOUND_DEVICE    = 101;  // Found the Device.

    private Context mContext;
    private BLE_Interface mBleIf;
    private String mAddress;

    /* Constructor */
    public ROHM_BLE(Context context, String address) {
        /* store caller context */
        this.mContext = context;
        this.mBleIf = (BLE_Interface) context;

        /* set mac address */
        this.mAddress = address;

        /*
          Reqirement
            1. Bluetooth LE supported
            2. Turn on Bluetooth
            3. Location permission
            4. Turn on location service
         */
        checkBLE();
    }

    /* scan status */
    private boolean mIsScanning;

    /* Start scan devices */
    public void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            mBTAdapter.startLeScan(this);
            mIsScanning = true;
        }
    }

    /* Stop scan devices */
    private void stopScan() {
        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(this);
        }
        mIsScanning = false;
    }

    /* Write string data to peer device via characteristic */
    public void writeString(String str) {
        mCharacter.setValue(str);
        mConnGatt.writeCharacteristic(mCharacter);
    }

    /*
     * BT/BLE checker
     * Check BLE capability, BT status
     */
    private void checkBLE(){
        /* Is Bluetooth LE supported?  */
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) ) {
            Toast.makeText(mContext, "BLE is not Supported", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Is turn on BT? */
        BluetoothManager manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(mContext, "BT is Unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mBTAdapter.isEnabled()) {
            Toast.makeText(mContext, "BT is Disabled", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        Log.d(TAG, "Found Device = [" + bluetoothDevice.getName() + "]");
        if( TGT_DEVICE_NAME.equals(bluetoothDevice.getName())
                && mAddress.equals(bluetoothDevice.getAddress())) {
            Log.d(TAG, "Device Name Match!!!"+bluetoothDevice.getAddress());
            mDevice = bluetoothDevice;
            stopScan();
            StartConnect();
        }
    }

    //====================================
    // For Connecting Device.
    protected void StartConnect(){
        mConnGatt = mDevice.connectGatt(mContext, false, mGattCB);
        mStatus = BluetoothProfile.STATE_CONNECTING;
    }

    private final BluetoothGattCallback mGattCB = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mStatus = newState;
                mConnGatt.discoverServices();
                Log.d(TAG, "Is Connected...");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Is Disconnected...");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            for(BluetoothGattService service : gatt.getServices()){
                if((service == null) || (service.getUuid() == null)){
                    continue;
                }

                if(ROHM_BLE_SERVICE_UUID.equalsIgnoreCase(service.getUuid().toString())){
                    mStatus = status;
                    mCharacter = service.getCharacteristic(UUID.fromString(ROHM_BLE_CHAR_UUID));
                    Log.d(TAG, "Service connected...");
                    setNotificationEnable();
                }
            }
        }

        private void setNotificationEnable(){
            mConnGatt.setCharacteristicNotification(mCharacter, true);
            BluetoothGattDescriptor desc = mCharacter.getDescriptor(UUID.fromString(ROHM_BLE_DESCRIPTOR));
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mConnGatt.writeDescriptor(desc);
            Log.e(TAG, "setNotificationEnable");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic ch){
            String strReceived = ch.getStringValue(0);
            Log.d(TAG, "Rcved! sz[" + strReceived.length() + "]  str["+ strReceived + "]");
            mBleIf.onBleReceived(strReceived);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "Read Status: "+status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "Write Status: "+status);
        }
    };
}
