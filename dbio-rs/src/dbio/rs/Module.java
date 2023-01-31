package dbio.rs;

import java.util.ArrayList;
import java.util.Collection;

import dbio.rs.resources.DefaultDBIOResource;

public class Module 
{
  public static Collection<Class<?>> resources()
  {
    Collection<Class<?>> c=new ArrayList<Class<?>>();
    c.add(DefaultDBIOResource.class);

    return c;
  }

  public static Collection<Class<?>> providers()
  {
    Collection<Class<?>> c=new ArrayList<Class<?>>();
    c.add(dbio.rs.filters.RequestFilter.class);
    c.add(dbio.rs.filters.ResponseFilter.class);
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
