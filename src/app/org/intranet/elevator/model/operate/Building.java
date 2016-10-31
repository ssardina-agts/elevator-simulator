/*
* Copyright 2003-2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model.operate;

import java.util.ArrayList;
import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.Door;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.Location;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.Model;
import org.intranet.sim.event.EventQueue;
import org.intranet.statistics.FloatColumn;
import org.intranet.statistics.IntColumn;
import org.intranet.statistics.LongColumn;
import org.intranet.statistics.Table;

/**
* The building is a factory for other domain objects that also links up
* CarRequests with the RequestIndicators for each Floor.
* 
* @author Neil McKellar and Chris Dailey
*/
public class Building
  extends Model
{
  private float height;  // external height of building
  private List<Floor> floors = new ArrayList<Floor>();
  private List<Car> cars = new ArrayList<Car>();
  private Controller metaController;
  private long seed;
  
  private List<Person> people = new ArrayList<Person>();
  
  private Building()
  {
    super(null);
  }

  public Building(EventQueue eQ, Controller controller)
  {
    super(eQ);
    metaController = controller;
  }

  public Building(EventQueue eQ, int numFloors, int numCars, int carCapacity,
      Controller controller, long seed)
  {
    this(eQ, controller);
    createFloors(numFloors);
    createCars(numCars, carCapacity);
    this.seed = seed;
    initialize();
  }

  public Building(EventQueue eQ, int numFloors, int numCars,
      Controller controller, long seed)
  {
    this(eQ, numFloors, numCars, 8, controller, seed);
  }
  
  private void initialize()
  {
	  metaController.initialize(eventQueue, this);
  }

  public void createFloors(int x)
  {
    for (int i = 0; i < x; i++)
    {
      // Units are feet in this example.
      final Floor newFloor = new Floor(eventQueue, i+1, 10*i, 9);
      floors.add(newFloor);
      height = 10 * (i + 1);
      
      CarRequest carRequest = new CarRequest(metaController, newFloor);
      newFloor.getCallPanel().addButtonListener(carRequest);
    }
  }

  public void createCars(int x, int capacity)
  {
    for (int i = 0; i < x; i++)
    {
      //default stopping distance is 3.0
      final Car car = new Car(eventQueue, Integer.toString(i), 0.0f, capacity, i, 3.0f);
      cars.add(car);

      // SOON: Move this to Floor or maybe CarEntrance or elsewhere
      car.addListener(new Car.Listener()
      {
        public void docked()
        {
          Floor location = car.getLocation();
          final CarEntrance entrance = location.getCarEntranceForCar(car);
          final Door door = entrance.getDoor();
          if (door.getState() != Door.State.CLOSED)
            throw new IllegalStateException("How could the door not be closed if we're only now docking with it?");
          door.open();
          // LATER : This relies on the door state not changing directly to CLOSED
          // because we add the listener after the call to open

          final boolean isUp = metaController.arrive(car);

          // set the up/down light on the car entrance
          if (isUp)
            entrance.setUp(true);
          else
            entrance.setDown(true);

          Door.Listener doorListener = new Door.Listener()
          {
            public void doorOpened() {}

            public void doorClosed()
            {
              door.removeListener(this);
              if (isUp)
                entrance.setUp(false);
              else
                entrance.setDown(false);

              metaController.setNextDestination(car);
              car.undock();
            }
          };
          door.addListener(doorListener, true);
        }
      });


      for (Floor floor : floors)
      {
        car.getFloorRequestPanel().addServicedFloor(floor);
        floor.createCarEntrance(car);
      }
    }
  }
  
  public float getHeight()
  {
    return height;
  }
  
  public int getNumFloors()
  {
    return floors.size();
  }
  
  public int getNumCars()
  {
    return cars.size();
  }
  
  public List<Floor> getFloors()
  {
    return floors;
  }

  public List<Car> getCars()
  {
    return cars;
  }
  
  public long getSeed()
  {
	  return seed;
  }
  
  public Floor getFloor(int index)
  {
    return floors.get(index);
  }
  
  public Person createPerson(Location startLocation, long id)
  {
    Person person = new Person(eventQueue, startLocation, id);
    people.add(person);
    return person;
  }
  
  public List<Table> getStatistics()
  {
    // TODO : Update existing tables instead of creating new ones.
    List<Table> tables = new ArrayList<Table>();
    tables.add(generatePersonTable());
    tables.add(generateCarTable());

    return tables;
  }

  private Table generateCarTable()
  {
    float[] travelDistances = new float[cars.size()];
    int[] numTravels = new int[cars.size()];

    int carNum = 0;
    String[] carRows = new String[cars.size()];
    for (Car car : cars)
    {
      carRows[carNum] = "Car " + (carNum + 1);
      travelDistances[carNum] = car.getTotalDistance();
      numTravels[carNum] = car.getNumTravels();
      carNum++;
    }

    Table carTable = new Table(carRows, "Car");
    carTable.addColumn(new FloatColumn("Travel Distance", travelDistances));
    carTable.addColumn(new IntColumn("Number of Stops", numTravels));
    return carTable;
  }

  private Table generatePersonTable()
  {
    long[] waitingTimes = new long[people.size()];
    long[] travelTimes = new long[people.size()];
    long[] totalTimes = new long[people.size()];

    int personNum = 0;
    String[] peopleRows = new String[people.size()];
    for (Person person : people)
    {
      peopleRows[personNum] = "Person " + (personNum + 1);
      waitingTimes[personNum] = person.getTotalWaitingTime();
      travelTimes[personNum] = person.getTotalTravelTime();
      totalTimes[personNum] = person.getTotalTime();
      personNum++;
    }
    Table personTable = new Table(peopleRows, "Person");
    personTable.addColumn(new LongColumn("Waiting Time", waitingTimes));
    personTable.addColumn(new LongColumn("Travel Time", travelTimes));
    personTable.addColumn(new LongColumn("Total Time", totalTimes));
    return personTable;
  }
}