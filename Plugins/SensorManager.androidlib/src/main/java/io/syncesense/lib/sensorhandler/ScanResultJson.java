package io.syncsense.lib.sensorhandler;

import static io.syncsense.lib.sensorhandler.JsonUtils.GSON;

import com.google.gson.Gson;

public class ScanResultJson {

    public int rssi;
    public String name;
    public String address;

    public ScanResultJson() {
    }

    public ScanResultJson(int rssi, String name, String address) {
        this.rssi = rssi;
        this.name = name;
        this.address = address;
    }

    public static String buildJson(String name, String address, int rssi) {
        return GSON.toJson(new ScanResultJson(
                rssi,
                name,
                address
        ));
    }
}
