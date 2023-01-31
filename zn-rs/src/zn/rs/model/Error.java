package zn.rs.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Error
{
  private String message;
  private int code;
  private List<String> details;

  public Error()
  {
    details=new ArrayList<String>();
  }
  
  public Error(int code, String msg)
  {
    super();
    this.code=code;
    this.message=msg;
    details=new ArrayList<String>();
  }

  public Error(int code, String msg, List<String> details)
  {
    this.code=code;
    this.message=msg;
    this.details=details;
  }  

  public Error(int code, String msg, Throwable t)
  {
    this.code=code;
    this.message=msg;

    StringWriter sw=new StringWriter();
    PrintWriter pw=new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.close();

    details=new ArrayList<String>();
    for(String part:sw.toString().split("\n")) addDetail(part);
  }
  
  public int getCode() {return code;}
  public void setCode(int code) {this.code = code;}

  public String getMessage() {return message;}
  public void setMessage(String message) {this.message = message;}

  public void addDetail(String item) {this.details.add(item);};
  public List<String> getDetails() {return details;}
  public void setDetails(List<String> details) {this.details = details;}


  
}
