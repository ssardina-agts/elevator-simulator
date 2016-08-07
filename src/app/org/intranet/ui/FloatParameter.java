/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class FloatParameter
  extends SingleValueParameter<Float>
{
  private float value;

  public FloatParameter(String desc, float defaultValue)
  {
    super(desc);
    value = defaultValue;
  }

  public void setValue(Float param)
  {
    value = param;
  }

  public Float getValue()
  {
    return value;
  }

  public float getFloatValue()
  {
    return value;
  }

  void setFloatValue(float newValue)
  {
    value = newValue;
  }

  public List<Float> getValues(Float min, Float max, Float inc)
  {
    if (min > max)
    {
      float temp = min;
      min = max;
      max = temp;
    }

    int capacity = (int)((max - min) / inc + 1); 
    List<Float> floatValues = new ArrayList<Float>(capacity);

    // populate the array list with strings representing the values
    for (float val = min; val <= max; val += inc)
      floatValues.add(val);

    return floatValues;
  }

  @Override
  public Float getValueFromString(String value)
  {
    return Float.valueOf(value);
  }

  @Override
  public void setValueFromString(String param)
  {
    value = Float.valueOf(param);
  }
}
