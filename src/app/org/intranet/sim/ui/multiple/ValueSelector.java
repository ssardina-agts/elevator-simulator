/*
 * Copyright 2004-2006 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.SingleValueParameter;


class ValueSelector
  extends JComponent
{
  private MultipleValueParameter param;
  private JLabel label = new JLabel("selector");
  private JSpinner spinner;
  private SingleValueParameter selected;
  private Listener listener;
  interface Listener
  {
    void valueChanged();
  }

  public ValueSelector(MultipleValueParameter rangeParam,
      final List<SingleValueParameter> paramList, Listener l)
  {
    super();
    setLayout(new FlowLayout());
    param = rangeParam;
    listener = l;
    selected = paramList.get(0);
    if (param.isMultiple())
    {
      spinner = new JSpinner(new AbstractSpinnerModel()
      {
        public Object getNextValue()
        {
          int currentIndex = paramList.indexOf(selected);
          if (currentIndex == -1) currentIndex = 0;
          else if (currentIndex >= paramList.size() - 1) return null;
          else currentIndex++;
          return paramList.get(currentIndex).getValue();
        }
    
        public Object getPreviousValue()
        {
          int currentIndex = paramList.indexOf(selected);
          if (currentIndex == -1) currentIndex = 0;
          else if (currentIndex == 0) return null;
          else currentIndex--;
          return paramList.get(currentIndex).getValue();
        }
    
        public Object getValue()
        {
          return selected.getValue();
        }
    
        public void setValue(Object value)
        {
          // The method setValue() is sometimes called with a String value.
          // This will fail to match any of our parameters, but
          // setValue() will be called again with a value of the correct type.
          for (SingleValueParameter param : paramList)
          {
            if (param.getValue().equals(value))
            {
              selected = param;
              fireStateChanged();
              listener.valueChanged();
            }
          }
        }
      });
      add(spinner);
    }
    add(label);
    setTertiary(); // by default
  }
  
  public void setPrimary()
  {
    if (!param.isMultiple())
      throw new IllegalStateException("Primary parameter must be a range parameter.");
    label.setText("Primary");
    label.setVisible(true);
    spinner.setVisible(false);
  }
  
  public void setSecondary()
  {
    if (!param.isMultiple())
      throw new IllegalStateException("Secondary parameter must be a range parameter.");
    label.setText("Secondary");
    label.setVisible(true);
    spinner.setVisible(false);
  }
  
  public void setTertiary()
  {
    label.setText(param.getSingleValue().toString());
    if (param.isMultiple())
    {
      label.setVisible(false);
      spinner.setVisible(true);
    }
  }

  public void setAverage()
  {
    if (!param.isMultiple())
      throw new IllegalStateException("Can't setAverage on a single-value parameter");
    label.setText("Average");
    label.setVisible(true);
    spinner.setVisible(false);
  }
  
  public MultipleValueParameter getParameter()
  {
    return param;
  }

  public SingleValueParameter getSelectedParameter()
  {
    return selected;
  }
}