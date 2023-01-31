package zn.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import zn.Logger;

public class ConsoleLogger extends Logger
{
  private static final String[] LEVEL_COLORS=new String[]{"\u001b[31m", "\u001b[33m", "\u001b[36m", "\u001b[32m", "\u001b[32m"};
  // private static final String RESET_COLOR="\u001b[0m";
  private static final String[] COLORED_LEVEL_TAGS=new String[]{"\u001b[31m[Error]\u001b[0m", "\u001b[33m[ Warn]\u001b[0m", "\u001b[36m[ Info]\u001b[0m", "\u001b[32m[Debug]\u001b[0m", "\u001b[32m[Trace]\u001b[0m"};
  private static final String[] PLAIN_LEVEL_TAGS=new String[]{"[Error]", "[ Warn]", "[ Info]", "[Debug]", "[Trace]"};

  private LogLevel logLevel;
  private List<LinePart> lineParts;
  private SimpleDateFormat dttmFormat;
  private boolean pretty=false;

  public ConsoleLogger(Map<String, Object> properties)
  {
    if(properties.containsKey("log-level")) logLevel=(LogLevel)properties.get("log-level");
    else logLevel=LogLevel.ERROR;
    
    if(properties.containsKey("pretty")) pretty=(Boolean)properties.get("pretty");

    if(properties.containsKey("dttm-format")) dttmFormat=new SimpleDateFormat((String)properties.get("dttm-format"));
    else dttmFormat=new SimpleDateFormat("MM/dd/YYYY HH:mm:ss");

    if(pretty && properties.containsKey("pretty-line-format")) lineParts=parseLineFormat((String)properties.get("pretty-line-format"));
    else if(properties.containsKey("line-format")) lineParts=parseLineFormat((String)properties.get("line-format"));
    else lineParts=parseLineFormat("${ts} ${level} ${msg}");

  }
  
  @Override public void error(Object... params) {log(LogLevel.ERROR, params);}
  @Override public void info(Object... params) {log(LogLevel.INFO, params);}
  @Override public void warn(Object... params) {log(LogLevel.WARN, params);}
  @Override public void debug(Object... params) {log(LogLevel.DEBUG, params);}
  @Override public void trace(Object... params) {log(LogLevel.TRACE, params);}

  private class LinePart
  {
    public int type;
    public String token;

    public LinePart(int type, String token)
    {
      this.type=type;
      this.token=token;
    }

    public String toString()
    {
      return type+":"+token;
    }
  }

  private List<LinePart> parseLineFormat(String format)
  {
    List<LinePart> parts=new ArrayList<LinePart>();
    Pattern pattern=Pattern.compile("\\$\\{(.*?)\\}");
    Matcher matcher=pattern.matcher(format);
    int start=0;
    while(matcher.find())
    {
      String text=format.substring(start, matcher.start());
      String variable=matcher.group(1);
      start=matcher.end();

      parts.add(new LinePart(0, text));
      parts.add(new LinePart(1, variable));
    }

    return parts;
  }
  
  private String generateLine(List<LinePart> parts, Map<String, Object> attributes, String color)
  {
    StringBuilder sb=new StringBuilder();
    for(LinePart part: parts)
    {
      if(part.type==0) sb.append(part.token);
      if(part.type==1)
      {
        Object attrVal=attributes.get(part.token);
        if(attrVal!=null) sb.append(attrVal);
        //else sb.append("${").append(part.token).append("}");
      }
    }
    sb.append("\n");
    //if(pretty) line = RESET_COLOR + LEVEL_COLORS[level.value] + line + RESET_COLOR;
    return sb.toString();
  }

  private String ex2str(Throwable t)
  {
    StringWriter sw=new StringWriter();
    PrintWriter pw=new PrintWriter(sw);
    t.printStackTrace(pw);

    pw.close();

    return sw.toString();
  }

  private String params2MsgStr(Object ... params)
  {
    StringBuilder sb=new StringBuilder();
    for(Object param: params)
    {
      if(param instanceof Throwable) sb.append(" ").append(ex2str((Throwable)param));
      else sb.append(" ").append(param);
    }
    StringBuilder msgstr=new StringBuilder();
    String[] parts=sb.substring(1).split("\n");
    msgstr.append(parts[0]).append(parts.length>1?" [#]":"");
    for(int i=1, l=parts.length;i<l;i++) msgstr.append("\n[#] ").append(parts[i]);
    return msgstr.toString();
  }

  @Override
  public void log(LogLevel level, Object... params) 
  {
    if(this.logLevel.value<level.value) return;
    
    Map<String, Object> attributes=new HashMap<String, Object>();
    attributes.putAll(threadLocalattributes.get());
    attributes.put("ts", dttmFormat.format(new Date()));
    attributes.put("level", pretty ? COLORED_LEVEL_TAGS[level.value] : PLAIN_LEVEL_TAGS[level.value]);
    // attributes.put("level", PLAIN_LEVEL_TAGS[level.value]);
    attributes.put("threadName", Thread.currentThread().getName());
    attributes.put("msg", params2MsgStr(params));

    String line=generateLine(lineParts, attributes, pretty ? LEVEL_COLORS[level.value] : null);

    System.out.print(line);
  }

  @Override
  public boolean canLog(LogLevel level) 
  {
    return this.logLevel.value>=level.value;
  }
  
  public static void main(String[] args)
  {
    Map<String, Object> properties=new HashMap<String, Object>();
    properties.put("level", LogLevel.TRACE);
    properties.put("pretty", true);
    properties.put("line-format", "<DBIO> ${level} [${ts}] [${threadName}] ${msg}");
    properties.put("dttm-format", "YYYY-MM-dd HH:mm:ss");
    ConsoleLogger sl=new ConsoleLogger(properties);
    sl.error("this is a error");
    sl.warn("this is a warning");
    sl.info("this is a info");
    sl.debug("this is a debug");
    sl.trace("this is a trace");
  }
}
