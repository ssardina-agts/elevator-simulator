package io.sarl.wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

import io.sarl.wrapper.event.EventTransmitter;
import io.sarl.wrapper.event.FloorRequestSensor;
import io.sarl.wrapper.event.WorldModelSensor;

public class WrapperModel
{
	private Map<Integer, Floor> floors;
	private Map<Integer, Car> cars;
	private Map<Integer, Direction> nextDirections;
	
	private EventQueue eventQueue;

	private NetworkHelper connection;

	public WrapperModel(EventQueue eQ, Building building, NetworkHelper connection)
	{
		floors = new HashMap<>();
		for (Floor floor : building.getFloors())
		{
			floors.put(floor.getFloorNumber(), floor);
		}
		
		cars = new HashMap<>();
		nextDirections = new HashMap<>();
		for (Car car : building.getCars())
		{
			cars.put(car.getId(), car);
			FloorRequestSensor sensor = new FloorRequestSensor(car, eQ);
			sensor.startPerceiving();
			nextDirections.put(car.getId(), Direction.NONE);
		}
		
		this.connection = connection;
		eventQueue = eQ;
		
		WorldModelSensor sensor = new WorldModelSensor(eQ, building);
		sensor.startPerceiving();

		eQ.addListener(new EventTransmitter(connection));
	}
	
	public Floor getFloor(int floorId)
	{
		return (floors.containsKey(floorId)) ? floors.get(floorId) : null;
	}
	
	public Car getCar(int carId)
	{
		return (cars.containsKey(carId)) ? cars.get(carId) : null;
	}
	
	public Collection<Floor> getAllFloors()
	{
		return new ArrayList<>(floors.values());
	}
	
	public Collection<Car> getAllCars()
	{
		return new ArrayList<>(cars.values());
	}
	
	public Direction getNextDirection(int carId)
	{
		return nextDirections.get(carId);
	}
	
	public void setNextDirection(int carId, Direction direction)
	{
		nextDirections.put(carId, direction);
	}
	
	public EventQueue getEventQueue()
	{
		return eventQueue;
	}
}
