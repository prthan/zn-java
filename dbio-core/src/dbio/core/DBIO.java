package dbio.core;

import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Struct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dbio.core.model.ChildData;
import dbio.core.model.Clause;
import dbio.core.model.Definition;
import dbio.core.model.Field;
import dbio.core.model.Filter;
import dbio.core.model.ListOptions;
import dbio.core.model.Parameter;
import dbio.core.model.SortField;
import dbio.core.model.TableColumn;
import dbio.core.model.error.InvalidDataError;
import zn.Logger;
import zn.logger.LogLevel;
import zn.Json;
import zn.rs.model.Error;
import zn.rs.model.Identity;
import zn.rs.model.error.InternalServerError;

@SuppressWarnings("unchecked")
public class DBIO 
{
  private static Logger LOG=Logger.get(DBIO.class);
  private Connection connection;
  private String dbType;
  private boolean autoTransactions;
  private Identity identity;
  private DefinitionStore defnStore;
  
  private static SimpleDateFormat DT_FMT=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  
  private static Map<String, Integer> parameterTypes=new HashMap<String, Integer>();
  static
  {
    parameterTypes.put("string", java.sql.Types.VARCHAR);
    parameterTypes.put("numeric", java.sql.Types.NUMERIC);
    parameterTypes.put("date", java.sql.Types.DATE);
    parameterTypes.put("timestamp", java.sql.Types.TIMESTAMP);
    parameterTypes.put("table", java.sql.Types.ARRAY);
    parameterTypes.put("struct", java.sql.Types.STRUCT);
  }

  public DBIO(String dbType, Connection connection)
  {
    this.connection=connection;
    this.dbType=dbType;
    this.autoTransactions=true;
  }

  private static Map<String, String> OPERATORS=new HashMap<String, String>();
  static
  {
    OPERATORS.put("li", "like");
    OPERATORS.put("nli", "not like");
    OPERATORS.put("eq", "=");
    OPERATORS.put("neq", "<>");
    OPERATORS.put("gt", ">");
    OPERATORS.put("lt", "<");
    OPERATORS.put("gte", ">=");
    OPERATORS.put("lte", "<=");
    OPERATORS.put("in", "in");
    OPERATORS.put("nin", "not in");
    OPERATORS.put("isnull", "is null");
    OPERATORS.put("isnil", "is nil");
    OPERATORS.put("inwnull", "in with null");
    OPERATORS.put("inwb", "in with blank");
    OPERATORS.put("inwnil", "in with nil");
  }

  public Connection getConnection() {return connection;}
  public void setConnection(Connection connection) {this.connection = connection;}
  public String getDbType() {return dbType;}
  public void setDbType(String dbType) {this.dbType = dbType;}
  public boolean isAutoTransactions() {return autoTransactions;}
  public void setAutoTransactions(boolean autoTransactions) {this.autoTransactions = autoTransactions;}
  public Identity getIdentity() {return identity;}
  public void setIdentity(Identity identity) {this.identity = identity;}

  public DefinitionStore getDefnStore() {return defnStore;}
  public void setDefnStore(DefinitionStore defnStore) {this.defnStore = defnStore;}

  public void begin()
  {
    LOG.info("BEGIN TxN");
  }

  public void commit() throws SQLException
  {
    LOG.info("COMMIT TxN");
    connection.commit();
  }

  public void rollback() throws SQLException
  {
    LOG.info("ROLLBACK TxN");
    connection.rollback();
  }

  private PreparedStatement $$prepareStatement(SQL sql) throws SQLException
  {
    String query=sql.getQuery();
    List<Object> binds=sql.getBinds();
    
    PreparedStatement stmt=null;
    String generatedKeyField=sql.getGeneratedKeyField();
    if(generatedKeyField!=null) stmt=connection.prepareStatement(query, new String[] {sql.getGeneratedKeyField()});
    else stmt=connection.prepareStatement(query);
    
    for(int i=0,l=binds.size();i<l;i++)
    {
      Object bindValue=binds.get(i);
      if(!(bindValue instanceof Date)) stmt.setObject(i+1, bindValue);
      else stmt.setObject(i+1, new java.sql.Date(((Date)bindValue).getTime()));
    }

    return stmt;
  }

  private ResultSet $$execStatement(PreparedStatement stmt) throws SQLException
  {
    stmt.execute();
    return stmt.getResultSet();
  }

  private ResultSet $$generatedKeys(PreparedStatement stmt, SQL sql) throws SQLException
  {
    ResultSet rs=null;
    if(sql.getQuery().toUpperCase().startsWith("INSERT")) rs=stmt.getGeneratedKeys();
    return rs;
  }

  private Object toValue(ResultSet rs, String colName, String type, String format) throws SQLException
  {
    Object value=null;

    if(type==null) value=rs.getObject(colName);
    else if("DATE|date".indexOf(type)!=-1)
    {
      java.sql.Date dateValue=rs.getDate(colName);
      if(dateValue!=null)
      {
        SimpleDateFormat dtfmt= (format==null ? DT_FMT : new SimpleDateFormat(format));
        value=dtfmt.format(new java.util.Date(dateValue.getTime()));
      }
    }
    else if("DATETIME|TIME|TIMESTAMP(6)|timestamp|datetime".indexOf(type)!=-1)
    {
      java.sql.Timestamp tsValue=rs.getTimestamp(colName);
      if(tsValue!=null)
      {
        SimpleDateFormat dtfmt= (format==null ? DT_FMT : new SimpleDateFormat(format));
        value=dtfmt.format(new java.util.Date(tsValue.getTime()));
      }
    }
    else value=rs.getObject(colName);

    return value;
  }

  private Object toValue(CallableStatement stmt, int index, String type, String format) throws SQLException
  {
    Object value=null;

    if("date".equals(type))
    {
      java.sql.Date dateValue=stmt.getDate(index);
      if(dateValue!=null)
      {
        SimpleDateFormat dtfmt= (format==null ? DT_FMT : new SimpleDateFormat(format));
        value=dtfmt.format(new java.util.Date(dateValue.getTime()));
      }
    }
    else if("timestamp".equals(type))
    {
      java.sql.Timestamp tsValue=stmt.getTimestamp(index);
      if(tsValue!=null)
      {
        SimpleDateFormat dtfmt= (format==null ? DT_FMT : new SimpleDateFormat(format));
        value=dtfmt.format(new java.util.Date(tsValue.getTime()));
      }
    }
    else value=stmt.getObject(index);

    return value;
  }

