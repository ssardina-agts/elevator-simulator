package au.edu.rmit.agtgrp.elevatorsim.action;

import au.edu.rmit.agtgrp.elevatorsim.Direction;
import au.edu.rmit.agtgrp.elevatorsim.WrapperModel;
import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Performs the 'sendCar' action
 *
 * @author Joshua Richards
 */
public class SendCarAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
    private WrapperModel model;
    private int carId;
    private Car car;
    private int floorId;
    private Floor floor;
    private Direction nextDirection;

    public SendCarAction(long actionId, WrapperModel model, JSONObject params) {
        super(actionId, model.getEventQueue());
        this.model = model;
        this.carId = params.getInt("car");
        this.floorId = params.getInt("floor");

        this.floor = model.getFloor(floorId);
        this.car = model.getCar(carId);

        String nextDirectionParam = params.getString("nextDirection");

        switch (nextDirectionParam) {
            case "up":
                this.nextDirection = Direction.UP;
                break;
            case "down":
                this.nextDirection = Direction.DOWN;
                break;
            default:
                // this causes performAction to return FAILED
                this.nextDirection = Direction.NONE;
                break;
        }
    }

    @Override
    protected ProcessingStatus performAction() {
        LOG.info("Sending car {} to floor {} then {}", carId, floorId, nextDirection);

        ProcessingStatus status = ProcessingStatus.FAILED;

        if (car == null) {
            failureReason = "No car with id: " + carId;
        } else if (floor == null) {
            failureReason = "No floor with id: " + floorId;
        } else if (nextDirection == Direction.NONE) {
            failureReason = "Next direction cannot be " + nextDirection;
        } else if (canChangeDestination()) {
            LOG.debug("About to set next destination to {} in direction {}", floor, nextDirection);
            model.setNextDirection(carId, nextDirection);
            car.setDestination(floor);
            status = ProcessingStatus.COMPLETED;
        } else {
            failureReason = "Illegal change in destination. Cannot send to " + floor;
        }

        LOG.debug("Processing Status: {}", status);
        if (status == ProcessingStatus.FAILED) {
            LOG.error(failureReason);
        }

        return status;
    }

    /**
     * Checks whether the destination can be changed for this action request.
     * For a given elevator (car), destination can be changed if the request for change is in the direction of current
     * travel of this car and it can safely stop.
     *
     * @return true if destination can be changed, false otherwise
     */
    private boolean canChangeDestination() {
        LOG.debug("Change destination check: {} to {}", car, floor);
        LOG.trace("Call hierarchy for this method:", new Exception());

        // Early exit: If the car wasn't moving, then we can simply send it to where it's been requested
        if (car.getDestination() == null) {
            LOG.debug("Change destination is allowed");
            return true;
        }

        float currentHeight = car.getHeight();
        float currentDestinationHeight = car.getDestination().getHeight();
        float stoppingDistance = car.getStoppingDistance();

        Direction currDirection = (currentHeight < currentDestinationHeight) ? Direction.UP : Direction.DOWN;

        float requestedDestinationHeight = floor.getHeight();

        // Too late to change to desired destination; elevator has gone past the requested floor or not within
        // safe stopping distance
        if ((currDirection == Direction.UP && requestedDestinationHeight - currentHeight < stoppingDistance) ||
                (currDirection == Direction.DOWN && currentHeight - requestedDestinationHeight < stoppingDistance)) {
            LOG.error("Change destination check failed: {} is past {}", car, floor);
            return false;
        }

        LOG.debug("Change destination is allowed");
        return true;
    }

}
