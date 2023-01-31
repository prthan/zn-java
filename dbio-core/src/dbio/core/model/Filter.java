package dbio.core.model;

public class Filter 
{
  private String field, op;
  private Object value;

  public Filter()
  {

  }

  public Filter(String field, String op, Object value)
  {
    this.field=field;
    this.op=op;
    this.value=value;
  }

  public String getField() {return field;}
  public void setField(String field) {this.field = field;}
  public String getOp() {return op;}
  public void setOp(String op) {this.op = op;}
  public Object getValue() {return value;}
  public void setValue(Object value) {this.value = value;}
}
