package io.syncsense.lib.sensorhandler;

import android.bluetooth.BluetoothGattCharacteristic;

public class CharacteristicItemJson {
    public  String characteristicUuid;

    public CharacteristicItemJson() {
    }

    public CharacteristicItemJson(BluetoothGattCharacteristic characteristic) {
        this.characteristicUuid = characteristic.getUuid().toString();;
    }
}
