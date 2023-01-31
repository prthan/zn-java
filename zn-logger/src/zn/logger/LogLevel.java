package zn.logger;

public enum LogLevel 
{
  ERROR(0), WARN(1), INFO(2), DEBUG(3), TRACE(4);

  public final int value;

  private LogLevel(int value) 
  {
    this.value = value;
  }
}

