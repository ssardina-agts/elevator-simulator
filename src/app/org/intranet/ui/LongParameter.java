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
 */
public class LongParameter
  extends SingleValueParameter<Long>
{
  long value;
  
  public LongParameter(String desc, long defaultValue)
  {
    super(desc);
    value = defaultValue;
  }
  
  public long getLongValue()
  {
    return value;
  }
  
  void setLongValue(long newValue)
  {
    value = newValue;
  }

  public void setValue(Long param)
  {
    setLongValue(param);
  }

  public Long getValue()
  {
    return value;
  }

  public List<Long> getValues(Long min, Long max, Long inc)
  {
    if (min > max)
    {
      long temp = min;
      min = max;
      max = temp;
    }

    int capacity = (int)((max - min) / inc + 1); 
    List<Long> longValues = new ArrayList<Long>(capacity);

    // populate the array list with strings representing the values
    for (long val = min; val <= max; val += inc)
      longValues.add(val);

    return longValues;
  }

  @Override
  public Long getValueFromString(String value)
  {
    return Long.valueOf(value);
  }

  @Override
  public void setValueFromString(String param)
  {
    value = Long.valueOf(param);
  }
}
