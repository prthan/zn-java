package zn.rs.model.error;

import javax.ws.rs.core.Response;

public class ResourceNotFoundError extends zn.rs.model.Error
{
  public ResourceNotFoundError(String msg)
  {
    super(Response.Status.NOT_FOUND.getStatusCode(), msg);
  }
}
