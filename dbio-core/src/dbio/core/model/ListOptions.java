package dbio.core.model;

import java.util.ArrayList;
import java.util.List;

import dbio.core.IOObject;
import zn.Json;

public class ListOptions
{
  private List<Filter> filters;
  private List<SortField> sort;
  private int page, pageSize;
  private List<String> fields;

  public ListOptions()
  {
    filters=new ArrayList<Filter>();
    sort=new ArrayList<SortField>();
    fields=new ArrayList<String>();
  }

  public List<Filter> getFilters() {return filters;}
  public void setFilters(List<Filter> filters) {this.filters = filters;}

  public List<SortField> getSort() {return sort;}
  public void setSort(List<SortField> sort) {this.sort = sort;}

  public int getPage() {return page;}
  public void setPage(int page) {this.page = page;}

  public int getPageSize() {return pageSize;}
  public void setPageSize(int pageSize) {this.pageSize = pageSize;}
  
  public List<String> getFields() {return fields;}
  public void setFields(List<String> fields) {this.fields = fields;}

  public ListOptions filter(String field, String op, Object value) {this.filters.add(new Filter(field, op, value)); return this;};
  public ListOptions sort(String field, String dir) {this.sort.add(new SortField(field, dir)); return this;}
  public ListOptions page(int page) {this.page=page; return this;}
  public ListOptions pageSize(int pageSize) {this.pageSize=pageSize; return this;}

  public static ListOptions from(IOObject obj)
  {
    return Json.parseJson(Json.stringify(obj), ListOptions.class);
  }
}
