/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public abstract class SingleValueParameter<T>
  extends Parameter
{
  public SingleValueParameter(String desc)
  {
    super(desc);
  }

  @SuppressWarnings("unchecked")
  public SingleValueParameter<T> clone()
  {
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(this);
      oos.close();
      byte[] bytes = baos.toByteArray();
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = new ObjectInputStream(bais);
      Object obj = ois.readObject();
      ois.close();
      return (SingleValueParameter<T>)obj;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  public abstract List<T> getValues(T min, T max, T inc);
  public abstract void setValue(T param);
  // SOON : This is only needed for MultipleSimulationArea.updateSimulationParameters().  Consider refactoring to remove createParameterSetIterator().
  public abstract void setValueFromString(String param);
  public abstract T getValue();
  public abstract T getValueFromString(String value);
}
