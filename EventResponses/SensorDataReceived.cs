using System;

public class SensorDataReceived: RawDataReceived
{
    public int index;
    public float accX;
    public float accY;
    public float accZ;
    public float gyroX;
    public float gyroY;
    public float gyroZ;

    public SensorDataReceived Init()
    {
        index = data[0];
        accX = BitConverter.ToSingle(data, 1);
        accY = BitConverter.ToSingle(data, 5);
        accZ = BitConverter.ToSingle(data, 9);
        gyroX = BitConverter.ToSingle(data, 13);
        gyroY = BitConverter.ToSingle(data, 17);
        gyroZ = BitConverter.ToSingle(data, 21);
        
        return this;
    }

    public string ToStringCSV()
    {
        return deviceAddress + "," + DateTime.Now.ToString("HH:mm:ss.fff") + "," + index + "," + accX + "," + accY +
               "," + accZ + "," + gyroX + "," + gyroY + "," + gyroZ;
    }
}