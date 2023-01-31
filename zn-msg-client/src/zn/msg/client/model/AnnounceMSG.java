package zn.msg.client.model;

import java.util.ArrayList;
import java.util.List;

public class AnnounceMSG
{
  public AnnounceMSG()
  {

  } 
  
  private Identity identity;

  public Identity getIdentity() {
    return identity;
  }

  public void setIdentity(Identity identity) {
    this.identity = identity;
  }

  private String clientType;

  public String getClientType() {
    return clientType;
  }

  public void setClientType(String clientType) {
    this.clientType = clientType;
  }
  
  private List<String> calls;

  public List<String> getCalls() {
    return calls;
  }

  public void setCalls(List<String> calls) {
    this.calls = calls;
  }

}
