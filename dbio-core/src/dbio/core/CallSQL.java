package dbio.core;

import java.util.List;

import com.google.gson.Gson;

import dbio.core.model.Definition;

public class CallSQL 
{
  private String query;
  private List<Object> binds;
  private IOObject result;
  private Definition definition;

  public CallSQL(Definition defn, String query, List<Object> binds)
  {
    this.definition=defn;
    this.query=query;
    this.binds=binds;
    result=new IOObject();
  }
 
  public String toString() {return new Gson().toJson(this);}
  public String getQuery() {return query;}
  public void setQuery(String query) {this.query = query;}

  public List<Object> getBinds() {return binds;}
  public void setBinds(List<Object> binds) {this.binds = binds;}
  public IOObject getResult() {return result;}
  public void setResult(IOObject result) {this.result = result;}
  public Definition getDefinition() {return definition;}
  public void setDefinition(Definition definition) {this.definition = definition;}
}
