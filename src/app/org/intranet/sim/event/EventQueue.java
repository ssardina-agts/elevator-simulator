/*
 * Copyright 2003, 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.event;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public final class EventQueue
{
  private long currentTime = -1; // Invalid time value initially

  private long lastTime = -1;
  private long lastEventProcessTime;

  private SortedSet<Event> eventSet =
    new TreeSet<Event>(new Event.EventTimeComparator());
  
  public interface Listener
  {
    void eventAdded(Event e);
    
    void eventRemoved(Event e);
    
    void eventError(Exception ex);
    
    void eventProcessed(Event e);
  }
  
  private List<Listener> listeners = new ArrayList<Listener>();
  private ReentrantLock lock = new ReentrantLock();
  
  private void acquireLock()
  {
	  lock.lock();
  }
  
  private void releaseLock()
  {
    while (lock.isHeldByCurrentThread())
	{
      lock.unlock();
	}
  }
  
  public void addEvent(Event event)
  {
    acquireLock();
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
    releaseLock();
  }
  
  public void removeEvent(Event event)
  {
	acquireLock();
    if (!eventSet.contains(event))
      throw new IllegalArgumentException("Cannot remove an Event that is not in the queue!");
    eventSet.remove(event);
    for (Listener listener : listeners)
      listener.eventRemoved(event);
    releaseLock();
  }
  
  public List<Event> getEventList()
  {
	acquireLock();
	releaseLock();
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
  public boolean processEventsUpTo(long time)
  {
	acquireLock();
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
    //currentTime = -1;
    lastTime = eventSet.size() == 0 ? lastEventProcessTime : time;
    
    releaseLock();
    return (numEventsProcessed != 0);
  }

  private int updateEventProgress()
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
  
  public void addListener(Listener listener)
  {
	acquireLock();
    listeners.add(listener);
    releaseLock();
  }
  
  public void removeListener(Listener listener)
  {
	acquireLock();
    listeners.remove(listener);
    releaseLock();
  }

  public long getCurrentTime()
  {
	acquireLock();
    if (currentTime == -1)
      throw new
        IllegalStateException("Current time is invalid when not processing events");
    releaseLock();
    return currentTime;
  }

  public long getLastEventProcessTime()
  {
    acquireLock();
    releaseLock();
    return lastEventProcessTime;
  }
}
