package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Misc 
{
  public static void main(String[] args) throws Exception
  {
    Pattern pattern=Pattern.compile("DECIMAL.*|DOUBLE|INT\\((\\d+)\\)|int\\((\\d+)\\) UNSIGNED");
    Matcher matcher=pattern.matcher("DECIMAL");
    System.out.println(matcher.matches());
  }
}
