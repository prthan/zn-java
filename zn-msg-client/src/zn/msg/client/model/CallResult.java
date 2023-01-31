package zn.msg.client.model;

import zn.Json;

public class CallResult 
{
  public CallResult()
  {

  }

  public <T> T to(Class<T> clazz)
  {
    return Json.parseJson(Json.stringify(this), clazz);
  }
  
  public static CallResult error(String code, String message)
  {
    CallResult callResult=new CallResult();
    callResult.setError(new CallError(code, message));
    return callResult;
  } 

  private CallError error;

  public CallError getError() {
    return error;
  }

  public void setError(CallError error) {
    this.error = error;
  }
  
}
