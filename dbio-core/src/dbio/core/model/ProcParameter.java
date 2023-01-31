package dbio.core.model;

import zn.Json;

public class ProcParameter extends TableColumn
{
  private String mode, typeName, itemTypeName;

  public ProcParameter() {}

  public String getMode() {return mode;}
  public void setMode(String mode) {this.mode = mode;};
  public String getTypeName() {return typeName;}
  public void setTypeName(String typeName) {this.typeName = typeName;}
  public String getItemTypeName() {return itemTypeName;}
  public void setItemTypeName(String itemTypeName) {this.itemTypeName = itemTypeName;}

  public String toString() {return Json.stringify(this);}
}
