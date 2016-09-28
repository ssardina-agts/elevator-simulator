/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import java.util.ArrayList;
import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.sim.event.EventQueue;

import au.edu.rmit.elevatorsim.ui.ControllerDialogCreator;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class MetaController
  implements Controller
{
  private List<CarController> carControllers = new ArrayList<CarController>();

  @Override
  public void initialize(EventQueue eQ, Building building)
  {
    carControllers.clear();
    for (Car car : building.getCars())
    {
      CarController cc = new CarController(car);
      carControllers.add(cc);
    }
  }

  @Override
  public void requestCar(Floor newFloor, Direction d)
  {
    CarController controller = findBestCar(newFloor, d);
    controller.addDestination(newFloor, d);
  }

  private CarController findBestCar(Floor floor, Direction direction)
  {
    // if only one car, duh
    if (carControllers.size() == 1) return carControllers.get(0);

    CarController c = null;
    float lowestCost = Float.MAX_VALUE;
    for (CarController controller : carControllers)
    {
      float cost = controller.getCost(floor, direction);
      if (cost < lowestCost)
      {
        c = controller;
        lowestCost = cost;
      }
      else if (cost == lowestCost)
      {
        // Previously, the simulation simply collected statistics.
        // With the addition of this comparison, the statistics being gathered
        // are affecting the outcome of the simulation.
        if (controller.getCar().getTotalDistance() < c.getCar().getTotalDistance())
          c = controller;
      }
    }

    return c;
  }

  @Override
  public String toString()
  {
    return "Default MetaController";
  }

  @Override
  public boolean arrive(Car car)
  {
    CarController c = getController(car);
    return c.arrive();
  }

  @Override
  public void setNextDestination(Car car)
  {
    CarController c = getController(car);
    c.setNextDestination();
  }

  private CarController getController(Car car)
  {
    CarController c = null;
    for (CarController controller : carControllers)
      if (controller.getCar() == car)
        c = controller;
    return c;
  }

  @Override
  public void setControllerDialogCreator(ControllerDialogCreator cdc) {}
}
