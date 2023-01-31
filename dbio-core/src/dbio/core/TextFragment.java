package dbio.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFragment 
{

  private static Map<String, String> parse(String content)
  {
    if(content==null) return null;

    Map<String, String> map=new HashMap<String, String>();

    Pattern pattern=Pattern.compile("@(.+?)(\n)*\\{\\{(\\s|\n)*(.+?)(\\s|\n)*\\}\\}", Pattern.MULTILINE | Pattern.DOTALL);

    Matcher matcher=pattern.matcher(content);
    while(matcher.find()) map.put(matcher.group(1), matcher.group(4));
    return map;
  }


  public static TextFragment fromFile(String filename)
  {
    TextFragment tf=new TextFragment();
    tf.map=TextFragment.parse(Utils.fileToStr(filename));

    return tf;
  }

  public static TextFragment fromRes(String resname)
  {
    TextFragment tf=new TextFragment();
    tf.map=TextFragment.parse(Utils.resToStr(resname));

    return tf;
  }

  private Map<String, String> map;

  private TextFragment()
  {
    
  }

  public String get(String name)
  {
    return map.get(name);
  }

  public Set<String> names()
  {
    return map.keySet();
  }

  public static void main(String[] args)
  {
    TextFragment tf=TextFragment.fromRes("/text.tf");
    System.out.println(tf.get("DEFN_TEMPLATE"));
  }
}
