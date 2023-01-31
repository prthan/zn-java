package zn.rs.resources;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import zn.Configuration;
import zn.rs.model.Identity;
import zn.rs.model.error.BadRequestError;
import zn.rs.model.error.InternalServerError;
import zn.rs.model.error.UnAuthorizedError;

public class BaseResource 
{
  protected Configuration config=Configuration.getInstance();
  protected @javax.ws.rs.core.Context ServletContext appContext;
  protected @javax.ws.rs.core.Context HttpServletRequest request;
  
  public BaseResource()
  {

  } 

  public zn.rs.model.Error ex2error(Exception ex)
  {
    String exstr=ex.toString();
    zn.rs.model.Error error=null;
    
    if(exstr.indexOf("HttpUnauthorizedException")!=-1) error=new UnAuthorizedError("unauthorized request");
    if(exstr.indexOf("Bad Request")!=-1) error=new BadRequestError();
    error=new InternalServerError(exstr);
    
    return error;
  }

  public void setAttr(String name, Object obj)
  {
    request.setAttribute(name, obj);
  }

  public <T> T attr(String name, Class<T> clazz)
  {
    return clazz.cast(request.getAttribute(name));
  }

  public Identity identity()
  {
    return attr("zn.identity", Identity.class);
  }

}
