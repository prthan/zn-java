package dbio.rs;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import dbio.core.DefinitionStore;
import zn.Logger;
import zn.Configuration;

public class ApplicationListener implements ServletContextListener
{
  private void initLogging()
  {
    System.out.println("<DBIO> Initializing logger");
    Configuration config=Configuration.getInstance();
    Map<String, Object> loggerConfig=config.$("logger", Map.class);
    Logger.config(loggerConfig);
  }

  private void loadConfiguration(String home)
  {
    String appConfigFile=home + "/config.json";
  
    System.out.println("<DBIO> loading config from file "+appConfigFile);
    Configuration.createInstanceFromJsonFile(appConfigFile);
  }

  private void loadAppResources(ServletContext ctxt, String home)
  {
    Logger LOG=Logger.get(ApplicationListener.class);
    LOG.info("loading app resources");
    DefinitionStore defnStore=new DefinitionStore(home);
    ctxt.setAttribute("dbio.defn.store", defnStore);
  }

  public void contextInitialized(ServletContextEvent evt)
  {
    ServletContext ctxt=evt.getServletContext();
    String context=ctxt.getContextPath().substring(1);
    String home=System.getProperty(context+".home");

    loadConfiguration(home);
    initLogging();
    loadAppResources(ctxt, home);

    Configuration config=Configuration.getInstance();
    config.put("dbio.home", home);

    Logger LOG=Logger.get(ApplicationListener.class);
    LOG.info(String.format("Staring %s API Application at /%s", config.$("name"), context));
  }

  public void contextDestroyed(ServletContextEvent evt)
  {
    Logger LOG=Logger.get(ApplicationListener.class);

    ServletContext ctxt=evt.getServletContext();
    String context=ctxt.getContextPath().substring(1);

    Configuration config=Configuration.getInstance();
    LOG.info(String.format("Stopping %s API Application at /%s", config.$("name"),context));

  }
}