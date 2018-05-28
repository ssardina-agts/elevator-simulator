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
  private List selectedValues;


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
        p.setValue(selectedValues.get(0));
        params.add(p);
      return params;
    }
    for (Object value: selectedValues)
    {
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

  public void setChoice(List selectedValues)
  {
    this.selectedValues = selectedValues;
  }

  public Object getSingleValue()
  {
    return selectedValues.get(0);
  }
}
