using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Text;
using UnityEngine;
using UnityEditor.Android;

public class AndroidPostProcessWifiDirect : IPostGenerateGradleAndroidProject
{
    public int callbackOrder => 0;
    public void OnPostGenerateGradleAndroidProject(string path)
    {
        string gradlePropertiesPath = path + "/../gradle.properties";
        if (!File.Exists(gradlePropertiesPath))
        {
            FileStream fileStream =  File.Create(gradlePropertiesPath);
            fileStream.Close();
        }
        string[] lines = File.ReadAllLines(gradlePropertiesPath);

        StringBuilder builder = new StringBuilder();
        foreach (string line in lines)
        {
            if (line.Contains("android.useAndroidX"))
            {
                continue;
            }
            if (line.Contains("android.enableJetifier"))
            {
                continue;
            }
            builder.AppendLine(line);
        }
        builder.AppendLine("android.useAndroidX=true");
        builder.AppendLine("android.enableJetifier=true");
        File.WriteAllText(gradlePropertiesPath, builder.ToString());
    }
}