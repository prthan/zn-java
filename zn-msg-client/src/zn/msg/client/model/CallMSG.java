package zn.msg.client.model;

import org.json.JSONObject;

import zn.Json;

public class CallMSG 
{
  private JSONObject payloadjo=null;

  public CallMSG()
  {

  }
  
  private String id, fn;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getFn() {
    return fn;
  }
  public void setFn(String name) {
    this.fn = name;
  }
  
  private Object payload;
  public Object getPayload() {
    return payload;
  }
  public void setPayload(Object payload) {
    this.payload = payload;
  }

  public void setPayloadJO(JSONObject jo) {payloadjo=jo;};

  public <T> T payload(Class<T> clazz) 
  {
    if(payloadjo==null) return null;
    return Json.parseJson(payloadjo.toString(), clazz);
  }
  
  
}
