package dbio.core.model;

public class SortField 
{
  private String field;
  private String dir;

  public SortField()
  {

  }

  public SortField(String field, String dir)
  {
    this.field=field;
    this.dir=dir;
  }

  public String getField() {return field;}
  public void setField(String field) {this.field = field;}

  public String getDir() {return dir;}
  public void setDir(String dir) {this.dir = dir;}

  
}
