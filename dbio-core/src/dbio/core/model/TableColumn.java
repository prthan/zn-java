package dbio.core.model;

public class TableColumn
{
  private String name;  
  private String type;
  private String nil;
  private String key;
  private int size, decimalDigits;

  public TableColumn(){}

  public void setName(String v) {this.name=v;};
  public String getName() {return this.name;};

  public void setType(String v) {this.type=v;};
  public String getType() {return this.type;};

  public void setNil(String v) {this.nil=v;};
  public String getNil() {return this.nil;};

  public void setKey(String v) {this.key=v;};
  public String getKey() {return this.key;};

  public void setSize(int v) {this.size=v;};
  public int getSize() {return this.size;}

  public int getDecimalDigits() {return decimalDigits;}
  public void setDecimalDigits(int decimalDigits) {this.decimalDigits = decimalDigits;};

  

}