  private List<TableColumn> getTableColumnsFromMetaData(ResultSetMetaData rsmd) throws SQLException
  {
    int l=rsmd.getColumnCount();
    List<TableColumn> list=new ArrayList<TableColumn>();
    for(int i=1; i<=l; i++)
    {
      TableColumn tc=new TableColumn();
      tc.setName(rsmd.getColumnName(i));
      tc.setType(rsmd.getColumnTypeName(i));
      tc.setSize(rsmd.getColumnDisplaySize(i));
      list.add(tc);
    }

    return list;
  }

  private Map<String, Field> dbFieldMapFromMetadata(ResultSetMetaData rsmd) throws SQLException
  {
    int l=rsmd.getColumnCount();
    Map<String, Field> map=new HashMap<String, Field>();
    for(int i=1; i<=l; i++)
    {
      Field field=new Field();
      String dbfieldname=rsmd.getColumnName(i);
      field.setName(Utils.toCamelcase(dbfieldname));
      field.setDbfield(dbfieldname);
      String type=rsmd.getColumnTypeName(i);
      if("DATE".indexOf(type)!=-1) type="date";
      if("DATETIME|TIME|TIMESTAMP(6)".indexOf(type)!=-1) type="datetime";
        
      field.setType(type);
      map.put(dbfieldname, field);
    }

    return map;
  }

  public void sql(SQL sql) throws SQLException
  {
    try
    (
      PreparedStatement stmt=$$prepareStatement(sql);
      ResultSet rs=$$execStatement(stmt);
      ResultSet krs=$$generatedKeys(stmt, sql);
    )
    {
      IOObjectList list=resultSetToList(rs, null);
      sql.setResults(list);
      if(krs!=null && krs.next()) sql.setGeneratedKey(krs.getObject(1));
    }
    catch(SQLException ex)
    {
      throw ex;
    }
  }

  public void sql(SQL sql, Map<String, Object> shape, Map<String, Field> dbFieldNameMap) throws SQLException
  {
    try
    (
      PreparedStatement stmt=$$prepareStatement(sql);
      ResultSet rs=$$execStatement(stmt);
    )
    {
      IOObjectList list=null;

      if(shape==null) list=resultSetToList(rs, dbFieldNameMap);
      else list=resultSetToListOfShape(rs, shape, dbFieldNameMap);

      sql.setResults(list);
    }
    catch(SQLException ex)
    {
      throw ex;
    }
  }

  public CallableStatement $$prepareCallStatement(CallSQL sql) throws SQLException
  {
    String query=sql.getQuery();
    List<Parameter> parameters=sql.getDefinition().parameters();
    List<Object> binds=sql.getBinds();
    
    CallableStatement stmt=null;
    stmt=connection.prepareCall(query);
    
    for(int i=0,l=binds.size();i<l;i++)
    {
      Parameter defnParameter=parameters.get(i);
      String mode=defnParameter.getMode();
      String name=defnParameter.getName();
      String type=defnParameter.getType();
      Object paramValue=binds.get(i);

      int paramType=parameterTypes.get(type);

      if("in".equals(mode) || "inout".equals(mode))
      {
        LOG.debug("setting "+mode+" parameter "+(i+1)+": "+ name);
        if(paramType==java.sql.Types.ARRAY) stmt.setArray(i+1, (Array)paramValue); 
        else stmt.setObject(i+1, paramValue);
      }
      if("out".equals(mode) || "inout".equals(mode))
      {
        LOG.debug("registering "+mode+" parameter "+(i+1)+": "+ name);
        if(paramType==java.sql.Types.ARRAY) stmt.registerOutParameter(i+1, paramType, defnParameter.getTableType());
        else if(paramType==java.sql.Types.STRUCT) stmt.registerOutParameter(i+1, paramType, defnParameter.getItemType());
        else stmt.registerOutParameter(i+1, paramType);
      }
    }

    return stmt;
  }

  public void callsql(CallSQL sql) throws SQLException
  {
    try
    (
      CallableStatement stmt=$$prepareCallStatement(sql);
    )
    {
      LOG.debug("calling proc: " + sql.getDefinition().procedure());
      stmt.execute();
      outputParametersToResults(stmt, sql);
      outputResultsetsToResults(stmt, sql);
    }
    catch(SQLException ex)
    {
      throw ex;
    }
  }

  public void exec(List<SQL> sqls) throws SQLException
  {
    if(autoTransactions) begin();
    try
    {
      for(SQL sql:sqls) this.sql(sql);
      if(autoTransactions) commit();
    }
    catch(SQLException ex)
    {
      LOG.error("error occured while executing sqls", ex);
      if(autoTransactions) rollback();
      throw ex;
    }
  }

  public List<Object> objToBinds(Map<String, Object> obj, List<String> objFieldsList)
  {
    List<Object> binds=new ArrayList<Object>();
    for(String field:objFieldsList)
    {
      binds.add(obj.get(field));
    }

    return binds;
  }

  public List<Filter> objToFilters(Map<String, Object> obj)
  {
    List<Filter> filters=new ArrayList<Filter>();
    for(Map.Entry<String, Object> entry:obj.entrySet())
    {
      Filter filter=new Filter(entry.getKey(), "eq", entry.getValue());
      if(entry.getValue() instanceof List) filter.setOp("in");
      filters.add(filter);
    }

    return filters;    
  }

  public Map<String, Field> dbfieldNameMap(Definition defn)
  {
    Map<String, Field> dbfieldNameMap=new HashMap<String, Field>();
    for(Map.Entry<String, Object> entry:defn.fields().entrySet())
    {
      String objFieldName=entry.getKey();
      Field defnField=defn.field(objFieldName);
      dbfieldNameMap.put(defnField.getDbfield(), defnField);
    }
    return dbfieldNameMap;
  }

  private IOObjectList resultSetToList(ResultSet rs, Map<String, Field> dbfieldNameMap) throws SQLException
  {
    if(rs==null) return null;

    ResultSetMetaData rsmd=rs.getMetaData();
    List<TableColumn> columns=getTableColumnsFromMetaData(rsmd);

    IOObjectList rows=new IOObjectList();
    while(rs.next())
    {
      IOObject row=new IOObject();
      for(TableColumn tc:columns)
      {
        String colName=tc.getName();
        String objFieldName=colName;
        String type=tc.getType();
        String format=null;
        if(dbfieldNameMap!=null)
        {
          Field defnField=dbfieldNameMap.get(colName);
          if(defnField!=null)
          {
            objFieldName=defnField.getName();
            type=defnField.getType();
            format=defnField.getFormat();
          }
        }
        else objFieldName=Utils.toCamelcase(colName);

        row.put(objFieldName, toValue(rs, colName, type, format));
      }
      rows.add(row);
    }
    return rows;
  }


