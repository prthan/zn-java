package zn.dio.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import zn.Configuration;
import zn.Logger;
import zn.dio.actions.DBQuery2JSON;
import zn.dio.actions.Excel2JSON;
import zn.dio.actions.JSON2DBTable;
import zn.dio.pipeline.Executor;

public class Launcher
{
  private Map<String, Options> targetOptions=new HashMap<String,Options>();

  public Launcher()
  {
    Options options=new Options();
    options.addOption("file", true, "input excel file that needs to be converted");
    options.addOption("sheet", true, "the name of the sheet to be converted, if this is not specified the entire workbook will be converted");
    options.addOption("output", true, "output where the converted content will be stored, if sheet is specified this should be a filename");
    options.addOption("help", false, "print this message");
    targetOptions.put("excel-to-json", options);

    options=new Options();
    options.addOption("db", true, "db source identifier, connect details are retrived from app.properties for this identifier");
    options.addOption("query", true, "the query to use as source for the data");
    options.addOption("table", true, "the table to use as source for the data");
    options.addOption("output", true, "output where the converted content will be stored, if sheet is specified this should be a filename");
    options.addOption("help", false, "print this message");
    targetOptions.put("db-to-json", options);

    options=new Options();
    options.addOption("file", true, "input json file that needs to be loaded to the db");
    options.addOption("db", true, "db source identifier, connect details are retrived from app.properties for this identifier");
    options.addOption("table", true, "the table to load the data to");
    options.addOption("truncate", true, "true, to truncate the table before loading the data");
    options.addOption("map", true, "jsonField=>dbField, a comma separated list of mapping");
    options.addOption("help", false, "print this message");
    targetOptions.put("json-to-db", options);

    options=new Options();
    options.addOption("file", true, "pipeline file that has the steps to execute");
    options.addOption("help", false, "print this message");
    targetOptions.put("pipeline", options);

  }

  public Options clOptions(String target)
  {
    return targetOptions.get(target);
  }

  public Map<String,String> targetOptions(CommandLine cl) throws Exception
  {
    Map<String, String> options=new HashMap<String,String>();
    Iterator<Option> iter=cl.iterator();
    while(iter.hasNext())
    {
      Option clOption=iter.next();
      options.put(clOption.getOpt(), clOption.getValue());
    }
    
    List<String> args=cl.getArgList();
    for(int i=0, l=args.size(); i<l; i++) options.put("%"+(i+1), args.get(i));
    return options;
  }

  public static void help()
  {
    System.out.println("Invalid command\n");
    System.out.println("Usage: \n      dio pipeline|excel-to-json|db-to-json|json-to-db [options]\n");
    System.out.println("To get help for the command use -help");
  }

  public static void initConfig()
  {
    String home=System.getProperty("dio.home");
    Configuration.createInstanceFromJsonFile(home+"/config.json");
    Configuration config=Configuration.getInstance();
    config.put("home", home);
    Logger.config(config.$("logger", Map.class));
  }

  public static void main(String[] args) throws Exception
  {
    if(args.length<2)
    {
      help();
      return;
    }

    Launcher launcher=new Launcher();
    String target=args[0];
    String[] arguments=Arrays.copyOfRange(args, 1, args.length);

    Options clOptions=launcher.clOptions(target);
    if(clOptions==null)
    {
      help();
      return;
    }

    CommandLineParser parser=new DefaultParser();
    CommandLine cl=parser.parse(clOptions, arguments);
    
    if(cl.hasOption("help"))
    {
      HelpFormatter helpFormatter=new HelpFormatter();  
      helpFormatter.printHelp("dio "+target+" [options]", clOptions);
      return;
    }


    Launcher.initConfig();
    
    Map<String, String> targetOptions=launcher.targetOptions(cl);
    if("excel-to-json".equals(target)) new Excel2JSON().convert(targetOptions);
    if("db-to-json".equals(target)) new DBQuery2JSON().convert(targetOptions);
    if("json-to-db".equals(target)) new JSON2DBTable().load(targetOptions);
    if("pipeline".equals(target)) new Executor().run(targetOptions);
  }
}
