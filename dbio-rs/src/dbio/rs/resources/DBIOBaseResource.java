package dbio.rs.resources;

import zn.Configuration;
import zn.rs.resources.BaseResource;

import java.sql.Connection;

import dbio.core.DBIO;
import dbio.core.DefinitionStore;
import dbio.core.model.Definition;
import dbio.rs.model.config.DBIOConfig;

public class DBIOBaseResource extends BaseResource
{
  public DBIOBaseResource()
  {

  } 
  
  public Connection connection()
  {
    return (Connection)attr("dbio.db.connection", Connection.class);
  }

  public DBIO dbio()
  {
    return (DBIO)attr("dbio.core", DBIO.class);
  }

  public Definition definition(String defnId)
  {
    Configuration config=Configuration.getInstance();
    DBIOConfig dbioConfig=config.$("dbio", DBIOConfig.class);
    
    DefinitionStore defnStore=(DefinitionStore)request.getAttribute("dbio.defnstore");

    return defnStore.getDefinition(defnId, dbioConfig.getType());
  }  
}
