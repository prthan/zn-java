package dbio.core.model;

import java.util.List;

public class Clause 
{
  String text;
  List<Object> binds;
  List<String> errors;

  public Clause() {};
  public Clause(String text, List<Object> binds, List<String> errors)
  {
    this.text=text;
    this.binds=binds;
    this.errors=errors;
  }
  public boolean hasErrors() {return errors!=null;};
  public String getText() {return text;}
  public void setText(String text) {this.text = text;}
  public List<Object> getBinds() {return binds;}
  public void setBinds(List<Object> binds) {this.binds = binds;}
  public List<String> getErrors() {return errors;}
  public void setErrors(List<String> errors) {this.errors = errors;}
  
}
