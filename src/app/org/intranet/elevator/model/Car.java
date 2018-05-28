/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import au.edu.rmit.agtgrp.elevatorsim.Transmittable;
import au.edu.rmit.agtgrp.elevatorsim.event.Percept;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * The states of Car are substates of MovableLocation:IDLE. Valid states:
 * <table border="1" cellspacing="0" cellpadding="2">
 * <tr>
 * <th rowspan="2">State</th>
 * <th colspan="2">Variables</th>
 * <th colspan="10">Transitions</th>
 * </tr>
 * <tr>
 * <th>destination</th>
 * <th>location</th>
 * <th>setDestination()</th>
 * <th>undock()</th>
 * <th>[MovableLocation.arrive()]</th>
 * </tr>
 * <tr>
 * <td>IDLE:UNDOCKED</td>
 * <td>null</td>
 * <td>null</td>
 * <td>MOVING or arrive(): DOCKED</td>
 * <td><i>Illegal</i></td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>MOVING</td>
 * <td>Set</td>
 * <td>null</td>
 * <td>MOVING or arrive(): DOCKED</td>
 * <td><i>Illegal</i></td>
 * <td>DOCKED<br/>
 * [docked()]</td>
 * </tr>
 * <tr>
 * <td>IDLE:UNDOCKING</td>
 * <td>Set</td>
 * <td>Set</td>
 * <td>UNDOCKING</td>
 * <td>MOVING</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>IDLE:DOCKED</td>
 * <td>null</td>
 * <td>Set</td>
 * <td>UNDOCKING</td>
 * <td>UNDOCKED</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * </table>
 *
 * @author Neil McKellar and Chris Dailey
 * @author Joshua Beale
 */
public final class Car extends MovableLocation {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private String name;
    private Floor location;
    private Floor destination;
    private FloorRequestPanel panel = new FloorRequestPanel();
    private List<Listener> listeners = new ArrayList<Listener>();
    private float stoppingDistance;
    private int id;

    public interface Listener {
        void docked();
    }

    private class ArrivalMessage implements Transmittable {
        private int dest;

        public ArrivalMessage() {
            dest = destination.getFloorNumber();
        }

        public String getName() {
            return "carArrived";
        }

        public JSONObject getDescription() {
            JSONObject ret = new JSONObject();
            ret.put("floor", dest);
            ret.put("car", id);
            return ret;
        }
    }

    private class MovementTracker implements MovableLocation.Listener {
        private float origin;
        private float dest;
        private PriorityQueue<Floor> floorsToPass;

        public MovementTracker(float origin, float dest) {
            this.origin = origin;
            this.dest = dest;
            if (origin == dest) {
                throw new IllegalArgumentException("no movement to track");
            }

            List<Floor> floorsInRange = new ArrayList<>(panel.getServicedFloors());
            floorsInRange.removeIf(f -> !floorInRange(f));
            if (floorsInRange.size() == 0) {
                floorsToPass = new PriorityQueue<>();
                return;
            }

            floorsToPass = new PriorityQueue<>(floorsInRange.size(), (floor1, floor2) ->
            {
                int ret = (int) (floor1.getHeight() - floor2.getHeight());
                return (origin < dest) ? ret : 0 - ret;
            });

            floorsToPass.addAll(floorsInRange);
        }

        private boolean floorInRange(Floor floor) {
            boolean ret = Math.min(origin, dest) < floor.getHeight() &&
                    Math.max(origin, dest) > floor.getHeight();
            return ret;
        }

        @Override
        public void heightChanged(float height) {
            Floor floorToPass = floorsToPass.peek();
            if (floorToPass == null) {
                Car.super.removeListener(this);
                return;
            }

            if (origin < dest) {
                if (height >= floorToPass.getHeight()) {
                    floorsToPass.poll();
                    fireFloorPassed(floorToPass);
                }
                return;
            }
            if (origin > dest) {
                if (height <= floorToPass.getHeight()) {
                    floorsToPass.poll();
                    fireFloorPassed(floorToPass);
                }
                return;
            }
        }

        private void fireFloorPassed(Floor passed) {
            eventQueue.addEvent(new Percept(eventQueue) {
                @Override
                public String getName() {
                    return "floorPassed";
                }

                @Override
                public JSONObject getDescription() {
                    JSONObject ret = new JSONObject();
                    ret.put("floor", passed.getFloorNumber());
                    ret.put("car", id);

                    return ret;
                }

            });
        }
    }

    public Car(EventQueue eQ, String name, float height, int capacity, int id, float stoppingDistance) {
        super(eQ, height, capacity);
        this.name = name;
        this.id = id;
        this.stoppingDistance = stoppingDistance;
    }

    /**
     * Travel to specified destination floor
     *
     * @param destination Floor to travel to
     */
    public void setDestination(Floor destination) {
        LOG.trace("{}.setDestination called for {}", this, destination, new Exception("stacktrace for this call"));
        this.destination = destination;
        if (location == null) {
            setDestinationHeight(destination.getHeight());
            if (destination.getHeight() != getHeight()) {
                super.addListener(new MovementTracker(getHeight(), destination.getHeight()));
            }
        }
    }

    /**
     * @param floor Destination
     * @return Time to travel to specified floor
     */
    public float getTravelTime(Floor floor) {
        float floorHeight = floor.getHeight();
        float travelDistance = floorHeight - getHeight();
        return getTravelTime(travelDistance);
    }

    /**
     * Undock from current location
     */
    public void undock() {
        if (location == null) throw new IllegalStateException("Must be docked to undock");

        location = null;
        if (destination != null) setDestinationHeight(destination.getHeight());
    }

    /**
     * @return Current destination floor
     */
    public Floor getDestination() {
        return destination;
    }

    /**
     * @return Current docked location floor
     */
    public Floor getLocation() {
        return location;
    }

    /**
     * @param listener
     */
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public float getStoppingDistance() {
        return stoppingDistance;
    }

    /**
     * @return FloorRequestPanel of Car
     */
    public FloorRequestPanel getFloorRequestPanel() {
        return panel;
    }

    /**
     * Get Floor of undocked, non-travelling Car
     * Really bad method? TODO re-factor me
     *
     * @return Floor at specified height attached to FloorRequestPanel
     */
    public Floor getFloorAt() {
        if (destination == null && location == null) return panel.getFloorAt(getHeight());
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.intranet.elevator.model.MovableLocation#getRatePerSecond()
     */
    public final float getRatePerSecond() {
        return (float) (1000 * 10.0 / 4030.0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.intranet.elevator.model.MovableLocation#arrive()
     */
    protected void arrive() {
        LOG.debug("I am {} and I have arrived at {}", this, destination);
        location = destination;
        destination = null;
        panel.requestFulfilled(location);
        fireDockedEvent();
    }

    /**
     * Notify Listeners that Car has docked to destination Location
     */
    private void fireDockedEvent() {
        for (Listener l : new ArrayList<Listener>(listeners))
            l.docked();
    }

    public Transmittable getArrivalMessage() {
        return new ArrivalMessage();
    }

    @Override
    public String toString() {
        return "Car[" + name + "]-" + id + "@" + location + "_TO_" + destination;
    }
}
