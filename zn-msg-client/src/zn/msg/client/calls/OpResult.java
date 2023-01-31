package zn.msg.client.calls;

import zn.msg.client.model.CallResult;

public class OpResult extends CallResult
{
  public OpResult(){}

  private Number value;

  public Number getValue() {
    return value;
  }

  public void setValue(Number value) {
    this.value = value;
  }
  
}
