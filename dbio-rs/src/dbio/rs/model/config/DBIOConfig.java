package dbio.rs.model.config;

public class DBIOConfig 
{
  private String type, dataSource;
  private String home;

  public DBIOConfig()
  {

  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getDataSource() {
    return dataSource;
  }
  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }  

  public String getHome() {
    return home;
  }
  public void setHome(String home) {
    this.home = home;
  }
}
