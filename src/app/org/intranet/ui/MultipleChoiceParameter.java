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
public final class MultipleChoiceParameter
    extends MultipleValueParameter
{
  private ChoiceParameter choiceParam;
  private Object[] selectedValues;

  public MultipleChoiceParameter(ChoiceParameter param)
  {
    super(param.getDescription());
    choiceParam = param;
  }

  public List<SingleValueParameter> getParameterList()
  {
    List<SingleValueParameter> params = new ArrayList<SingleValueParameter>();
    if (!isMultiple)
    {
      ChoiceParameter p = choiceParam.clone();
      p.setValue(selectedValues[0]);
      params.add(p);
      return params;
    }
    for (int i = 0; i < selectedValues.length; i++)
    {
      Object value = selectedValues[i];
      ChoiceParameter p = choiceParam.clone();
      p.setValue(value);
      params.add(p);
    }
    return params;
  }

  public List getLegalValues()
  {
    return choiceParam.getLegalValues();
  }

  public void setChoice(Object[] selectedValues)
  {
    this.selectedValues = selectedValues;
  }

  public Object getSingleValue()
  {
    return selectedValues[0];
  }
}
