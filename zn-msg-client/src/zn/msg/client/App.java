package zn.msg.client;

import java.util.Base64;
import java.util.Map;
import java.util.Base64.Encoder;

import zn.Configuration;
import zn.ExecutorManager;
import zn.Logger;
import zn.Http;

import zn.msg.Client;
import zn.msg.client.calls.IncProductFn;
import zn.msg.client.calls.OpFn;
import zn.msg.client.model.Identity;
import zn.msg.client.model.TokenResponse;

public class App 
{
  private Logger LOG=Logger.get(App.class);

  private static Client client;

  public App()
  {

  }

  public static void init() throws Exception
  {
    String home=System.getProperty("zn.msg.client.home");
    String appConfigFile=home + "/config.json";

    System.out.println("<zn-msg-client> loading config from file "+appConfigFile);
    Configuration.createInstanceFromJsonFile(appConfigFile);
    Configuration.getInstance().put("home", home);
    
    System.out.println("<zn-msg-client> Initializing logger");
    Configuration config=Configuration.getInstance();
    Map<String, Object> loggerConfig=config.$("logger", Map.class);
    Logger.config(loggerConfig);

    Logger LOG=Logger.get(App.class);
    LOG.info("Home="+home);

    ExecutorManager eman=new ExecutorManager("executor");
    
    client=new Client();
    client.setExecutorManager(eman);
    
    String location=config.$("msg-client.location");
    String ctx=config.$("msg-client.ctx");
    
    String tokenUrl=config.$("openid.tokenUrl");
    String clientId=config.$("openid.client-id");
    String clientSecret=config.$("openid.client-secret");
    String grantType=config.$("openid.grant-type");
    String scope=config.$("openid.scope");

    Encoder b64enc = Base64.getEncoder();
    String authorization=new String(b64enc.encode(String.format("%s:%s", clientId, clientSecret).getBytes()));
    
    Http http=new Http();
    http.post(tokenUrl, String.format("grant_type=%s&scope=%s", grantType, scope))
        .withHeader("content-type", "application/x-www-form-urlencoded")
        .withHeader("authorization", "Basic " + authorization);

    LOG.info("getting token ...");
    TokenResponse tokenResponse=http.$(TokenResponse.class);
    LOG.info("token=", tokenResponse.getAccessToken());

    Identity identity=new Identity();
    identity.setToken(tokenResponse.getAccessToken());
    identity.setAppid("zn-msg-client");
    
    client.addCallImpl("jclient::sum", OpFn.Sum.class.getName());
    client.addCallImpl("jclient::diff", OpFn.Diff.class.getName());
    client.addCallImpl("jclient::product", OpFn.Product.class.getName());
    client.addCallImpl("jclient::division", OpFn.Division.class.getName());
    client.addCallImpl("jclient::incBy20Product", IncProductFn.class.getName());

    client.connect(location, ctx, identity);
  }

  public static void start()
  {

  }

  public static void main(String[] args) throws Exception
  {
    // System.setProperty("zn.msg.client.home", "/u03/workspaces/zn/deploy");
    App.init();
    App.start();
  }
}
