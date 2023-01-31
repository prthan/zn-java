package dbio.rs.model;

import java.util.List;

import dbio.core.IOObject;
import dbio.core.IOObjectList;

@SuppressWarnings("unchecked")
public class IORequest extends IOObject
{
  public IORequest() {super();}

  public boolean isList() {return this.containsKey("list");};
  public IOObjectList list() {return IOObjectList.fromList($("list", List.class));};
}
