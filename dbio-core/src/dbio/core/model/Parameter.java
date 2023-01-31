package dbio.core.model;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Parameter 
{
  private String name, fieldName, mode, type, tableType, itemType, format;
  private List<String> validation;

  public Parameter(){}

  public static Parameter fromMap(Map<String, Object> map)
  {
    if(map==null) return null;
    
    Parameter param=new Parameter();
    param.setName((String)map.get("name"));
    param.setFieldName((String)map.get("fieldName"));
    param.setMode((String)map.get("mode"));
    param.setType((String)map.get("type"));
    param.setTableType((String)map.get("tableType"));
    param.setItemType((String)map.get("itemType"));
    param.setValidation((List<String>)map.get("validation"));

    return param;  
  }

  public String getName() {return name;}
  public void setName(String name) {this.name = name;}
  public String getFieldName() {return fieldName;}
  public void setFieldName(String fieldName) {this.fieldName = fieldName;}
  public String getMode() {return mode;}
  public void setMode(String mode) {this.mode = mode;}
  public String getType() {return type;}
  public void setType(String type) {this.type = type;}
  public String getTableType() {return tableType;}
  public void setTableType(String tableType) {this.tableType = tableType;}
  public String getItemType() {return itemType;}
  public void setItemType(String itemType) {this.itemType = itemType;}
  public String getFormat() {return format;}
  public void setFormat(String format) {this.format = format;}
  public List<String> getValidation() {return validation;}
  public void setValidation(List<String> validation) {this.validation = validation;}
  
}
