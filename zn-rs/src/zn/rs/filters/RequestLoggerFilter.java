package zn.rs.filters;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import zn.Logger;
import zn.rs.Constants;
import zn.Utils;


@Provider @PreMatching @Priority(100)
public class RequestLoggerFilter implements ContainerRequestFilter 
{
  private static Logger LOG=Logger.get(RequestLoggerFilter.class);

  public void filter(ContainerRequestContext ctx) throws IOException 
  {
    String reqId=Utils.shortid();
    LOG.set("reqID", reqId);

    ctx.setProperty(Constants.CTX.ATTR_REQID, reqId);
    ctx.setProperty(Constants.CTX.ATTR_SERVE_STARTTIME, System.nanoTime());

    UriInfo uriInfo= ctx.getUriInfo();
    String requestURI=uriInfo.getRequestUri().toString();
    LOG.info("🟣 << ["+ctx.getMethod()+"] "+ requestURI);
  }

}