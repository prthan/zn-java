package zn.dio.actions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.google.gson.Gson;

import zn.Configuration;
import zn.Logger;
import zn.Utils;
import zn.dio.pipeline.action.BaseAction;

public class JSON2CSVFile extends BaseAction
{
  private static final Logger LOG=Logger.get(JSON2CSVFile.class);
 
  public JSON2CSVFile()
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
    String outputFileName="output.csv";
    Map<String, Integer> fieldMappings=null;
    
    if(options.get("map")!=null) fieldMappings=mappings(options.get("map"));
    if(options.get("infile")!=null) jsonFileName=options.get("infile");  
    if(options.get("outfile")!=null) outputFileName=options.get("outfile");  

    LOG.debug("output file: "+outputFileName);
    generateFile(outputFileName, jsonFileName, fieldMappings);
  }  

  public void generateFile(String outputFileName, String jsonFileName, Map<String, Integer> fieldMappings) throws Exception
  {
    FileReader fin=new FileReader(jsonFileName);
    BufferedReader bin=new BufferedReader(fin);
    CSVPrinter csvwriter=CSVFormat.EXCEL.print(new File(outputFileName), Charset.forName("UTF-8"));

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
        csvwriter.printRecord(mapValuesToColumns(fieldPositionsMap, headerValues));
        continue;
      }
      if(jsonData.get("@//footer")!=null) continue;
      
      List<String> dataRowValues=mapValuesToColumns(fieldPositionsMap, jsonData);
      csvwriter.printRecord(dataRowValues);

      lineCount++;
    }

    bin.close();
    fin.close();
    csvwriter.flush();
    csvwriter.close();

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

  public List<String> mapValuesToColumns(Map<String, Integer> fieldMappings, Map<?,?> fieldValues)
  {
    List<String> rval=new ArrayList<String>();
    
    String[] values=new String[fieldMappings.size()];
    for(Map.Entry<String, Integer> entry:fieldMappings.entrySet())
    {
      String field=entry.getKey();
      Integer position=entry.getValue();
      values[position-1]=""+fieldValues.get(field);
    }
    rval=Arrays.asList(values);
    return rval;
  }

}
