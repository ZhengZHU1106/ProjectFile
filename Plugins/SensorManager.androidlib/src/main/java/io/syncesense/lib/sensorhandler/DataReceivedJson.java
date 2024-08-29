package io.syncsense.lib.sensorhandler;

import static io.syncsense.lib.sensorhandler.JsonUtils.GSON;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Base64;

public class DataReceivedJson{

    public String deviceAddress;
    public String deviceName;
    public String characteristicUuid;
    public String dataBase64;

    public DataReceivedJson() {
    }

    public DataReceivedJson( String deviceName, String deviceAddress, String characteristicUuid, String dataBase64) {
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.characteristicUuid = characteristicUuid;
        this.dataBase64 = dataBase64;
    }

    @SuppressLint("MissingPermission")
    public static String buildJson(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        return GSON.toJson(new DataReceivedJson(
                gatt.getDevice().getName(),
                gatt.getDevice().getAddress(),
                characteristic.getUuid().toString(),
                Base64.encodeToString(characteristic.getValue(), Base64.DEFAULT)
        ));
    }
}
