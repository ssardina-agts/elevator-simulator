/*
* Copyright 2003-2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

import org.intranet.sim.event.EventQueue;

/**
* @author Neil McKellar and Chris Dailey
*/
public final class Floor
  extends Location
{
  private int number;
  // distance from the ground
  private float ceiling;  // relative to the floor's height
  private CarRequestPanel callPanel = new CarRequestPanel(new CarRequestPanel.Approver()
  {
    // Don't keep the request light on if there is an entrance with a down light
    public boolean approveDownRequest()
    {
      for (CarEntrance entrance : carEntrances)
        if (entrance.isDown())
          return false;
      return true;
    }

    // Don't keep the request light on if there is an entrance with an up light
    public boolean approveUpRequest()
    {
      for (CarEntrance entrance : carEntrances)
        if (entrance.isUp())
          return false;
      return true;
    }
  });
  private List<CarEntrance> carEntrances = new ArrayList<CarEntrance>();

  // TODO: Make a sequence diagram with all the passing off of notification
  private CarEntrance.CarEntranceListener carEntranceListener =
    new CarEntrance.CarEntranceListener()
  {
    public void arrivedUp(CarEntrance entrance)
    {
      callPanel.arrivedUp(entrance);
    }

    public void arrivedDown(CarEntrance entrance)
    {
      callPanel.arrivedDown(entrance);
    }
  };

  public Floor(EventQueue eQ, int number, float height, float ceiling)
  {
    super(eQ, height, 500);
    this.number = number;
    this.ceiling = ceiling;
  }
  
  public int getFloorNumber()
  {
    return number;
  }
  
  public float getCeiling()
  {
    return ceiling;
  }
  
  public float getAbsoluteCeiling()
  {
    return getHeight() + ceiling;
  }
  
  public CarRequestPanel getCallPanel()
  {
    return callPanel;
  }

  public void createCarEntrance(Car car)
  {
    carEntrances.add(new CarEntrance(eventQueue, this, car, carEntranceListener));
  }

  public List<CarEntrance> getCarEntrances()
  {
    return carEntrances;
  }

  public CarEntrance getOpenCarEntrance(boolean up)
  {
    for (CarEntrance carEntrance : carEntrances)
    {
      if (carEntrance.getDoor().isOpen())
      {
        if (up && carEntrance.isUp())
          return carEntrance;
        if (!up && carEntrance.isDown())
          return carEntrance;
      }
    }
    return null;
  }

  public CarEntrance getCarEntranceForCar(Location destination)
  {
    for (CarEntrance carEntrance : carEntrances)
      if (carEntrance.getDoor().getTo() == destination)
        return carEntrance;
    return null;
  }
  
  public String toString()
  {
    return "Floor" + number + "@" + getHeight();
  }
}