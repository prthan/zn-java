package zn.msg.client;

import zn.msg.Client;

public class Context 
{
  private Client client;

  public Context(Client client)
  {
    this.client=client;
  }

  public Client client() {return client;};
}
