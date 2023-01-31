package zn.rs.filters;

import java.io.IOException;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import zn.Logger;
import zn.Configuration;

@Provider
@PreMatching
@Priority(200)
public class CORSResponseFilter implements ContainerResponseFilter 
{
  private static Logger LOG=Logger.get(CORSRequestFilter.class);

  public void filter(ContainerRequestContext ctxreq, ContainerResponseContext ctxres) throws IOException 
  {
    Configuration config=Configuration.getInstance();
    
    LOG.debug("🔹 CORS response filter");
    
    String origin=ctxreq.getHeaderString("origin");
    if(origin!=null)
    {
      LOG.debug("🔹 CORS response filter > Origin: "+origin);
      List<String> allowedOrigins=config.$("origins", List.class);
      
      if(allowedOrigins!=null && allowedOrigins.contains(origin))
      {
        ctxres.getHeaders().add("access-control-allow-origin", origin);
        ctxres.getHeaders().add("access-control-max-age", "10");
        ctxres.getHeaders().add("access-control-allow-credentials", "true");
      }
    }

    String method=ctxreq.getMethod();
    if("OPTIONS".equals(method))
    {
      String accessReqMethod=ctxreq.getHeaderString("access-control-request-method");
      String accessReqHeaders=ctxreq.getHeaderString("access-control-request-headers");
      if(accessReqMethod!=null)
      {
        LOG.debug("🔹 CORS response filter > Access-Control-Request-Method: "+accessReqMethod);
        ctxres.getHeaders().add("access-control-allow-methods", accessReqMethod);
      }
      if(accessReqHeaders!=null)
      {
        LOG.debug("🔹 CORS response filter > Access-Control-Request-Headers: "+accessReqHeaders);
        ctxres.getHeaders().add("access-control-allow-headers", accessReqHeaders);
      }
    };

  }
}