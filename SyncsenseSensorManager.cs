using System;
using System.IO;
using UnityEngine;

/// <summary>
/// This class is the main entry point for the Syncsense Sensor SDK.
///
/// It is a singleton and should be accessed through the Instance property.
///
/// Copyright (c) 2020 Syncsense All rights reserved.
/// </summary>
public class SyncsenseSensorManager : Singleton<SyncsenseSensorManager>
{
    public bool debug = false;
    
    private bool _inited = false;
    
    private AndroidJavaObject _pluginInstance;

    public static event Action<ScanResult> OnScanResultEvent;
    public static event Action<ScanError> OnScanErrorEvent;
    public static event Action<ConnectionStateChange> OnDeviceConnectionStateChangeEvent;
    public static event Action<ServicesDiscovered> OnServicesDiscoveredEvent;
    public static event Action<RawDataReceived> OnRawDataReceivedEvent;
    public static event Action<SensorDataReceived> OnSensorDataReceivedEvent;
    public static event Action<BatteryDataReceived> OnBatteryDataReceivedEvent;

    public const string MOTION_SERVICE_UUID = "49740000-0f51-43fc-be01-5ce169d39b47";
    public const string ACCL_GYRO_CHARACTERISTIC_UUID = "49740004-0f51-43fc-be01-5ce169d39b47";
    
    public const string BATTERY_SERVICE_UUID =        "0000180f-0000-1000-8000-00805f9b34fb";
    public const string BATTERY_CHARACTERISTIC_UUID = "00002a19-0000-1000-8000-00805f9b34fb";
    
    public const string LED_SERVICE_UUID =        "49730000-0f51-43fc-be01-5ce169d39b47";
    public const string LED1_CHARACTERISTIC_UUID = "49730001-0f51-43fc-be01-5ce169d39b47";
    public const string LED2_CHARACTERISTIC_UUID = "49730002-0f51-43fc-be01-5ce169d39b47";
    
    public bool writeToFile = false;
    public int writeToFileBufferSize = 128;
    private string _pathForLogs = null;
    private StreamWriter _logWriter;

    new void Awake() {
        base.Awake();
        
        if (Application.platform == RuntimePlatform.Android) {
            using (AndroidJavaClass pluginClass = new AndroidJavaClass("io.syncsense.lib.sensorhandler.SyncsenseSensorManager")) {
                if (pluginClass != null)
                {
                    _pluginInstance = new AndroidJavaObject("io.syncsense.lib.sensorhandler.SyncsenseSensorManager",
                        gameObject.name, debug);
                    
                    _inited = true;
                }
            }
        }
        else
        {
            // TODO - on Editor
            _inited = true;
        }    
        
        _pathForLogs = Application.persistentDataPath;
        if (debug) Debug.Log("SyncsenseSensorManager - Path for log files: " + _pathForLogs);
        
        if (writeToFile)
        {
            InitializeWriteToLogFile();
        }
    }

    /// <summary>
    /// Check to see if the app has Access Fine Location permissions granted
    /// </summary>
    public bool HasPermissions()
    {
        return _pluginInstance.Call<bool>("hasPermissions");
    }
    
    /// <summary>
    /// Checks to see if Bluetooth is enabled on the device.
    /// </summary>
    /// <returns> true if bluetooth is enabled, or false if disabled/unsupported </returns>
    public bool IsBluetoothEnabled()
    {
        return _pluginInstance.Call<bool>("isBluetoothEnabled");
    }
    
    /// <summary>
    /// Requests the user to enable Bluetooth on the device.
    ///
    /// Callback id for the request is 11111199 in case you require to implement `onRequestPermissionsResult`
    /// on the main activity of you project.
    /// </summary>
    public void RequestBluetoothEnable()
    {
        _pluginInstance.Call("requestBluetoothEnable");
    }
    
    public void RequestPermissions()
    {
        _pluginInstance.Call("requestPermissions");
    }

    /// <summary>
    /// Attempts to start a BLE scan for an undetermined amount of time.
    ///
    /// It is only stopped once the StopScan method is called.
    /// </summary>
    public void StartScan()
    {
        _pluginInstance.Call("startScan");
    }
    
    /// <summary>
    /// Attempts to start a BLE scan for a given amount of time or if StopScan is called.
    /// </summary>
    /// <param name="scanTimeMilliseconds"> the amount of time until it is stopped automatically.
    /// If value <= 0 it runs until StopScan is called </param>
    public void StartScan(int scanTimeMilliseconds)
    {
        _pluginInstance.Call("startScan", scanTimeMilliseconds);
    }

