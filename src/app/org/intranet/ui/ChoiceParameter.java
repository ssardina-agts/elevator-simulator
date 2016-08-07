/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public final class ChoiceParameter<T>
    extends SingleValueParameter<T>
{
  private T value;
  private List<T> legalValues;

  public ChoiceParameter(String desc, List<T> legalValues,
    T defaultValue)
  {
    super(desc);
    value = defaultValue;
    this.legalValues = legalValues;
  }

  public List<T> getLegalValues()
  {
    return legalValues;
  }
  
  public void setValue(T param)
  {
    for (T legalValue : legalValues)
    {
      if (legalValue.equals(param))
      {
        value = legalValue;
        return;
      }
    }
    throw new IllegalArgumentException("Parameter is not a legal value.");
  }

  public T getValue()
  {
    return value;
  }
  
  public Object getChoiceValue()
  {
    return value;
  }

  public List<T> getValues(T min, T max, T inc)
  {
    throw new UnsupportedOperationException();
  }
  
  public ChoiceParameter<T> clone()
  {
    ChoiceParameter<T> cp = new ChoiceParameter<T>(getDescription(), legalValues,
        value);
    return cp;
  }

  public T getValueFromString(String value)
  {
    for (T legalValue : legalValues)
      if (legalValue.toString().equals(value))
        return legalValue;
    return null;
  }

  @Override
  public void setValueFromString(String param)
  {
    for (T legalValue : legalValues)
    {
      if (legalValue.toString().equals(param))
      {
        value = legalValue;
        return;
      }
    }
    throw new IllegalArgumentException("Parameter is not a legal value.");
  }
}
