package dbio.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import dbio.core.model.Field;

@SuppressWarnings("unchecked")
public class Validator 
{
  private static SimpleDateFormat DT_FMT=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private List<String> errors=new ArrayList<String>();
  private List<Field> defnFields;

  public Validator(List<Field> defnFields)
  {
    this.defnFields=defnFields;
  }

  public boolean hasErrors() {return this.errors.size()>0;};
  public List<String> errors() {return this.errors;};

  public void validateList(List<? extends Object> list, List<String> ignoreFields)
  {
    for(Object obj:list) this.validateObject((Map<String, Object>)obj, ignoreFields);
  }

  public void validateObject(Map<String, Object> obj)
  {
    this.validateObject(obj, null);
  }

  public void validateObject(Map<String, Object> obj, List<String> ignoreFields)
  {
    if(obj==null) return;
    
    for(Field field:defnFields)
    {
      String fieldName=field.getName();
      if(ignoreFields!=null && ignoreFields.contains(fieldName)) continue;
      
      Object value=obj.get(fieldName);
      List<String> validations=field.getValidation();
      for(String validation:validations) doValidation(validation, fieldName, value, errors);
    }
  }

  public static void doValidation(String validation, String name, Object value, List<String> errors)
  {
    if(validation==null) return;
    
    if("required".equals(validation)) checkRequired(name, value, errors);
    if(value==null) return;
    
    if("string".equals(validation)) checkIsString(name, value, errors);
    if("numeric".equals(validation)) checkIsNumeric(name, value, errors);
    if("integer".equals(validation)) checkIsInteger(name, value, errors);
    if("date".equals(validation)) checkIsDate(name, value, errors);
    if("timestamp".equals(validation)) checkIsDate(name, value, errors);
    if(validation.startsWith("max"))
    {
      String[] parts=validation.split(":");
      checkMaxLength(name, value, Integer.parseInt(parts[1]), errors);
    }
    if(validation.startsWith("regex"))
    {
      String[] parts=validation.split(":");
      checkPattern(name, value, parts[1], errors);
    }
  }
  public static void checkIsString(String name, Object value, List<String> errors)
  {
    if(!(value instanceof String)) errors.add(String.format("%s: %s is not a string", name, value));
  }

  public static void checkRequired(String name, Object value, List<String> errors)
  {
    if(value==null) errors.add(String.format("%s is required", name));
  }

  public static void checkIsNumeric(String name, Object value, List<String> errors)
  {
    if(!(value instanceof Number)) errors.add(String.format("%s: %s is not a number", name, value));
  }

  public static void checkIsInteger(String name, Object value, List<String> errors)
  {
    if(value instanceof Number)
    {
      Integer nvalue=null;
      try
      {
        nvalue=((Number)value).intValue();
      }
      catch(Exception ex)
      {

      }
      if(nvalue==null) errors.add(String.format("%s: %s is not an integer", name, value));
    }
    else if(!(value instanceof Integer)) errors.add(String.format("%s: %s is not an integer", name, value));
  }

  public static void checkIsDate(String name, Object value, List<String> errors)
  {
    if((value instanceof String))
    {
      Date dateValue=null;
      try
      {
        dateValue=DT_FMT.parse(""+value);
      }
      catch(Exception ex)
      {

      }
      if(dateValue==null) errors.add(String.format("%s: %s is not a date", name, value));
    }
    else if(!(value instanceof Date)) errors.add(String.format("%s: %s is not a date", name, value));
  }

  public static void checkMaxLength(String name, Object value, int size, List<String> errors)
  {
    if(value!=null && (""+value).length()>size) errors.add(String.format("the size of %s: %S is more than %s", name, value, size));
  }

  public static void checkPattern(String name, Object value, String pstr, List<String> errors)
  {
    if(value!=null && !(""+value).matches(pstr.substring(1, pstr.length()-2))) errors.add(String.format("%s: %s is invalid, does not match the expected pattern", name, value));
  }

  public static void main(String[] args)
  {
    Object value="11111111111";
    System.out.println((""+value).matches("^\\d{0,11}$"));
  }
}
