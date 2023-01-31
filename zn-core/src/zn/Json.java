package zn;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Json 
{
  private static Gson gson()
  {
    GsonBuilder gbuilder=new GsonBuilder();
    return gbuilder.create();
  }

  private static Gson gson(boolean pretty, boolean includeNulls)
  {
    GsonBuilder gbuilder=new GsonBuilder();
    if(includeNulls) gbuilder.serializeNulls();
    if(pretty) gbuilder.setPrettyPrinting();

    return gbuilder.create();
  }


  public static String stringify(Object o) 
  {
    return gson().toJson(o);
  }

  public static String stringifyPretty(Object o) 
  {
    return gson(true, false).toJson(o);
  }

  public static String stringify(Object o, boolean pretty, boolean includeNulls) 
  {
    return gson(pretty, includeNulls).toJson(o);
  }

  public static <T> T parseJson(String str, Class<T> clazz) 
  {
    return gson().fromJson(str, clazz);
  }

  public static <T> T parseJson(String str, Type type) 
  {
    return gson().fromJson(str, type);
  }

  public static <T> T fromFile(String fileName, Class<T> clazz)
  {
    T obj=null;
    try
    {
      FileReader stream=new FileReader(fileName);
      obj=gson().fromJson(stream, clazz);
      stream.close();
    }
    catch(IOException ex)
    {
      ex.printStackTrace();
      obj=null;
    }

    return obj;
  }

  public static <T> T fromStream(InputStream stream, Class<T> clazz)
  {
    T obj=gson().fromJson(new InputStreamReader(stream), clazz);
    return obj;
  }

  public static void toFile(Object obj, String fileName) throws IOException
  {
    FileWriter stream=new FileWriter(fileName);
    gson(true, false).toJson(obj, stream);
    stream.close();
  }
}
