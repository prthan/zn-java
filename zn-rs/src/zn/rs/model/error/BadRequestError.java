package zn.rs.model.error;

import javax.ws.rs.core.Response;

public class BadRequestError extends zn.rs.model.Error
{
  public BadRequestError()
  {
    super(Response.Status.BAD_REQUEST.getStatusCode(), "Bad Request");
  }
}
