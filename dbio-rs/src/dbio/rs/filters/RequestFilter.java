package dbio.rs.filters;

import java.io.IOException;
import java.sql.Connection;

import javax.annotation.Priority;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;


import dbio.core.DBIO;
import dbio.core.DefinitionStore;
import dbio.rs.model.config.DBIOConfig;

import zn.Logger;
import zn.Configuration;
import zn.rs.model.Identity;

@Provider @PreMatching @Priority(500)
public class RequestFilter implements ContainerRequestFilter 
{
  private static Logger LOG=Logger.get(RequestFilter.class);

  private @javax.ws.rs.core.Context ServletContext appContext;
  public void filter(ContainerRequestContext ctx) throws IOException 
  {
    setupDBConnection(ctx);
  }

  private void setupDBConnection(ContainerRequestContext ctx)
  {
    Configuration config=Configuration.getInstance();
    DBIOConfig dbioConfig=config.$("dbio", DBIOConfig.class);
    String type=dbioConfig.getType();
    String dsname=dbioConfig.getDataSource();
    DefinitionStore defnStore=(DefinitionStore)appContext.getAttribute("dbio.defn.store");

    try
    {
      Context initContext = new InitialContext();
      Context webContext = (Context)initContext.lookup("java:/comp/env");

      LOG.debug("🔹 getting db connection from "+dsname);
      DataSource ds = (DataSource) webContext.lookup(dsname);
      
      Connection con=ds.getConnection();
      con.setAutoCommit(false);

      DBIO dbio=new DBIO(type, con);
      dbio.setIdentity((Identity)ctx.getProperty("zn.identity"));
      dbio.setDefnStore(defnStore);

      ctx.setProperty("dbio.db.connection", con);
      ctx.setProperty("dbio.core", dbio);
      ctx.setProperty("dbio.defnstore", defnStore);
    }
    catch(Exception ex)
    {
      LOG.error("error occured while getting db connection from "+dsname);
      LOG.error(ex);
    }

  }

}