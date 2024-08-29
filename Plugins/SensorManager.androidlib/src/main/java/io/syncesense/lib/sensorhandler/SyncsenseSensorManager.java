package io.syncsense.lib.sensorhandler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 *
 */
public class SyncsenseSensorManager {

    private static final int REQUEST_ENABLE_BT = 11111199;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 11111198;
    private static final int PERMISSION_BLUETOOTH_SCAN_LOCATION = 11111197;

    private static final String debugTag = "SyncsenseSensorManager";
    private boolean debug = false;

    private final Context context;
    private final String gameObjectName;

    private boolean permissionGranted = false;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler = new Handler();

    private final Hashtable<String, BluetoothGatt> connectedDevices = new Hashtable<>();

    private boolean mScanning = false;

    public SyncsenseSensorManager(String gameObjectName, boolean enableDebug) {
        this.context = UnityPlayer.currentActivity.getApplicationContext();
        this.gameObjectName = gameObjectName;
        this.debug = enableDebug;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        permissionGranted = hasPermissions();

        if (debug)
            Log.wtf(debugTag, "Native plugin inited. Access Fine Location permission is: " + permissionGranted);
    }

    /**
     * Checks to see if Manifest.permission.ACCESS_FINE_LOCATION permissions was Granted.
     *
     * @return boolean
     */
    public boolean hasPermissions() {
        if (permissionGranted)
            return true;

        permissionGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionGranted = context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        return permissionGranted;
    }

