package zn.rs.model.config;

public class OpenId 
{
  private String type;
  private String clientId, clientSecret, tokenUrl;
  private String signingCert, signingAlgo;
  private String issuer, redirectUrl;

  public OpenId(){}

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientID) {
    this.clientId = clientID;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getTokenUrl() {
    return tokenUrl;
  }

  public void setTokenUrl(String tokenUrl) {
    this.tokenUrl = tokenUrl;
  }

  public String getSigningCert() {
    return signingCert;
  }

  public void setSigningCert(String signingCert) {
    this.signingCert = signingCert;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getSigningAlgo() {
    return signingAlgo;
  }

  public void setSigningAlgo(String signingAlgo) {
    this.signingAlgo = signingAlgo;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  
}
