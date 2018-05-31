/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import au.edu.rmit.agtgrp.elevatorsim.SimulatorParams;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.Person;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.Model;
import org.intranet.sim.Simulator;
import org.intranet.sim.event.Event;
import org.intranet.ui.FloatParameter;
import org.intranet.ui.IntegerParameter;
import org.intranet.ui.LongParameter;

import java.util.Random;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class EveningTrafficElevatorSimulator
        extends Simulator {
    private IntegerParameter floorsParameter;
    private IntegerParameter carsParameter;
    private IntegerParameter ridersParameter;
    private FloatParameter   durationParameter;
    private IntegerParameter stdDeviationParameter;

    private Building building;

    public EveningTrafficElevatorSimulator() {
        super();
    }

    @Override
    protected void initialiseParameters() {
        if (SimulatorParams.instance().isValid()) {
            SimulatorParams params = SimulatorParams.instance();
            floorsParameter = new IntegerParameter("Number of Floors", params.getParamValueInt("floors"));
            carsParameter = new IntegerParameter("Number of Elevators", params.getParamValueInt("cars"));
            ridersParameter = new IntegerParameter("Number of People per Floor", params.getParamValueInt("riders"));
            durationParameter = new FloatParameter("Rider insertion time (hours)", params.getParamValueLong("duration"));
            stdDeviationParameter = new IntegerParameter("Standard Deviation", params.getParamValueInt("stdDeviation"));
            seedParameter = new LongParameter("Random seed", params.getParamValueLong("seed"));
        } else {
            floorsParameter = new IntegerParameter("Number of Floors", 10);
            carsParameter = new IntegerParameter("Number of Elevators", 3);
            ridersParameter = new IntegerParameter("Number of People per Floor", 20);
            durationParameter = new FloatParameter("Rider insertion time (hours)", 1.0f);
            stdDeviationParameter = new IntegerParameter("Standard Deviation", 1);
            seedParameter = new LongParameter("Random seed", 635359);
        }

        parameters.add(floorsParameter);
        parameters.add(carsParameter);
        parameters.add(ridersParameter);
        parameters.add(durationParameter);
        parameters.add(stdDeviationParameter);
        parameters.add(seedParameter);

        addControllerParameter();
    }

    @Override
    public void initializeModel() {
        int numFloors = floorsParameter.getIntegerValue();
        int numCars = carsParameter.getIntegerValue();
        int numRiders = ridersParameter.getIntegerValue();
        float duration = durationParameter.getFloatValue();
        int stdDeviation = stdDeviationParameter.getIntegerValue();
        long seed = seedParameter.getLongValue();

        Controller controller = getController();
        building = new Building(getEventQueue(), numFloors, numCars, controller, seedParameter.getLongValue());
        // destination floor is the ground floor
        final Floor destFloor = building.getFloor(0);
        destFloor.setCapacity(Integer.MAX_VALUE);

        Random rand = new Random(seed);

        for (int i = 1; i < numFloors; i++) {
            Floor startingFloor = building.getFloor(i);
            for (int j = 0; j < numRiders; j++) {
                final Person person = building.createPerson(startingFloor, j);
                // time to insert
                // Convert a gaussian[-1, 1] to a gaussian[0, 1]
                float gaussian = (getGaussian(rand, stdDeviation) + 1) / 2;
                // Apply gaussian value to the duration (in hours)
                // and convert to milliseconds
                long insertTime = (long) (gaussian * duration * 3600 * 1000);

                // insertion event for destination at time
                Event event = new CarRequestEvent(insertTime, person, startingFloor, destFloor);
                getEventQueue().addEvent(event);
            }
        }
    }

    public final Model getModel() {
        return building;
    }

    public String getDescription() {
        return "Evening Traffic Rider Insertion";
    }

    public Simulator duplicate() {
        return new EveningTrafficElevatorSimulator();
    }

    private static float getGaussian(Random rand, int stddev) {
        while (true) {
            float gaussian = (float) (rand.nextGaussian() / stddev);
            if (gaussian >= -1.0f && gaussian <= 1.0f) return gaussian;
        }
    }
}
