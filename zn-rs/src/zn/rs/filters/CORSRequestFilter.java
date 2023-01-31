package zn.rs.filters;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import zn.Logger;

@Provider
@PreMatching
@Priority(200)
public class CORSRequestFilter implements ContainerRequestFilter 
{
  private static Logger LOG=Logger.get(CORSRequestFilter.class);

  public void filter( ContainerRequestContext ctxreq) throws IOException 
  {
    LOG.debug("🔹 CORS request filter");
    String method=ctxreq.getMethod();

    if("OPTIONS".equals(method)) ctxreq.abortWith(Response.status(Response.Status.OK).build());
  }
}

