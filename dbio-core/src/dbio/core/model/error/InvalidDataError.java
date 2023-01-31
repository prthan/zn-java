package dbio.core.model.error;

import java.util.List;

import javax.ws.rs.core.Response;

public class InvalidDataError extends zn.rs.model.Error
{
  public InvalidDataError(String msg)
  {
    super(Response.Status.BAD_REQUEST.getStatusCode(), msg);
  }
  public InvalidDataError(String msg, List<String> details)
  {
    super(Response.Status.BAD_REQUEST.getStatusCode(), msg);
    this.setDetails(details);
  }
}
