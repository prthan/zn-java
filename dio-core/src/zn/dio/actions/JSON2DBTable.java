package zn.dio.actions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import zn.Configuration;
import zn.Logger;
import zn.dio.pipeline.action.BaseAction;

public class JSON2DBTable extends BaseAction
{
  private static final Logger LOG=Logger.get(JSON2DBTable.class);
 
  private Connection connection;
  private int batchSize;

  public JSON2DBTable()
  {

  }

  public Error execute(Map<String, String> options)
  {
    Error rc=null;
    LOG.info("executing pipeline action");
    try
    {
      load(options);
    }
    catch(Exception ex)
    {
      LOG.error("Error occured while executing the pipeline action");
      LOG.error(ex);
      rc=new Error(ex.getMessage());
    }

    return rc;
  }

  public void load(Map<String,String> options) throws Exception
  {
    String db=options.get("db");
    String table=options.get("table");
    String jsonFileName="data.json";
    
    boolean truncate="true".equals(options.get("truncate"));

    Map<String, String> fieldMappings=mappings(options.get("map"));
    if(options.get("file")!=null) jsonFileName=options.get("file");  
    openDBConnection(db);
    if(truncate) truncateTable(table);
    loadFile(db, table, jsonFileName, fieldMappings);
    closeDBConnection();

  }  

  public void loadFile(String db, String table, String jsonFileName, Map<String, String> map) throws Exception
  {
    FileReader fin=new FileReader(jsonFileName);
    BufferedReader bin=new BufferedReader(fin);

    String insertSQL=getInsertSQL(table, map);
    LOG.info("SQL : "+insertSQL);

    PreparedStatement stmt=connection.prepareStatement(insertSQL);

    String str;
    Gson gson=new Gson();
    Map<?,?> fields=null;

    int lineCount=0;
    while((str=bin.readLine())!=null)
    {
      Map<?,?> jsonData=gson.fromJson(str, Map.class);
      if(jsonData.get("@//header")!=null)
      {
        fields=(Map<?,?>)jsonData.get("@fields");
        continue;
      }
      if(jsonData.get("@//footer")!=null) continue;


      List<Object> values=bindValues(map, jsonData, str);
      for(int i=0, l=values.size();i<l;i++) stmt.setObject(i+1, values.get(i));

      lineCount++;
      stmt.addBatch();

      if(lineCount==batchSize)
      {
        LOG.info("inserting batch...");
        stmt.executeBatch();
        connection.commit();
        lineCount=0;
      }
    }

    if(lineCount>0)
    {
      LOG.info("inserting batch...");
      stmt.executeBatch();
      connection.commit();
    }

    stmt.close();
    bin.close();
    fin.close();
  }

  public Map<String, String> mappings(String options)
  {
    Map<String, String> rval=new HashMap<String, String>();
    if(options==null) return rval;

    for(String mapping:options.split(","))
    {
      String[] attrs=mapping.split("=>");
      rval.put(attrs[0].trim(), attrs[1].trim());
    }

    return rval;
  }

  public String getInsertSQL(String table, Map<String,String> map)
  {
    StringBuilder bindHolder=new StringBuilder();
    StringBuilder fieldHolder=new StringBuilder();

    for(Map.Entry<String, String> entry:map.entrySet())
    {
      String dbColumn=entry.getValue();
      fieldHolder.append(", ").append(dbColumn);
      bindHolder.append(", ?");
    }

    StringBuilder sb=new StringBuilder("insert into ")
    .append(table).append("(")
    .append(fieldHolder.substring(2))
    .append(") values(").append(bindHolder.substring(2)).append(")");

    return sb.toString();
  }

  public List<Object> bindValues(Map<String,String> map, Map jsonData, String jsonRow)
  {
    List<Object> list=new ArrayList<Object>();
    for(Map.Entry<String, String> entry:map.entrySet())
    {
      String jsonField=entry.getKey();
      if(jsonField.startsWith("'") && jsonField.endsWith("'")) list.add(jsonField.substring(1, jsonField.length()-1));
      else if("@json".equals(jsonField)) list.add(jsonRow);
      else list.add(jsonData.get(jsonField));
    }

    return list;
  }

  public void openDBConnection(String id) throws Exception
  {
    Configuration config=Configuration.getInstance();
    Map<?,?> db=config.$("db."+id, Map.class);

    LOG.info("opening db connection, connect-id="+id);
    LOG.debug("connect id: "+id+" ==> "+db.get("connect-str"));
    
    Driver driver=(Driver)Class.forName((String)db.get("driver")).getDeclaredConstructor().newInstance();
    DriverManager.registerDriver(driver);
    connection=DriverManager.getConnection((String)db.get("connect-str"), (String)db.get("userid"), (String)db.get("password"));
    connection.setAutoCommit(false);
    if(config.$("batch-size", Number.class)!=null) batchSize=(config.$("batch-size", Number.class)).intValue();
    LOG.info("connected to db");
  }

  public void closeDBConnection() throws Exception
  {
    connection.close();
    LOG.info("disconnected from db");
  }
  
  public void truncateTable(String tableName) throws Exception
  {
    LOG.info("truncating table "+tableName);
    PreparedStatement stmt=connection.prepareStatement("truncate table "+tableName);
    stmt.execute();
    stmt.close();
  }

  public static void main(String[] args) throws Exception
  {
    System.setProperty("dio.home", "C:/root/workspaces/dio/dist");
    Configuration.getInstance();
   
    Map<String, String> options=new HashMap<String, String>();
    options.put("file", "C:/root/workspaces/workdesk/misc/demand/test/demand.json");
    options.put("map", "uniqueId=>unique_id");
    options.put("db", "wd");
    options.put("table", "stage.ds_demand_stg");
    options.put("truncate", "true");

    JSON2DBTable loader=new JSON2DBTable();
    loader.load(options);
  }
}
