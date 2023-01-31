package zn.rs.model;

import java.util.Map;

import zn.model.JSONObject;

public class JWT implements JSONObject
{
  private Map<?,?> header, payload;

  public Map<?,?> getHeader() {
    return header;
  }

  public void setHeader(Map<?,?> header) {
    this.header = header;
  }

  public Map<?,?> getPayload() {
    return payload;
  }

  public void setPayload(Map<?,?> payload) {
    this.payload = payload;
  }
  
}
