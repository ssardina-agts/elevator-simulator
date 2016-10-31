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
import org.intranet.sim.event.EventQueue;
import org.intranet.ui.IntegerParameter;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class ThreePersonElevatorSimulator
  extends Simulator
{
  private IntegerParameter floorsParameter;
  private IntegerParameter carsParameter;
//  private LongParameter durationParameter;
  
  private Building building;

  public ThreePersonElevatorSimulator()
  {
    super();
    floorsParameter = new IntegerParameter("Number of Floors",5);
    parameters.add(floorsParameter);
    carsParameter = new IntegerParameter("Number of Elevators",2);
    parameters.add(carsParameter);
//    durationParameter = new LongParameter("Sim duration (ms)",5000);
//    parameters.add(durationParameter);
    
    addControllerParameter();
  }
  
  @Override
  public void initializeModel()
  {
    int numFloors = floorsParameter.getIntegerValue();
    int numCars = carsParameter.getIntegerValue();
    
    EventQueue eQ = getEventQueue();

    Controller controller = getController();
    building = new Building(getEventQueue(), numFloors, numCars, controller, seedParameter.getLongValue());

    final Person a = building.createPerson(building.getFloor(1), 1);
    Event eventA = new CarRequestEvent(0, a, building.getFloor(1), building.getFloor(2));
    eQ.addEvent(eventA);

    final Person b = building.createPerson(building.getFloor(1), 2);
    Event eventB = new CarRequestEvent(0, b, building.getFloor(1), building.getFloor(4));
    eQ.addEvent(eventB);

    final Person c = building.createPerson(building.getFloor(3), 3);
    Event eventC = new CarRequestEvent(0, c, building.getFloor(3), building.getFloor(1));
    eQ.addEvent(eventC);
  }

  public final Model getModel()
  {
    return building;
  }
  
  public String getDescription()
  {
    return "Three Person Elevator";
  }

  public Simulator duplicate()
  {
    return new ThreePersonElevatorSimulator();
  }
}