  public Map<String, Object> autoShapeFromDefnFields(List<Field> defnFields)
  {
    Map<String, Object> rval=new HashMap<String, Object>();
    for(Field field:defnFields) rval.put(field.getName(), "#{row."+field.getDbfield()+"}");
    return rval;
  }

  public IOObject rowToShape(ResultSet rs, Map<String, Object> shape, Map<String, Field> dbFieldNameMap) throws SQLException
  {
    IOObject shapeObj=new IOObject();
    for(Map.Entry<String, Object> entry:shape.entrySet())
    {
      String field=entry.getKey();
      Object entryValue=entry.getValue();
      if(entryValue instanceof String)
      {
        Pattern pattern=Pattern.compile("\\#\\{row\\.(.+)\\}");
        Matcher matcher=pattern.matcher((String)entryValue);
        if(matcher.matches())
        {
          String dbfieldname=matcher.group(1);
          String type=null;
          String format=null;
          if(dbFieldNameMap!=null)
          {
            Field defnField=dbFieldNameMap.get(dbfieldname);
            type=defnField.getType();
            format=defnField.getFormat();
          }
          
          shapeObj.put(field, toValue(rs, dbfieldname, type, format));
        }
        else shapeObj.put(field, entryValue);
      }
      if(entryValue instanceof Map) shapeObj.put(field, rowToShape(rs, (Map<String, Object>)entryValue, dbFieldNameMap));
    }

    return shapeObj;
  }

  public IOObject dbStructToShape(Object[] structAttributes, Map<String, Object> shape, Map<String, Field> dbFieldNameMap, Map<String, Integer> indexMap) throws SQLException
  {
    IOObject shapeObj=new IOObject();
    for(Map.Entry<String, Object> entry:shape.entrySet())
    {
      String field=entry.getKey();
      Object entryValue=entry.getValue();
      if(entryValue instanceof String)
      {
        Pattern pattern=Pattern.compile("\\#\\{row\\.(.+)\\}");
        Matcher matcher=pattern.matcher((String)entryValue);
        if(matcher.matches())
        {
          String dbfieldname=matcher.group(1);
          //Field defnField=dbFieldNameMap.get(dbfieldname);
          shapeObj.put(field, structAttributes[indexMap.get(dbfieldname)]);
        }
        else shapeObj.put(field, entryValue);
      }
      if(entryValue instanceof Map) shapeObj.put(field, dbStructToShape(structAttributes, (Map<String, Object>)entryValue, dbFieldNameMap, indexMap));
    }

    return shapeObj;
  }

  public IOObjectList resultSetToListOfShape(ResultSet rs, Map<String, Object> shape, Map<String, Field> dbFieldNameMap) throws SQLException
  {
    IOObjectList list=new IOObjectList();
    while(rs.next()) list.add(rowToShape(rs, shape, dbFieldNameMap));

    return list;
  }

  public IOObjectList dbArrayToListOfShape(Array array, Map<String, Object> shape, Map<String, Field> dbFieldNameMap, Map<String, Integer> indexMap) throws SQLException
  {
    IOObjectList list=new IOObjectList();
    for(Object item:(Object[])array.getArray())
    {
      list.add(dbStructToShape(((Struct)item).getAttributes(), shape, dbFieldNameMap, indexMap));
    }    

    return list;
  }

  public void outputParametersToResults(CallableStatement stmt, CallSQL sql) throws SQLException
  {
    IOObject result=sql.getResult();
    Definition defn=sql.getDefinition();
    List<Parameter> parameters=defn.parameters();
    for(int i=0,l=parameters.size();i<l;i++)
    {
      Parameter parameter=parameters.get(i);
      String mode=parameter.getMode();
      String type=parameter.getType();
      String fieldName=parameter.getFieldName();
      String name=parameter.getName();
      String format=parameter.getFormat();

      if("out".equals(mode) || "inout".equals(mode))
      {
        LOG.debug("getting "+mode+" parameter "+(i+1)+": "+name);

        if("table".equals(type))
        {
          Array array=stmt.getArray(i+1);
          if(array==null) continue;

          String itemType=parameter.getItemType();
          Map<String, Field> itemFieldsDefn=defn.typeFields(itemType);
          List<Field> itemFields=defn.typeFieldsList(itemType);
          Map<String, Object> shape=defn.typeShape(itemType);
          if(shape==null) shape=autoShapeFromDefnFields(defn.typeFieldsList(itemType));

          Map<String, Integer> indexMap=new HashMap<String, Integer>();
          int j=0;
          for(Field field:itemFields) indexMap.put(field.getDbfield(), j++);
          result.put(fieldName, dbArrayToListOfShape(array, shape, itemFieldsDefn, indexMap));
        }
        else if("struct".equals(type))
        {
          Struct struct=(Struct)stmt.getObject(i+1);
          if(struct==null) continue;

          String itemType=parameter.getItemType();
          Map<String, Field> itemFieldsDefn=defn.typeFields(itemType);
          List<Field> itemFields=defn.typeFieldsList(itemType);
          Map<String, Object> shape=defn.typeShape(itemType);
          if(shape==null) shape=autoShapeFromDefnFields(defn.typeFieldsList(itemType));

          Map<String, Integer> indexMap=new HashMap<String, Integer>();
          int j=0;
          for(Field field:itemFields) indexMap.put(field.getDbfield(), j++);
          result.put(fieldName, dbStructToShape(struct.getAttributes(), shape, itemFieldsDefn, indexMap));
        }
        else result.put(fieldName,toValue(stmt, i+1, type, format));
      }
    }
  }

