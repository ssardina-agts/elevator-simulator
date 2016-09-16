/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.Person;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.elevator.model.operate.controller.MetaController;
import org.intranet.sim.Model;
import org.intranet.sim.Simulator;
import org.intranet.sim.event.Event;
import org.intranet.ui.IntegerParameter;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class UpToFourThenDownSimulator
  extends Simulator
{
  private IntegerParameter floorsParameter;
  private IntegerParameter carsParameter;
  
  private Building building;

  public UpToFourThenDownSimulator()
  {
    super();
    floorsParameter = new IntegerParameter("Number of Floors",5);
    parameters.add(floorsParameter);
    carsParameter = new IntegerParameter("Number of Elevators",1);
    parameters.add(carsParameter);
  }
  
  @Override
  public Controller initializeModel()
  {
    int numFloors = floorsParameter.getIntegerValue();
    int numCars = carsParameter.getIntegerValue();

    Controller controller = new MetaController();
    building = new Building(getEventQueue(), numFloors, numCars,
        controller);

    final Person a = building.createPerson(building.getFloor(3), 3);
    Event event = new CarRequestEvent(0, a, building.getFloor(3), building.getFloor(3));
    getEventQueue().addEvent(event);
    
    return controller;
  }

  public final Model getModel()
  {
    return building;
  }
  
  public String getDescription()
  {
    return "Elevator Travels Up To Process A Down Request";
  }

  public Simulator duplicate()
  {
    return new UpToFourThenDownSimulator();
  }
}
