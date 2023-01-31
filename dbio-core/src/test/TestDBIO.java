package test;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;

import dbio.core.DBIO;
import dbio.core.IOObject;
import dbio.core.IOObjectList;
import dbio.core.Outcome;
import dbio.core.SQL;
import dbio.core.Utils;
import dbio.core.model.Definition;
import dbio.core.model.ListOptions;
import zn.Logger;
import zn.Json;

public class TestDBIO
{
  private static final String APP_HOME="/u02/workspaces/dbio-java/workspace/deploy/instances/mysql";

  private static Logger LOG=Logger.get(TestDBIO.class);

  public static Connection createMySQLConnection() throws Exception
  {
    String connectStr="jdbc:mysql://192.168.1.10:6306/zn";
    String userid="znuser";
    String password="znuser";

    Connection connection=DriverManager.getConnection(connectStr, userid, password);
    connection.setAutoCommit(false);
    LOG.info(String.format("connected to database, %s", connectStr));

    return connection;
  }

  public static Connection createOracleConnection() throws Exception
  {
    String connectStr="jdbc:oracle:thin:@130.61.80.185:1521/OCIPDB01.sub07301043290.vcn01paas01.oraclevcn.com";
    String userid="ihub";
    String password="passw0rd";

    // Properties props=new Properties();
    // props.put("oracle.jdbc.J2EE13Compliant", "true");
    // props.put("user", userid);
    // props.put("password", password);

    Connection connection=DriverManager.getConnection(connectStr, userid, password);
    connection.setAutoCommit(false);
    LOG.info(String.format("connected to database, %s", connectStr));

    return connection;
  }

  public static void releaseConnection(Connection conn) throws SQLException
  {
    conn.close();
    LOG.info("connection released");    
  }
  
  public static void testExec(DBIO dbio) throws Exception
  {
    List<SQL> sqls=new ArrayList<SQL>();
    sqls.add(new SQL("select * from customer where oid<=10", new ArrayList<Object>()));
    sqls.add(new SQL("select * from customer where oid=1", new ArrayList<Object>()));

    dbio.exec(sqls);

    System.out.println(sqls.get(0).getResults());
    System.out.println(sqls.get(1).getResults());
  }

  public static void testCreate(DBIO dbio) throws Exception
  {
    IOObject dataset=Json.fromFile(APP_HOME + "/sample/data-set.json", IOObject.class);
    Definition customerDefn=Json.fromFile(APP_HOME + "/definitions/"+dbio.getDbType()+"/customer.json", Definition.class);
    IOObjectList list=IOObjectList.fromList(dataset.$("create-customers", List.class));
    Outcome<IOObjectList> outcome=dbio.create(customerDefn, list, "create customers");
    if(outcome.isError()) System.out.println(outcome.getError());
    else System.out.println(outcome.getValue());
 }

  public static void testUpdate(DBIO dbio) throws Exception
  {
    IOObject dataset=Json.fromFile(APP_HOME + "/sample/data-set.json", IOObject.class);
    Definition customerDefn=Json.fromFile(APP_HOME + "/definitions/"+dbio.getDbType()+"/customer.json", Definition.class);
    IOObjectList list=IOObjectList.fromList(dataset.$("update-customers", List.class));
    Outcome<IOObjectList> outcome=dbio.update(customerDefn, list, "update customers");
    if(outcome.isError()) System.out.println(outcome.getError());
  }

  public static void testDelete(DBIO dbio) throws Exception
  {
    IOObject dataset=Json.fromFile(APP_HOME + "/sample/data-set.json", IOObject.class);
    Definition customerDefn=Json.fromFile(APP_HOME + "/definitions/"+dbio.getDbType()+"/customer.json", Definition.class);
    IOObjectList list=IOObjectList.fromList(dataset.$("delete-customers", List.class));
    Outcome<List<Object>> outcome=dbio.delete(customerDefn, list, "delete customers");
    if(outcome.isError()) System.out.println(outcome.getError());
  }
  
  public static void testList(DBIO dbio) throws Exception
  {
    IOObject dataset=Json.fromFile(APP_HOME + "/sample/data-set.json", IOObject.class);
    // Definition customerDefn=Json.fromFile(APP_HOME + "/definitions/"+dbio.getDbType()+"/customer.json", Definition.class);
    Definition customerDefn=Json.fromFile(APP_HOME + "/definitions/test/customer-o.json", Definition.class);
    ListOptions options=new ListOptions();
    options
      // .filter("firstName", "eq", "John")
      // .filter("lastName", "eq", "Wick")
      // .sort("oid", "desc")
      .page(1);
           
    Outcome<IOObjectList> outcome=dbio.list(customerDefn, options, "select customers");
    if(outcome.isError()) System.out.println(outcome.getError());
    else System.out.println(Json.stringifyPretty(outcome.getValue()));
  }

  public static void testGet(DBIO dbio) throws Exception
  {
    IOObject dataset=Json.fromFile(APP_HOME + "/sample/data-set.json", IOObject.class);
    Definition customerDefn=Json.fromFile(APP_HOME + "/definitions/"+dbio.getDbType()+"/customer.json", Definition.class);
           
    Outcome<IOObject> outcome=dbio.get(customerDefn, 1, "get customer");
    if(outcome.isError()) System.out.println(outcome.getError());
    else System.out.println(outcome.getValue());

  }

  public static void testCall(DBIO dbio) throws Exception
  {
    IOObject dataset=Json.fromFile(APP_HOME + "/sample/data-set.json", IOObject.class);
    Definition defn=Json.fromFile(APP_HOME + "/definitions/"+dbio.getDbType()+"/test-proc.json", Definition.class);
           
    Outcome<IOObject> outcome=dbio.call(defn, dataset.$("call-test-proc-"+dbio.getDbType()), "call test proc");
    if(outcome.isError()) System.out.println(outcome.getError());
    else System.out.println(Json.stringifyPretty(outcome.getValue()));

  }
  
  public static void dump(ResultSet rs) throws Exception
  {
    ResultSetMetaData rsmd=rs.getMetaData();
    List<String> columns=new ArrayList<String>();
    for(int i=1,l=rsmd.getColumnCount();i<=l;i++) columns.add(rsmd.getColumnName(i));
    while(rs.next())
    {
      for(String columnName:columns) System.out.print(columnName+"="+rs.getObject(columnName)+",");
      System.out.println();
    }
  }

  public static void testMetadata(Connection connection) throws Exception
  {
    DatabaseMetaData dbmd=connection.getMetaData();
    ResultSet rs1=dbmd.getTables(null, null, "CUSTOMER", null);
    dump(rs1);
    rs1.close();

    System.out.println(">>>>>> table columns");
    ResultSet rs2=dbmd.getColumns(null, null, "CUSTOMER", null);
    dump(rs2);
    rs2.close();
  }

  public static void main(String[] args) throws Exception
  {
    Connection connection=createOracleConnection();
    DBIO dbio=new DBIO("oracle", connection);
    
    // Connection connection=createMySQLConnection();
    // DBIO dbio=new DBIO("mysql", connection);

    dbio.setIdentity(new zn.rs.model.Identity("sysadmin", 4, "Sytem Admin"));
    
    // testExec(dbio);
    // testCreate(dbio);
    // testUpdate(dbio);
    // testDelete(dbio);
    testList(dbio);
    // testGet(dbio);
    // testCall(dbio);
    // testMetadata(connection);

    releaseConnection(connection);
  }
}