  public void outputResultsetsToResults(CallableStatement stmt, CallSQL sql) throws SQLException
  {
    int i=1;
    IOObject result=sql.getResult();
    Map<String, Object> resultSets=sql.getDefinition().resultSets();
    ResultSet rs=stmt.getResultSet();
    while(rs!=null)
    {
      Map<String, Field> dbFieldNameMap=dbFieldMapFromMetadata(rs.getMetaData());
      Map<String, Object> resultSet=(Map<String, Object>)resultSets.get(""+i);
      
      String field=(String)resultSet.get("field");
      if(field==null) field="results-"+i;

      Map<String, Object> shape=(Map<String, Object>)resultSet.get("shape");

      if(shape!=null) result.put(field, resultSetToListOfShape(rs, shape, dbFieldNameMap));
      else result.put(field, resultSetToList(rs, dbFieldNameMap));

      i++;
      if(stmt.getMoreResults(CallableStatement.CLOSE_CURRENT_RESULT))
      {
        rs=stmt.getResultSet();
        dbFieldNameMap=dbFieldMapFromMetadata(rs.getMetaData());
      }
      else
      {
        if(rs!=null) rs.close();
        rs=null;
      };
    }
  }

  public String inListCondition(List<Object> value, List<Object> binds, String dbfield, String op) 
  {
    List<String> placeHolders=new ArrayList<String>();
    for(Object v:value)
    {
      binds.add(v);
      placeHolders.add("?");
    }

    return String.format("%s %s (%s)", dbfield, op, String.join(", ", placeHolders));
  }

  public Clause toWhereClause(Definition defn, ListOptions options, String conjunction)
  {
    List<Object> binds=new ArrayList<Object>();
    List<String> conditions=new ArrayList<String>();
    List<String> errors=new ArrayList<String>();

    if(options.getFilters()==null || options.getFilters().size()==0) return new Clause("", binds, null);

    for(Filter filter: options.getFilters())
    {
      String op=filter.getOp();
      String field=filter.getField();
      Object bindValue=filter.getValue();

      Field defnField=defn.field(field);
      String coop=OPERATORS.get(op);

      if(defnField==null)
      {
        errors.add(String.format("field: %s is invalid", field));
        continue;
      }

      if(coop==null)
      {
        errors.add(String.format("operations: %s is invalid", op));
        continue;
      }

      
      String dbfield=defnField.getDbfield();
      List<String> condition=new ArrayList<String>();

      if("eq".equals(op))
      {
        binds.add(bindValue);
        condition.add(String.format("%s = ?", dbfield));
      }

      if("neq".equals(op))
      {
        binds.add(bindValue);
        condition.add(String.format("%s <> ?", dbfield));
      }

      if("gt".equals(op))
      {
        binds.add(bindValue);
        condition.add(String.format("%s > ?", dbfield));
      }

      if("lt".equals(op))
      {
        binds.add(bindValue);
        condition.add(String.format("%s < ?", dbfield));
      }

      if("gte".equals(op))
      {
        binds.add(bindValue);
        condition.add(String.format("%s >= ?", dbfield));
      }

      if("lte".equals(op))
      {
        binds.add(bindValue);
        condition.add(String.format("%s <= ?", dbfield));
      }

      if("li".equals(op))
      {
        binds.add(bindValue);
        condition.add(String.format("%s like ?", dbfield));
      }

      if("in".equals(op)) condition.add(inListCondition((List<Object>)bindValue, binds, dbfield, "in"));

      if("nin".equals(op)) condition.add(inListCondition((List<Object>)bindValue, binds, dbfield, "not in"));

      if("isnull".equals(op)) condition.add(inListCondition((List<Object>)bindValue, binds, dbfield, "is null"));

      if("isnil".equals(op)) condition.add(String.format("(%s is null or %s = ''", dbfield));

      if("inwnil".equals(op))
      {
        condition.add(String.format("(%s is null or %s = ''", dbfield));
        List<Object> l=(List<Object>)bindValue;
        if(l!=null && l.size()>0) condition.add(String.format(" or %s", inListCondition((List<Object>)bindValue, binds, dbfield, "in")));
        condition.add(")");
      }

      if("inwb".equals(op))
      {
        condition.add(String.format("(%s = ''", dbfield));
        List<Object> l=(List<Object>)bindValue;
        if(l!=null && l.size()>0) condition.add(String.format(" or %s", inListCondition((List<Object>)bindValue, binds, dbfield, "in")));
        condition.add(")");
      }

      if("inwnull".equals(op))
      {
        condition.add(String.format("(%s is null", dbfield));
        List<Object> l=(List<Object>)bindValue;
        if(l!=null && l.size()>0) condition.add(String.format(" or %s", inListCondition((List<Object>)bindValue, binds, dbfield, "in")));
        condition.add(")");
      }

      conditions.add(String.join("", condition));
    }
    
    String text=conditions.size()==0 ? "" : "where " + String.join(" "+conjunction+" ", conditions);
    return new Clause(text, binds, errors.size()==0 ? null :  errors);
  }

  public Clause toWhereClause(Definition defn, Map<String, Object> obj)
  {
    ListOptions options=new ListOptions();
    options.setFilters(objToFilters(obj));
    return toWhereClause(defn, options, "and");
  }

  public Clause toSortClause(Definition defn, ListOptions options)
  {
    List<String> sortClause=new ArrayList<String>();
    List<String> errors=new ArrayList<String>();

    for(SortField sortField:options.getSort())
    {
      String field=sortField.getField();
      Field defnField=defn.field(field);
      if(defnField==null)
      {
        errors.add(String.format("sortfield: %s is invalid", field));
        continue;
      }

      sortClause.add(String.format("%s %s", defnField.getDbfield(), "asc".equals(sortField.getDir()) ? "asc" : "desc"));
    }

    String text=sortClause.size()==0 ? "" : "order by " + String.join(", ", sortClause);
    return new Clause(text, null, errors.size()==0 ? null :  errors);
  }

  public Clause toSetClause(Definition defn, Map<String, Object> obj)
  {
    List<String> setFields=new ArrayList<String>();
    List<Object> binds=new ArrayList<Object>();
    List<String> errors=new ArrayList<String>();

    for(Map.Entry<String, Object> entry:obj.entrySet())
    {
      String objFieldName=entry.getKey();
      Field field=defn.field(objFieldName);
      if(field!=null)
      {
        setFields.add(String.format("%s = ?", field.getDbfield()));
        binds.add(entry.getValue());
      }
      else errors.add(String.format("field: %s is invalid", objFieldName));
    }
    
    String text="";
    if(setFields.size()==0) errors.add("invalid field list");
    else text="set " + String.join(", ", setFields);

    return new Clause(text, binds, errors.size()==0 ? null :  errors);
  }

