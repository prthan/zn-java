package zn.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import zn.Json;
import zn.model.JSONObject;


@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonBodyWriter implements MessageBodyWriter<JSONObject>
{
  public JsonBodyWriter()
  {
    super();
  }

  @Override
  public boolean isWriteable(Class<?> c, Type type, Annotation[] annotation, MediaType mediaType)
  {
    return true;
  }

  @Override
  public long getSize(JSONObject jst, Class<?> c, Type type, Annotation[] annotation, MediaType mediaType)
  {
    return 0L;
  }

  @Override
  public void writeTo(JSONObject json, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                    WebApplicationException
  {
        entityStream.write(Json.stringify(json).getBytes());
  }
}
