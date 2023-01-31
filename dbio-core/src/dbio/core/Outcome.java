package dbio.core;

public class Outcome<T>
{
  private zn.rs.model.Error error;
  private T value;
  
  public Outcome(zn.rs.model.Error error, T value)
  {
    this.error=error;
    this.value=value;
  }

  public Outcome(zn.rs.model.Error error)
  {
    this.error=error;
  }

  public Outcome(T value)
  {
    this.value=value;
  }

  public boolean isError() {return error!=null;};
  public zn.rs.model.Error getError() {return error;}
  public void setError(zn.rs.model.Error error) {this.error = error;}
  public T getValue() {return value;}
  public void setValue(T value) {this.value = value;}

}
