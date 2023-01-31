package dbio.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import dbio.core.IOObject;
import zn.Json;

@SuppressWarnings("unchecked")
public class Definition extends IOObject
{
  public Definition()
  {

  }

  public String table()
  {
    return $("table", String.class);
  }

  public String view()
  {
    return $("view", String.class);
  }

  public String query()
  {
    return $("query", String.class);
  }

  public String procedure()
  {
    return $("procedure", String.class);
  }

  public List<Field> fieldsList()
  {
    IOObject fields=fields();
    List<Field> list=new ArrayList<Field>();
    for(String fieldName:fields.keySet()) list.add(field(fieldName));
    return list;
  }
  
  public IOObject fields()
  {
    return IOObject.fromMap($("fields", Map.class));
  }

  public Field field(String fieldName)
  {
    Map<String, Object> fields=$("fields", Map.class);
    Field field=Field.fromMap((Map<String, Object>)fields.get(fieldName));
    if(field!=null) field.setName(fieldName);

    return field;
  }

  public String name()
  {
    return $("name", String.class);
  }

  public String identityField()
  {
    return $("identityField", String.class);
  }

  public String identityDBField()
  {
    return field(identityField()).getDbfield();
  }

  public boolean isAuditEnabled()
  {
    return $("audit", Boolean.class) == true;
  }

  public IOObject auditFields()
  {
    return IOObject.fromMap($("auditFields", Map.class));
  }

  public String createdByAuditField()
  {
    return isAuditEnabled() ? auditFields().$("createdBy", String.class, "createdBy") : null;
  }

  public String createdOnAuditField()
  {
    return isAuditEnabled() ? auditFields().$("createdOn", String.class, "createdOn") : null;
  }

  public String updatedByAuditField()
  {
    return isAuditEnabled() ? auditFields().$("updatedBy", String.class, "updatedBy") : null;
  }

  public String updatedOnAuditField()
  {
    return isAuditEnabled() ? auditFields().$("updatedOn", String.class, "updatedOn") : null;
  }

  public Map<String, Object> shape()
  {
    return $("shape");
  }

  public List<Parameter> parameters()
  {
    List<Parameter> list=new ArrayList<Parameter>();
    List<Object> defnParameters=$("parameters", List.class);
    for(Object defnParameter:defnParameters) list.add(Parameter.fromMap((Map<String, Object>)defnParameter));
    return list;
  }

  public Parameter parameter(int i)
  {
    List<Object> defnParameters=$("parameters", List.class);
    return Parameter.fromMap((Map<String, Object>)defnParameters.get(i));
  }

  public IOObject types()
  {
    return IOObject.fromMap($("types", Map.class));
  }

  public Map<String, Field> typeFields(String typeName)
  {
    Map<String, Object> types=$("types", Map.class);
    Map<String, Object> type=(Map<String, Object>)types.get(typeName);
    Map<String, Object> defnFields=(Map<String, Object>)type.get("fields");
    Map<String, Field> fields=new LinkedHashMap<String, Field>();
    
    for(Map.Entry<String, Object> entry:defnFields.entrySet())
    {
      String fieldName=entry.getKey();
      Field field=Field.fromMap((Map<String, Object>)entry.getValue());
      if(field!=null) field.setName(fieldName);
      fields.put(fieldName, field);
    }

    return fields;
  }

  public List<Field> typeFieldsList(String typeName)
  {
    Map<String, Object> types=$("types");
    Map<String, Object> type=(Map<String, Object>)types.get(typeName);
    Map<String, Object> defnFields=(Map<String, Object>)type.get("fields");
    List<Field> fields=new ArrayList<Field>();
    
    for(Map.Entry<String, Object> entry:defnFields.entrySet())
    {
      String fieldName=entry.getKey();
      Field field=Field.fromMap((Map<String, Object>)entry.getValue());
      if(field!=null) field.setName(fieldName);
      fields.add(field);
    }

    return fields;
  }

  public Map<String, Object> typeShape(String typeName)
  {
    Map<String, Object> types=$("types");
    Map<String, Object> type=(Map<String, Object>)types.get(typeName);
    return (Map<String, Object>)type.get("shape");
  }

  public Map<String, Object> resultSets()
  {
    return $("result-sets");
  }

  public List<ChildData> children()
  {
    List<Object> children=(List<Object>)get("children");
    if(children==null) return new ArrayList<ChildData>();

    return Json.parseJson(Json.stringify(children), new TypeToken<List<ChildData>>(){}.getType());
  }
}
