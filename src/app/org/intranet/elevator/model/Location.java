/*
* Copyright 2003-2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

import org.intranet.elevator.model.operate.Person;
import org.intranet.sim.ModelElement;
import org.intranet.sim.event.EventQueue;

/**
* @author Neil McKellar and Chris Dailey
*/
public class Location
  extends ModelElement
{
  private float height;
  private int capacity;
  private List<Person> occupants = new ArrayList<Person>();
  
  Location(EventQueue eQ, float height, int capacity)
  {
    super(eQ);
    this.height = height;
    this.capacity = capacity;
  }

  public final float getHeight()
  {
    return height;
  }

  protected void setHeight(float newHeight)
  {
    height = newHeight; 
  }
  
  public final void personEnters(Person person)
  {
    if (isAtCapacity())
      throw new IllegalStateException("Location is at capacity: " + capacity);
    occupants.add(person);
  }
  

  public final List<Person> getOccupants()
  {
    return occupants;
  }
  
  public final void personLeaves(Person person)
  {
    if (!occupants.remove(person))
    {
      throw new IllegalStateException("Person is not in this location.");
    }
  }
  
  public final boolean isAtCapacity()
  {
    return (occupants.size() == capacity);
  }
  
  public final int getCapacity()
  {
    return capacity;
  }

  public final void setCapacity(int numOccupants)
  {
    capacity = numOccupants;
  }
}