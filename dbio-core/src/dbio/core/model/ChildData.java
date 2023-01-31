package dbio.core.model;

public class ChildData 
{
  private String name, defn, rel, linkParentField, linkChildField;
  private ListOptions dataListOptions;

  public ChildData()
  {

  }

  public String getName() {return name;}
  public void setName(String name) {this.name = name;}
  public String getDefn() {return defn;}
  public void setDefn(String defn) {this.defn = defn;}
  public String getRel() {return rel;}
  public void setRel(String rel) {this.rel = rel;}
  public String getLinkParentField() {return linkParentField;}
  public void setLinkParentField(String linkParentField) {this.linkParentField = linkParentField;}
  public String getLinkChildField() {return linkChildField;}
  public void setLinkChildField(String linkChildField) {this.linkChildField = linkChildField;}
  public ListOptions getDataListOptions() {return dataListOptions;}
  public void setDataListOptions(ListOptions dataListOptions) {this.dataListOptions = dataListOptions;}
}
