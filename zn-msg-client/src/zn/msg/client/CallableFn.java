package zn.msg.client;

import zn.Logger;
import zn.msg.client.model.CallError;
import zn.msg.client.model.CallMSG;
import zn.msg.client.model.CallResult;

abstract public class CallableFn implements Runnable
{
  private String name;
  private Context context;
  private CallMSG callMsg;
  
  public CallableFn()
  {

  }

  public CallableFn(String name)
  {
    this.name=name;
  }

  public String name() {return name;};
  
  public final void setContext(Context context) {this.context=context;}
  public final Context context() {return context;};

  public final void setMSG(CallMSG callMsg) {this.callMsg=callMsg;}
  public final CallMSG msg() {return callMsg;};

  public final void run()
  {
    Logger LOG=Logger.get(getClass());
    LOG.info("executing call "+name);
    try
    {
      _do((CallResult result) -> context.client().returnCall(callMsg, result));
    }
    catch(Exception ex)
    {
      LOG.error(ex);
      context.client().returnCall(callMsg, CallResult.error("CALL-EXECUTION-FAILURE", ex.getMessage()));
    }
    
  }

  abstract public void _do(Callback<CallResult> cb);
}
