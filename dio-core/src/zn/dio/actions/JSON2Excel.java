package zn.dio.actions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;

import zn.Configuration;
import zn.Logger;
import zn.Utils;
import zn.dio.pipeline.action.BaseAction;

public class JSON2Excel extends BaseAction
{
  private static final Logger LOG=Logger.get(JSON2Excel.class);
 
  public JSON2Excel()
  {

  }

  public Error execute(Map<String, String> options)
  {
    Error rc=null;
    LOG.info("executing pipeline action");
    try
    {
      generate(options);
    }
    catch(Exception ex)
    {
      LOG.error("Error occured while executing the pipeline action");
      LOG.error(ex);
      rc=new Error(ex.getMessage());
    }

    return rc;
  }

  public void generate(Map<String,String> options) throws Exception
  {
    String jsonFileName="data.json";
    String outputFileName="output.xlsx";
    String templateFileName=null;
    int templateDataRowNum=1;
    Map<String, Integer> fieldMappings=null;
    
    if(options.get("map")!=null) fieldMappings=mappings(options.get("map"));
    if(options.get("infile")!=null) jsonFileName=options.get("infile");  
    if(options.get("outfile")!=null) outputFileName=options.get("outfile");  
    if(options.get("template")!=null) templateFileName=options.get("template");  
    if(options.get("templaterow")!=null) templateDataRowNum=Integer.parseInt(options.get("templaterow"));  

    LOG.debug("output file: "+outputFileName);
    Workbook wb=null;
    Sheet sheet=null, templateSheet=null;
    Row templateDataRow=null;

    if(templateFileName!=null)
    {
      wb=createWorkbookFromTemplate(templateFileName, outputFileName);
      templateSheet=wb.getSheetAt(0);
      templateDataRow=templateSheet.getRow(templateDataRowNum);
    }
    else wb=createWorkbook(outputFileName);

    sheet=templateSheet!=null?wb.cloneSheet(0):wb.createSheet();
    
    populateSheet(sheet, jsonFileName, fieldMappings, templateDataRow);
    writeWorkbookToFile(wb, outputFileName, sheet, templateSheet, templateDataRowNum);
  }  

  public void populateSheet(Sheet sheet, String jsonFileName, Map<String, Integer> fieldMappings, Row templateDataRow) throws Exception
  {
    FileReader fin=new FileReader(jsonFileName);
    BufferedReader bin=new BufferedReader(fin);

    String str;
    Gson gson=new Gson();
    Map<?,?> fields=null;

    int lineCount=1;
    Map<String, Integer> fieldPositionsMap=fieldMappings;

    while((str=bin.readLine())!=null)
    {
      Map<?,?> jsonData=gson.fromJson(str, Map.class);
      if(jsonData.get("@//header")!=null) 
      {
        fields=(Map)jsonData.get("@fields");
        Map<String, String> headerValues=new HashMap<String, String>();
        Map<String, Integer> fieldMappingsFromHeader=new HashMap<String, Integer>();
        int i=1;
        for(Map.Entry<?, ?> entry: fields.entrySet())
        {
          String field=(String)entry.getKey();
          headerValues.put(field, field);
          fieldMappingsFromHeader.put(field, i++);
        }
        if(fieldPositionsMap==null) fieldPositionsMap=fieldMappingsFromHeader;
        if(templateDataRow==null) writeRow(sheet, mapValuesToColumns(fieldPositionsMap, headerValues), 0, null);
        continue;
      }
      if(jsonData.get("@//footer")!=null) continue;
      
      List<Object> dataRowValues=mapValuesToColumns(fieldPositionsMap, jsonData);
      writeRow(sheet, dataRowValues, lineCount, templateDataRow);

      lineCount++;
    }

    bin.close();
    fin.close();
  }

  public Map<String, Integer> mappings(String options)
  {
    Map<String, Integer> rval=new HashMap<String, Integer>();
    if(options==null) return rval;

    for(String mapping:options.split(","))
    {
      String[] attrs=mapping.split("=>");
      rval.put(attrs[0].trim(), Integer.parseInt(attrs[1].trim()));
    }

    return rval;
  }

  public List<Object> mapValuesToColumns(Map<String, Integer> fieldMappings, Map<?,?> fieldValues)
  {
    List<Object> rval=new ArrayList<Object>();
    
    Object[] values=new Object[fieldMappings.size()];
    for(Map.Entry<String, Integer> entry:fieldMappings.entrySet())
    {
      String field=entry.getKey();
      Integer position=entry.getValue();
      values[position-1]=fieldValues.get(field);
    }
    rval=Arrays.asList(values);
    return rval;
  }

  private Workbook createWorkbookFromTemplate(String templateFile, String outputFile) throws IOException
  {
    Configuration config=Configuration.getInstance();
    FileInputStream instream=new FileInputStream(config.$("home")+"/templates/"+templateFile);
    FileOutputStream outstream=new FileOutputStream(outputFile);
    byte[] buffer=new byte[4*1024];
    int x=-1;
    while((x=instream.read(buffer))!=-1) outstream.write(buffer, 0, x);
    instream.close();
    outstream.close();

    instream=new FileInputStream(outputFile);
    Workbook wb=WorkbookFactory.create(instream);
    instream.close();

    return wb;
  }

  private Workbook createWorkbook(String outputFile) throws IOException
  {
    Workbook wb=null;
    if(outputFile.toUpperCase().endsWith(".XLSX")) wb=new XSSFWorkbook();
    else wb=new HSSFWorkbook();

    return wb;
  }

  public void writeRow(Sheet sheet, List<Object> values, int rowNum, Row templateDataRow) throws IOException
  {
    Row headerRow=null;
    headerRow=sheet.createRow(rowNum);
    for(int i=0,l=values.size();i<l;i++)
    {
      Cell cell=headerRow.createCell(i);
      Object value=values.get(i);
      if(value instanceof String) cell.setCellValue((String)value);
      else if(value instanceof Number) cell.setCellValue(((Number)value).doubleValue());
      else if(value instanceof Boolean) cell.setCellValue((Boolean)value);
      else cell.setCellValue((String)value);
      applyCellStyle(templateDataRow, i, cell);
    }
    applyRowHeight(templateDataRow, headerRow);
  }  

  public void writeWorkbookToFile(Workbook wb, String filename, Sheet sheet, Sheet templateSheet, int templateRowNum) throws IOException
  {
    String name="Data";
    
    if(templateSheet!=null)
    {
      int numCols=templateSheet.getRow(templateRowNum).getLastCellNum();
      for(int i=0;i<numCols;i++) applyColumnWidth(sheet, templateSheet, i);
      name=wb.getSheetName(0);
      wb.removeSheetAt(0);
    }
    wb.setSheetName(0, name);

    FileOutputStream stream=new FileOutputStream(filename);
    wb.write(stream);
    wb.close();
    stream.close();
  }  

  private void applyRowHeight(Row templateDataRow, Row row)
  {
    if(templateDataRow!=null) row.setHeight(templateDataRow.getHeight());
  }

  private void applyCellStyle(Row templateDataRow, int colNum, Cell cell)
  {
    if(templateDataRow!=null) cell.setCellStyle(templateDataRow.getCell(colNum).getCellStyle());
  }
  
  private void applyColumnWidth(Sheet sheet, Sheet templateSheet, int colNum)
  {
    if(templateSheet!=null) sheet.setColumnWidth(colNum, templateSheet.getColumnWidth(colNum));
  }  
}
