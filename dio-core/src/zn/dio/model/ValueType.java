package zn.dio.model;

public class ValueType 
{
  public final static int TYPE_UNKNOWN=-1;
  public final static int TYPE_NULL=0;
  public final static int TYPE_STRING=1;
  public final static int TYPE_NUMERIC=2;
  public final static int TYPE_DATE=3;
  public final static int TYPE_BOOLEAN=4;
  public final static int TYPE_TIME=5;
  
  private final static String NUMERIC_TYPES="numeric|number|double|int|float|long";
  private final static String DATE_TYPES="date|datetime|time";
  private final static String STRING_TYPES="string|text|varchar|char";
  private final static String BOOLEAN_TYPES="bool|boolean";

  public static int from(String type)
  {
    if(type==null) return TYPE_NULL;
    if(STRING_TYPES.indexOf(type.toLowerCase())!=-1) return TYPE_STRING;
    if(NUMERIC_TYPES.indexOf(type.toLowerCase())!=-1) return TYPE_NUMERIC;
    if(DATE_TYPES.indexOf(type.toLowerCase())!=-1) return TYPE_DATE;
    if(BOOLEAN_TYPES.indexOf(type.toLowerCase())!=-1) return TYPE_BOOLEAN;

    return TYPE_UNKNOWN;
  }

  public static String toString(int valueType)
  {
    if(valueType==TYPE_UNKNOWN) return "unknown";
    if(valueType==TYPE_NULL) return "null";
    if(valueType==TYPE_STRING) return "string";
    if(valueType==TYPE_NUMERIC) return "numeric";
    if(valueType==TYPE_DATE) return "date";
    if(valueType==TYPE_BOOLEAN) return "boolean";
    if(valueType==TYPE_TIME) return "datetime";

    return null;
  }
}