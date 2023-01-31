package zn.dio.pipeline;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import zn.Configuration;
import zn.Logger;
import zn.dio.pipeline.action.BaseAction;

public class Executor 
{
  private static final Logger LOG=Logger.get(Executor.class);
  private Pattern varnamepattern=Pattern.compile("\\$\\{(env|vars|args)\\.([^\\}]+)\\}");
  
  public Executor()
  {
  }

  public void run(Map<String, String> options) throws Exception
  {
    String fileName="dio-pipeline.json";
    if(options.get("file")!=null) fileName=options.get("file");

    FileReader reader=new FileReader(fileName);
    Gson gson=new Gson();
    Map<?,?> pipeline=gson.fromJson(reader, Map.class);
    Map<String, String> vars=toMapOfStringValues(pipeline.get("vars"));
    expandMap(vars, vars, options);

    LOG.info("executing pipeline : "+pipeline.get("name"));
    LOG.info("vars : "+vars);

    Configuration config=Configuration.getInstance();
    Map<?,?> dioActions=config.$("dio.actions", Map.class);
    
    List<?> steps=(List<?>)pipeline.get("steps");
    for(Object obj:steps)
    {
      Map<String, String> step=toMapOfStringValues(obj);
      String name=(String)step.get("name");
      if("true".equals(step.get("skip")))
      {
        LOG.info("skipping step : "+name);
        continue;
      };

      expandMap(step, vars, options);
      String action=(String)step.get("action");
      String actionClass=(String)dioActions.get(action);

      LOG.info("executing step : "+name +", action="+action);
      if(actionClass==null)
      {
        LOG.error("action class "+actionClass+" not found for action "+action);
        break;
      }

      Class<?> actnclazz=Class.forName(actionClass);
      BaseAction actn=(BaseAction)actnclazz.getConstructor(null).newInstance();
      Error rc=actn.execute(step);
      if(rc!=null)
      {
        if(!"continue".equals(step.get("on-error")))
        {
          LOG.error("step resulted in error : "+rc.getMessage()+", stopping pipeline");
          break;
        }
        else LOG.error("step resulted in error : "+rc.getMessage()+", continuing pipeline");
      }
    }
    LOG.info("pipeline execution complete");
  }

  public Map<String, String> toMapOfStringValues(Object obj)
  {
    Map<String, String> rval=new HashMap<String, String>();
    if(obj==null) return rval;

    Map<?,?> map=(Map<?,?>)obj;
    Iterator<?> iter=map.keySet().iterator();
    while(iter.hasNext())
    {
      String key=(String)iter.next();
      rval.put(key, (String)map.get(key));
    }

    return rval;
  }

  public void expandMap(Map<String,String> map, Map<String, String> vars, Map<String, String> options)
  {
    Iterator<String> keys=map.keySet().iterator();
    while(keys.hasNext())
    {
      String key=keys.next();
      String expandedValue=expandVars(map.get(key), vars, options);
      map.put(key, expandedValue);
    }
  }

  public String expandVars(String str, Map<String, String> vars, Map<String, String> options)
  {
    Matcher matcher=varnamepattern.matcher(str);
    StringBuilder rval=new StringBuilder();

    int start=0;
    while(matcher.find())
    {
      rval.append(str.substring(start, matcher.start(0)));
      String varType=matcher.group(1).toLowerCase();
      String varName=matcher.group(2);
      String varValue=null;
      if("vars".equals(varType)) varValue=vars.get(varName);
      if("env".equals(varType)) varValue=System.getenv(varName);
      if("args".equals(varType)) varValue=(options!=null ? options.get(varName) : null);
      if(varValue!=null) rval.append(varValue);
      start=matcher.end(0);
    }
    rval.append(str.substring(start));

    return rval.toString();
  }

  public static void initConfig()
  {
    String home=System.getProperty("dio.home");
    Configuration.createInstanceFromJsonFile(home+"/config.json");
    Configuration config=Configuration.getInstance();
    config.put("home", home);
  }
    
  public static void main(String[] args) throws Exception
  {
    System.setProperty("dio.home", "C:/root/workspaces/dio/dist");
    initConfig();
   
    Map<String, String> options=new HashMap<String, String>();
    options.put("file", "C:/root/workspaces/dio/dist/pipeline/demand-supply.load.pileline.json");

    new Executor().run(options);
  }
}
