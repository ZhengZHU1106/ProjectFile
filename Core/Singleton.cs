using UnityEngine;

public abstract class Singleton<T> : MonoBehaviour where T : MonoBehaviour
{
    private static T instance;
    public static T Instance => instance;

    protected void Awake()
    {
        if (instance != null && instance != this)
        {
            throw new System.Exception("Object of type " + typeof(T).ToString() + " is already used in another game object");
        }
        else
        {
            instance = this as T;
            DontDestroyOnLoad(gameObject);
        }
    }
}