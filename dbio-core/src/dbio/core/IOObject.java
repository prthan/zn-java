package dbio.core;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import zn.Json;
import zn.model.JSONObject;

@SuppressWarnings("unchecked")
public class IOObject extends LinkedHashMap<String, Object> implements JSONObject
{
  public IOObject()
  {

  }

  public static IOObject fromMap(Map<String, Object> map)
  {
    if(map==null) return null;

    IOObject obj=new IOObject();
    obj.putAll(map);
    return obj;
  }
  
  public static IOObject fromJsonString(String jsonString)
  {
    Gson gson=new Gson();
    Map<String,Object> map=gson.fromJson(jsonString, new TypeToken<Map<String, Object>>(){}.getType());
    IOObject o=new IOObject();
    o.putAll(map);
    return o;
  }

  public static IOObject fromFile(String filename) throws IOException
  {
    Gson gson=new Gson();
    FileReader reader=new FileReader(filename);
    Map<String,Object> map=gson.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
    reader.close();
    
    IOObject o=new IOObject();
    o.putAll(map);
    return o;
  }

  public String toJsonString(){return Json.stringify(this);}
  public String toString() {return this.toJsonString();};

  public <CT> CT $(String field, Class<CT> clazz)
  {
    return clazz.cast(this.get(field));
  }

  public <CT> CT $(String field, Class<CT> clazz, CT defaultValue)
  {
    return clazz.cast(this.getOrDefault(field, defaultValue));
  }

  public boolean isnull(String field)
  {
    return this.get(field) == null;
  }

  public IOObject $(String field)
  {
    return IOObject.fromMap((Map<String, Object>)this.get(field));
  }
}

