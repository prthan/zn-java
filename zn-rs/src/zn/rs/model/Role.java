package zn.rs.model;

public class Role 
{
  private String name,descr;

  public Role(String n, String d)
  {
    name=n;
    descr=d;
  }

  public void setName(String v){name=v;};
  public String getName(){return name;};
  public void setDescr(String v){descr=v;};
  public String getDescr(){return descr;};
}
