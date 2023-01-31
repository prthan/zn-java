package zn.dio.model;

public class Field 
{
  private String name;
  private String type;
  private String format;
  private String label;

  public Field(String name, String type, String format, String label)
  {
    this.name=name;
    this.type=type;
    this.format=format;
    this.label=label;
  }

  public String getName() {return name;};
  public void setName(String v) {name=v;};
  public String getType() {return type;};
  public void setType(String v) {type=v;};
  public String getFormat() {return format;};
  public void setFormat(String v) {format=v;};
  public String getLabel() {return label;};
  public void setLabel(String v) {label=v;};
}
