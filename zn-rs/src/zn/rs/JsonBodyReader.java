package zn.rs;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import zn.Json;
import zn.model.JSONObject;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class JsonBodyReader implements MessageBodyReader<JSONObject>
{
  public JsonBodyReader()
  {
    super();
  }

  @Override
  public boolean isReadable(Class<?> c, Type type, Annotation[] annotation, MediaType mediaType)
  {
    return true;
  }

  @Override
  public JSONObject readFrom(Class<JSONObject> json, Type type, Annotation[] annotations, MediaType mediaType,
                       MultivaluedMap<String, String> httpHeaders, InputStream inputStream) throws IOException,
                                                                                                   WebApplicationException
  {
    return Json.fromStream(inputStream, json);
  }
}
