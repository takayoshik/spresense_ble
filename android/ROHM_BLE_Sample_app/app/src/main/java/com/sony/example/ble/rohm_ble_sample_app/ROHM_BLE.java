package com.sony.example.ble.rohm_ble_sample_app;

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

    private final String  TGT_DEVICE_NAME = "LapisDev";
    private final String  TGT_DEVICE_ADDR = "AB:CD:19:84:06:14";

    private final String  TAG = "SPRESENSE_BLE";

    private final String ROHM_BLE_SERVICE_UUID = "0179BBD0-5351-48B5-BF6D-2167639BC867";
    private final String ROHM_BLE_CHAR_UUID    = "0179BBD1-5351-48B5-BF6D-2167639BC867";
    private final String ROHM_BLE_PROPATIES    = "00002902-0000-1000-8000-00805F9B34FB";

    //====================================
    // For BLE Classes.
    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice mDevice;
    private BluetoothGatt mConnGatt;
    private BluetoothGattCharacteristic mCharacter;

    private int mStatus;
    // Additional BLE State.
    private final int STATE_SCANNING        = 100;  // Scanning the Device.
    private final int STATE_FOUND_DEVICE    = 101;  // Found the Device.

    private Context mContext;

    public ROHM_BLE(Context context) {
        this.mContext = context;
        initBLE();
    }

    public void writeString(String str) {
        mCharacter.setValue(str);
        mConnGatt.writeCharacteristic(mCharacter);
    }

    private void initBLE(){

        //===============================================
        // Check BT is available.
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) ) {
            Toast.makeText(mContext, "BLE is not Supported", Toast.LENGTH_SHORT).show();
            return;
        }
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

    //====================================
    // For Scanning Device.
    private boolean mIsScanning;

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        Log.d(TAG, "Found Device = [" + bluetoothDevice.getName() + "]");
        if( TGT_DEVICE_NAME.equals(bluetoothDevice.getName())){
//                && TGT_DEVICE_ADDR.equals(bluetoothDevice.getAddress())) {
            Log.d(TAG, "Device Name Match!!!"+bluetoothDevice.getAddress());
            mDevice = bluetoothDevice;
            stopScan();
            StartConnect();
        }
    }

    public void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            mBTAdapter.startLeScan(this);
            mIsScanning = true;
        }
    }

    private void stopScan() {
        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(this);
        }
        mIsScanning = false;
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
            BluetoothGattDescriptor desc = mCharacter.getDescriptor(UUID.fromString(ROHM_BLE_PROPATIES));
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mConnGatt.writeDescriptor(desc);
            Log.e(TAG, "setNotificationEnable");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic ch){
            String strReceived = ch.getStringValue(0);
            Log.d(TAG, "Rcved! sz[" + strReceived.length() + "]  str["+ strReceived + "]");
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
