package zn;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import zn.Json;
import zn.Logger;
import zn.Utils;
import zn.model.JSONObject;

public class Http 
{
  private String url;
  private String method;
  private Map<String, String> headers=new HashMap<String, String>();
  private Object body;
  private InputStream responseStream;
  private Exception requestError;

  private static Logger LOG=Logger.get(Http.class);

  public Http()
  {

  }

  public Http get(String url)
  {
    method="GET";
    this.url=url;
    return this;
  }

  public Http post(String url, Object body)
  {
    method="POST";
    this.url=url;
    this.body=body;
    return this;
  }

  public Http put(String url, Object body)
  {
    method="PUT";
    this.url=url;
    this.body=body;
    return this;
  }

  public Http delete(String url)
  {
    method="DELETE";
    this.url=url;
    return this;
  }

  public Http patch(String url)
  {
    method="PATCH";
    this.url=url;
    return this;
  }

  public Http options(String url)
  {
    method="OPTIONS";
    this.url=url;
    return this;
  }

  public Http method(String method)
  {
    this.method=method;
    return this;
  }

  public Http url(String url)
  {
    this.url=url;
    return this;
  }

  public Http withHeaders(Map<String, String> headers)
  {
    this.headers.putAll(headers);
    return this;
  }

  public Http withHeader(String headerName, String headerValue)
  {
    this.headers.put(headerName, headerValue);
    return this;
  }

  public Http body(Object body)
  {
    this.body=body;
    return this;
  }

  public Exception error() {return requestError;}
  public String $() {return $(String.class);}
  public InputStream $stream() {return $(InputStream.class);}
  public byte[] $bytes() {return $(byte[].class);}
  public <T extends JSONObject> T $json(Class<T> clazz) {return $(clazz);}

  public <T> T $(Class<T> clazz)
  {
    T rval=null;
    try
    {
      doRequest();
      if(InputStream.class.isAssignableFrom(clazz)) rval=clazz.cast(responseStream);
      else if(byte[].class.isAssignableFrom(clazz)) rval=clazz.cast(Utils.streamToBytes(responseStream));
      else if(String.class.isAssignableFrom(clazz)) rval=clazz.cast(Utils.streamToString(responseStream));
      else if(JSONObject.class.isAssignableFrom(clazz)) rval=clazz.cast(Json.fromStream(responseStream, clazz));
      else rval=clazz.cast(Utils.streamToString(responseStream));
    }
    catch(Exception ex)
    {
      requestError=ex;
    }
    return rval;
  }
  

  private void doRequest() throws Exception
  {
    LOG.debug(String.format("🕷️ http %s: %s", method, url));
    HttpURLConnection uc=(HttpURLConnection)new URL(url).openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setRequestMethod(method);
    if(headers!=null)
    {
      LOG.debug(String.format("🕷️ http headers: %s", Json.stringify(headers)));
      for(Map.Entry<String, String> entry:headers.entrySet())
      {
        uc.setRequestProperty(entry.getKey(), entry.getValue());
      }
    }
    
    if(body!=null)
    {
      LOG.debug(String.format("🕷️ http body: %s", body));
      OutputStream os = uc.getOutputStream();
      os.write(body instanceof byte[] ? (byte[])body : body.toString().getBytes());
    }
    
    this.responseStream=uc.getInputStream();
  }


  public static void main(String[] args) throws Exception
  {
    String response=new Http().get("https://www.google.com").$();
    System.out.println(response);
  }
}
