using System.Collections.Generic;

[System.Serializable]
public class ServiceItem
{
    public string serviceUuid;
    public int type;
    public int instanceId;
    public List<CharacteristicItem> characteristics;
    
}