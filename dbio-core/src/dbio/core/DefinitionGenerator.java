package dbio.core;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dbio.core.model.ProcParameter;
import dbio.core.model.TableColumn;
import zn.Json;

@SuppressWarnings("unchecked")
public class DefinitionGenerator 
{
  private DBIO dbio;

  private static final TextFragment textFragments=TextFragment.fromRes("/text.tf");

  public DefinitionGenerator(DBIO dbio)
  {
    this.dbio=dbio;
  }

  private String type(String type)
  {
    if(type==null) return null;

    if("VARCHAR|VARCHAR2|LONGTEXT|MEDIUMTEXT".indexOf(type)!=-1) return "string";
    if("DECIMAL|DOUBLE|INT|INT UNSIGNED|NUMBER".indexOf(type)!=-1) return "numeric";
    if("DATE".indexOf(type)!=-1) return "date";
    if("DATETIME|TIME|TIMESTAMP(6)".indexOf(type)!=-1) return "datetime";
    if("TABLE".indexOf(type)!=-1) return "table";
    if("OBJECT".indexOf(type)!=-1) return "struct";

    return null;
  }

  private List<String> validation(TableColumn column)
  {
    List<String> validations=new ArrayList<String>();
    if("NO".equals(column.getNil())) validations.add("required");
    
    String dbtype=column.getType();
    if("VARCHAR|VARCHAR2".indexOf(dbtype)!=-1)
    {
      validations.add("string");
      validations.add("max:"+column.getSize());
    }

    if("LONGTEXT|MEDIUMTEXT".indexOf(dbtype)!=-1)
    {
      validations.add("string");
    }

    if("DOUBLE|INT".indexOf(dbtype)!=-1)
    {
      validations.add("numeric");
    }    

    if("DECIMAL".indexOf(dbtype)!=-1)
    {
      validations.add("numeric");
      if(column.getSize()>0 && column.getDecimalDigits()>0) validations.add(String.format("regex:/^[-+]?\\d{0,%s}}(\\.\\d{1,%s})?$/", column.getSize()-column.getDecimalDigits(), column.getDecimalDigits()));
    }    

    if("INT UNSIGNED".equals(dbtype))
    {
      validations.add("integer");
      validations.add(String.format("regex:/^\\d{0,%s}}$", column.getSize()));
    }    

    if("INT".equals(dbtype))
    {
      validations.add("integer");
      validations.add(String.format("regex:/^[-+]?\\d{0,%s}}$", column.getSize()));
    }    

    if("NUMBER".equals(dbtype))
    {
      validations.add("numeric");
    }    

    if("DATETIME|DATE|TIME|TIMESTAMP(6)".indexOf(dbtype)!=-1)
    {
      validations.add("date");
    }

    return validations;
  }

  private List<TableColumn> getTableMetadata(String schema, String tableName) throws SQLException
  {
    DatabaseMetaData dbmd=dbio.getConnection().getMetaData();
    ResultSet columnsRs=dbmd.getColumns(null, schema, tableName, null);
    List<TableColumn> list=new ArrayList<TableColumn>();
    while(columnsRs.next())
    {
      TableColumn col=new TableColumn();
      col.setName(columnsRs.getString("COLUMN_NAME"));
      col.setSize(columnsRs.getInt("COLUMN_SIZE"));
      col.setNil(columnsRs.getString("IS_NULLABLE"));
      col.setType(columnsRs.getString("TYPE_NAME"));
      col.setKey(columnsRs.getString("IS_AUTOINCREMENT"));
      col.setDecimalDigits(columnsRs.getInt("DECIMAL_DIGITS"));
      list.add(col);

      System.out.println(col.getName()+": "+col.getType());
    }
    

    return list;
  }

  public List<ProcParameter> getProcMetadata(String schema, String procName) throws SQLException
  {
    List<ProcParameter> list=new ArrayList<ProcParameter>();
    
    String query=textFragments.get("GET_PROC_PARAM_" + dbio.getDbType().toUpperCase());
    List<Object> binds=new ArrayList<Object>(Arrays.asList(new String[]{procName}));
    SQL sql=new SQL(query, binds);
    dbio.sql(sql);
    IOObjectList results=sql.getResults();

    for(IOObject obj:results)
    {
      ProcParameter param=new ProcParameter();
      param.setName(obj.$("paramName", String.class));
      param.setType(obj.$("paramType", String.class));
      String mode=obj.$("paramMode", String.class).toLowerCase();
      if("in/out".equals(mode)) mode="inout";
      param.setMode(mode);
      param.setTypeName(obj.$("typeName", String.class));
      param.setItemTypeName(obj.$("itemTypeName", String.class));

      list.add(param);
    }

    return list;
  }

