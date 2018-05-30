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
public class ThreePersonBugSimulator
        extends Simulator {
//    private IntegerParameter floorsParameter;
    private IntegerParameter carsParameter;

    private Building building;

    public ThreePersonBugSimulator() {
        super();
    }

    @Override
    protected void initialiseParameters() {
        if (SimulatorParams.instance().isParamsLoaded()) {
            SimulatorParams params = SimulatorParams.instance();
//        floorsParameter = new IntegerParameter("Number of Floors", 10);
            carsParameter = new IntegerParameter("Insert second request at", params.getParamValueInt("cars"));
        } else {
//        floorsParameter = new IntegerParameter("Number of Floors", 10);
            carsParameter = new IntegerParameter("Insert second request at", 29000);

        }

//        parameters.add(floorsParameter);
        parameters.add(carsParameter);

        addControllerParameter();
    }

    @Override
    public void initializeModel() {
//    int numFloors = floorsParameter.getValue();
        int numCars = carsParameter.getIntegerValue();

        Controller controller = getController();
        building = new Building(getEventQueue(), 6, 1, controller, seedParameter.getLongValue());

        createPerson(3, 0, 0, 1);
        createPerson(1, 2, numCars, 2);
//    createPerson(4, 8, 20000, 3);
    }

    private void createPerson(int start, final int dest, long simTime, long id) {
        final Person person = building.createPerson(building.getFloor(start), id);
        // insertion event for destination at time
        Event event = new CarRequestEvent(simTime, person, building.getFloor(start), building.getFloor(dest));
        getEventQueue().addEvent(event);
    }

    public final Model getModel() {
        return building;
    }

    public String getDescription() {
        return "Three Person Trip Bug";
    }

    public Simulator duplicate() {
        return new ThreePersonBugSimulator();
    }
}
