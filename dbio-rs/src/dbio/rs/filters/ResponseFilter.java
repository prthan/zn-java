package dbio.rs.filters;

import java.io.IOException;
import java.sql.Connection;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import zn.Logger;

@Provider @Priority(500)
public class ResponseFilter implements ContainerResponseFilter 
{
  private static Logger LOG=Logger.get(ResponseFilter.class);

  public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException
  {
    cleanupDBConnection(req);
  }

  private void cleanupDBConnection(ContainerRequestContext ctx)
  {
    Connection con=(Connection)ctx.getProperty("dbio.db.connection");
    if(con==null) return;

    LOG.debug("🔹 releasing db connection");

    try
    {
      con.rollback();
      con.close();
    }
    catch(Exception ex)
    {
      LOG.error("error occured while closing db connection");
      LOG.error(ex);
    }
  }
}