package zn.rs.model;

import java.util.List;

public class Identity 
{
  private String userid, displayName, refId;
  private int oid;
  private List<Role> roles;

  public Identity(){};

  public Identity(String userid, int oid, String displayName)
  {
    this.userid=userid;
    this.oid=oid;
    this.displayName=displayName;
  };

  public String getUserid() {return userid;}
  public void setUserid(String userid) {this.userid = userid;};

  public int getOid() {return oid;}
  public void setOid(int oid) {this.oid = oid;  }

  public String getDisplayName() {return displayName;}
  public void setDisplayName(String displayName) {this.displayName = displayName;}

  public List<Role> getRoles() {return roles;}
  public void setRoles(List<Role> roles) {this.roles = roles;}

  public String getRefId() {return refId;}
  public void setRefId(String refId) {this.refId = refId;}
}