  public Clause toInsertIntoClause(Definition defn, Map<String, Object> obj)
  {
    List<String> fields=new ArrayList<String>();
    List<Object> binds=new ArrayList<Object>();
    List<String> errors=new ArrayList<String>();
    List<String> placeHolders=new ArrayList<String>();

    for(Map.Entry<String, Object> entry:obj.entrySet())
    {
      String objFieldName=entry.getKey();
      Field field=defn.field(objFieldName);
      if(field!=null)
      {
        fields.add(field.getDbfield());
        binds.add(entry.getValue());
        placeHolders.add("?");
      }
      else errors.add(String.format("field: %s is invalid", objFieldName));
    }

    String text="";
    if(fields.size()==0) errors.add("invalid field list");
    else text=String.format("(%s) values (%s)", String.join(", ", fields), String.join(", ", placeHolders));
    
    return new Clause(text, binds, errors.size()==0 ? null :  errors);
  }

  public Clause toSelectClause(Definition defn, ListOptions options)
  {
    List<String> fields=new ArrayList<String>();
    List<String> errors=new ArrayList<String>();

    List<String> selectFields=new ArrayList<String>(options.getFields());
    if(selectFields.size()==0) for(Map.Entry<String, Object> entry:defn.fields().entrySet()) selectFields.add(entry.getKey());
    
    for(String selectField:selectFields)
    {
      Field field=defn.field(selectField);
      if(field!=null) fields.add(field.getDbfield());
      else errors.add(String.format("field: %s is invalid", selectField));
    }

    String text="";
    if(fields.size()==0) errors.add("invalid field list");
    else text=String.join(", ", fields);

    return new Clause(text, null, errors.size()==0 ? null :  errors);
  }


  public Object toParameterValue(Definition defn, Parameter defnParam, Object obj) throws Exception
  {
    Object value=null;
    
    String paramType=defnParam.getType();

    if("table".equals(paramType)) value=toDBArray(defn, (List<Object>)obj, defnParam.getTableType(), defnParam.getItemType());
    else if("struct".equals(paramType))
    {
      String itemType=defnParam.getItemType();
      Map<String, Field> itemFieldsDefn=defn.typeFields(itemType);
      value=toDBStruct((Map<String, Object>)obj, itemType, itemFieldsDefn);
    }
    else value=obj;

    return value;

  }

  public Array toDBArray(Definition defn, List<Object> list, String tableType, String itemType) throws Exception
  {
    Map<String, Field> itemFieldsDefn=defn.typeFields(itemType);

    ArrayList<Struct> tableList=new ArrayList<Struct>();
    for(Object item:list) tableList.add(toDBStruct((Map<String, Object>)item, itemType, itemFieldsDefn));

    Method createArray=connection.getClass().getMethod("createARRAY", String.class, Object.class);
    createArray.setAccessible(true);
    Array table=(Array)createArray.invoke(connection, new Object[]{tableType, tableList.toArray()});

    return table;
  }

  public Struct toDBStruct(Map<String, Object> item, String itemType, Map<String, Field> itemFieldsDefn) throws Exception
  {
    List<Object> attributes=new ArrayList<Object>();
    for(Object k:itemFieldsDefn.keySet()) attributes.add(item.get((String)k));
    
    Struct struct=connection.createStruct(itemType, attributes.toArray());
    return struct;
  }

  public Outcome<List<SQL>> generateSQLForCreate(Definition defn, IOObjectList list, String name)
  {
    String identityDBField=defn.identityDBField();

    Error error=new InvalidDataError("create sql generation error");
    List<SQL> sqls=new ArrayList<SQL>();

    for(IOObject obj:list)
    {
      Clause insertIntoClause=toInsertIntoClause(defn, obj);
      if(insertIntoClause.hasErrors())
      {
        error.getDetails().addAll(insertIntoClause.getErrors());
        continue;
      }

      String query=String.format("insert into %s %s", defn.table(), insertIntoClause.getText());

      SQL sql=new SQL(query, insertIntoClause.getBinds());
      sql.setGeneratedKeyField(identityDBField);
      sqls.add(sql);
    }

    if(LOG.canLog(LogLevel.DEBUG))
    {
      for(SQL sql:sqls)
      {
        LOG.debug("create sql >");
        LOG.debug("     query : " + sql.getQuery());
        LOG.debug("     binds : " + Json.stringify(sql.getBinds()));
      }
    }

    return new Outcome<List<SQL>>(error.getDetails().size() > 0 ? error : null, sqls);
  }

  public Outcome<IOObjectList> create(Definition defn, IOObjectList list, String name)
  {
    LOG.debug("create >");
    LOG.debug("  name : " + name);
    LOG.debug("   obj : " + Json.stringify(list));

    List<Field> defnFields=defn.fieldsList();
    boolean audit=defn.isAuditEnabled();
    
    for(IOObject obj:list)
    {
      if(audit)
      {
        obj.put(defn.createdByAuditField(), identity.getOid());
        obj.put(defn.createdOnAuditField(), new Date());
      }
    }

    Validator validator=new Validator(defnFields);
    List<String> ignoreFields=Arrays.asList(new String[]{defn.identityField()});
    validator.validateList(list, ignoreFields);
    if(validator.hasErrors()) return new Outcome<IOObjectList>(new InvalidDataError("data has validation errors", validator.errors()));
    
    Outcome<List<SQL>> generateSQLOutcome=generateSQLForCreate(defn, list, name);
    if(generateSQLOutcome.isError()) return new Outcome<IOObjectList>(generateSQLOutcome.getError());

    List<SQL> sqls=generateSQLOutcome.getValue();

    try
    {
      exec(sqls);
    }
    catch(SQLException ex)
    {
      return new Outcome<IOObjectList>(new InternalServerError("error occured while executing sql", ex));
    }
    
    List<Object> oids=new ArrayList<Object>();
    for(SQL sql:sqls) oids.add(sql.getGeneratedKey());

    ListOptions listOptions=new ListOptions().filter(defn.identityField(), "in", oids);
    return list(defn, listOptions, "list created objects: " + defn.name());
  }

