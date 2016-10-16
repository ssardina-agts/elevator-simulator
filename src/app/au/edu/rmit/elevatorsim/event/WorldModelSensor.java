package au.edu.rmit.elevatorsim.event;

import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.sim.event.EventQueue;
import org.json.JSONArray;
import org.json.JSONObject;

public class WorldModelSensor extends Sensor
{
	private Building baseModel;
	
	public WorldModelSensor(EventQueue eventQueue, Building model) 
	{
		super(eventQueue);
		baseModel = model;
	}
	
	@Override
	public void startPerceiving() 
	{
		enqueuePercept(new ModelChangePercept());
	}
	
	/**
	 * Represents the current state of the entire model.
	 * Must be transmitted at the beginning of the simulation before any
	 * other Event so a client can initialize its model.
	 * May also be transmitted periodically over the course of the simulation
	 * but this behaviour is not currently implemented.
	 * @author Joshua Richards
	 * @author Matthew McNally
	 *
	 */
	private class ModelChangePercept extends Percept 
	{
		public ModelChangePercept()
		{
			super(perceptSequence);
		}

		@Override
		public String getName() 
		{
			return "modelChanged";
		}
		
		@Override
		public JSONObject getDescription()
		{
			JSONObject jsonWorldModel = new JSONObject();
		
			JSONArray carsJsonArray = new JSONArray();
			List<Car> cars = baseModel.getCars();
			
			for (Car car : cars)
			{
				JSONObject carJson = new JSONObject();
				carJson.put("id", car.getId());
				
				JSONArray servicedFloorsJsonArray = new JSONArray();
				List<Floor> servicedFloors = car.getFloorRequestPanel()
						.getServicedFloors();
				for (Floor floor : servicedFloors)
				{
					servicedFloorsJsonArray.put(floor.getFloorNumber());
				}
				
				carJson.put("servicedFloors", servicedFloorsJsonArray);
				carJson.put("capacity", car.getCapacity());
				// can be inferred in the initial state
				carJson.put("occupants", car.getOccupants().size());
				carJson.put("currentHeight", car.getHeight());
				
				carsJsonArray.put(carJson);
			}
			
			jsonWorldModel.put("cars", carsJsonArray);
			
			JSONArray floorsJsonArray = new JSONArray();
			List<Floor> floors = baseModel.getFloors();
			
			for (Floor floor : floors)
			{
				JSONObject floorJson = new JSONObject();
				floorJson.put("id", floor.getFloorNumber());
				floorJson.put("height", floor.getHeight());
				
				floorsJsonArray.put(floorJson);
			}
			
			jsonWorldModel.put("floors", floorsJsonArray);
			return jsonWorldModel;
		}
	}
}