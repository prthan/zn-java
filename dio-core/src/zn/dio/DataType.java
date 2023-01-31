package zn.dio;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataType
{
  public static Double str2num(String str, String format, Locale locale)
  {
    Double rval;
    DecimalFormat fmt=null;
    
    if(format!=null&&!"".equals(format))
    {
      if(locale!=null)
      {
        fmt = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        fmt.applyPattern(format);
      }
      else fmt=new DecimalFormat(format);
    }
    
    if(fmt!=null)
    {
      ParsePosition pos=new ParsePosition(0);
      Number num=fmt.parse(str, pos);
      if(pos.getIndex()==str.length())
      {
        rval=num.doubleValue();
      }
      else rval=null;
    }
    else
    {
      try
      {
        rval=Double.parseDouble(str);
      }
      catch(Exception ex)
      {
        rval=null;
      }
    }

    return rval;
  }

  public static Date str2date(String str, String format)
  {
    Date rval;
    SimpleDateFormat fmt=null;
    if(format==null) fmt=new SimpleDateFormat("dd/MM/yyyy");
    else fmt=new SimpleDateFormat(format);
    
    fmt.setLenient(false);
    try
    {
      rval=fmt.parse(str);  
    }
    catch(ParseException ex)
    {
      rval=null;
    }
    
    return rval;
  }
}