    /// <summary>
    /// Stops the current BLE scan.
    /// </summary>
    public void StopScan()
    {
        _pluginInstance.Call("stopScan");
    }
    
    /// <summary>
    /// Async method to attempt to connect to a BLE device with the given address.
    ///
    /// Connection to a BLE device is a multiple stage process, and this method only attempts to start the connection. Updates on the connection status
    /// is received through the OnConnectionStateChangeEvent.
    /// 
    /// </summary>
    /// <param name="deviceAddress">the address</param>
    /// <returns> true if the attempt to connect the the device is performed successfully. Returns false if the device can not be found on the scan, or if the device is already connected </returns>
    public bool ConnectToDevice(string deviceAddress)
    {
        return _pluginInstance.Call<bool>("connectToDevice", deviceAddress);
    }
    
    /// <summary>
    ///  Async method to attempt to disconnect from a BLE device with the given address.
    /// </summary>
    /// <param name="deviceAddress"> the address </param>
    /// <returns>true if the device is considered as connected and disconnect was called, false if the device is not considered as connected.</returns>
    public bool DisconnectFromDevice(string deviceAddress)
    {
        return _pluginInstance.Call<bool>("disconnectFromDevice", deviceAddress);
    }
    
    /// <summary>
    /// 
    /// </summary>
    /// <param name="deviceAddess"></param>
    /// <returns></returns>
    public bool DiscoverServicesForDevice(string deviceAddess)
    {
        return _pluginInstance.Call<bool>("discoverServicesForDevice", deviceAddess);
    }
    
    /// <summary>
    /// Attempts to subscript to Characteristic notification, of a particular characteristic on a particular service of a particular device.
    ///
    /// It may fail for the following reasons:
    ///  - The device this address corresponds to is not connected
    ///  - The device for this address does not have a service with the provided uuidService
    ///  - The device for this address does not have a characteristic with the provided uuidService and uuidCharacteristic
    ///  - Unable to enable Characteristics notifications at the Characteristic level
    ///  - Unable to write enabled Characteristics notifications back to the device
    ///
    /// If successful, updates of the characteristic value will be received through the OnDataReceived method.
    ///
    /// A failure to subscribe to a characteristic will not trigger the OnDataReceived method.
    ///
    /// A failure to subscribe to a characteristic due to enabling at any level, like Characteristic level or writing back to device can be and should be retried. 
    /// </summary>
    /// <param name="deviceAddress"> the device address</param>
    /// <param name="serviceUuid"> the service uuid</param>
    /// <param name="characteristicUuid"> the characteristic uuid</param>
    /// <returns>true if subscription was successfull, false for the reasons described above</returns>
    public bool SubscribeToCharacteristic(string deviceAddress, string serviceUuid, string characteristicUuid)
    {
        return _pluginInstance.Call<bool>("subscribeToCharacteristic", deviceAddress, serviceUuid, characteristicUuid);
    }
    
    public bool UnsubscribeToCharacteristic(string deviceAddress, string serviceUuid, string characteristicUuid)
    {
        return _pluginInstance.Call<bool>("unsubscribeToCharacteristic", deviceAddress, serviceUuid, characteristicUuid);
    }

    public bool WriteToCharacteristic(string deviceAddress, string serviceUuid, string characteristicUuid, byte[] data)
    {
        return _pluginInstance.Call<bool>("writeCharacteristicNoResponse", deviceAddress, serviceUuid, characteristicUuid, data);
    }
    
    public bool SubscribeToSensorData(string deviceAddress)
    {
        return SubscribeToCharacteristic(deviceAddress, MOTION_SERVICE_UUID, ACCL_GYRO_CHARACTERISTIC_UUID);
    }
    
    public bool UnsubscribeToSensorData(string deviceAddress)
    {
        return UnsubscribeToCharacteristic(deviceAddress, MOTION_SERVICE_UUID, ACCL_GYRO_CHARACTERISTIC_UUID);
    }
    
    public bool SubscribeToBatteryData(string deviceAddress)
    {
        return SubscribeToCharacteristic(deviceAddress, BATTERY_SERVICE_UUID, BATTERY_CHARACTERISTIC_UUID);
    }
    
    public bool UnsubscribeToBatteryData(string deviceAddress)
    {
        return UnsubscribeToCharacteristic(deviceAddress, BATTERY_SERVICE_UUID, BATTERY_CHARACTERISTIC_UUID);
    }
    
    public bool EnableGreenLed(string deviceAddress, bool enable)
    {
        return WriteToCharacteristic(deviceAddress, LED_SERVICE_UUID, LED2_CHARACTERISTIC_UUID, new byte[] { (byte) (enable ? 1 : 0) });
    }
    
