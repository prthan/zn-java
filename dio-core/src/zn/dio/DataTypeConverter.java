package zn.dio;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import zn.dio.model.ValueType;

public class DataTypeConverter
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

  public static Date str2date(String str, String format, Locale locale)
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

  public static Boolean str2bool(String str)
  {
    Boolean rval=null;
    try
    {
      Boolean.parseBoolean(str);
    }
    catch(Exception ex)
    {
      rval=null;
    }
    return rval;
  }

  public static String num2str(Double value, String format, Locale locale)
  {
    String rval;
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
    
    if(fmt!=null) rval=fmt.format(value);
    else rval=value.toString();

    return rval;
  }

  public static String date2str(Date date, String format, Locale locale)
  {
    String rval;
    SimpleDateFormat fmt=null;
    if(format==null) fmt=new SimpleDateFormat("dd/MM/yyyy");
    else fmt=new SimpleDateFormat(format);
    
    rval=fmt.format(date);
    return rval;

  }

  public static String bool2str(Boolean bool)
  {
    return bool.toString();
  }  

  public static Object convert(int fromType, String fromValue, String fromFormat, int toType, String toFormat, Locale locale)
  {
    if(fromType==ValueType.TYPE_STRING)
    {
      if(toType==ValueType.TYPE_STRING) return fromValue;
      if(toType==ValueType.TYPE_NUMERIC) return str2num(fromValue, fromFormat, locale);
      if(toType==ValueType.TYPE_DATE) return str2date(fromValue, fromFormat, locale);
      if(toType==ValueType.TYPE_BOOLEAN) return str2bool(fromValue);
    }

    if(fromType==ValueType.TYPE_DATE)
    {
      Date date=str2date(fromValue, fromFormat, locale);
      if(toType==ValueType.TYPE_DATE) return date;
      if(toType==ValueType.TYPE_STRING) return date2str(date, toFormat, locale);
      if(toType==ValueType.TYPE_NUMERIC) return date.getTime();
    }

    if(fromType==ValueType.TYPE_NUMERIC)
    {
      Double num=str2num(fromValue, fromFormat, locale);
      if(toType==ValueType.TYPE_NUMERIC) return num;
      if(toType==ValueType.TYPE_STRING) return num2str(num, toFormat, locale);
      if(toType==ValueType.TYPE_DATE) return new Date(num.longValue());
    }

    if(fromType==ValueType.TYPE_BOOLEAN)
    {
      Boolean bool=str2bool(fromValue);
      if(toType==ValueType.TYPE_BOOLEAN) return bool;
      if(toType==ValueType.TYPE_STRING) return bool2str(bool);
    }

    return null;
  }
}