  public List<TableColumn> getTypeMetadata(String schema, String typeName) throws SQLException
  {
    List<TableColumn> list=new ArrayList<TableColumn>();
    
    
    String query=textFragments.get("GET_TYPE_ATTRS_ORACLE");
    SQL sql=new SQL(query, new ArrayList<Object>(Arrays.asList(new String[]{typeName})));
    dbio.sql(sql);
    IOObjectList results=sql.getResults();

    System.out.println(results);
    for(IOObject obj:results)
    {
      TableColumn col=new TableColumn();
      col.setName(obj.$("attrName", String.class));
      Object length=obj.get("length");
      if(length!=null) col.setSize(Integer.parseInt(length.toString()));
      col.setType(obj.$("attrTypeName", String.class));
      list.add(col);
    }

    return list;
  }

  public void generateForTable(String schema, String tableName, String viewName, String outputFileName) throws SQLException, IOException
  {
    Map<String, TableColumn> map=new LinkedHashMap<String, TableColumn>();

    for(TableColumn t:getTableMetadata(schema, tableName)) map.put(t.getName(), t);
    if(viewName!=null)
    {
      for(TableColumn t:getTableMetadata(schema, viewName))
      {
        String name=t.getName();
        if(!map.containsKey(name)) map.put(name, t);
      }
    }
    
    String identityField=null;
    Map<String, Object> fields=new LinkedHashMap<String, Object>();
    Map<String, String> shape=new LinkedHashMap<String, String>();

    for(TableColumn column:map.values())
    {
      String name=Utils.toCamelcase(column.getName());
      String dbfieldName=column.getName();

      if("YES".equals(column.getKey())) identityField=name;
      Map<String, Object> fieldDefn=new LinkedHashMap<String, Object>();
      fieldDefn.put("dbfield", dbfieldName);
      fieldDefn.put("type", type(column.getType()));
      fieldDefn.put("validation", validation(column));
      fieldDefn.put("label", name); // Fix This: to convert dbfield to proper label
      fields.put(name, fieldDefn);
      shape.put(name, String.format("#{row.%s}", dbfieldName));
    }

    String defnTemplate=textFragments.get("DEFN_TEMPLATE");

    Map<String, Object> definition=(Map<String, Object>)Json.parseJson(defnTemplate, Map.class);
    definition.put("name", tableName);
    definition.put("table", tableName);
    definition.put("view", viewName==null ? tableName : viewName);
    definition.put("identityField", identityField);
    definition.put("keyFields", Arrays.asList(new String[]{identityField}));
    definition.put("fields", fields);
    definition.put("shape", shape);
    definition.remove("query");
    definition.remove("procedure");

    Json.toFile(definition, outputFileName);
  }

  public void generateForProc(String schema, String procName, String outputFileName) throws SQLException, IOException
  {
    Map<String, ProcParameter> map=new LinkedHashMap<String, ProcParameter>();

    for(ProcParameter p:getProcMetadata(schema, procName)) map.put(p.getName(), p);
    Set<String> typeNames=new HashSet<String>();
    List<Object> parameters=new ArrayList<Object>();
    for(ProcParameter param:map.values())
    {
      String fieldName=Utils.toCamelcase(param.getName());
      String name=param.getName();

      Map<String, Object> paramDefn=new LinkedHashMap<String, Object>();
      paramDefn.put("name", name);
      paramDefn.put("fieldName", fieldName);
      paramDefn.put("mode", param.getMode());
      String type=type(param.getType());
      paramDefn.put("type", type);
      
      String typeName=param.getTypeName();
      if(typeName!=null)
      {
        if("table".equals(type)) paramDefn.put("tableType", typeName);
        else paramDefn.put("itemType", typeName);
      }
      String itemType=param.getItemTypeName();
      if(itemType!=null)
      {
        paramDefn.put("itemType", itemType);
        typeNames.add(itemType);
      }

      parameters.add(paramDefn);
    }

    Map<String, Object> types=new LinkedHashMap<String, Object>();
    if(typeNames.size()!=0 && "oracle".equals(dbio.getDbType()))
    {
      for(String typeName: typeNames)
      {
        Map<String, Object> typeDefn=new HashMap<String, Object>();
        Map<String, Object> fields=new HashMap<String, Object>();
        Map<String, Object> shape=new HashMap<String, Object>();
        typeDefn.put("fields", fields);
        typeDefn.put("shape", shape);
        List<TableColumn> list=getTypeMetadata(schema, typeName);
        Map<String, Object> fieldDefn=new HashMap<String, Object>();
        for(TableColumn column:list)
        {
          String name=Utils.toCamelcase(column.getName());
          String dbfieldName=column.getName();
          fieldDefn.put("dbfield", column.getName());
          fieldDefn.put("type", type(column.getType()));
          fieldDefn.put("validation", validation(column));
          fields.put(name, fieldDefn);
          shape.put(name, String.format("#{row.%s}", dbfieldName));
        }

        types.put(typeName, typeDefn);
      }
    }

    String defnTemplate=textFragments.get("DEFN_TEMPLATE");

    Map<String, Object> definition=(Map<String, Object>)Json.parseJson(defnTemplate, Map.class);
    definition.put("name", Utils.toCamelcase(procName));
    definition.put("procedure", procName);
    definition.put("parameters", parameters);
    definition.put("types", types);
    definition.remove("table");
    definition.remove("view");
    definition.remove("query");

    Json.toFile(definition, outputFileName);
  }

}