  public Outcome<IOObject> create(Definition defn, IOObject obj, String name)
  {
    LOG.debug("create >");
    LOG.debug("  name : " + name);
    LOG.debug("   obj : " + Json.stringify(obj));

    boolean audit=defn.isAuditEnabled();
    if(audit)
    {
      obj.put(defn.createdByAuditField(), identity.getOid());
      obj.put(defn.createdOnAuditField(), new Date());
    }

    List<Field> defnFields=defn.fieldsList();
    Validator validator=new Validator(defnFields);
    validator.validateObject(obj, Arrays.asList(new String[]{defn.identityField()}));
    if(validator.hasErrors()) return new Outcome<IOObject>(new InvalidDataError("data has validation errors", validator.errors()));

    IOObjectList list=new IOObjectList();
    list.add(obj);

    Outcome<List<SQL>> generateSQLOutcome=generateSQLForCreate(defn, list, name);
    if(generateSQLOutcome.isError()) return new Outcome<IOObject>(generateSQLOutcome.getError());

    List<SQL> sqls=generateSQLOutcome.getValue();

    try
    {
      exec(sqls);
    }
    catch(SQLException ex)
    {
      return new Outcome<IOObject>(new InternalServerError("error occured while executing sql", ex));
    }
    
    Object oid=sqls.get(0).getGeneratedKey();
    
    return get(defn, oid, "get created object: "+defn.name());
  }

  public Outcome<List<SQL>> generateSQLForUpdate(Definition defn, IOObjectList list, String name)
  {
    Error error=new InvalidDataError("update sql generation error");
    List<SQL> sqls=new ArrayList<SQL>();
    for(IOObject obj:list)
    {
      Map<String, Object> setObj=obj.$("$set", Map.class);
      Map<String, Object> whereObj=obj.$("$where", Map.class);
      if(setObj==null || whereObj==null) continue;

      Clause setClause=toSetClause(defn, setObj);
      if(setClause.hasErrors())
      {
        error.getDetails().addAll(setClause.getErrors());
        continue;
      }

      Clause whereClause=toWhereClause(defn, whereObj);
      if(whereClause.hasErrors())
      {
        error.getDetails().addAll(whereClause.getErrors());
        continue;
      }

      String query=String.format("update %s %s %s", defn.table(), setClause.getText(), whereClause.getText());

      List<Object> binds=new ArrayList<Object>(setClause.getBinds());
      binds.addAll(whereClause.getBinds());

      sqls.add(new SQL(query, binds));
    }

    if(LOG.canLog(LogLevel.DEBUG))
    {
      for(SQL sql:sqls)
      {
        LOG.debug("update sql >");
        LOG.debug("     query : " + sql.getQuery());
        LOG.debug("     binds : " + Json.stringify(sql.getBinds()));
      }
    }

    return new Outcome<List<SQL>>(error.getDetails().size() > 0 ? error : null, sqls);
  }

  public Outcome<IOObjectList> update(Definition defn, IOObjectList list, String name)
  {
    LOG.debug("update >");
    LOG.debug("  name : " + name);
    LOG.debug("   obj : " + Json.stringify(list));

    String identityFieldName=defn.identityField();
    List<Field> defnFields=defn.fieldsList();
    boolean audit=defn.isAuditEnabled();
    IOObjectList mappedObjList=new IOObjectList();
    List<Map<String, Object>> setObjList=new ArrayList<Map<String, Object>>();

    List<String> unpackerrors=new ArrayList<String>();
    for(IOObject obj:list)
    {
      Map<String, Object> setObj=obj.$("$set", Map.class);
      if(setObj==null)
      {
        if(obj.get(identityFieldName)!=null)
        {
          setObj=new LinkedHashMap<String, Object>();
          Map<String, Object> whereObj=new LinkedHashMap<String, Object>();
          for(Map.Entry<String, Object> entry: obj.entrySet())
          {
            String objFieldName=entry.getKey();
            if(!objFieldName.equals(identityFieldName)) setObj.put(objFieldName, entry.getValue());
            else whereObj.put(objFieldName, entry.getValue());
          }
          IOObject mappedObj=new IOObject();
          mappedObj.put("$set", setObj);
          mappedObj.put("$where", whereObj);
          mappedObjList.add(mappedObj);

          setObjList.add(setObj);
        }
        else unpackerrors.add(String.format("%s is required", identityFieldName));
      }

      if(audit && setObj!=null)
      {
        setObj.put(defn.updatedByAuditField(), identity.getOid());
        setObj.put(defn.updatedOnAuditField(), new Date());
      }
    }

    if(unpackerrors.size()>0) return new Outcome<IOObjectList>(new InvalidDataError("data has validation errors", unpackerrors));
    Validator validator=new Validator(defnFields);
    List<String> ignoreFields=Arrays.asList(new String[]{defn.identityField()});
    validator.validateList(setObjList, ignoreFields);
    if(validator.hasErrors()) return new Outcome<IOObjectList>(new InvalidDataError("data has validation errors", validator.errors()));

    Outcome<List<SQL>> generateSQLOutcome=generateSQLForUpdate(defn, mappedObjList, name);
    if(generateSQLOutcome.isError()) return new Outcome<IOObjectList>(generateSQLOutcome.getError());
    
    List<SQL> sqls=generateSQLOutcome.getValue();

    try
    {
      exec(sqls);
    }
    catch(SQLException ex)
    {
      return new Outcome<IOObjectList>(new InternalServerError("error occured while executing sql", ex));
    }
    
    List<Object> oids=new ArrayList<Object>();
    for(IOObject obj:list)
    {
      if(obj.containsKey(identityFieldName)); oids.add(obj.get(identityFieldName));
    }
    if(oids.size()==0) new Outcome<IOObjectList>(null, null);

    ListOptions listOptions=new ListOptions().filter(identityFieldName, "in", oids);
    return list(defn, listOptions, "list created objects: " +defn.name());
  }

