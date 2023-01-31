package dbio.core;

import dbio.core.model.Parameter;

public class ParameterBindValue 
{
  private Parameter parameter;
  private Object value;

  public ParameterBindValue(Parameter parameter, Object value)
  {
    this.value=value;
    this.parameter=parameter;
  }

  public Object getValue() {return value;}
  public void setValue(Object value) {this.value = value;}
  public Parameter getParameter() {return parameter;}
  public void setParameter(Parameter parameter) {this.parameter = parameter;}
}
