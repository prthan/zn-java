package dbio.core;

import java.io.File;

import dbio.core.model.Definition;
import zn.Json;

public class DefinitionStore 
{
  public String location;

  public DefinitionStore(String location)
  {
    this.location=location;
  }
  
  public Definition getDefinition(String defnId, String dbtype)
  {
    String fileLocPrefix=location +"/definitions";

    File defnFile=new File(fileLocPrefix+"/"+dbtype+"/"+defnId+".json");
    if(!defnFile.exists()) defnFile=new File(fileLocPrefix+"/"+defnId+".json");
    if(!defnFile.exists()) return null;

    Definition defn=Json.fromFile(defnFile.getAbsolutePath(), Definition.class);
    return defn;
    
  }

  public Definition getDefinition(String defnId)
  {
    String fileLocPrefix=location +"/definitions";

    File defnFile=new File(fileLocPrefix+"/"+defnId+".json");
    if(!defnFile.exists()) return null;

    Definition defn=Json.fromFile(defnFile.getAbsolutePath(), Definition.class);
    return defn;
    
  }

}
