package dbio.core.model;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Field 
{
  private String name, dbfield, type, label, format;
  private List<String> validation;

  public Field()
  {

  }

  public static Field fromMap(Map<String, Object> map)
  {
    if(map==null) return null;
    
    Field field=new Field();
    field.setDbfield((String)map.get("dbfield"));
    field.setType((String)map.get("type"));
    field.setLabel((String)map.get("label"));
    field.setFormat((String)map.get("format"));
    field.setValidation((List<String>)map.get("validation"));

    return field;  
  }

  public String getName() {return name;}
  public void setName(String name) {this.name = name;}
  public String getDbfield() {return dbfield;}
  public void setDbfield(String dbfield) {this.dbfield = dbfield;}
  public String getType() {return type;}
  public void setType(String type) {this.type = type;}
  public String getLabel() {return label;}
  public void setLabel(String label) {this.label = label;}
  public List<String> getValidation() {return validation;}
  public void setValidation(List<String> validation) {this.validation = validation;}
  public String getFormat() {return format;}
  public void setFormat(String format) {this.format = format;}
}
