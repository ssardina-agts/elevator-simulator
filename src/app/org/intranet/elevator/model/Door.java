/*
* Copyright 2003, 2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

import org.intranet.sim.ModelElement;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.event.TrackingUpdateEvent;
import org.json.JSONObject;

/**
* A door that opens and closes.
* Valid states:
* <table border="1" cellspacing="0" cellpadding="2">
*  <tr>
*   <th rowspan="2">State</th>
*   <th colspan="1">Variables</th>
*   <th colspan="10">Transitions</th>
*  </tr>
*  <tr>
*   <th>state</th>
*   <th>open()</th>
*   <th>[OpenEvent]</th>
*   <th>close()</th>
*   <th>[CloseEvent]</th>
*  </tr>
*  <tr>
*   <td>CLOSED</td>
*   <td/>
*   <td>OPENING</td>
*   <td><i>Impossible</i></td>
*   <td><i>Illegal</i></td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>OPENING</td>
*   <td/>
*   <td><i>Illegal</i></td>
*   <td>opened(): OPENED<br/>[doorOpened()]</td>
*   <td><i>Illegal</i></td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>OPENED</td>
*   <td/>
*   <td><i>Illegal</i></td>
*   <td><i>Impossible</i></td>
*   <td>close(): CLOSING</td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>CLOSING</td>
*   <td/>
*   <td>OPENING</td>
*   <td><i>Impossible</i></td>
*   <td><i>Illegal</i></td>
*   <td>closed(): CLOSED<br/>[doorClosed()]</td>
*  </tr>
* </table> 
* @author Neil McKellar and Chris Dailey
*/
public class Door
  extends ModelElement
{
  private State state = State.CLOSED;
  /**
   * Varies from 0 to 100.  The door starts closed.
   */
  private int percentClosed = 100;
  private Floor from;
  private Car to;
  private List<Listener> listeners = new ArrayList<Listener>();
  private List<Listener> priorityListeners = new ArrayList<Listener>();
  private Event event;
  private static final long CLOSE_TIME = 2000; 
  private static final long CLOSE_WAIT_TIME = 3000; 

  public static final class State
  {
    private String name;
    private State(String value)
    {
      name = value;
    }
    public static final State OPENED = new State("Opened");
    public static final State OPENING = new State("Opening");
    public static final State CLOSED = new State("Closed");
    public static final State CLOSING = new State("Closing");
    public final String toString()
    {
      return name;
    }
  }
  public static interface Listener
  {
    void doorOpened();
    void doorClosed();
  }
  
  private class OpenEvent extends TrackingUpdateEvent
  {
    public OpenEvent()
    {
      super(eventQueue.getCurrentTime(), percentClosed,
        eventQueue.getCurrentTime() +
        (long)(percentClosed / 100F * CLOSE_TIME), 0);
    }
    
    public void updateTime()
    {
      percentClosed = (int)currentValue(eventQueue.getCurrentTime());
    }

    public void perform()
    {
      event = null;
      percentClosed = 0;
      opened();
    }

	@Override
	public String getName()
	{
		return "doorOpened";
	}

	@Override
	public JSONObject getDescription()
	{
		JSONObject ret = new JSONObject();
		ret.put("car", to.id);
		ret.put("floor", from.getFloorNumber());
		return ret;
	}
  }
  
  private class CloseEvent extends TrackingUpdateEvent
  {
    public CloseEvent()
    {
      super(eventQueue.getCurrentTime(), percentClosed,
        eventQueue.getCurrentTime() +
        (long)((100 - percentClosed)/ 100F * CLOSE_TIME), 100);
    }
    public void updateTime()
    {
      percentClosed = (int)currentValue(eventQueue.getCurrentTime());
    }

    public void perform()
    {
      event = null;
      percentClosed = 100;
      closed();
    }
    @Override
    public String getName()
    {
      return "doorClosed";
    }
    @Override
    public JSONObject getDescription()
    {
      JSONObject ret = new JSONObject();
      ret.put("car", to.id);
      ret.put("floor", from.getFloorNumber());
      return ret;
    }
  }

  Door(EventQueue eQ, Floor fromLocation, Car toLocation)
  {
    super(eQ);
    from = fromLocation;
    to = toLocation;
  }

  public State getState()
  {
    return state;
  }
  
  private void setState(State state)
  {
    this.state = state;
  }

  /**
   * Returns the percentage the door is closed in its current state.
   * @return An int between 0 and 100.
   */
  public int getPercentClosed()
  {
    return percentClosed;
  }

  public Floor getFrom()
  {
    return from;
  }

  public Car getTo()
  {
    return to;
  }

  public boolean isOpen()
  {
    return (percentClosed == 0);
  }

  public void open()
  {
    if (state == State.OPENED || state == State.OPENING)
      throw new IllegalStateException();
    if (state == State.CLOSING)
    {
      eventQueue.removeEvent(event);
      event = null;
    }
    setState(State.OPENING);
    // The starting percentClosed is how much we have left to open
    if (event != null)
      throw new IllegalStateException("Already handling an event!");
    event = new OpenEvent();
    eventQueue.addEvent(event);
  }

  void close()
  {
    if (state == State.CLOSED || state == State.CLOSING || state == State.OPENING)
      throw new IllegalStateException("Can't close while in " + state);
    setState(State.CLOSING);
    if (event != null)
      throw new IllegalStateException("Already handling an event!");
    event = new CloseEvent();
    eventQueue.addEvent(event);
  }

  private void closed()
  {
    setState(State.CLOSED);
    percentClosed = 100;
    // Notification occurs with the high priority listeners first.
    List<Listener> listenersCopy = new ArrayList<Listener>(priorityListeners);
    listenersCopy.addAll(listeners);
    for (Listener l : listenersCopy)
      l.doorClosed();
  }

  private void opened()
  {
    setState(State.OPENED);
    percentClosed = 0;
    List<Listener> listenersCopy = new ArrayList<Listener>(priorityListeners);
    listenersCopy.addAll(listeners);
    for (Listener l : listenersCopy)
      l.doorOpened();
    if (event != null)
      throw new IllegalStateException("Already handling an event!");
  }

  public void addListener(Listener l, boolean highPriority)
  {
    if (highPriority)
      priorityListeners.add(l);
    else
      listeners.add(l);
  }
  public void removeListener(Listener l)
  {
    listeners.remove(l);
    priorityListeners.remove(l);
  }

  public long getMinimumCycleTime()
  {
    return 2 * CLOSE_TIME + CLOSE_WAIT_TIME;    
  }
}