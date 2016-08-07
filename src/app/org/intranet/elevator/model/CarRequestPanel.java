/*
* Copyright 2003 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

/**
* @author Neil McKellar and Chris Dailey
*/
public final class CarRequestPanel
{
  interface Approver
  {
    boolean approveUpRequest();
    boolean approveDownRequest();
  }

  private boolean up;
  private boolean down;
  private Approver approver;
  private List<ButtonListener> buttonListeners =
    new ArrayList<ButtonListener>();
  private List<ArrivalListener> arrivalListeners =
    new ArrayList<ArrivalListener>();
  
  CarRequestPanel(Approver a)
  {
    super();
    approver = a;
  }
  
  public boolean isUp()
  {
    return up;
  }
  
  public boolean isDown()
  {
    return down;
  }
  
  public void pressUp()
  {
    if (up || !approver.approveUpRequest())
      return;
    up = true;
    for (ButtonListener l : new ArrayList<ButtonListener>(this.buttonListeners))
      l.pressedUp();
  }
  
  public void pressDown()
  {
    if (down || !approver.approveDownRequest())
      return;
    down = true;
    for (ButtonListener l : new ArrayList<ButtonListener>(this.buttonListeners))
      l.pressedDown();
  }
  
  void arrivedUp(CarEntrance entrance)
  {
    up = false;
    for (ArrivalListener l :
      new ArrayList<ArrivalListener>(this.arrivalListeners))
    {
      l.arrivedUp(entrance);
    }
  }
  
  void arrivedDown(CarEntrance entrance)
  {
    down = false;
    for (ArrivalListener l :
      new ArrayList<ArrivalListener>(this.arrivalListeners))
    {
      l.arrivedDown(entrance);
    }
  }
  
  public void addButtonListener(ButtonListener listener)
  {
    buttonListeners.add(listener);
  }
  
  public void removeButtonListener(ButtonListener listener)
  {
    buttonListeners.remove(listener);
  }
  
  public void addArrivalListener(ArrivalListener listener)
  {
    arrivalListeners.add(listener);
  }
  
  public void removeArrivalListener(ArrivalListener listener)
  {
    arrivalListeners.remove(listener);
  }
  
  public static interface ButtonListener
  {
    void pressedUp();
    
    void pressedDown();
  }
  
  public static interface ArrivalListener
  {
    void arrivedUp(CarEntrance entrance);
    
    void arrivedDown(CarEntrance entrance);
  }
}