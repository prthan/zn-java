package zn.model;


public class TokenResponse implements JSONObject
{
  public TokenResponse()
  {

  }

  private String access_token;

  public String getAccessToken() {
    return access_token;
  }

  public void setAccessToken(String access_token) {
    this.access_token = access_token;
  }

  private String id_token;

  public String getIdToken() {
    return id_token;
  }

  public void setIdToken(String id_token) {
    this.id_token = id_token;
  }

  
}
