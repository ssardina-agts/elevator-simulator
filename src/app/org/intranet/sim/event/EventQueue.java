/*
 * Copyright 2003, 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.event;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public final class EventQueue
{
  private long currentTime = -1; // Invalid time value initially

  private long lastTime = -1;
  private long lastEventProcessTime;
  
  private boolean waitingForEvents = false;

  private SortedSet<Event> eventSet =
    new ConcurrentSkipListSet<Event>(new Event.EventTimeComparator());
  
  public interface Listener
  {
    void eventAdded(Event e);
    
    void eventRemoved(Event e);
    
    void eventError(Exception ex);
    
    void eventProcessed(Event e);
    
    void simulationEnded();
  }
  
  private List<Listener> listeners = new ArrayList<Listener>();
  
  public synchronized void addEvent(Event event)
  {
//System.out.println("EventQueue event at currentTime=" + currentTime +
// " for time="+event.getTime()+ ", class="+event.getClass().getName());
    if (event.getTime() < lastTime)
    {
      throw new IllegalArgumentException(
                  "Event occurs *before* the last time we processed: " +
                  event.getTime() + " < " + lastTime);
    }
    if ((currentTime != -1) && (event.getTime() < currentTime))
    {
      throw new IllegalArgumentException(
                "Event occurs *before* the current time: " +
                event.getTime() + " < " + currentTime);
    }
    if (eventSet.contains(event))
      throw new IllegalArgumentException("Cannot re-add an Event to the queue!");
    eventSet.add(event);

    for (Listener listener : listeners)
      listener.eventAdded(event);
  }
  
  public synchronized void removeEvent(Event event)
  {
    if (!eventSet.contains(event))
      throw new IllegalArgumentException("Cannot remove an Event that is not in the queue!");
    eventSet.remove(event);
    for (Listener listener : listeners)
      listener.eventRemoved(event);
  }
  
  public synchronized List<Event> getEventList()
  {
    return new ArrayList<Event>(eventSet);
  }
  
  /**
   * Processes events in the event list up to the requested time.
   * The method throws an exception if the requested time is before the 
   * last processed time mark.  The remainging pending events that require
   * updates are also notified.
   * @param time The requested time to process up to in the list of events.
   * @throws RuntimeException When the requested time is before the last time.
   * @return true if events were processed
   */
  public synchronized boolean processEventsUpTo(long time)
  {
    if (time < lastTime)
      throw new RuntimeException("Requested time is earlier than last time.");

    int numEventsProcessed = 0;
    do
    {
      if (eventSet.size() == 0) break;  // can't process events if there aren't any
      Event currentEvent = eventSet.first();
      // Since eventSet is ordered, and we're only interested in processing events
      // up to 'time', if we find an event after 'time' then we stop processing
      // the Set.
      if (currentEvent.getTime() > time) break;
      // Now we know the event needs to be processed
      removeEvent(currentEvent);
      long oldCurrentTime = currentTime;
      currentTime = currentEvent.getTime();
      try {
        // If the time has progressed, we must update the TrackingUpdateEvents
        // so further calculations in Event.perform() are based on up-to-date
        // values.
        if (oldCurrentTime != currentTime)
          numEventsProcessed += updateEventProgress();

        lastEventProcessTime = currentTime;
        currentEvent.perform();
        for (Listener listener : listeners) {
          listener.eventProcessed(currentEvent);
        }
        numEventsProcessed++;
      }
      catch(Exception e)
      {
        e.printStackTrace();
        for (Listener l : listeners)
          l.eventError(e);
      }
    } while (true);
    currentTime = time;
    numEventsProcessed += updateEventProgress();
    lastTime = eventSet.size() == 0 ? lastEventProcessTime : time;
    
    return (numEventsProcessed != 0);
  }

  private synchronized int updateEventProgress()
  {
    int numEventsProcessed = 0;
    // Update any events that have incremental progress between states
    for (Event evt : eventSet)
    {
      if (evt instanceof IncrementalUpdateEvent)
      {
        IncrementalUpdateEvent updateEvent = (IncrementalUpdateEvent) evt;
        try
        {
          updateEvent.updateTime();
          numEventsProcessed++;
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }
    return numEventsProcessed;
  }
  
  public synchronized void addListener(Listener listener)
  {
    listeners.add(listener);
  }
  
  public synchronized void removeListener(Listener listener)
  {
    listeners.remove(listener);
  }

  public synchronized long getCurrentTime()
  {
    return currentTime;
  }

  public synchronized long getLastEventProcessTime()
  {
    return lastEventProcessTime;
  }
  
  public synchronized void end()
  {
	  for (Listener l : listeners)
	  {
		  l.simulationEnded();
	  }
  }
  
  public synchronized void stopWaitingForEvents()
  {
	  this.waitingForEvents = false;
  }
  
  public synchronized void waitForEvents()
  {
	  this.waitingForEvents = true;
  }
  
  public synchronized boolean isWaitingForEvents()
  {
	  return this.waitingForEvents;
  }
}
