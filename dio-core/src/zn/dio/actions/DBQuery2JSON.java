package zn.dio.actions;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import zn.dio.model.Value;
import zn.Configuration;
import zn.Logger;
import zn.dio.model.Field;
import zn.dio.model.ValueType;
import zn.dio.pipeline.action.BaseAction;

public class DBQuery2JSON extends BaseAction
{
  private static final Logger LOG=Logger.get(DBQuery2JSON.class);
  private static SimpleDateFormat ISO_DATE_TIME_FMT=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
 
  private Connection connection;
  private int batchSize;

  public DBQuery2JSON() 
  {
    
  }

  public Error execute(Map<String, String> options)
  {
    Error rc=null;
    LOG.info("executing pipeline action");
    try
    {
      convert(options);
    }
    catch(Exception ex)
    {
      LOG.error("Error occured while executing the pipeline action");
      LOG.error(ex);
      rc=new Error(ex.getMessage());
    }
    
    return rc;
  }

  public void convert(Map<String,String> options) throws Exception
  {
    String db=options.get("db");
    String query=options.get("query");
    String jsonFileName="query-output.json";
    if(options.get("table")!=null)
    {
      query="select * from "+options.get("table");
      jsonFileName=options.get("table")+".json";
    }
    if(options.get("output")!=null) jsonFileName=options.get("output");

    openDBConnection(db);
    convertQuery(db, query, jsonFileName);
    closeDBConnection();
  }

  public void convertQuery(String db, String query, String jsonfilename) throws Exception
  {
    LOG.info("SQL : "+query);
    LOG.info("extracting query output to "+jsonfilename);

    PreparedStatement stmt=connection.prepareStatement(query);
    ResultSet rs=stmt.executeQuery();
    ResultSetMetaData rsmd=rs.getMetaData();

    FileWriter fwriter=new FileWriter(jsonfilename);
    PrintWriter writer=new PrintWriter(fwriter);

    List<String> headers=columnNames(rsmd);
    List<String> jsonFields=toJsonFields(headers);
    
    LOG.info("query columns : "+headers);
    LOG.info("  json fields : "+jsonFields);

    int numColumns=headers.size();

    Map<String, Object> headerMap=new LinkedHashMap<String, Object>();
    headerMap.put("@//header", "dio-converter");
    headerMap.put("@db", db);
    headerMap.put("@query", query);
    headerMap.put("@created-on", ISO_DATE_TIME_FMT.format(new Date()));
    Map<String, Field> fieldsMap=new LinkedHashMap<String, Field>();

    Gson gson=new Gson();
    int row=0;
    while(rs.next())
    {
      row++;
      //LOG.debug("adding row "+row);

      List<Value> rowValues=row2ListValues(rs, numColumns, rsmd);
      Map<String, Object> map=new LinkedHashMap<String, Object>();
      for(int i=0, l=numColumns;i<l;i++)
      {
        String jsonField=jsonFields.get(i);
        Value value=rowValues.get(i);
        int jsonFieldType=value.getType();

        Object jsonFieldValue=value.getObjValue();
        //System.out.println(jsonField+":"+jsonFieldValue);
        if(jsonFieldType==ValueType.TYPE_DATE && jsonFieldValue!=null) jsonFieldValue=ISO_DATE_TIME_FMT.format(jsonFieldValue);
        map.put(jsonField, jsonFieldValue);
        if(row==1) fieldsMap.put(jsonField, new Field(jsonField, ValueType.toString(jsonFieldType), value.getFormat(), headers.get(i)));
      }
      if(row==1)
      {
        headerMap.put("@fields", fieldsMap);
        writer.println(gson.toJson(headerMap));  
      }
      String jsonRow=gson.toJson(map);
      writer.println(jsonRow);
    }

    Map<String, Object> footerMap=new LinkedHashMap<String, Object>();
    footerMap.put("@//footer", "");
    footerMap.put("@rows", row);
    writer.println(gson.toJson(footerMap));  

    writer.close();
    fwriter.close();
  }

  public List<String> columnNames(ResultSetMetaData rsmd) throws Exception
  {
    List<String> list=new ArrayList<String>();
    int columns=rsmd.getColumnCount();

    for(int i=1; i<=columns; i++)
    {
      list.add(rsmd.getColumnName(i));
    }
    return list;
  }

  public List<Value> row2ListValues(ResultSet rs, int length, ResultSetMetaData rsmd) throws Exception
  {
    List<Value> list=new ArrayList<Value>();
    for(int i=1;i<=length;i++)
    {
      int columnType=rsmd.getColumnType(i);
      if(columnType==Types.VARCHAR)
      {
        Value value=new Value(ValueType.TYPE_STRING, rs.getObject(i));
        list.add(value);
      }
      else if(columnType==Types.DATE || columnType==Types.TIME || columnType==Types.TIMESTAMP || columnType==Types.TIMESTAMP_WITH_TIMEZONE || columnType==Types.TIME_WITH_TIMEZONE)
      {
        Value value=new Value(ValueType.TYPE_DATE, rs.getDate(i));
        list.add(value);
      }
      else if(columnType==Types.BIT || columnType==Types.INTEGER || columnType==Types.TINYINT)
      {
        Value value=new Value(ValueType.TYPE_NUMERIC, rs.getInt(i));
        list.add(value);
      }
      else if(columnType==Types.DECIMAL || columnType==Types.DOUBLE || columnType==Types.FLOAT || columnType==Types.REAL)
      {
        Value value=new Value(ValueType.TYPE_NUMERIC, rs.getDouble(i));
        list.add(value);
      }
      else if(columnType==Types.BIGINT)
      {
        Value value=new Value(ValueType.TYPE_NUMERIC, rs.getBigDecimal(i));
        list.add(value);
      }
      else if(columnType==Types.BOOLEAN)
      {
        Value value=new Value(ValueType.TYPE_BOOLEAN, rs.getObject(i));
        list.add(value);
      }
      else 
      {
        Value value=new Value(ValueType.TYPE_STRING, rs.getString(i));
        list.add(value);
      }
    }
    return list;
  }

  public List<String> toJsonFields(List<String> fields)
  {
    List<String> rval=new ArrayList<String>();
    for(String field:fields) rval.add(toJsonField(field));
    return rval;
  }

  public String toJsonField(String field)
  {
    StringBuilder rval=new StringBuilder();
    byte[] chars=field.toLowerCase().getBytes();
    boolean cc=false;
    for(byte c:chars)
    {
      if(c>=97 && c<=122)
      {
        rval.append((char)(cc ? c-32 : c));
        cc=false;
      }
      else if(c>=48 && c<=57) rval.append((char)c);
      else cc=true;
    }
    
    return rval.toString();
  }

  public String toName(String str)
  {
    StringBuilder rval=new StringBuilder();
    byte[] chars=str.toLowerCase().getBytes();
    for(byte c:chars)
    {
      if((c>=97 && c<=122) || (c>=48 && c<=57)) rval.append((char)c);
      else rval.append("-");
    }
    
    return rval.toString();
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

  public static void main(String[] args) throws Exception
  {
    System.setProperty("dio.home", "C:/root/workspaces/dio/dist");
    Configuration.getInstance();

    Map<String, String> options=new HashMap<String, String>();
    options.put("db", "wd");
    options.put("query", "select * from demand");
    options.put("output", "C:\\root\\workspaces\\dataio\\temp\\output.json");

    DBQuery2JSON converter=new DBQuery2JSON();
    converter.convert(options);
  }

}
