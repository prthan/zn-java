package zn.rs.model.response;

import javax.ws.rs.core.Response;

import zn.rs.model.Error;
import zn.model.JSONObject;

public class BaseResponse implements JSONObject
{
  private Error error;
  
  public BaseResponse()
  {
    super();
  }

  public BaseResponse(Error error)
  {
    this.error=error;
  }

  public void setError(Error error)
  {
    this.error = error;
  }

  public Error getError()
  {
    return error;
  }

  public Response toResponse()
  {
    int statusCode=Response.Status.OK.getStatusCode();
    Error error=getError();
    if(error!=null) statusCode=error.getCode();
    return Response.status(statusCode).entity(this).build();
  }
}
