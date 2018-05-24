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
    private int floorId;
    private Direction nextDirection;

    public SendCarAction(long actionId, WrapperModel model, JSONObject params) {
        super(actionId, model.getEventQueue());
        this.model = model;
        this.carId = params.getInt("car");
        this.floorId = params.getInt("floor");

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
        ProcessingStatus status = ProcessingStatus.FAILED;
        Floor floor = model.getFloor(floorId);
        Car car = model.getCar(carId);

        LOG.debug("Performing the action for SendCarAction for car {} to floor {}" +
                " towards {}", carId, floorId, nextDirection.toString());
        if (car == null) {
            LOG.error("SendCarAction is missing the car!");
            failureReason = "No car with id: " + carId;
            status = ProcessingStatus.FAILED;
        } else if (floor == null) {
            LOG.error("SendCarAction is missing the floor!");
            failureReason = "No floor with id: " + floorId;
            status = ProcessingStatus.FAILED;
        } else if (nextDirection == Direction.NONE) {
            LOG.error("SendCarAction is missing the next towards direction!");
            failureReason = "Invalid value for 'nextDirection' parameter. Valid values are 'up' and 'down'";
            status = ProcessingStatus.FAILED;
        } else {
            if (model.getCar(carId).getDestination() == null  || LegalChangeDestination()) {
                LOG.debug("SendCarAction about to set the next direction to {}", nextDirection.toString());
                model.setNextDirection(carId, nextDirection);
                car.setDestination(floor);
                status = ProcessingStatus.IN_PROGRESS;
            } else {
                LOG.error("Cannot send car, requires illegal change of destination to {} in direction {}", floor, nextDirection);
            }
        }
        return status;
    }

    private boolean LegalChangeDestination() {
        Floor floor = model.getFloor(floorId);
        Car car = model.getCar(carId);

        LOG.debug("Call changeDestination: {}", car);
        LOG.debug("Current Destination is: {}", car.getDestination());
        LOG.debug("    New Destination is: {}", floor);
        LOG.trace("Call hierarchy for this method:", new Exception());

        if (car.getDestination() == null ) {
            return true;
        }

        float currentHeight = car.getHeight();
        float lastDestHeight = car.getDestination().getHeight();
        float newDestHeight = floor.getHeight();
        Direction currDirection = (currentHeight < lastDestHeight) ? Direction.UP : Direction.DOWN;

            // TODO: this is very tight, we need to giv emore space because it can be OK now but the elevator is traveling fast!
        if (currDirection == Direction.UP) {
            if (newDestHeight < currentHeight) {
                failureReason = "Car " + carId + " already past floor " + floorId;
                return false;
            }
        } else {
            if (newDestHeight > currentHeight) {
                failureReason = "Car " + carId + " already past floor " + floorId;
                return false;
            }
        }

        return true;
    }



    /**
     * Called by performAction if the car is already in transit
     *
     * @return the value that should be returned by performAction
     */
    private ProcessingStatus changeDestination() {
        Floor floor = model.getFloor(floorId);
        Car car = model.getCar(carId);

        LOG.debug("Call changeDestination: {}", car);
        LOG.debug("Current Destination is: {}", car.getDestination());
        LOG.debug("    New Destination is: {}", floor);
        LOG.trace("Call hierarchy for this method:", new Exception());

        float currentHeight = car.getHeight();
        float lastDestHeight = car.getDestination().getHeight();
        float newDestHeight = floor.getHeight();
        Direction currDirection = (currentHeight < lastDestHeight) ?
                Direction.UP : Direction.DOWN;

        // TODO: this is very tight, we need to giv emore space because it can be OK now but the elevator is traveling fast!
        if (currDirection == Direction.UP) {
            if (newDestHeight < currentHeight) {
                failureReason = "Car " + carId + " already past floor " + floorId;
                return ProcessingStatus.FAILED;
            }
        } else {
            if (newDestHeight > currentHeight) {
                failureReason = "Car " + carId + " already past floor " + floorId;
                return ProcessingStatus.FAILED;
            }
        }

        return ProcessingStatus.IN_PROGRESS;
    }

}
