package dbio.core;

import java.util.List;

import com.google.gson.Gson;

public class SQL 
{
  private String query;
  private List<Object> binds;
  private Object generatedKey;
  private String generatedKeyField;
  private IOObjectList results;
  private int pageSize;

  public SQL(String query, List<Object> binds)
  {
    this.query=query;
    this.binds=binds;
  }
  
  public String getQuery() {return query;}
  public void setQuery(String query) {this.query = query;}
  public List<Object> getBinds() {return binds;}
  public void setBinds(List<Object> binds) {this.binds = binds;}
  public Object getGeneratedKey() {return generatedKey;}
  public void setGeneratedKey(Object generatedKey) {this.generatedKey = generatedKey;}
  public String getGeneratedKeyField() {return generatedKeyField;}
  public void setGeneratedKeyField(String generatedKeyField) {this.generatedKeyField = generatedKeyField;}
  public IOObjectList getResults() {return results;}
  public void setResults(IOObjectList results) {this.results = results;}
  public int getPageSize() {return pageSize;}
  public void setPageSize(int pageSize) {this.pageSize = pageSize;}

  public String toString() {return new Gson().toJson(this);}
}
