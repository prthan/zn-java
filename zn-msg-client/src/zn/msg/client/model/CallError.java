package zn.msg.client.model;

public class CallError 
{
  public CallError()
  {

  } 
  
  public CallError(String code, String message)
  {
    this.code=code;
    this.message=message;
  } 

  private String code, message;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
  
}
