package zn.rs.model.config;

import java.util.List;

import zn.model.JSONObject;

public class Config implements JSONObject
{
  private String name, id, mode, context;
  private OpenId openId;
  private Auth auth;
  private List<String> origins;
  private String home;
  
  public Config(){}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public OpenId getOpenId() {
    return openId;
  }

  public void setOpenId(OpenId openId) {
    this.openId = openId;
  }

  public Auth getAuth() {
    return auth;
  }

  public void setAuth(Auth auth) {
    this.auth = auth;
  }

  public List<String> getOrigins() {
    return origins;
  }

  public void setOrigins(List<String> origins) {
    this.origins = origins;
  }

  public String getHome() {
    return home;
  }

  public void setHome(String home) {
    this.home = home;
  }

  
}
