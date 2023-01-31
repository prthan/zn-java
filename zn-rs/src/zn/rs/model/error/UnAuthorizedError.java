package zn.rs.model.error;

import javax.ws.rs.core.Response;

public class UnAuthorizedError extends zn.rs.model.Error
{
  public UnAuthorizedError(String msg)
  {
    super(Response.Status.UNAUTHORIZED.getStatusCode(), "Unauthorized Access: "+msg);
  }
}
