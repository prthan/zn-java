package dbio.rs;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig
{
  public Application()
  {
    packages("zn.rs", "dbio.rs");
    register(LoggingFeature.class);
  }
}
