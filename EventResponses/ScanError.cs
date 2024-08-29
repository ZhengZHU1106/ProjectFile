[System.Serializable]
public sealed class ScanError
{
    public int errorCode;
}

public sealed class ScanErrorTypes
{
    public static int SCAN_FAILED_ALREADY_STARTED = 1;
    public static int SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = 2;
    public static int SCAN_FAILED_FEATURE_UNSUPPORTED = 4;
    public static int SCAN_FAILED_INTERNAL_ERROR = 3;
    public static int SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES = 5;
    public static int SCAN_FAILED_SCANNING_TOO_FREQUENTLY = 6;
    
    public static int BLUETOOTH_NOT_ENABLED = -5;
    public static int MISSING_BLUETOOTH_SCAN_PERMISSIONS = -4;
    public static int MISSING_ACCESS_FINE_LOCATION_PERMISSIONS = -3;
}