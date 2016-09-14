/*
 * Copyright 2003,2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

import org.intranet.sim.ModelElement;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

/**
* Deals with obstructions.  The state transitions look like this: 
* <table border="1" cellspacing="0" cellpadding="2">
*  <tr>
*   <th rowspan="2">State</th>
*   <th colspan="1">Variables</th>
*   <th colspan="10">Transitions</th>
*  </tr>
*  <tr>
*   <th>state</th>
*   <th>obstruct()</th>
*   <th>unobstruct()</th>
*   <th>[ClearEvent]</th>
*  </tr>
*  <tr>
*   <td>CLEAR</td>
*   <td/>
*   <td>OBSTRUCTED<br/>[sensorObstructed()]</td>
*   <td>UNOBSTRUCTED</td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>OBSTRUCTED</td>
*   <td/>
*   <td><i>Illegal</i></td>
*   <td>UNOBSTRUCTED</td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>UNOBSTRUCTED</td>
*   <td/>
*   <td>OBSTRUCTED<br/>[sensorObstructed()]</td>
*   <td><i>Illegal</i></td>
*   <td>CLEAR<br/>[sensorCleared()]</td>
*  </tr>
* </table>
* @author Neil McKellar and Chris Dailey
*/
public class DoorSensor
  extends ModelElement
{
  private State state = State.CLEAR;
  private Event clearEvent = null;
  private List<Listener> listeners = new ArrayList<Listener>();
  private Door door;

  private final class ClearEvent
      extends Event
  {
    private ClearEvent(long newTime)
    {
      super(newTime);
    }

    public void perform()
    {
      clear();
      clearEvent = null;
    }

    @Override
    public String getName()
    {
      return "doorSensorClear";
    }

    @Override
    public JSONObject getDescription()
    {
      JSONObject ret = new JSONObject();
      ret.put("floor", door.getFrom().getFloorNumber());
      ret.put("car", door.getTo().getId());
      return ret;
    }
  }
  public static interface Listener
  {
    void sensorCleared();
    void sensorObstructed();
    void sensorUnobstructed();
  }
  public static class State
  {
    private String name;

    private State(String state)
    {
      name = state;
    }

    /**
     * The sensor is unobstructed and the door is available to be closed.
     * The only way out of this state is to obstruct().
     */
    public static final State CLEAR = new State("CLEAR");
    /**
     * Someone is obstructing the way.  The door cannot be closed.  The only
     * way out of this state is to unobstruct().
     */
    public static final State OBSTRUCTED = new State("OBSTRUCTED");
    /**
     * There is noone in the way of the door, and after a timeout the state
     * will change to CLEAR unless another obstruct() occurs.
     */
    public static final State UNOBSTRUCTED = new State("UNOBSTRUCTED");
    
    public String toString() { return name; }
  }

  public DoorSensor(EventQueue eQ, Door door)
  {
    super(eQ);
    this.door = door;
  }

  public State getState()
  {
    return state;
  }

  public void obstruct()
  {
    if (state == State.OBSTRUCTED)
      throw new IllegalStateException("Can't reobstruct");
    if (state == State.UNOBSTRUCTED)
    {
      // cancel the pending clearEvent, the door sensor is obstructed again
      eventQueue.removeEvent(clearEvent);
      clearEvent = null;
    }
    state = State.OBSTRUCTED;
    for (Listener l : new ArrayList<Listener>(listeners))
      l.sensorObstructed();
  }

  public void unobstruct()
  {
    if (state == State.UNOBSTRUCTED)
      throw new IllegalStateException("Can't unobstruct unless obstructed, state");
    state = State.UNOBSTRUCTED;
    clearEvent = new ClearEvent(eventQueue.getCurrentTime() + 3000);
    eventQueue.addEvent(clearEvent);

    for (Listener l : new ArrayList<Listener>(listeners))
      l.sensorUnobstructed();
  }

  private void clear()
  {
    state = State.CLEAR;
    for (Listener l : new ArrayList<Listener>(listeners))
      l.sensorCleared();
  }

  public void addListener(Listener l)
  {
    listeners.add(l);
  }
  public void removeListener(Listener l)
  {
    listeners.remove(l);
  }
}
