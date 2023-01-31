package zn.dio.model;

public class ValueError extends Value
{
  private String message;
  private String colid;
  private int col;
  public ValueError(String colid, String message, int col)
  {
    super();
    this.colid=colid;
    this.message=message;
    this.col=col;
  }
  
  public String toString()
  {
    StringBuilder sb=new StringBuilder();
    sb.append("[").append(colid).append("] ").append(message);
    
    return sb.toString();
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public void setColid(String colid)
  {
    this.colid = colid;
  }

  public String getColid()
  {
    return colid;
  }

  public void setCol(int col)
  {
    this.col = col;
  }

  public int getCol()
  {
    return col;
  }
}
