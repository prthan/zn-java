package zn.dio.model;

public class Value
{
  private int type;
  private String data;
  private String format;
  private Object objvalue;

  public static final Value NULL=new Value(ValueType.TYPE_NULL, null, null);
  public Value()
  {
    
  }

  public Value(int type, Object objvalue)
  {
    this.type=type;
    this.objvalue=objvalue;
  }
  
  public Value(int type, String data, String format)
  {
    this.type=type;
    this.data=data;
    this.format=format;
  }

  public String toString()
  {
    return "<"+type+","+data+">";
  }

  public void setType(int type)
  {
    this.type = type;
  }

  public int getType()
  {
    return type;
  }

  public void setData(String data)
  {
    this.data = data;
  }

  public String getData()
  {
    return data;
  }

  public void setFormat(String format)
  {
    this.format = format;
  }

  public String getFormat()
  {
    return format;
  }
  
  public void setObjValue(Object obj)
  {
    objvalue=obj;
  }

  public Object getObjValue()
  {
    return objvalue;
  }
}
