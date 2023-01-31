package dbio.core.model;

public class ValidationError 
{
  private String field, message;

  public ValidationError(String field, String message)
  {
    this.field=field;
    this.message=message;
  }

  public String getField() {return field;}
  public void setField(String field) {this.field = field;}
  public String getMessage() {return message;}
  public void setMessage(String message) {this.message = message;}
}
