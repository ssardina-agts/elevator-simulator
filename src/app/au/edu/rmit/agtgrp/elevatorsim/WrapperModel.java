package au.edu.rmit.agtgrp.elevatorsim;

import au.edu.rmit.agtgrp.elevatorsim.event.FloorRequestSensor;
import au.edu.rmit.agtgrp.elevatorsim.event.WorldModelSensor;
import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.sim.event.EventQueue;
import org.intranet.statistics.Table;

import java.util.*;

/**
 * Holds all model information that needs to be accessed by wrapper components.
 * Manages sensors
 * @author Joshua Richards
 */
public class WrapperModel
{
	private Building building;
	// using Maps in case ids aren't sequential
	private Map<Integer, Floor> floors;
	private Map<Integer, Car> cars;
	// the next direction each car will travel after it arrives
	// at its current destination.
	// Direction.NONE value indicates the car is not moving
	private Map<Integer, Direction> nextDirections;
	
	private EventQueue eventQueue;

	/**
	 * Initializes internal model representation, creates sensors
	 * @param eQ The simulation's EventQueue
	 * @param building The Building that was passed to WrapperController
	 */
	public WrapperModel(EventQueue eQ, Building building)
	{
		this.building = building;
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
			// listens for floor requests and adds percepts to the EventQueue
			FloorRequestSensor sensor = new FloorRequestSensor(car, eQ);
			sensor.startPerceiving();
			nextDirections.put(car.getId(), Direction.NONE);
		}
		
		eventQueue = eQ;
		
		// transmits the initial state of the model
		WorldModelSensor sensor = new WorldModelSensor(eQ, building);
		sensor.startPerceiving();
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
	
	public List<Table> getStatistics()
	{
		return building.getStatistics();
	}
	
	public long getSeed()
	{
		return building.getSeed();
	}
}
