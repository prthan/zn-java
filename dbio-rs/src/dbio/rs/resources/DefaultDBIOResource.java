package dbio.rs.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import dbio.core.DBIO;
import dbio.core.IOObject;
import dbio.core.IOObjectList;
import dbio.core.Outcome;
import dbio.core.model.Definition;
import dbio.core.model.ListOptions;
import dbio.rs.model.IORequest;
import dbio.rs.model.IOResponse;
import zn.rs.model.error.MethodNotAllowedError;
import zn.rs.model.error.ResourceNotFoundError;

@Path("/{defnId}")
public class DefaultDBIOResource extends DBIOBaseResource
{
  @POST @Consumes("application/json") @Produces("application/json")
  public Response create(@PathParam("defnId") String defnId, IORequest request)
  {
    Definition defn=definition(defnId);
    if(defn==null) return createResponse(new ResourceNotFoundError(String.format("definition for %s not", defnId)));

    if(defn.table()!=null)
    {
      if(request.isList()) return createEntityList(defn, request);
      else return createEntity(defn, request);
    }

    if(defn.procedure()!=null) return callProc(defn, request);

    return createResponse(new MethodNotAllowedError( "This method is not allowed"));
  }

  private Response createEntityList(Definition defn, IORequest request)
  {
    DBIO dbio=dbio();

    String info=String.format("create %s", defn.name());

    IOObjectList list=request.list();
    Outcome<IOObjectList> outcome=dbio.create(defn, list, info);
    if(outcome.isError()) return createResponse(outcome.getError());

    IOResponse wrapper=new IOResponse();
    wrapper.put("list", outcome.getValue());

    return createResponse(wrapper);
  }

  private Response createEntity(Definition defn, IORequest request)
  {
    DBIO dbio=dbio();

    String info=String.format("create %s", defn.name());

    Outcome<IOObject> outcome=dbio.create(defn, request, info);
    if(outcome.isError()) return createResponse(outcome.getError());
    
    IOResponse wrapper=new IOResponse();
    wrapper.putAll(outcome.getValue());
    
    return createResponse(wrapper);
  }

  private Response callProc(Definition defn, IORequest request)
  {
    DBIO dbio=dbio();

    String info=String.format("call %s", defn.name());
    
    Outcome<IOObject> outcome=dbio.call(defn, request, info);
    if(outcome.isError()) return createResponse(outcome.getError());
    
    IOResponse wrapper=new IOResponse();
    wrapper.putAll(outcome.getValue());
    
    return createResponse(wrapper);
  }

  @PUT @Consumes("application/json") @Produces("application/json")
  public Response update(@PathParam("defnId") String defnId, IORequest request)
  {
    Definition defn=definition(defnId);
    if(defn==null) return createResponse(new ResourceNotFoundError(String.format("definition for %s not", defnId)));

    if(defn.table()!=null)
    {
      if(request.isList()) return updateEntityList(defn, request);
      else return updateEntity(defn, request);
    }

    return createResponse(new MethodNotAllowedError("Method is not allowed"));
  }

  private Response updateEntityList(Definition defn, IORequest request)
  {
    DBIO dbio=dbio();
    String info=String.format("update %s", defn.name());
    
    IOObjectList list=request.list();
    Outcome<IOObjectList> outcome=dbio.update(defn, list, info);
    if(outcome.isError()) return createResponse(outcome.getError());

    IOResponse wrapper=new IOResponse();
    IOObjectList updatedList=outcome.getValue();

    if(updatedList!=null) wrapper.put("list", updatedList);
    else wrapper.put("status", "done");
    
    return createResponse(wrapper);
  }

  private Response updateEntity(Definition defn, IORequest request)
  {
    DBIO dbio=dbio();
    String info=String.format("update %s", defn.name());

    Outcome<IOObject> outcome=dbio.update(defn, request, info);
    if(outcome.isError()) createResponse(outcome.getError());

    IOResponse wrapper=new IOResponse();
    IOObject updatedObj=outcome.getValue();

    if(updatedObj!=null) wrapper.putAll(updatedObj);
    else wrapper.put("status", "done");
    
    return createResponse(wrapper);
  }

  @DELETE @Path("/{oid}") 
  public Response delete(@PathParam("defnId") String defnId, @PathParam("oid") String oid)
  {
    Definition defn=definition(defnId);
    if(defn==null) return createResponse(new ResourceNotFoundError(String.format("definition for %s not", defnId)));

    if(defn.table()!=null) return deleteEntity(defn, oid);
    
    return createResponse(new MethodNotAllowedError("Method is not allowed"));
  }

  private Response deleteEntity(Definition defn, String oid)
  {
    DBIO dbio=dbio();
    String info=String.format("delete %s", defn.name());

    IOObject obj=new IOObject();
    obj.put(defn.identityField(), oid);
    Outcome<Object> outcome=dbio.delete(defn, obj, info);
    if(outcome.isError()) return createResponse(outcome.getError());

    IOResponse wrapper=new IOResponse();
    wrapper.put("status", "done");
    
    return createResponse(wrapper);
  }

  @POST @Path("/list") @Consumes("application/json") @Produces("application/json")
  public Response list(@PathParam("defnId") String defnId, IORequest request)
  {
    Definition defn=definition(defnId);
    if(defn==null) return createResponse(new ResourceNotFoundError(String.format("definition for %s not", defnId)));

    if(defn.table()!=null||defn.query()!=null) return listEntity(defn, ListOptions.from(request));
    
    return createResponse(new MethodNotAllowedError("Method is not allowed"));
  }

  private Response listEntity(Definition defn, ListOptions options)
  {
    DBIO dbio=dbio();

    String info=String.format("list %s", defn.name());

    Outcome<IOObjectList> outcome=dbio.list(defn, options, info);
    if(outcome.isError()) return createResponse(outcome.getError());

    IOObjectList list=outcome.getValue();
    IOResponse wrapper=new IOResponse();
    wrapper.put("list", list);
    wrapper.put("next", list.isNext());
    
    return createResponse(wrapper);
  }

  @GET @Path("/{oid}") @Produces("application/json")
  public Response get(@PathParam("defnId") String defnId, @PathParam("oid") String oid)
  {
    Definition defn=definition(defnId);
    if(defn==null) return createResponse(new ResourceNotFoundError(String.format("definition for %s not", defnId)));
    
    if(defn.table()!=null||defn.query()!=null) return getEntity(defn, oid);
    
    return createResponse(new MethodNotAllowedError("Method is not allowed"));
  }

  private Response getEntity(Definition defn, String oid)
  {
    DBIO dbio=dbio();

    String info=String.format("get %s", defn.name());

    Outcome<IOObject> outcome=dbio.get(defn, oid, info);
    if(outcome.isError()) return createResponse(outcome.getError());

    IOObject obj=outcome.getValue();
    IOResponse wrapper=new IOResponse();
    if(obj!=null) wrapper.putAll(obj);
    
    return createResponse(wrapper);
  }

  private Response createResponse(Object obj)
  {
    int status=200;
    Object resobj=obj;
    
    if(obj instanceof zn.rs.model.Error)
    {
      IOResponse ior=new IOResponse();
      ior.put("error", obj);
      resobj=ior;
      status=((zn.rs.model.Error)obj).getCode();
    }

    ResponseBuilder builder=Response.status(status);
    builder.entity(resobj);
    return builder.build();
  }
}
