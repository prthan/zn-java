package zn.rs;

import java.util.ArrayList;
import java.util.Collection;

public class Module 
{
  public static Collection<Class<?>> resources()
  {
    Collection<Class<?>> c=new ArrayList<Class<?>>();
    return c;
  }

  public static Collection<Class<?>> providers()
  {
    Collection<Class<?>> c=new ArrayList<Class<?>>();
    c.add(zn.rs.filters.OpenIDRequestFilter.class);
    c.add(zn.rs.filters.CORSRequestFilter.class);
    c.add(zn.rs.filters.CORSResponseFilter.class);
    c.add(zn.rs.filters.RequestLoggerFilter.class);
    c.add(zn.rs.filters.ResponseLoggerFilter.class);

    c.add(zn.rs.JsonBodyReader.class);
    c.add(zn.rs.JsonBodyWriter.class);
    return c;
  }

  public static Collection<Class<?>> all()
  {
    Collection<Class<?>> c=new ArrayList<Class<?>>();
    c.addAll(Module.resources());
    c.addAll(Module.providers());

    return c;
  }
}
