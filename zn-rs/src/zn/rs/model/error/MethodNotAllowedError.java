package zn.rs.model.error;

import javax.ws.rs.core.Response;

public class MethodNotAllowedError extends zn.rs.model.Error
{
  public MethodNotAllowedError(String msg)
  {
    super(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), msg);
  }
}
