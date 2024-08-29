using System.Collections.Generic;

[System.Serializable]
public class ServicesDiscovered
{
    public int status;
    public string deviceName;
    public string deviceAddress;
    public List<ServiceItem> services;
    
}