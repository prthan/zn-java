package zn.rs.filters;

import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.ext.Provider;

import zn.Logger;
import zn.rs.Constants;
import zn.Utils;

@Provider
@Priority(100)
public class ResponseLoggerFilter implements ContainerResponseFilter 
{
  private static Logger LOG=Logger.get(ResponseLoggerFilter.class);

  public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException
  {
    UriInfo uriInfo= req.getUriInfo();
    String requestURI=uriInfo.getRequestUri().toString();

    long startTime=(Long)req.getProperty(Constants.CTX.ATTR_SERVE_STARTTIME);
    long endTime=System.nanoTime();
    int status=res.getStatus();
    LOG.info(String.format("%s >> status=%s, serve time=%s, uri=%s", status>=200 && status <=299 ? "🟩" : "🟥",res.getStatus(), Utils.calcTimeDiff(startTime, endTime), requestURI));
    
    String reqId=(String)req.getProperty(Constants.CTX.ATTR_REQID);
    if(reqId!=null) res.getHeaders().add(Constants.HEADER.REQID, reqId);
  }

}