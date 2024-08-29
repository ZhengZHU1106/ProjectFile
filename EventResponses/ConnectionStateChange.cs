[System.Serializable]
public sealed class ConnectionStateChange
{
    
    public int status;
    public int newState;
    public string deviceName;
    public string deviceAddress;
    
}

public sealed class ConnectionState
{
    public const int STATE_DISCONNECTED = 0;
    public const int STATE_CONNECTING = 1;
    public const int STATE_CONNECTED = 2;
    public const int STATE_DISCONNECTING = 3;
}

public sealed class ConnectionStatus
{
    public const int GATT_SUCCESS = 0;
    public const int GATT_BLUETOOTH_HARDWARE_TIMEOUT = 8;
}