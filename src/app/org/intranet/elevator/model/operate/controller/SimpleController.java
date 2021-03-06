/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import au.edu.rmit.agtgrp.elevatorsim.ui.ControllerDialogCreator;
import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarRequestPanel;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.sim.event.EventQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Goal 1: (Complete)
 * Elevators should stop when there are no car requests and no floor requests.
 * 
 * Goal 2: (Incomplete) Optimization - skip unnecessary floors 
 * If a button has been pressed, the elevator goes to that floor.
 * If there is a person in the car, then we travel to the appropriate floor.
 * 
 * @author Neil McKellar and Chris Dailey
 */
public class SimpleController
    implements Controller
{
  private List<Car> cars = new ArrayList<Car>();
  private boolean up = true;
  private boolean carsMoving = false;

  public SimpleController()
  {
    super();
  }

  @Override
  public void initialize(EventQueue eQ, Building building)
  {
    cars.clear();
    for (Car car : building.getCars())
    {
      cars.add(car);
    }
    carsMoving = false;
    up = true;
  }

  @Override
  public void requestCar(Floor newFloor, Direction d)
  {
    moveCars();
  }

  private void moveCars()
  {
    if (!carsMoving)
      for (Car car : cars)
        sendToNextFloor(car);
    carsMoving = true;
  }

  // TODO: Reduce code duplication between isUp(), getCurrentIndex(), and sendToNextFloor()
  @Override
  public boolean arrive(Car car)
  {
    List floors = car.getFloorRequestPanel().getServicedFloors();
    int idx = getCurrentIndex(car);
    // At the top floor, go down; at the bottom floor go up
    up = (idx == floors.size() - 1) ? false : idx == 0 ? true : up;
    return up;
  }
  
  private int getCurrentIndex(Car car)
  {
    Floor currentFloor = car.getLocation();
    if (currentFloor == null) currentFloor = car.getFloorAt();
    List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
    return floors.indexOf(currentFloor);
  }

  private void sendToNextFloor(Car car)
  {
    int idx = getCurrentIndex(car);
    // Next floor depends on the direction
    idx += arrive(car) ? 1 : -1;
    List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
    Floor nextFloor = floors.get(idx);
    car.setDestination(nextFloor);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName();
  }

  private void evaluateCarsMoving(final Car car)
  {
    carsMoving = false;
    for (Floor f : car.getFloorRequestPanel().getServicedFloors())
    {
      CarRequestPanel crp = f.getCallPanel();
      if (crp.isUp() || crp.isDown())
      {
        carsMoving = true;
        break;
      }
    }
    if (car.getFloorRequestPanel().getRequestedFloors().size() > 0)
      carsMoving = true;
  }

  @Override
  public void setNextDestination(Car car)
  {
    evaluateCarsMoving(car);

    // The end-condition of the simulation is roughly here.  If
    // carsMoving is false, there will not be new events created
    // for cars.  At this point, only events from other sources
    // (either the Simulation itself or a Person) will cause the
    // simulation to continue.
    if (carsMoving)
      sendToNextFloor(car);
  }

  @Override
  public void setControllerDialogCreator(ControllerDialogCreator cdc) {}
  
  public String getInitMessage()
  {
    return "loading...";
  }

  @Override
  public void close() {}
}
