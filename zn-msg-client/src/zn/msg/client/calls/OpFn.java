package zn.msg.client.calls;

import zn.msg.Client;
import zn.msg.client.CallableFn;
import zn.msg.client.Callback;
import zn.msg.client.model.CallResult;

public class OpFn
{
  public static class Sum extends CallableFn
  {
    public Sum(){super("sum");}
    public void _do(Callback<CallResult> $$)
    {
      OpRequest payload=msg().payload(OpRequest.class);
      OpResult result=new OpResult();
      result.setValue(payload.getA() + payload.getB());
      $$.$(result);
    }
  };

  public static class Diff extends CallableFn
  {
    public Diff(){super("diff");}
    public void _do(Callback<CallResult> $$)
    {
      OpRequest payload=msg().payload(OpRequest.class);
      OpResult result=new OpResult();
      
      result.setValue(payload.getA() - payload.getB());
      $$.$(result);
    }
  };

  public static class Product extends CallableFn
  {
    public Product(){super("product");}
    public void _do(Callback<CallResult> $$)
    {
      OpRequest payload=msg().payload(OpRequest.class);
      OpResult result=new OpResult();
      result.setValue(payload.getA() * payload.getB());
      $$.$(result);
    }
  };

  public static class Division extends CallableFn
  {
    public Division(){super("division");}
    public void _do(Callback<CallResult> $$)
    {
      OpRequest payload=msg().payload(OpRequest.class);

      OpResult result=new OpResult();
      result.setValue(payload.getA() / payload.getB());
      $$.$(result);
    }
  };

}