    public boolean isBluetoothEnabled() {

        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.e(debugTag, "This device does not support Bluetooth.");
            return false;
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                // Bluetooth is enabled
                return true;
            } else {
                // Bluetooth is disabled
                return false;
            }
        }
    }

    public void requestPermissions() {
        if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            UnityPlayer.currentActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                UnityPlayer.currentActivity.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_BLUETOOTH_SCAN_LOCATION);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        UnityPlayer.currentActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public void startScan() {
        if (debug) Log.wtf(debugTag, "Calling Scan for BLE for undetermined amount of time");
        startScan(-1);
    }

    public void startScan(int scanTimeMilliseconds) {
        if (!isBluetoothEnabled()) {
            Log.e(debugTag, "Scan could not start. Maybe bluetooth is not enabled or is unsupported");

            UnityPlayer.UnitySendMessage(gameObjectName, "OnScanError",
                    ScanErrorJson.buildJson(
                            ScanErrorJson.ScanErrorCustomCodes.BLUETOOTH_NOT_ENABLED.errorCode
                    )
            );
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (UnityPlayer.currentActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.e(debugTag, "Scan failed with error code - Missing BLUETOOTH_SCAN permission");

                UnityPlayer.UnitySendMessage(gameObjectName, "OnScanError",
                        ScanErrorJson.buildJson(
                                ScanErrorJson.ScanErrorCustomCodes.MISSING_BLUETOOTH_SCAN_PERMISSIONS.errorCode
                        )
                );
                return;
            }
        }

        if (!hasPermissions()) {
            Log.e(debugTag, "Scan failed with error code - Missing ACCESS_FINE_LOCATION permission");

            UnityPlayer.UnitySendMessage(gameObjectName, "OnScanError",
                    ScanErrorJson.buildJson(
                            ScanErrorJson.ScanErrorCustomCodes.MISSING_ACCESS_FINE_LOCATION_PERMISSIONS.errorCode
                    )
            );
            return;
        }


        if (debug) Log.wtf(debugTag, "Starting the Scan for BLE devices");
        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if (scanTimeMilliseconds > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;

                    bluetoothLeScanner.stopScan(mLeScanCallback);
                }
            }, scanTimeMilliseconds);
        }

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setDeviceName("Cadence_Sensor")
                .build();
        filters.add(filter);

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
        settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);

        mScanning = true;
        bluetoothLeScanner.startScan(filters, settingsBuilder.build(), mLeScanCallback);
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (!isBluetoothEnabled()) {
            Log.e(debugTag, "Scan could not start. Maybe bluetooth is not enabled or is unsupported");
            return;
        }

        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mScanning = false;
        bluetoothLeScanner.stopScan(mLeScanCallback);

    }

    @SuppressLint("MissingPermission")
    public boolean connectToDevice(String deviceAddress){
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        if (device!=null) {
            if (connectedDevices.containsKey(deviceAddress)){
                Log.e(debugTag, "The device this address corresponds to is already connected");
                return false;
            }
            connectedDevices.put(deviceAddress,
                    device.connectGatt(context, false, mGattCallback));
            return true;
        } else {
            Log.e(debugTag, "The device this address corresponds can not be found.");
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public boolean disconnectFromDevice(String deviceAddress){
        if (connectedDevices.containsKey(deviceAddress)){
            connectedDevices.get(deviceAddress).disconnect();
            connectedDevices.get(deviceAddress).close();
            connectedDevices.remove(deviceAddress);
            return true;
        } else {
            Log.e(debugTag, "The device this address corresponds to is not connected");
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public boolean discoverServicesForDevice(String deviceAddress){
        if (connectedDevices.containsKey(deviceAddress)){
            return connectedDevices.get(deviceAddress).discoverServices();
        } else {
            Log.e(debugTag, "The device this address corresponds to is not connected");
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public boolean subscribeToCharacteristic( String deviceAddress, String uuidService, String uuidCharacteristic){
        if (connectedDevices.containsKey(deviceAddress)){
            BluetoothGatt mDevice = connectedDevices.get(deviceAddress);
            BluetoothGattService mService = mDevice.getService(UUID.fromString(uuidService));
            if (mService == null) {
                Log.e(debugTag, "Subscription - The device for this address does not have a service with the provided uuidService");
                return false;
            }

            BluetoothGattCharacteristic mCharacteristic = mService.getCharacteristic(UUID.fromString(uuidCharacteristic));
            if (mCharacteristic == null) {
                Log.e(debugTag, "Subscription - The device for this address does not have a characteristic with the provided uuidService and uuidCharacteristic");
                return false;
            }

            // enables notifications for a specific characteristic on a BLE device at the characteristic level,
            boolean result = mDevice.setCharacteristicNotification(mCharacteristic, true);
            if (!result) {
                Log.e(debugTag, "Subscription - Unable to enable Characteristics notifications at the Characteristic level");
                return false;
            }

            // update the value of the characteristic's descriptor (typically the CCCD) to reflect this change, and finally write this updated descriptor value back to the device.
            BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptors().get(0);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            result = mDevice.writeDescriptor(descriptor);
            if (!result) {
                Log.e(debugTag, "Subscription - Unable to write enabled Characteristics notifications back to the device");
                return false;
            }
            Log.e(debugTag, "Subscription - Finishing subscription");
            return true;
        } else {
            Log.e(debugTag, "Subscription - The device this address corresponds to is not connected");
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public boolean unsubscribeToCharacteristic(String deviceAddress, String uuidService, String uuidCharacteristic) {
        BluetoothGatt mDevice = connectedDevices.get(deviceAddress);
        BluetoothGattCharacteristic mCharacteristic;

        for (BluetoothGattService service: mDevice.getServices()) {
            if (service.getUuid().toString().equals(uuidService)) {
                mCharacteristic = service.getCharacteristic(UUID.fromString(uuidCharacteristic));
                if (mCharacteristic != null) {
                
                    mDevice.setCharacteristicNotification(mCharacteristic, false); 
                    BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptors().get(0);
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    return mDevice.writeDescriptor(descriptor);
                    
                    /* Log.wtf(debugTag, "--------------------------- UNSUSBRICING ---------------------------");
                    BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptors().get(0);
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        mDevice.writeDescriptor(descriptor);
                    } */
                }
            }
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    public boolean writeCharacteristicNoResponse(String deviceAddress, String uuidService, String uuidCharacteristic, byte[] value) {
        BluetoothGatt mDevice = connectedDevices.get(deviceAddress);
        BluetoothGattCharacteristic bt_chr;

        for (BluetoothGattService service: mDevice.getServices()) {
            if (service.getUuid().toString().equals(uuidService)) {
                bt_chr = service.getCharacteristic(UUID.fromString(uuidCharacteristic));
                if (bt_chr != null) {
                    bt_chr.setValue(value);
                    return mDevice.writeCharacteristic (bt_chr);
                }
            }
        }
        return false;
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        /**
         *
         * @param callbackType ScanSettings.CALLBACK_TYPE_ALL_MATCHES, ScanSettings#CALLBACK_TYPE_FIRST_MATCH or ScanSettings#CALLBACK_TYPE_MATCH_LOST
         * @param result item reported
         */
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (debug) Log.wtf(debugTag, "Received Scan Result.");
            UnityPlayer.UnitySendMessage(gameObjectName, "OnScanResult",
                    ScanResultJson.buildJson(
                            result.getDevice().getName(),
                            result.getDevice().getAddress(),
                            result.getRssi()
                    )
            );
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            // Do nothing here
        }

        /**
         *
         * @param errorCode int: Error code (one of SCAN_FAILED_*) for scan failure. Value is SCAN_FAILED_ALREADY_STARTED, SCAN_FAILED_APPLICATION_REGISTRATION_FAILED, SCAN_FAILED_INTERNAL_ERROR, SCAN_FAILED_FEATURE_UNSUPPORTED, SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES, or SCAN_FAILED_SCANNING_TOO_FREQUENTLY
         */
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(debugTag, "Scan failed with error code: " + errorCode );

            UnityPlayer.UnitySendMessage(gameObjectName, "OnScanError",
                    ScanErrorJson.buildJson(
                            errorCode
                    )
            );
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            UnityPlayer.UnitySendMessage(gameObjectName, "OnDeviceConnectionStateChange",
                    ConnectionStateChangeJson.buildJson(
                            status,
                            newState,
                            gatt
                    )
            );

            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // connectedDevices.remove(gatt.getDevice().getAddress());
                disconnectFromDevice(gatt.getDevice().getAddress());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            UnityPlayer.UnitySendMessage(gameObjectName, "OnServicesDiscovered",
                    ServicesDiscoveredJson.buildJson(
                            gatt,
                            status
                    )
            );
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (debug) Log.wtf(debugTag, "onCharacteristicChanged!!!!");
            UnityPlayer.UnitySendMessage(gameObjectName, "OnDataReceived",
                    DataReceivedJson.buildJson(
                            gatt,
                            characteristic
                    )
            );

        }
    };
}
