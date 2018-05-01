package au.edu.rmit.agtgrp.elevatorsim;

import au.edu.rmit.agtgrp.elevatorsim.action.Action;
import au.edu.rmit.agtgrp.elevatorsim.action.ListenerThread;
import au.edu.rmit.agtgrp.elevatorsim.event.EventTransmitter;
import au.edu.rmit.agtgrp.elevatorsim.ui.ControllerDialogCreator;
import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Entry point for controlling the simulation over a network.
 * Opens a connection to a client, transmits events,
 * and listens for actions before performing them.
 *
 * @author Joshua Richards
 */
public class NetworkWrapperController implements Controller, EventTransmitter.Listener {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkWrapperController.class.getSimpleName());
    private WrapperModel model;
    private NetworkHelper connection;
    private ListenerThread listenerThread;
    private EventTransmitter eventTransmitter;

    @Override
    public void initialize(EventQueue eQ, Building building) {
        onEnd();
        eQ.waitForEvents();
        try {
            // TODO: add option to change port
            connection = new NetworkHelper(ElsimSettings.get().getPort());
        } catch (IOException e) {
            // TODO: show error message on time out
            throw new RuntimeException(e);
        }
        model = new WrapperModel(eQ, building);

        // listen for events and transmit them to client
        eventTransmitter = new EventTransmitter(connection, model);
        eventTransmitter.addListener(this);
        eQ.addListener(eventTransmitter);
        connection.addListener(eventTransmitter);
        // listen for actions from client and perform them
        listenerThread = new ListenerThread(connection, model);
        listenerThread.setMessageHandler("eventProcessed", (JSONObject message) ->
        {
            eventTransmitter.onEventProcessedByClient(message.getLong("id"));
        });
        listenerThread.setMessageHandler("heartbeat", (JSONObject message) ->
        {
            Action heartbeatAction = new Action(message.getLong("id"), eQ) {
                @Override
                protected ProcessingStatus performAction() {
                    return ProcessingStatus.COMPLETED;
                }

            };
            // can't use eventqueue in case where simulation is paused
            heartbeatAction.perform();
            eventTransmitter.eventProcessed(heartbeatAction);
        });
        listenerThread.start();
    }

    @Override
    public void onEnd() {
        close();
    }

    @Override
    public void close() {
        if (listenerThread != null) {
            listenerThread.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void requestCar(Floor newFloor, org.intranet.elevator.model.operate.controller.Direction d) {
    }

    @Override
    public boolean arrive(Car car) {
        Direction d = model.getNextDirection(car.getId());

        if (d != Direction.NONE) {
            // register the car as not moving
            model.setNextDirection(car.getId(), Direction.NONE);
            return d == Direction.UP;
        }

        throw new RuntimeException(
                "arrive called for car that is not known to be moving. carId: " + car.getId()
        );
    }

    @Override
    public void setNextDestination(Car car) {
        LOG.debug("Shouldn't I be setting the next destination of my {} here?", car);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void setControllerDialogCreator(ControllerDialogCreator cdc) {
        connection.setControllerDialogCreator(cdc);
    }

    @Override
    public String getInitMessage() {
        return "Waiting for connection on port " + ElsimSettings.get().getPort();
    }
}
