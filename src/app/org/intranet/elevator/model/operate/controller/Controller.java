/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.sim.event.EventQueue;

import au.edu.rmit.elevatorsim.ui.ControllerDialogCreator;

/**
 * @author Neil McKellar and Chris Dailey
 */
public interface Controller
{
  public void initialize(EventQueue eQ, Building building);
  public void requestCar(Floor newFloor, Direction d);
  /**
   * To be called only once when a car arrives at a location.  This allows the
   * controller to update any internal data structures that were keeping track
   * of where the car was going to.
   * @param car The car that is arriving.
   * @return Whether the car is going up after this arrival.  Can be used to
   *         set the direction light on the entrance.
   */
  public boolean arrive(Car car);
  public void setNextDestination(Car car);
  public void setControllerDialogCreator(ControllerDialogCreator cdc);
}
