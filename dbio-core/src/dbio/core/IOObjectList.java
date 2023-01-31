package dbio.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import zn.Json;
import zn.model.JSONObject;

@SuppressWarnings("unchecked")
public class IOObjectList extends ArrayList<IOObject> implements JSONObject
{
  private int pageSize=-1;
  private boolean next=false;

  public IOObjectList()
  {
    super();
  }
  
  public int getPageSize() {return pageSize;}
  public void setPageSize(int pageSize) {this.pageSize = pageSize;}
  public boolean isNext() {return next;  }
  public void setNext(boolean next) {this.next = next;}

  public static IOObjectList fromList(List<? extends Object> l)
  {
    IOObjectList list=new IOObjectList();
    for(Object item: l)
    {
      list.add(IOObject.fromMap((Map<String, Object>)item));
    }

    return list;
  }

  public String toJsonString(){return Json.stringify(this);}
  public String toString() {return this.toJsonString();};
}
