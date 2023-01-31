package zn;

import java.util.HashMap;
import java.util.Map;

import zn.logger.LogLevel;

public class Logger 
{
  private static final Map<String, Object> config=new HashMap<String, Object>();
  
  protected static final ThreadLocal<Map<String, Object>> threadLocalattributes=new ThreadLocal<Map<String, Object>>()
  {
    protected Map<String, Object> initialValue()
    {
      return new HashMap<String, Object>();
    }
  };
  
  public static void config(Map<String, Object> properties)
  {
    Logger.config.putAll(properties);
  }

  public static Logger get(String name)
  {
    String level=(String)config.get("level");
    String matchedPackageName="";
    for(Map.Entry<String, Object> entry:config.entrySet())
    {
      String key=entry.getKey();
      if(key.startsWith("level."))
      {
        String packageName=key.substring(6);
        if(name.matches(packageName+".*") && packageName.length() > matchedPackageName.length())  matchedPackageName=packageName;
      }
    }

    if(!"".equals(matchedPackageName)) level=(String)config.get("level."+matchedPackageName);

    Map<String, Object> properties=new HashMap<String, Object>();
    properties.putAll(config);
    properties.put("name", name);
    properties.put("log-level", LogLevel.valueOf(level==null? "INFO" : level));

    Logger logger=null;
    try
    {
      String className=(String)properties.get("class");
      if(className==null) className="zn.logger.ConsoleLogger";
      
      Class<?> clazz=Class.forName(className);
      logger=(Logger)clazz.getDeclaredConstructor(Map.class).newInstance(properties);
    }
    catch(Exception ex)
    {
      System.out.println("uanble to create the logger class "+ex);
      ex.printStackTrace();
    }

    return logger;
  }

  public static Logger get(Class<?> clazz)
  {
    return get(clazz.getName());
  }

  public void set(String name, Object value) 
  {
    Map<String, Object> attributes=threadLocalattributes.get();
    if(attributes==null)
    {
      attributes=new HashMap<String, Object>();
      threadLocalattributes.set(attributes);
    }
    attributes.put(name, value);
  };

  public void log(LogLevel level, Object ... params) {};
  public void error(Object ... params) {};
  public void info(Object ... params) {};
  public void warn(Object ... params) {};
  public void debug(Object ... params) {};
  public void trace(Object ... params) {};
  public boolean canLog(LogLevel level) {return false;};
  

  public static void main(String[] args) throws Exception
  {
    // Configuration.createInstanceFromJsonFile("/u02/workspaces/dbio-java/deploy/instances/dbio/config.json");
    // Configuration config=Configuration.getInstance();
    // Map<String, Object> loggerConfig=(Map)config.get("logger");
    // Logger.config(loggerConfig);
    // Logger logger=Logger.get(Logger.class);
    // logger.info("This is a test");
  }
}
