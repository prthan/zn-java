package zn.rs.model.error;

import javax.ws.rs.core.Response;


public class InternalServerError extends zn.rs.model.Error
{
  public InternalServerError(String msg)
  {
    super(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Srror: "+msg);
  }

  public InternalServerError(String msg, Throwable t)
  {
    super(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Srror: "+msg, t);
  }
}
