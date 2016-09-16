/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.Person;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.elevator.model.operate.controller.MetaController;
import org.intranet.elevator.model.operate.controller.SimpleController;
import org.intranet.sim.Model;
import org.intranet.sim.Simulator;
import org.intranet.sim.event.Event;
import org.intranet.ui.ChoiceParameter;
import org.intranet.ui.IntegerParameter;
import org.intranet.ui.LongParameter;

import io.sarl.wrapper.NetworkWrapperController;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class RandomElevatorSimulator
  extends Simulator
{
  private IntegerParameter floorsParameter;
  private IntegerParameter carsParameter;
  private IntegerParameter capacityParameter;
  private IntegerParameter ridersParameter;
  private LongParameter durationParameter;
  private LongParameter seedParameter;
  private ChoiceParameter controllerParameter;
  
  private Building building;

  public RandomElevatorSimulator()
  {
    super();
    floorsParameter = new IntegerParameter("Number of Floors",10);
    parameters.add(floorsParameter);
    carsParameter = new IntegerParameter("Number of Elevators",3);
    parameters.add(carsParameter);
    capacityParameter = new IntegerParameter("Elevator Capacity", 8);
    parameters.add(capacityParameter);
    ridersParameter = new IntegerParameter("Number of People", 20);
    parameters.add(ridersParameter);
    durationParameter = new LongParameter("Simulation Duration (ms)", 50000);
    parameters.add(durationParameter);
    seedParameter = new LongParameter("Random seed", System.currentTimeMillis());
    parameters.add(seedParameter);

    List<Controller> controllers = new ArrayList<Controller>();
    Controller controller1 = new MetaController();
    Controller controller2 = new SimpleController();
    controllers.add(controller1);
    controllers.add(controller2);
    controllers.add(new NetworkWrapperController());
    controllerParameter = new ChoiceParameter<Controller>("Controller", controllers,
        controller1);
    parameters.add(controllerParameter);
  }

  @Override
  public Controller initializeModel()
  {
    int numFloors = floorsParameter.getIntegerValue();
    int numCars = carsParameter.getIntegerValue();
    int carCapacity = capacityParameter.getIntegerValue();
    int numRiders = ridersParameter.getIntegerValue();
    long duration = durationParameter.getLongValue();
    long seed = seedParameter.getLongValue();
    Controller controller = (Controller)controllerParameter.getChoiceValue();

    building = new Building(getEventQueue(), numFloors, numCars, carCapacity, controller);
    
    Random rand = new Random(seed);

    for (int i = 0; i < numRiders; i++)
    {
      // starting floor
      Floor startingFloor = building.getFloor(rand.nextInt(numFloors));
      final Person person = building.createPerson(startingFloor, i);
      // destination floor
      Floor floor = null;
      do {
        floor = building.getFloor(rand.nextInt(numFloors));
      } while (floor == startingFloor);
      final Floor destFloor = floor;
      // time to insert
      long insertTime = rand.nextInt((int)duration);
      // insertion event for destination at time
      Event event = new CarRequestEvent(insertTime, person, startingFloor, destFloor);
      getEventQueue().addEvent(event);
    }
    
    return controller;
  }
  
  public final Model getModel()
  {
    return building;
  }
  
  public String getDescription()
  {
    return "Random Rider Insertion";
  }

  public Simulator duplicate()
  {
    return new RandomElevatorSimulator();
  }
}
