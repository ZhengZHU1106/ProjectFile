package io.syncsense.lib.sensorhandler;

import static io.syncsense.lib.sensorhandler.JsonUtils.GSON;

public class ScanErrorJson {

    public int errorCode;

    public ScanErrorJson() {
    }

    public ScanErrorJson(int errorCode) {
        this.errorCode = errorCode;
    }

    public static String buildJson(int errorCode) {
        return GSON.toJson(new ScanErrorJson(
                errorCode
        ));
    }

    public enum ScanErrorCustomCodes {
        BLUETOOTH_NOT_ENABLED(-5),
        MISSING_BLUETOOTH_SCAN_PERMISSIONS(-4),
        MISSING_ACCESS_FINE_LOCATION_PERMISSIONS(-3),
        ;

        public final int errorCode;
        ScanErrorCustomCodes(int i) {
            errorCode = i;
        }
    }
}