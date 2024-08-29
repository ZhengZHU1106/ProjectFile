using System;

public class BatteryDataReceived: RawDataReceived
{
    public int batteryLevel;
    
    public BatteryDataReceived Init()
    {
        base.Init();
        batteryLevel = int.Parse(BitConverter.ToString(data), System.Globalization.NumberStyles.HexNumber);
        return this;
    }
}