    public bool EnableRedLed(string deviceAddress, bool enable)
    {
        return WriteToCharacteristic(deviceAddress, LED_SERVICE_UUID, LED1_CHARACTERISTIC_UUID, new byte[] { (byte) (enable ? 1 : 0) });
    }
    
    // ------------------------------------------------------------------------
    
    /// <summary>
    /// Enables or disables the writing of the received data to a CSV file.
    /// 
    /// </summary>
    /// <param name="enable"></param>
    public void EnableWriteToFile(bool enable)
    {
        if (enable == writeToFile)
            return;
        
        if (enable)
        {
            InitializeWriteToLogFile();
            writeToFile = true;
        }
        else
        {
            writeToFile = false;
            // TO STOP WRITING TO FILE
            if (_logWriter != null)
            {
                _logWriter.Flush();
                _logWriter.Close();
                _logWriter = null;
            }
        }
    }
    
    /// <summary>
    /// Returns true if the writing of the received data to a CSV file is enabled.
    /// </summary>
    /// <returns></returns>
    public bool IsWriteToFileEnabled()
    {
        return writeToFile;
    }
    
    private void InitializeWriteToLogFile()
    {
        string filePath = Path.Combine(_pathForLogs, "LOG-" + DateTime.Now.ToString("yyyy-MM-dd_HHmmss") + ".csv");
        _logWriter = new StreamWriter(filePath, true);
        _logWriter.WriteLine("Mac Address, Timestamp, index, Acc X, Acc Y, Acc Z, Gyro X, Gyro Y, Gyro Z");
    }
    
    private void OnDestroy()
    {
        if (_logWriter != null)
        {
            _logWriter.Flush();
            _logWriter.Close();
            _logWriter = null;
        }
    }

    /// <summary>
    /// This method gets called from the Native android plugin when a new BLE device is found.
    /// </summary>
    /// <param name="rawData"> a String of a json object </param>
    private void OnScanResult(string rawData)
    {
        OnScanResultEvent?.Invoke(JsonUtility.FromJson<ScanResult>(rawData));
    }
    
    /// <summary>
    /// This method gets called from the Native android plugin when an error regarding scanning for BLE devices occurs.
    /// </summary>
    /// <param name="rawData"> a String of a json object </param>
    private void OnScanError(string rawData)
    {
        OnScanErrorEvent?.Invoke(JsonUtility.FromJson<ScanError>(rawData));
    }
    
    /// <summary>
    /// When a connecting operation to a BLE device or disconnect from a BLE device is performed, this method receives the status of the operation.
    ///
    /// When a disconnect happens the device is automatically removed from the list of connected devices.
    ///
    /// An unexpected disconnect will also trigger this method. 
    /// </summary>
    /// <param name="rawData"> a String of a json object </param>
    private void OnDeviceConnectionStateChange(string rawData)
    {
        OnDeviceConnectionStateChangeEvent?.Invoke(JsonUtility.FromJson<ConnectionStateChange>(rawData));
    }

    /// <summary>
    /// When DiscoverServicesForDevice is called successfully, this method will return asynchronously the results of the operation.
    /// </summary>
    /// <param name="rawData"> a String of a json object </param>
    private void OnServicesDiscovered(string rawData)
    {
        OnServicesDiscoveredEvent?.Invoke(JsonUtility.FromJson<ServicesDiscovered>(rawData));
    }
    
    /// <summary>
    /// When subscribed to a characteristic, this method will receive the updates of the characteristic value.
    /// </summary>
    /// <param name="rawData"></param>
    private void OnDataReceived(string rawData)
    {
        RawDataReceived tempData = JsonUtility.FromJson<RawDataReceived>(rawData)
            .Init();
        OnRawDataReceivedEvent?.Invoke(tempData);

        if (tempData.characteristicUuid.ToLower().Equals(ACCL_GYRO_CHARACTERISTIC_UUID.ToLower()))
        {
            SensorDataReceived sensorDataReceived = tempData.ToSensorDataReceived();
            OnSensorDataReceivedEvent?.Invoke(sensorDataReceived);
            
            if (writeToFile)
            {
                _logWriter.WriteLine(sensorDataReceived.ToStringCSV());
            }
        }
        else if (tempData.characteristicUuid.ToLower().Equals(BATTERY_CHARACTERISTIC_UUID.ToLower()))
        {
            OnBatteryDataReceivedEvent?.Invoke(tempData.ToBatteryDataReceived());
        }
        
        
    }
    
}
