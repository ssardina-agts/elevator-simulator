package io.sarl.wrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.intranet.elevator.model.Car;

import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

import io.sarl.wrapper.action.ListenerThread;
import io.sarl.wrapper.event.EventTransmitter;

import org.intranet.elevator.model.operate.*;

/**
 * Entry point for controlling the simulation over a network.
 * Opens a connection to a client, transmits events,
 * and listens for actions before performing them.
 * 
 * @author Joshua Richards
 */
public class WrapperController implements Controller
{
	private WrapperModel model;

	@Override
	public void initialize(EventQueue eQ, Building building)
	{
		NetworkHelper connection;
		try
		{
			// TODO: add option to change port
			// TODO: show dialog while waiting for connection
			connection = new NetworkHelper(8081);
		}
		catch (IOException e)
		{
			// TODO: show error message on time out
			throw new RuntimeException(e);
		}
		model = new WrapperModel(eQ, building);
		
		// listen for events and transmit them to client
		eQ.addListener(new EventTransmitter(connection));
		// listen for actions from client and perform them
		new ListenerThread(connection, model).start();
	}
	
	@Override
	public void requestCar(Floor newFloor, org.intranet.elevator.model.operate.controller.Direction d)
	{
	}

	@Override
	public boolean arrive(Car car)
	{
		Direction d = model.getNextDirection(car.getId());
		
		if (d != Direction.NONE)
		{
			// register the car as not moving
			model.setNextDirection(car.getId(), Direction.NONE);
			return d == Direction.UP;
		}
		
		throw new RuntimeException(
				"arrive called for car that is not known to be moving. carId: " + car.getId()
		);
	}

	@Override
	public void setNextDestination(Car car)
	{
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
}