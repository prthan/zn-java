package dbio.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils 
{
  public static String toCamelcase(String str)
  {
    StringBuilder sb=new StringBuilder();
    boolean upper=false;
    for(char c:str.toCharArray())
    {
      if(c=='_')
      {
        upper=true;
        continue;
      }
      if(upper)
      {
        sb.append((""+c).toUpperCase());
        upper=false;
      }
      else sb.append((""+c).toLowerCase());
    }
    
    return sb.toString();
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

  public static Integer toInteger(String val)
  {
    Integer ival=null;
    if(val!=null)
    {
      try{ival=Integer.parseInt(val);}
      catch(Exception ex){}
    }

    return ival;
  }

  public static String streamToStr(InputStream stream) throws IOException
  {
    if (stream == null) return null;

    StringBuilder sb = new StringBuilder();
    byte[] buff = new byte[1024 * 2];
    int x = 0;
    while ((x = stream.read(buff)) != -1) sb.append(new String(buff, 0, x));

    return sb.toString();
  }
  
  public static String fileToStr(String fileName)
  {
    String str=null;
    try
    {
      FileInputStream stream=new FileInputStream(fileName);
      str=streamToStr(stream);
      stream.close();
    }
    catch(Exception ex)
    {
      System.out.println("error occured while reading from file " + fileName + ", "+ex.toString());
      ex.printStackTrace();
    }
    
    return str;
  }
  
  public static String resToStr(String res)
  {
    String str=null;
    try
    {
      str=streamToStr(Utils.class.getResourceAsStream(res));
    }
    catch(Exception ex)
    {
      System.out.println("error occured while reading from resource " + res + ", "+ex.toString());
      ex.printStackTrace();
    }
    
    return str;
  }  
}
