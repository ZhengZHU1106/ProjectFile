using System;

public class RawDataReceived
{
    public string deviceAddress;
    public string deviceName;
    public string characteristicUuid;
    public string dataBase64;

    public byte[] data = null;
    
    public RawDataReceived Init()
    {
        data = Convert.FromBase64String(dataBase64);
        return this;
    }
    
    public SensorDataReceived ToSensorDataReceived()
    {
        SensorDataReceived tempData = new SensorDataReceived();
        tempData.deviceAddress = this.deviceAddress;
        tempData.deviceName = this.deviceName;
        tempData.characteristicUuid = this.characteristicUuid;
        tempData.dataBase64 = this.dataBase64;
        tempData.data = this.data;
        
        return tempData.Init();
    }
    
    public BatteryDataReceived ToBatteryDataReceived()
    {
        BatteryDataReceived tempData = new BatteryDataReceived();
        tempData.deviceAddress = this.deviceAddress;
        tempData.deviceName = this.deviceName;
        tempData.characteristicUuid = this.characteristicUuid;
        tempData.dataBase64 = this.dataBase64;
        tempData.data = this.data;
        
        return tempData.Init();
    }
}