package zn.msg.client.model;

public class Identity 
{
  public Identity()
  {

  } 

  public Identity(String token, String appid)
  {
    this.token=token;
    this.appid=appid;
  } 

  private String token, appid;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getAppid() {
    return appid;
  }

  public void setAppid(String appid) {
    this.appid = appid;
  }

  
}
