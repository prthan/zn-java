package zn.msg.client;

import org.json.JSONObject;

import zn.msg.Client;

public interface EventHandler 
{
  public void onConnect(Client client, String id);
  public void onDisconnect(Client client);
  public void onMessage(Client client, JSONObject jo);
}
