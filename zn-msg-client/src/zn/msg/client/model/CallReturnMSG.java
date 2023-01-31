package zn.msg.client.model;

import org.json.JSONObject;

import zn.Json;

public class CallReturnMSG 
{
  private JSONObject resultjo=null;
  
  public CallReturnMSG(){}  

  private String id;
  private CallResult result;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public CallResult getResult() {
    return result;
  }
  public void setResult(CallResult result) {
    this.result = result;
  }

  public void setResultJO(JSONObject jo) {resultjo=jo;};

  public <T> T result(Class<T> clazz) 
  {
    if(resultjo==null) return null;
    return Json.parseJson(resultjo.toString(), clazz);
  }  
}
