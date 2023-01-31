package zn.rs.filters;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import zn.Logger;
import zn.Configuration;
import zn.JWTUtil;
import zn.rs.Constants;
import zn.rs.model.Identity;
import zn.rs.model.config.Auth;
import zn.rs.model.config.OpenId;
import zn.rs.model.error.InternalServerError;
import zn.rs.model.error.UnAuthorizedError;
import zn.rs.model.response.BaseResponse;

@Provider @PreMatching @Priority(300)
public class OpenIDRequestFilter implements ContainerRequestFilter 
{
  private static Logger LOG=Logger.get(OpenIDRequestFilter.class);

  public void filter(ContainerRequestContext ctx) throws IOException 
  {
    Configuration config=Configuration.getInstance();
    Auth auth=config.$("auth", Auth.class);
    OpenId opendId=config.$("openId", OpenId.class);

    LOG.debug("🔹 Authorization filter");

    if(!auth.isEnabled()) return;

    String authorization=ctx.getHeaderString("authorization");
    if(authorization==null)
    {
      ctx.abortWith(new BaseResponse(new UnAuthorizedError("Missing authorization")).toResponse());
      return;
    }

    if(!authorization.startsWith("Bearer "))
    {
      ctx.abortWith(new BaseResponse(new UnAuthorizedError("Invalid authorization mode")).toResponse());
      return;
    }
    
    String token=authorization.substring(7);
    String keySpec=opendId.getSigningCert();
    JWTUtil jwtutil=new JWTUtil();
    try
    {
      String result=jwtutil.verifyToken(token, keySpec);
      if(!"TOKEN-VALID".equals(result))
      {
        ctx.abortWith(new BaseResponse(new UnAuthorizedError("Invalid authorization: "+result)).toResponse());
        return;  
      }
      
      Map<?,?> decodedToken=jwtutil.decodeToken(token);
      LOG.debug("🔹 Token", decodedToken);
      ctx.setProperty(Constants.CTX.ATTR_TOKEN, decodedToken);

      setupIdentity(ctx);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      LOG.error(ex);
      ctx.abortWith(new BaseResponse(new InternalServerError("Internal server error: "+ex.toString())).toResponse());
    }
    
  }

  private void setupIdentity(ContainerRequestContext ctx)
  {
    Configuration config=Configuration.getInstance();
    OpenId openId=config.$("openId", OpenId.class);

    Map<?,?> decodedToken=(Map<?,?>)ctx.getProperty(Constants.CTX.ATTR_TOKEN);
    Identity identity=new Identity();

    if("oracle".equals(openId.getType()))
    {
      identity.setDisplayName((String)decodedToken.get("user_displayname"));
      identity.setUserid((String)decodedToken.get("sub"));
      identity.setRefId((String)decodedToken.get("user_id"));
    }

    ctx.setProperty(Constants.CTX.ATTR_IDENTITY, identity);
    
  }    
}