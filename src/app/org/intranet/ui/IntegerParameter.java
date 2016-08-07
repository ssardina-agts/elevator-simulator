/*
 * Copyright 2003, 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 * 
 *
 */
public class IntegerParameter
  extends SingleValueParameter<Integer>
{
  int value;
  
  public IntegerParameter(String desc, int defaultValue)
  {
    super(desc);
    value = defaultValue;
  }
  
  public int getIntegerValue()
  {
    return value;
  }
  
  void setIntegerValue(int newValue)
  {
    value = newValue;
  }

  public void setValue(Integer param)
  {
    setIntegerValue(param);
  }

  public Integer getValue()
  {
    return value;
  }

  public List<Integer> getValues(Integer min, Integer max, Integer inc)
  {
    if (min > max)
    {
      int temp = min;
      min = max;
      max = temp;
    }

    int capacity = (max - min) / inc + 1; 
    List<Integer> intValues = new ArrayList<Integer>(capacity);

    // populate the array list with strings representing the values
    for (int val = min; val <= max; val += inc)
      intValues.add(val);

    return intValues;
  }

  @Override
  public Integer getValueFromString(String value)
  {
    return Integer.valueOf(value);
  }

  @Override
  public void setValueFromString(String param)
  {
    value = Integer.valueOf(param);
  }
}