  public Outcome<IOObject> update(Definition defn, IOObject obj, String name)
  {
    LOG.debug("update >");
    LOG.debug("  name : " + name);
    LOG.debug("   obj : " + Json.stringify(obj));

    String identityFieldName=defn.identityField();

    boolean audit=defn.isAuditEnabled();
    IOObjectList mappedObjList=new IOObjectList();

    Map<String, Object> setObj=obj.$("$set", Map.class);
    if(setObj==null)
    {
      setObj=new LinkedHashMap<String, Object>();
      Map<String, Object> whereObj=new LinkedHashMap<String, Object>();
      for(Map.Entry<String, Object> entry: obj.entrySet())
      {
        String objFieldName=entry.getKey();
        if(!objFieldName.equals(identityFieldName)) setObj.put(objFieldName, entry.getValue());
        else whereObj.put(objFieldName, entry.getValue());
      }
      IOObject mappedObj=new IOObject();
      mappedObj.put("$set", setObj);
      mappedObj.put("$where", whereObj);
      mappedObjList.add(mappedObj);
    }

    if(audit)
    {
      obj.put(defn.updatedByAuditField(), identity.getOid());
      obj.put(defn.updatedOnAuditField(), new Date());
    }

    List<Field> defnFields=defn.fieldsList();
    Validator validator=new Validator(defnFields);
    validator.validateObject(obj, Arrays.asList(new String[]{defn.identityField()}));
    if(validator.hasErrors()) return new Outcome<IOObject>(new InvalidDataError("data has validation errors", validator.errors()));
    
    Outcome<List<SQL>> generateSQLOutcome=generateSQLForUpdate(defn, mappedObjList, name);
    if(generateSQLOutcome.isError()) return new Outcome<IOObject>(generateSQLOutcome.getError());
    
    List<SQL> sqls=generateSQLOutcome.getValue();

    try
    {
      exec(sqls);
    }
    catch(SQLException ex)
    {
      return new Outcome<IOObject>(new InternalServerError("error occured while executing sql", ex));
    }
    
    if(!obj.containsKey(identityFieldName)) return new Outcome<IOObject>(null, null);

    return get(defn, obj.get(identityFieldName), "get updated object: "+defn.name());
  }

  public Outcome<List<SQL>> generateSQLForDelete(Definition defn, IOObjectList list, String name)
  {
    Error error=new InvalidDataError("delete sql generation error");
    List<SQL> sqls=new ArrayList<SQL>();
    for(IOObject obj:list)
    {

      Clause whereClause=toWhereClause(defn, obj);
      if(whereClause.hasErrors())
      {
        error.getDetails().addAll(whereClause.getErrors());
        continue;
      }

      String query=String.format("delete from %s %s", defn.table(), whereClause.getText());
      sqls.add(new SQL(query, whereClause.getBinds()));
    }

    if(LOG.canLog(LogLevel.DEBUG))
    {
      for(SQL sql:sqls)
      {
        LOG.debug("delete sql >");
        LOG.debug("     query : " + sql.getQuery());
        LOG.debug("     binds : " + Json.stringify(sql.getBinds()));
      }
    }

    return new Outcome<List<SQL>>(error.getDetails().size() > 0 ? error : null, sqls);
  }

  public Outcome<List<Object>> delete(Definition defn, IOObjectList list, String name)
  {
    LOG.debug("delete >");
    LOG.debug("  name : " + name);
    LOG.debug("   obj : " + Json.stringify(list));

    //Todo: add validation logic
    
    Outcome<List<SQL>> generateSQLOutcome=generateSQLForDelete(defn, list, name);
    if(generateSQLOutcome.isError()) return new Outcome<List<Object>>(generateSQLOutcome.getError());
    
    List<SQL> sqls=generateSQLOutcome.getValue();

    try
    {
      exec(sqls);
    }
    catch(SQLException ex)
    {
      return new Outcome<List<Object>>(new InternalServerError("error occured while executing sql", ex));
    }
    
    return new Outcome<List<Object>>(null, null);
  }

  public Outcome<Object> delete(Definition defn, IOObject obj, String name)
  {
    LOG.debug("delete >");
    LOG.debug("  name : " + name);
    LOG.debug("   obj : " + Json.stringify(obj));

    //Todo: add validation logic
    
    IOObjectList list=new IOObjectList();
    list.add(obj);

    Outcome<List<SQL>> generateSQLOutcome=generateSQLForDelete(defn, list, name);
    if(generateSQLOutcome.isError()) return new Outcome<Object>(generateSQLOutcome.getError());
    
    List<SQL> sqls=generateSQLOutcome.getValue();

    try
    {
      exec(sqls);
    }
    catch(SQLException ex)
    {
      return new Outcome<Object>(new InternalServerError("error occured while executing sql", ex));
    }
    
    return new Outcome<Object>(null, null);
  }

  public Outcome<SQL> generateSQLForList(Definition defn, ListOptions options, String name)
  {
    Error error=new InvalidDataError("select sql generation error");

    Clause selectClause=toSelectClause(defn, options);
    if(selectClause.hasErrors())
    {
      error.getDetails().addAll(selectClause.getErrors());
      return new Outcome<SQL>(error);
    }

    Clause whereClause=toWhereClause(defn, options, "and");
    if(whereClause.hasErrors())
    {
      error.getDetails().addAll(whereClause.getErrors());
      return new Outcome<SQL>(error);
    }

    Clause sortClause=toSortClause(defn, options);
    if(sortClause.hasErrors())
    {
      error.getDetails().addAll(sortClause.getErrors());
      return new Outcome<SQL>(error);
    }

    String fromObject=defn.view();
    if(defn.query()!=null) fromObject="(" + defn.query() + ") a";

    String query=String.format("select %s from %s %s %s", selectClause.getText(), fromObject, whereClause.getText(), sortClause.getText());
    List<Object> binds=whereClause.getBinds();

    int page=options.getPage();
    int pageSize=options.getPageSize();
    if(pageSize==0) pageSize=25;

    if(page>0)
    {
      if("mysql".equals(dbType)) query += String.format(" limit %s offset %s", pageSize+1, (page-1) * pageSize);
      if("oracle".equals(dbType))
      {
        int l=(page-1) * pageSize;
        int r=l+pageSize+1;
        query = String.format("select a.* from (select b.*, rownum as rnum from (%s) b) a where a.rnum >= %s and a.rnum<= %s", query, l, r);
      }
    }

    SQL sql=new SQL(query, binds);
    sql.setPageSize(pageSize);

    LOG.debug("list sql >");
    LOG.debug("   query : " + sql.getQuery());
    LOG.debug("   binds : " + Json.stringify(sql.getBinds()));

    return new Outcome<SQL>(error.getDetails().size() > 0 ? error : null, sql);
  }

