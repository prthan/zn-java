package zn.msg.client.calls;

import zn.Json;
import zn.Logger;
import zn.msg.client.CallableFn;
import zn.msg.client.Callback;
import zn.msg.client.model.CallResult;
import zn.msg.client.model.CallReturnMSG;

public class IncProductFn extends CallableFn
{
  private Logger LOG=Logger.get(IncProductFn.class);

  private Callback<CallResult> $$;
  private OpRequest payload;
  private OpResult incAResult;
  private OpResult incBResult;
  
  public IncProductFn(){super("IncProductFn");}

  public void _do(Callback<CallResult> $$)
  {
    payload=msg().payload(OpRequest.class);
    this.$$=$$;
    
    OpRequest incARequest=new OpRequest();
    incARequest.setA(payload.getA());
    incARequest.setB(20);
    
    LOG.debug("A=", payload.getA());
    LOG.debug("B=", payload.getB());
    LOG.debug("calling client-1::sum to increment A by 20");
    context().client().call("client-1::sum", incARequest, new IncACallback());
  }

  public class IncACallback implements Callback<CallReturnMSG>
  {
    public void $(CallReturnMSG returnMSG)
    {
      incAResult=returnMSG.result(OpResult.class);
      LOG.debug("increment A by 20=", incAResult.getValue());
      OpRequest incBRequest=new OpRequest();
      incBRequest.setA(payload.getB());
      incBRequest.setB(20);
  
      LOG.debug("calling client-1::sum to increment B by 20");
      context().client().call("client-1::sum", incBRequest, new IncBCallback());
    }
  }

  public class IncBCallback implements Callback<CallReturnMSG>
  {
    public void $(CallReturnMSG returnMSG)
    {
      incBResult=returnMSG.result(OpResult.class);;
      LOG.debug("increment B by 20=", incBResult.getValue());

      int incrementedA=incAResult.getValue().intValue();
      int incrementedB=incBResult.getValue().intValue();

      OpResult finalResult=new OpResult();
      finalResult.setValue(incrementedA * incrementedB);

      LOG.debug("returing product");
      $$.$(finalResult);
    }
  }

}
