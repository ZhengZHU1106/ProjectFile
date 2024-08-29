package io.syncsense.lib.sensorhandler;

import static io.syncsense.lib.sensorhandler.JsonUtils.GSON;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;

public class ServicesDiscoveredJson {

    public int status;
    public String deviceName;
    public String deviceAddress;
    public List<ServiceItemJson> services = new ArrayList<>();

    public ServicesDiscoveredJson() {
    }

    @SuppressLint("MissingPermission")
    public ServicesDiscoveredJson(BluetoothGatt gatt, int status) {
        this.status = status;
        this.deviceName = gatt.getDevice().getName();
        this.deviceAddress = gatt.getDevice().getAddress();

        for (BluetoothGattService service: gatt.getServices()) {
            services.add(new ServiceItemJson(service));
        }
    }

    public static String buildJson(BluetoothGatt gatt, int status) {
        return GSON.toJson(new ServicesDiscoveredJson(
                gatt, status
        ));
    }

}
