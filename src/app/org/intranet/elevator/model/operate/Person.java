/*
* Copyright 2003,2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model.operate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.CarRequestPanel;
import org.intranet.elevator.model.Door;
import org.intranet.elevator.model.DoorSensor;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.Location;
import org.intranet.sim.ModelElement;
import org.intranet.sim.Simulator;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.event.TrackingUpdateEvent;
import org.json.JSONObject;

/**
* A Person moves around the building, calling elevators, entering elevators,
* and leaving elevators. 
* Person states:
* <table border="1" cellspacing="0" cellpadding="2">
*  <tr>
*   <th rowspan="2">State</th>
*   <th colspan="2">Variables</th>
*   <th colspan="11">Transitions</th>
*  </tr>
*  <tr>
*   <th>destination</th>
*   <th>currentLocation</th>
*   <th>setDestination()</th>
*   <th>CarRequestPanel<br/>[arrivedUp/Down]</th>
*   <th>Door<br/>[doorClosed]</th>
*   <th>Car<br/>[docked]</th>
*   <th>Door<br/>[doorOpened]</th>
*   <th>[leftCar]</th>
*  </tr>
*  <tr>
*   <td>Idle</td>
*   <td>null</td>
*   <td>Set</td>
*   <td>Idle if destination is same<br/>
*       pressButton(): Waiting if elevator is elsewhere<br/>
*       enterCar(): Travelling if elevator is there</td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>Waiting</td>
*   <td>Set</td>
*   <td>Set to Floor</td>
*   <td><i>Illegal?</i></td>
*   <td>enterCar(): Travelling if success<br/>
*       waitForDoorClose(): DoorClosing if car is full</td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>DoorClosing</td>
*   <td>Set</td>
*   <td>Set to Floor</td>
*   <td><i>Illegal?</i></td>
*   <td><i>Impossible</i></td>
*   <td>doorClosed(): Waiting via pressedUp/Down()</td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>Travelling</td>
*   <td>Set</td>
*   <td>Set to Car</td>
*   <td><i>Illegal?</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td>waitForDoorOpen(): DoorOpening</td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>DoorOpening</td>
*   <td>Set</td>
*   <td>Set to Car</td>
*   <td><i>Illegal?</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td>leaveCar(): LeavingCar</td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>LeavingCar</td>
*   <td>Set</td>
*   <td>Set to Car</td>
*   <td><i>Illegal?</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td>Idle</td>
*  </tr>
*  <tr>
*   <td>Moving</td>
*   <td>Set</td>
*   <td>??</td>
*   <td><i>Illegal</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i>: Door is obstructed</td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*   <td><i>Impossible</i></td>
*  </tr>
* </table>
* @author Neil McKellar and Chris Dailey
*/
public final class Person
  extends ModelElement
{
  private final class WaitForDoorAvailableLeavingListener
      implements CarEntrance.DoorWaitListener
  {
    private final CarEntrance entrance;

    private WaitForDoorAvailableLeavingListener(CarEntrance entrance)
    {
      super();
      this.entrance = entrance;
    }

    public void doorAvailable()
    {
      entrance.getDoorSensor().obstruct();
      // TODO: Deal with the floor being at capacity.
      percentMoved = 0;
      eventQueue.addEvent(new LeavingCarEvent(eventQueue.getCurrentTime(), 0,
        eventQueue.getCurrentTime() + 2000, 100, entrance));
      leaveCar();
    }
  }

  private final class PayAttentionToSensorListener
      implements DoorSensor.Listener
  {
    private final boolean up;

    private PayAttentionToSensorListener(boolean up)
    {
      super();
      this.up = up;
    }

    public void sensorCleared() {}

    public void sensorObstructed() {}

    public void sensorUnobstructed()
    {
      tryToEnterCar(up);
    }
  }
  private final class PayAttentionToDoorListener
      implements Door.Listener
  {
    private final DoorSensor.Listener listener;
    private final DoorSensor sensor;
    private final boolean up;
    private final Door door;
    private final CarEntrance entrance;

    private PayAttentionToDoorListener(DoorSensor.Listener listener,
      DoorSensor sensor, boolean up, Door door, CarEntrance entrance)
    {
      super();
      this.listener = listener;
      this.sensor = sensor;
      this.up = up;
      this.door = door;
      this.entrance = entrance;
    }

    public void doorOpened()
    {
      tryToEnterCar(up);
    }

    public void doorClosed()
    {
      // stop paying attention to this entrance
      payAttentionEntrances.remove(entrance);
      sensor.removeListener(listener);
      sensorListenerMap.remove(listener);
      door.removeListener(this);
      doorListenerMap.remove(this);
    }
  }

  private final class WaitToGetOffListener
      implements Car.Listener
  {
    private final Car car;

    private WaitToGetOffListener(Car car)
    {
      super();
      this.car = car;
    }

    public void docked()
    {
      if (destination == car.getLocation())
      {
        car.removeListener(this);
        waitForDoorOpen();
      }
    }
  }

  private final class LeavingCarEvent
      extends TrackingUpdateEvent
  {
    private final CarEntrance entrance;

    private LeavingCarEvent(long time, float begin, long time2, float end, CarEntrance entrance)
    {
      super(time, begin, time2, end);
      this.entrance = entrance;
    }

    public void updateTime()
    {
      percentMoved = (int)currentValue(eventQueue.getCurrentTime());
    }

    public void perform()
    {
      percentMoved = -1;
      movePerson(destination);
      entrance.getDoorSensor().unobstruct();
      destination = null;
    }
    
    public String getName()
    {
      return "personLeftCar";
    }
    
    public JSONObject getDescription()
    {
      JSONObject ret = new JSONObject();
      ret.put("car", entrance.getDoor().getTo().getId());
      return ret;
    }
  }
  private final class EnteringCarEvent
      extends TrackingUpdateEvent
  {
    private final CarEntrance entrance;

    private EnteringCarEvent(long time, float begin, long time2, float end, CarEntrance entrance)
    {
      super(time, begin, time2, end);
      this.entrance = entrance;
    }

    public void updateTime()
    {
      percentMoved = (int)currentValue(eventQueue.getCurrentTime());
    }

    public void perform()
    {
      percentMoved = -1;
      enterCar(entrance);
      entrance.getDoorSensor().unobstruct();
    }
    
    public String getName()
    {
      return "personEnteredCar";
    }
    
    public JSONObject getDescription()
    {
      JSONObject ret = new JSONObject();
      ret.put("car", entrance.getDoor().getTo().getId());
      return ret;
    }
  }

  private final class WaitingArrivalListener
      implements CarRequestPanel.ArrivalListener
  {
    private final boolean up;

    private WaitingArrivalListener(boolean up)
    {
      super();
      this.up = up;
    }

    public void arrivedUp(CarEntrance entrance)
    {
      if (!up) return;
      payAttentionToEntrance(entrance, up);
      tryToEnterCar(up);
    }

    public void arrivedDown(CarEntrance entrance)
    {
      if (up) return;
      payAttentionToEntrance(entrance, up);
      tryToEnterCar(up);
    }
  }

  private static int nextId = 0;
  private Floor destination;
  private Location currentLocation;
  private int percentMoved = -1;
  
  private Map<DoorSensor.Listener, DoorSensor> sensorListenerMap =
    new HashMap<DoorSensor.Listener, DoorSensor>();
  private Map<Door.Listener, Door> doorListenerMap =
    new HashMap<Door.Listener, Door>();
  private CarRequestPanel.ArrivalListener arrivalListener;

  private long totalWaitingTime;
  private long startWaitTime = -1;
  private long totalTravelTime;
  private long startTravelTime = -1;
  private List<CarEntrance> payAttentionEntrances =
    new ArrayList<CarEntrance>();
  private long identifier;  // primarily for debug purposes
  
  Person(EventQueue eQ, Location startLocation, long id)
  {
    super(eQ);
    identifier = nextId++;
    // TODO: Deal with the start location being at capacity.
    movePerson(startLocation);
  }
  
  public void setDestination(Floor newDestination)
  {
    if (newDestination == currentLocation)
    {
      destination = null;
      return;
    }
    destination = newDestination;
    int currentFloorNumber = ((Floor)currentLocation).getFloorNumber();
    int destinationFloorNumber = newDestination.getFloorNumber();
    boolean up = destinationFloorNumber > currentFloorNumber;

    beginWaiting();
    startPayingAttention(up);
    tryToEnterCar(up);
  }

  private void startPayingAttention(final boolean up)
  {
    Floor here = (Floor)currentLocation;
    final CarRequestPanel callButton = here.getCallPanel();
    arrivalListener = new WaitingArrivalListener(up);
    callButton.addArrivalListener(arrivalListener);

    for (CarEntrance thisEntrance : ((Floor)currentLocation).getCarEntrances())
    {
      if (thisEntrance.getDoor().getState() != Door.State.CLOSED &&
          ((thisEntrance.isUp() && up) || (!up && thisEntrance.isDown())))
      {
        payAttentionToEntrance(thisEntrance, up);
      }
    }
  }

  private void payAttentionToEntrance(CarEntrance entrance, final boolean up)
  {
    if (payAttentionEntrances.contains(entrance))
      return;

    final DoorSensor sensor = entrance.getDoorSensor();
    final Door door = entrance.getDoor();
    // pay attention to unobstructed
    final DoorSensor.Listener sensorListener =
      new PayAttentionToSensorListener(up);
    sensor.addListener(sensorListener);
    sensorListenerMap.put(sensorListener, sensor);
    // pay attention to doorClosed
    Door.Listener doorListener = new PayAttentionToDoorListener(sensorListener,
      sensor, up, door, entrance);
    door.addListener(doorListener, false);
    doorListenerMap.put(doorListener, door);
    payAttentionEntrances.add(entrance);
  }
  
  private void stopPayingAttention()
  {
    Floor here = (Floor)currentLocation;
    final CarRequestPanel callButton = here.getCallPanel();
    callButton.removeArrivalListener(arrivalListener);
    arrivalListener = null;

    for (DoorSensor.Listener listener : sensorListenerMap.keySet())
    {
      DoorSensor sensor = sensorListenerMap.get(listener);
      sensor.removeListener(listener);
    }
    sensorListenerMap.clear();

    for (Door.Listener listener : doorListenerMap.keySet())
    {
      Door door = doorListenerMap.get(listener);
      door.removeListener(listener);
    }
    doorListenerMap.clear();

    payAttentionEntrances.clear();
  }

  private void tryToEnterCar(boolean up)
  {
	if (!(currentLocation instanceof Floor))
	{
		return;
	}
    Floor here = (Floor)currentLocation;
    final CarRequestPanel callButton = here.getCallPanel();
    List<Door> fullCarsAt = new ArrayList<>();

    for (CarEntrance entrance : here.getCarEntrances())
    {
      Door door = entrance.getDoor();
      DoorSensor sensor = entrance.getDoorSensor();
      Car car = (Car)door.getTo();
      // Note that occasionally in real life people will accidentally not
      // pay attention to the light and get on an elevator going the wrong
      // direction.
      // LATER: We may eventually want to simulate this.
      boolean isCarGoingMyWay = (up && entrance.isUp()) ||
        (!up && entrance.isDown());
      // if door is open && sensor is !obstructed && not at capacity
      boolean isEntranceAvailable = door.getState() != Door.State.CLOSED && 
        isCarGoingMyWay && !car.isAtCapacity();
      boolean isEntranceOccupied =
        sensor.getState() == DoorSensor.State.OBSTRUCTED || 
        entrance.arePeopleWaitingToGetOut();
      if (isEntranceAvailable && !isEntranceOccupied)
      {
        stopPayingAttention();
        beginEnterCar(entrance);
        return;
      }
      
      if (!isEntranceAvailable && car.isAtCapacity())
      {
    	door.addListener(new Door.Listener()
		{
    		Floor origin = here;
			@Override
			public void doorOpened() {}
			
			@Override
			public void doorClosed()
			{
              if (origin == currentLocation)
              {
                startPayingAttention(up);
                eventQueue.addEvent(new Simulator.CarRequestEvent(
    	          eventQueue.getCurrentTime(),
    	          Person.this,
    	          origin, 
    	          destination
                ));
              }
              door.removeListener(this);
			}
		}, true);
      }
    }

    if (up && !callButton.isUp())
      callButton.pressUp();
    if (!up && !callButton.isDown())
      callButton.pressDown();
  }
  
  private void beginEnterCar(final CarEntrance entrance)
  {
    entrance.getDoorSensor().obstruct();
    long currentTime = eventQueue.getCurrentTime();
    Event enteringCarEvent = new EnteringCarEvent(currentTime, 0.0f,
      currentTime + 2000, 100.0f, entrance);
    eventQueue.addEvent(enteringCarEvent);
  }

  private void beginWaiting()
  {
    if (startWaitTime == -1)
      startWaitTime = eventQueue.getCurrentTime();
  }
  private void endWaiting()
  {
    if (startWaitTime == -1)
      throw new IllegalStateException("Can't end waiting when not already waiting.");
    totalWaitingTime += eventQueue.getCurrentTime() - startWaitTime;
    startWaitTime = -1;
  }
  private void beginTravel()
  {
    if (startTravelTime != -1)
      throw new IllegalStateException("Can't begin travelling while already travelling");
    startTravelTime = eventQueue.getCurrentTime();
  }
  private void endTravel()
  {
    if (startTravelTime == -1)
      throw new IllegalStateException("Can't end travel when not already travelling.");
    totalTravelTime += eventQueue.getCurrentTime() - startTravelTime;
    startTravelTime = -1;
  }

  public long getTotalWaitingTime()
  {
    return totalWaitingTime;
  }

  public long getTotalTravelTime()
  {
    return totalTravelTime;
  }

  public long getTotalTime()
  {
    return totalWaitingTime + totalTravelTime;
  }

  public Floor getDestination()
  {
    return destination;  
  }

  /**
   * When a person arrives at the destination, this is all the processing that
   * has to happen.
   */
  private void leaveCar()
  {
    endTravel();
  }
  
  /**
   * The person enters the car through the specified entrance.
   * This method is called after the TrackingUpdateEvent that animates
   * moving the person to the elevator car has completed.
   * @param carEntrance The entrance to the car.
   */
  private void enterCar(CarEntrance carEntrance)
  {
    final Door departureDoor = carEntrance.getDoor();
    final Car car = (Car) departureDoor.getTo();

    movePerson(car);

    endWaiting();
    beginTravel();
    car.getFloorRequestPanel().requestFloor(destination);
    // setup for getting out of the car
    car.addListener(new WaitToGetOffListener(car));
  }
  
  /**
   * Move the person to the specified destination.
   * @param destination Where the person moves to.
   */
  private void movePerson(Location destination)
  {
    if (destination == null)
      throw new IllegalArgumentException("Cannot move to a null destination.");

    if (destination.isAtCapacity())
      throw new IllegalStateException("Cannot move person when " + 
          destination.getClass().getSimpleName() + " is at capacity: " +
          destination.getCapacity() + ".");

    if (currentLocation != null)
      currentLocation.personLeaves(this);
    // personEnters() is guaranteed to succeed
    // because capacity was checked above.
    destination.personEnters(this);
    currentLocation = destination;
  }

  private void waitForDoorOpen()
  {
    final CarEntrance entrance = destination.getCarEntranceForCar(currentLocation);
    entrance.waitToEnterDoor(new WaitForDoorAvailableLeavingListener(entrance));
  }

  public int getPercentMoved()
  {
    return percentMoved;
  }
}