package zn.rs.model.config;

import java.util.List;

public class Auth 
{
  private boolean enabled;
  private List<String> ignoreUrls;
  
  public Auth()
  {

  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public List<String> getIgnoreUrls() {
    return ignoreUrls;
  }

  public void setIgnoreUrls(List<String> ignoreUrls) {
    this.ignoreUrls = ignoreUrls;
  }

  
}
