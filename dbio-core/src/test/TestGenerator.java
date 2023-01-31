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
import java.util.Map;

import dbio.core.DBIO;
import dbio.core.DefinitionGenerator;
import dbio.core.model.ProcParameter;
import zn.Logger;

public class TestGenerator
{
  private static final String APP_HOME="/u02/workspaces/dbio-java/deploy/test";

  static
  {
    // Configuration.createInstanceFromJsonFile("/u02/workspaces/dbio-java/deploy/instances/dbio/config.json");
    // Configuration config=Configuration.getInstance();
    // Map<String, Object> loggerConfig=(Map)config.get("logger");
    // Logger.config(loggerConfig);
  }

  private static Logger LOG=Logger.get(TestDBIO.class);

  public static Connection createMySQLConnection() throws Exception
  {
    String connectStr="jdbc:mysql://localhost:6306/zn";
    String userid="znuser";
    String password="znuser";

    LOG.info(String.format("connecting to db, %s", connectStr));
    Connection connection=DriverManager.getConnection(connectStr, userid, password);
    connection.setAutoCommit(false);
    LOG.info(String.format("connected to database, %s", connectStr));

    return connection;
  }

  public static void dump(ResultSet rs) throws Exception
  {
    ResultSetMetaData rsmd=rs.getMetaData();
    List<String> columns=new ArrayList<String>();
    for(int i=1,l=rsmd.getColumnCount();i<=l;i++)
    {
      String colName=rsmd.getColumnName(i);
      columns.add(colName);
      System.out.println(colName);
    }
    while(rs.next())
    {
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
      for(String columnName:columns) System.out.println(columnName+"="+rs.getObject(columnName)+",");
    }
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
  
  public static void testMetadata(Connection connection) throws Exception
  {
    DatabaseMetaData dbmd=connection.getMetaData();
    System.out.println(">>>>>> table columns");
    //ResultSet rs2=dbmd.getColumns(null, "IHUB", "CUSTOMER", null);
    // ResultSet rs2=dbmd.getProcedureColumns(null, null, "test_proc1", null);
    // ResultSet rs2=dbmd.getProcedures(null, null, null);
    // ResultSet rs2=dbmd.getProcedureColumns(null, "IHUB", "TEST_PROC1", null);
    ResultSet rs2=dbmd.getTypeInfo();
    dump(rs2);
    rs2.close();
  }
    
  public static void testTableGenerator(DBIO dbio) throws Exception
  {
    DefinitionGenerator g=new DefinitionGenerator(dbio);
    g.generateForTable("IHUB", "CUSTOMER", null, APP_HOME + "/definitions/test/customer-o.json");
  }
  public static void testProcGenerator(DBIO dbio) throws Exception
  {
    DefinitionGenerator g=new DefinitionGenerator(dbio);
    //List<ProcParameter> params=g.getProcMetadata("IHUB", "TEST_PROC1");
    //g.generateForProc("IHUB", "TEST_PROC1", APP_HOME + "/definitions/test/test-proc-o.json");
    g.generateForProc("zn", "test_proc1", APP_HOME + "/definitions/test/test-proc-m.json");
  }
  public static void main(String[] args) throws Exception
  {
    // Connection connection=createOracleConnection();
    // DBIO dbio=new DBIO("oracle", connection);
    
    Connection connection=createMySQLConnection();
    DBIO dbio=new DBIO("mysql", connection);


    //testTableGenerator(dbio);
    testProcGenerator(dbio);
    // testMetadata(connection);

    releaseConnection(connection);
  }
}