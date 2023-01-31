package zn.dio.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.gson.Gson;

import zn.dio.model.Value;
import zn.Logger;
import zn.dio.model.Field;
import zn.dio.model.ValueType;
import zn.dio.pipeline.action.BaseAction;

public class Excel2JSON extends BaseAction
{
  private static final Logger LOG=Logger.get(Excel2JSON.class);
  
  private Workbook wb;
  
  private static SimpleDateFormat ISO_DATE_TIME_FMT=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
  
  public Excel2JSON()
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
  
  public void convert(Map<String,String> options) throws IOException, ParseException
  {
    String excelfilename=null;
    if(options.get("infile")!=null) excelfilename=options.get("infile");
    else if(options.get("file")!=null) excelfilename=options.get("file");
    
    String sheetName=null;
    if(options.get("sheet")!=null) sheetName=options.get("sheet");

    File file=new File(excelfilename);
    String fileName=file.getName();

    String folder=file.getParentFile().getAbsolutePath();

    FileInputStream excelStream=new FileInputStream(file);
    wb=WorkbookFactory.create(excelStream);

    if(sheetName!=null)
    {
      String jsonFileName=folder + "/" + toName(fileName.substring(0, fileName.lastIndexOf("."))) + "-" + toName(sheetName) + ".json";
      if(options.get("output")!=null) jsonFileName=options.get("output");

      LOG.info("converting sheet ["+sheetName +"] from excel file "+excelfilename+" => "+jsonFileName);
      Sheet sheet=wb.getSheet(sheetName);
      convertSheet(sheet, excelfilename, jsonFileName);
    }
    else
    {
      String outputFolder=folder;
      if(options.get("output")!=null) outputFolder=options.get("output");

      convertWorkbook(wb, excelfilename, outputFolder);
    }

    wb.close();
    excelStream.close();
    
  }

  public void convertWorkbook(Workbook wb, String excelFileName, String folder) throws IOException, ParseException
  {
    LOG.info("converting excel file "+excelFileName +" to json");
    File file=new File(excelFileName);
    String fileName=file.getName();

    String prefix=toName(fileName.substring(0, fileName.lastIndexOf(".")));
    for(int i=0, l=wb.getNumberOfSheets();i<l;i++)
    {
      String sheetName=wb.getSheetName(i);
      String jsonFileName=folder + "/" + prefix + "-" + toName(sheetName) + ".json";
      LOG.info("converting sheet ["+sheetName +"] => "+jsonFileName);
      Sheet sheet=wb.getSheetAt(i);
      convertSheet(sheet, excelFileName, jsonFileName);
    }
  }

  public void convertSheet(Sheet sheet, String excelFileName, String jsonfilename) throws IOException, ParseException
  {
    FileWriter fwriter=new FileWriter(jsonfilename);
    PrintWriter writer=new PrintWriter(fwriter);

    Iterator<Row> rowIter=sheet.iterator();
    List<String> headers=row2ListString(rowIter.next());
    List<String> jsonFields=toJsonFields(headers);
    
    LOG.info("sheet headers : "+headers);
    LOG.info("  json fields : "+jsonFields);

    int numColumns=headers.size();

    Map<String, Object> headerMap=new LinkedHashMap<String, Object>();
    headerMap.put("@//header", "dio-converter");
    headerMap.put("@excel-file", excelFileName);
    headerMap.put("@sheet", sheet.getSheetName());
    headerMap.put("@created-on", ISO_DATE_TIME_FMT.format(new Date()));
    Map<String, Field> fieldsMap=new LinkedHashMap<String, Field>();

    Gson gson=new Gson();
    int row=0;
    while(rowIter.hasNext())
    {
      row++;
      //LOG.debug("adding row "+row);

      List<Value> rowValues=row2ListValues(rowIter.next(), numColumns);
      Map<String, Object> map=new LinkedHashMap<String, Object>();
      for(int i=0, l=numColumns;i<l;i++)
      {
        String jsonField=jsonFields.get(i);
        Value value=rowValues.get(i);
        int jsonFieldType=value.getType();

        Object jsonFieldValue=value.getObjValue();
        if(jsonFieldType==ValueType.TYPE_DATE) jsonFieldValue=ISO_DATE_TIME_FMT.format(value.getObjValue());
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

  public List<String> row2ListString(Row row)
  {
    List<String> list=new ArrayList<String>();
    Iterator<Cell> celliterator=row.cellIterator();
    while(celliterator.hasNext())
    {
      Cell cell=celliterator.next();
      list.add(cell.toString());
    }
    return list;
  }

  public List<Value> row2ListValues(Row row, int length)
  {
    List<Value> list=new ArrayList<Value>();
    for(int i=0;i<length;i++)
    {
      Cell cell=row.getCell(i);
      if(cell==null)
      {
        list.add(Value.NULL);
        continue;
      }

      CellType cellType=cell.getCellType();
      
      if(cellType==CellType.BLANK) list.add(Value.NULL);
      else if(cellType==CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
      {
        Value value=new Value(ValueType.TYPE_DATE, cell.getDateCellValue());
        value.setFormat(cell.getCellStyle().getDataFormatString());
        list.add(value);
      }
      else if(cellType==CellType.NUMERIC)
      {
        Value value=new Value(ValueType.TYPE_NUMERIC, cell.getNumericCellValue());
        value.setFormat(cell.getCellStyle().getDataFormatString());
        list.add(value);
      }
      else if(cellType==CellType.BOOLEAN)
      {
        Value value=new Value(ValueType.TYPE_BOOLEAN, cell.getBooleanCellValue());
        value.setFormat(cell.getCellStyle().getDataFormatString());
        list.add(value);
      }
      else
      {
        Value value=new Value(ValueType.TYPE_STRING, cell.getStringCellValue());
        value.setFormat(cell.getCellStyle().getDataFormatString());
        list.add(value);
      }
    }
    while(list.size() < length) list.add(Value.NULL);
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

  public static void main(String[] args) throws Exception
  {
    Map<String, String> options=new HashMap<String, String>();
    options.put("file", "C:\\root\\workspaces\\workdesk\\misc\\demand\\dd-loader.xlsx");
    //options.put("output", "C:\\root\\workspaces\\workdesk\\misc\\demand\\test\\demand.json");
    options.put("sheet", "demand");

    Excel2JSON converter=new Excel2JSON();
    converter.convert(options);
  }

}
