/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public final class RangeParameter<T>
  extends MultipleValueParameter
{
  private T baseValue;
  private T maxValue;
  private T incrementValue;
  private SingleValueParameter<T> param;

  public RangeParameter(SingleValueParameter<T> p)
  {
    super(p.getDescription());
    param = p;
    setBaseValue(p.getValue());
    setMaxValue(p.getValue());
    setIncrementValueFromString("1");
  }

  public void setBaseValue(T base)
  {
    baseValue = base;
  }
  public void setBaseValueFromString(String baseString)
  {
    setBaseValue(param.getValueFromString(baseString));
  }
  public T getBaseValue() { return baseValue; }
  
  public void setMaxValue(T max)
  {
    maxValue = max;
  }
  public void setMaxValueFromString(String maxString)
  {
    setMaxValue(param.getValueFromString(maxString));
  }
  public T getMaxValue() { return maxValue; }
  
  public void setIncrementValue(T incr)
  {
    incrementValue = incr;
  }
  public void setIncrementValueFromString(String incrString)
  {
    setIncrementValue(param.getValueFromString(incrString));
  }
  public T getIncrementValue() { return incrementValue; }

  public List<SingleValueParameter<T>> getParameterList()
  {
    List<SingleValueParameter<T>> params = new ArrayList<SingleValueParameter<T>>();
    if (!isMultiple)
    {
      param.setValue(baseValue);
      params.add(param);
      return params;
    }
    for (T value : getValues(baseValue, maxValue, incrementValue))
    {
      SingleValueParameter<T> p = param.clone();
      p.setValue(value);
      params.add(p);
    }
    return params;
  }

  public List<T> getValues(T min, T max, T inc)
  {
    return param.getValues(min, max, inc);
  }

  public Object getSingleValue()
  {
    return getBaseValue();
  }
}
