package io.syncsense.lib.sensorhandler;

import static io.syncsense.lib.sensorhandler.JsonUtils.GSON;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;

public class ConnectionStateChangeJson {

    public int status;
    public int newState;
    public String deviceName;
    public String deviceAddress;

    public ConnectionStateChangeJson() {
    }

    @SuppressLint("MissingPermission")
    public ConnectionStateChangeJson(int status, int newState, BluetoothGatt device) {
        this.status = status;
        this.newState = newState;
        this.deviceName = device.getDevice().getName();
        this.deviceAddress = device.getDevice().getAddress();
    }

    public static String buildJson(int status, int newState, BluetoothGatt device) {
        return GSON.toJson(new ConnectionStateChangeJson(
                status,
                newState,
                device
        ));
    }

}
