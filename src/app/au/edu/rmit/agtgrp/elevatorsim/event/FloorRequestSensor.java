package au.edu.rmit.agtgrp.elevatorsim.event;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.FloorRequestPanel;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

/**
 * Listens for Floor requests and adds adds Percepts to the eventQueue
 * @author Matthew McNally
 */
public class FloorRequestSensor extends Sensor
{
	private Car car;
	private FloorRequestPanel floorRequestPanel;
	
	public FloorRequestSensor(Car car, EventQueue eventQueue)
	{
		super(eventQueue);
		this.car = car;
		floorRequestPanel = car.getFloorRequestPanel();
	}
	
	@Override
	public void startPerceiving() 
	{
		FloorRequestPanel.Listener listener = new FloorRequestPanel.Listener()
		{
			@Override
			public void floorRequested(Floor destinationFloor)
			{
				enqueuePercept(new FloorRequestPercept(destinationFloor));
			}
		};
		
		floorRequestPanel.addListener(listener);
	}
	
	private class FloorRequestPercept extends Percept
	{
		private Floor requestedFloor;
		
		public FloorRequestPercept(Floor requestedFloor)
		{
			super(perceptSequence);
			this.requestedFloor = requestedFloor;
		}
		
		@Override
		public String getName() 
		{
			return "floorRequested";
		}
		
		@Override
		public JSONObject getDescription()
		{
			JSONObject floorRequestJson = new JSONObject();
			floorRequestJson.put("car", car.getId());
			floorRequestJson.put("floor", requestedFloor.getFloorNumber());
			
			return floorRequestJson;
		}
	}
}
