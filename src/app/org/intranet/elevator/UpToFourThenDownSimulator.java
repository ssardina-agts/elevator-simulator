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
public class UpToFourThenDownSimulator
        extends Simulator {
    private IntegerParameter floorsParameter;
    private IntegerParameter carsParameter;

    private Building building;

    public UpToFourThenDownSimulator() {
        super();
    }

    @Override
    protected void initialiseParameters() {
        if (SimulatorParams.instance().isParamsLoaded()) {
            SimulatorParams params = SimulatorParams.instance();
            floorsParameter = new IntegerParameter("Number of Floors", params.getParamValueInt("floors"));
            carsParameter = new IntegerParameter("Number of Elevators", params.getParamValueInt("cars"));
        } else {
            floorsParameter = new IntegerParameter("Number of Floors", 5);
            carsParameter = new IntegerParameter("Number of Elevators", 1);
        }

        parameters.add(floorsParameter);
        parameters.add(carsParameter);

        addControllerParameter();
    }

    @Override
    public void initializeModel() {
        int numFloors = floorsParameter.getIntegerValue();
        int numCars = carsParameter.getIntegerValue();

        Controller controller = getController();
        building = new Building(getEventQueue(), numFloors, numCars, controller, seedParameter.getLongValue());

        final Person a = building.createPerson(building.getFloor(3), 3);
        Event event = new CarRequestEvent(0, a, building.getFloor(3), building.getFloor(3));
        getEventQueue().addEvent(event);
    }

    public final Model getModel() {
        return building;
    }

    public String getDescription() {
        return "Elevator Travels Up To Process A Down Request";
    }

    public Simulator duplicate() {
        return new UpToFourThenDownSimulator();
    }
}
