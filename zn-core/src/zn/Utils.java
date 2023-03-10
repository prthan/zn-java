package zn;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils 
{
  public static Properties propertiesFromResource(String res)
  {
    Properties properties=null;
    
    try
    {
      InputStream stream=Utils.class.getResourceAsStream(res);
      properties=new Properties();
      properties.load(stream);
    }
    catch(Exception ex)
    {
      System.out.println("error occured while loading properties from resource "+res+", "+ex.toString());
      ex.printStackTrace();
    }
    
    return properties;
  }

  public static Properties propertiesFromFile(String fileName)
  {
    Properties properties=null;
    
    try
    {
      FileInputStream stream=new FileInputStream(fileName);
      properties=new Properties();
      properties.load(stream);
      stream.close();
    }
    catch(Exception ex)
    {
      System.out.println("error occured while loading properties from file "+fileName+", "+ex.toString());
      ex.printStackTrace();
    }
    
    return properties;
  }

  public static byte[] streamToBytes(InputStream stream) throws IOException
  {
    if (stream == null) return null;

    ByteArrayOutputStream byteStream=new ByteArrayOutputStream();

    byte[] buff = new byte[1024 * 2];
    int x = 0;
    while ((x = stream.read(buff)) != -1) byteStream.write(buff, 0, x);

    return byteStream.toByteArray();
  }

  public static String streamToString(InputStream stream) throws IOException
  {
    if (stream == null) return null;

    StringBuilder sb = new StringBuilder();
    byte[] buff = new byte[1024 * 2];
    int x = 0;
    while ((x = stream.read(buff)) != -1) sb.append(new String(buff, 0, x));

    return sb.toString();
  }
  
  public static String fileToString(String fileName)
  {
    String str=null;
    try
    {
      FileInputStream stream=new FileInputStream(fileName);
      str=streamToString(stream);
      stream.close();
    }
    catch(Exception ex)
    {
      System.out.println("error occured while reading from file " + fileName + ", "+ex.toString());
      ex.printStackTrace();
    }
    
    return str;
  }
  
  public static String resourceToString(String res)
  {
    String str=null;
    try
    {
      str=streamToString(Utils.class.getResourceAsStream(res));
    }
    catch(Exception ex)
    {
      System.out.println("error occured while reading from resource " + res + ", "+ex.toString());
      ex.printStackTrace();
    }
    
    return str;
  }

  public static String expandStringTemplate(String template, Map<String, ? extends Map<String, String>> ctxvars, List<String> missingVars)
  {
    Pattern pattern=Pattern.compile("\\$\\{(.+?)\\}");
    
    int s=0;
    StringBuilder sb=new StringBuilder();
    Matcher matcher=pattern.matcher(template);
    while(matcher.find())
    {
      sb.append(template.substring(s, matcher.start()));
      String match=matcher.group(0);
      String varToken=match.substring(2, match.length()-1);
      String[] varParts=varToken.split("\\.");
      Map<String, String> ctx=ctxvars.get(varParts[0].toLowerCase());
      if(ctx!=null)
      {
        String varValue=ctx.get(varParts[1]);
        if(varValue!=null) sb.append(ctx.get(varParts[1]));
        else
        {
          sb.append(match);
          if(missingVars!=null) missingVars.add(varToken);
        }
      }
      else sb.append(match);
      s=matcher.end();
    }
    sb.append(template.substring(s));
    return sb.toString();
  }

  public static Map<String, String> propertiesToMap(Properties props)
  {
    Map<String, String> map=new HashMap<String, String>();
    for(Map.Entry<Object, Object> entry:props.entrySet())
    {
      map.put((String)entry.getKey(), entry.getValue().toString());
    }

    return map;
  }

  public static String shortid()
  {
    char[] a=Long.toString(Double.doubleToLongBits(Math.random()), 36).substring(2).toCharArray();
    char[] b=Long.toString(Double.doubleToLongBits(System.currentTimeMillis()), 36).toCharArray();
    int al=a.length;
    int bl=b.length;
    String[] rval=new String[al+bl];

    for(int i=0; i<al; i++)
    {
      rval[i*2]=""+a[i];
      if(i<bl) rval[i*2+1]=""+b[i];
    }
    for(int i=al+1;i<bl;i++) rval[al*2 + bl-i]=""+b[i];
    return String.join("", rval);
  }

  public static String calcTimeDiff(long startTime, long endTime)
  {
    String rval="";
    double servTime=TimeUnit.MILLISECONDS.convert(endTime-startTime, TimeUnit.NANOSECONDS);
    
    rval = String.format("%.3f ms", servTime);
    if(servTime>1000)
    {
      servTime /=1000;
      rval = String.format("%.3f s", servTime);
    }

    return rval;
  }

  public static <T> T mapValue(Map<?,?> map, String path, Class<T> clazz)
  {
    Object value=null;
    Map<?, ?> m=map;

    for(String part:path.split("\\."))
    {
      value=m.get(part);
      if(value==null) return null;
      if(value instanceof Map) m=(Map<?,?>)value;
    }

    return clazz.cast(value);
  }

  public static String mapValue(Map<?,?> map, String path)
  {
    return mapValue(map, path, String.class);
  }

  public static <T> T mapToObj(Map<?,?> map, Class<T> objClass)
  {
    return Json.parseJson(Json.stringify(map), objClass);
  }
  
  public final static SimpleDateFormat DTTM_HTTP_HEADER_FMT=new SimpleDateFormat("EEE, dd MMM YYYY HH:mm:ss z");
  static 
  {
    DTTM_HTTP_HEADER_FMT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  public final static SimpleDateFormat DTTM_RFC3339_FMT=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");

  public static void main(String[] args)
  {
    String v="db.test";
    for(String p:v.split("."))
    {
      System.out.println(">>>"+p);
    }
  }
}