  public Outcome<IOObjectList> list(Definition defn, ListOptions options, String name)
  {
    LOG.debug("list   >");
    LOG.debug("  name : " + name);
    LOG.debug("   obj : " + Json.stringify(options));

    
    Outcome<SQL> generateSQLOutcome=generateSQLForList(defn, options, name);
    if(generateSQLOutcome.isError()) return new Outcome<IOObjectList>(generateSQLOutcome.getError());
    

    Map<String, Object> shape=defn.shape();
    if(shape==null) shape=autoShapeFromDefnFields(defn.fieldsList());

    Map<String, Field> dbFieldNameMap=dbfieldNameMap(defn);

    SQL sql=generateSQLOutcome.getValue();

    try
    {
      this.sql(sql, shape, dbFieldNameMap);
    }
    catch(SQLException ex)
    {
      return new Outcome<IOObjectList>(new InternalServerError("error occured while executing sql", ex));
    }
    IOObjectList list=sql.getResults();
    list.setPageSize(sql.getPageSize());
    list.setNext(list.size()>sql.getPageSize());
    if(list.isNext()) list.remove(list.size()-1);

    if(list.size()==0) new Outcome<IOObjectList>(null, list);
    for(ChildData child:defn.children())
    {
      LOG.debug("getting child data: "+child.getDefn());
      Definition childDefn=defnStore.getDefinition(child.getDefn(), dbType);
      String linkParentField=child.getLinkParentField();
      String linkChildField=child.getLinkChildField();
      String rel=child.getRel();
      String childFieldName=child.getName();

      Map<Object, Object> map=new HashMap<Object, Object>();
      for(IOObject obj:list) map.put(obj.get(linkParentField), obj);
      List<Object> parentIds=new ArrayList<Object>(map.keySet());

      ListOptions childListOptions=child.getDataListOptions();
      if(childListOptions==null) childListOptions=new ListOptions();
      childListOptions.filter(child.getLinkChildField(), "in", parentIds);
      
      Outcome<IOObjectList> childOutcome=list(childDefn, childListOptions, "list child "+child.getDefn()+" for "+defn.name());
      if(childOutcome.isError()) return new Outcome<IOObjectList>(childOutcome.getError());

      for(IOObject childItem:childOutcome.getValue())
      {
        IOObject parentItem=(IOObject)map.get(childItem.get(linkChildField));
        if("m".equals(rel))
        {
          if(parentItem.get(childFieldName)==null) parentItem.put(childFieldName, new ArrayList<Object>());
          ArrayList<Object> children=(ArrayList<Object>)parentItem.get(childFieldName);
          children.add(childItem);
        }
        else if("1".equals(rel))
        {
          parentItem.put(childFieldName, childItem);
        }
      }
    }
    return new Outcome<IOObjectList>(null, list);
  }

  
  public Outcome<IOObject> get(Definition defn, Object oid, String name)
  {
    LOG.debug("get    >");
    LOG.debug("  name : " + name);
    LOG.debug("   oid : " + oid);

    ListOptions options=new ListOptions().filter(defn.identityField(), "eq", oid);
    
    Outcome<SQL> generateSQLOutcome=generateSQLForList(defn, options, name);
    if(generateSQLOutcome.isError()) return new Outcome<IOObject>(generateSQLOutcome.getError());
    

    Map<String, Object> shape=defn.shape();
    if(shape==null) shape=autoShapeFromDefnFields(defn.fieldsList());

    Map<String, Field> dbFieldNameMap=dbfieldNameMap(defn);

    SQL sql=generateSQLOutcome.getValue();

    try
    {
      this.sql(sql, shape, dbFieldNameMap);
    }
    catch(SQLException ex)
    {
      return new Outcome<IOObject>(new InternalServerError("error occured while executing sql", ex));
    }
    
    if(sql.getResults().size()==0) return new Outcome<IOObject>(null, null);
    return new Outcome<IOObject>(null, sql.getResults().get(0));
  }

  public List<String> validateParameterValue(Definition defn, Parameter defnParam, Object obj) throws Exception
  {
    String paramType=defnParam.getType();

    if("table".equals(paramType))
    {
      String itemType=defnParam.getItemType();
      List<Field> typeFields=defn.typeFieldsList(itemType);
      Validator validator=new Validator(typeFields);

      List<Object> list=(List<Object>)obj;
      for(Object o:list) validator.validateObject((Map<String, Object>)o);
      return validator.errors();
    }
    else if("struct".equals(paramType))
    {
      String itemType=defnParam.getItemType();
      List<Field> typeFields=defn.typeFieldsList(itemType);
      Validator validator=new Validator(typeFields);
      validator.validateObject((Map<String, Object>)obj);
      return validator.errors();
    }
    else
    {
      List<String> errors=new ArrayList<String>(0);
      List<String> validations=defnParam.getValidation();
      if(validations!=null)
      {
        for(String validation:validations) Validator.doValidation(validation, defnParam.getName(), obj, errors);
      }
      return errors;
    }
  }

  public Outcome<CallSQL> generateSQLForCall(Definition defn, IOObject obj, String name)
  {
    Error error=new InvalidDataError("call sql generation error");

    List<Object> binds=new ArrayList<Object>();
    List<String> placeHolders=new ArrayList<String>();

    try
    {
      List<String> validationErrors=new ArrayList<String>();
      for(Parameter parameter:defn.parameters())
      {
        Object v=obj.get(parameter.getFieldName());
        validationErrors.addAll(validateParameterValue(defn, parameter, v));
      }
      if(validationErrors.size()>0) return new Outcome<CallSQL>(new InvalidDataError("data has validation errors", validationErrors));
      
      for(Parameter parameter:defn.parameters())
      {
        Object v=obj.get(parameter.getFieldName());
        binds.add(toParameterValue(defn, parameter, v));
        placeHolders.add("?");
      }
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      return new Outcome<CallSQL>(new InternalServerError("error occured while executing sql", ex));
    }

    String query=String.format("{call %s(%s)}", defn.procedure(), String.join(", ", placeHolders));
    CallSQL sql=new CallSQL(defn, query, binds);

    LOG.debug("call sql   >");
    LOG.debug("     query : " + sql.getQuery());
    //LOG.debug("     binds :" + Json.stringify(sql.getBinds()));

    return new Outcome<CallSQL>(error.getDetails().size() > 0 ? error : null, sql);
  }

  public Outcome<IOObject> call(Definition defn, IOObject obj, String name)
  {
    LOG.debug("call   >");
    LOG.debug("  name : " + name);
    LOG.debug("   obj : " + Json.stringify(obj));

    
    Outcome<CallSQL> generateSQLOutcome=generateSQLForCall(defn, obj, name);
    if(generateSQLOutcome.isError()) return new Outcome<IOObject>(generateSQLOutcome.getError());
    

    CallSQL sql=generateSQLOutcome.getValue();

    try
    {
      this.callsql(sql);
    }
    catch(SQLException ex)
    {
      return new Outcome<IOObject>(new InternalServerError("error occured while executing sql", ex));
    }
    
    return new Outcome<IOObject>(null, sql.getResult());
  }

}
