package zn;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration extends LinkedHashMap<String, Object>
{
  private Configuration(){};
  
  private static Configuration __instance;
  public static Configuration getInstance() {return __instance;}

  public static void createInstanceFromJsonFile(String fileName)
  {
    __instance=Json.fromFile(fileName, Configuration.class);
  }

  public static void createInstanceFromResource(String resource)
  {
    InputStream stream=Configuration.class.getResourceAsStream(resource);
    __instance=Json.fromStream(stream, Configuration.class);
  }

  public <T> T $(String name, Class<T> clazz)
  {
    Object value=null;
    String clazzName=clazz.getName();
    if(String.class.getName().equals(clazzName))
    {
      value=Utils.mapValue(this, name, clazz);
      return value!=null ? clazz.cast(value) : null;
    }
    if(Boolean.class.getName().equals(clazzName))
    {
      value=Utils.mapValue(this, name, clazz);
      return value!=null ? clazz.cast(value) : null;
    }
    if(Number.class.isAssignableFrom(clazz))
    {
      value=Utils.mapValue(this, name, clazz);
      return value!=null ? clazz.cast(value) : null;
    }
    if(Map.class.isAssignableFrom(clazz))
    {
      value=Utils.mapValue(this, name, clazz);
      return value!=null ? Json.parseJson(Json.stringify(value), clazz) : null;
    }
    value=Utils.mapValue(this, name, Map.class);
    return value!=null ? Json.parseJson(Json.stringify(value), clazz) : null;
  }

  public String $(String name)
  {
    Object value=$(name, String.class);
    if(value==null) return null;

    return (String)value;
  }

}
