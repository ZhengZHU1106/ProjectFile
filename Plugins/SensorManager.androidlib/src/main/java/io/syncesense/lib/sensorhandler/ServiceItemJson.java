package io.syncsense.lib.sensorhandler;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;

public class ServiceItemJson {

    public String serviceUuid;
    public int type;
    public int instanceId;

    public List<CharacteristicItemJson> characteristics = new ArrayList<>();

    public ServiceItemJson() {
    }

    public ServiceItemJson(BluetoothGattService service) {
        this.serviceUuid = service.getUuid().toString();
        this.type = service.getType();
        this.instanceId = service.getInstanceId();

        for (BluetoothGattCharacteristic characteristic: service.getCharacteristics()) {
            characteristics.add(new CharacteristicItemJson(characteristic));
        }
    }
}
