/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import au.edu.rmit.agtgrp.elevatorsim.SimulatorParams;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.Person;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.Model;
import org.intranet.sim.Simulator;
import org.intranet.sim.event.Event;
import org.intranet.ui.IntegerParameter;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class NoIdleElevatorCarSimulator
        extends Simulator {
    private IntegerParameter downDestParameter;
    private IntegerParameter upDestParameter;
    private IntegerParameter floorsParameter;
    private IntegerParameter carsParameter;

    private Building building;

    public NoIdleElevatorCarSimulator() {
        super();
    }

    @Override
    protected void initialiseParameters() {
        if (SimulatorParams.instance().isValid()) {
            SimulatorParams params = SimulatorParams.instance();
            floorsParameter = new IntegerParameter("Number of Floors", params.getParamValueInt("floors"));
            carsParameter = new IntegerParameter("Number of Elevators", params.getParamValueInt("cars"));
            upDestParameter = new IntegerParameter("Up Destination", params.getParamValueInt("upDest"));
            downDestParameter = new IntegerParameter("Down Destination", params.getParamValueInt("downDest"));
        } else {
            floorsParameter = new IntegerParameter("Number of Floors", 5);
            carsParameter = new IntegerParameter("Number of Elevators", 1);
            upDestParameter = new IntegerParameter("Up Destination", 3);
            downDestParameter = new IntegerParameter("Down Destination", 2);
        }

        parameters.add(floorsParameter);
        parameters.add(carsParameter);
        parameters.add(upDestParameter);
        parameters.add(downDestParameter);

        addControllerParameter();
    }

    @Override
    public void initializeModel() {
        int numFloors = floorsParameter.getIntegerValue();
        int numCars = carsParameter.getIntegerValue();
        final int upDest = upDestParameter.getIntegerValue() - 1;
        final int downDest = downDestParameter.getIntegerValue() - 1;

        Controller controller = getController();
        building = new Building(getEventQueue(), numFloors, numCars, controller, seedParameter.getLongValue());
        final Person a = building.createPerson(building.getFloor(1), 1);
        Event eventA = new CarRequestEvent(0, a, building.getFloor(1), building.getFloor(upDest));
        getEventQueue().addEvent(eventA);

        final Person c = building.createPerson(building.getFloor(3), 3);
        Event eventC = new CarRequestEvent(0, c, building.getFloor(3), building.getFloor(downDest));
        getEventQueue().addEvent(eventC);
    }

    public final Model getModel() {
        return building;
    }

    public String getDescription() {
        return "People Going Different Directions, Only One Car";
    }

    public Simulator duplicate() {
        return new NoIdleElevatorCarSimulator();
    }
}
