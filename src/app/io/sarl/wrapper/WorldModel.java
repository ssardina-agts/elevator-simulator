package io.sarl.wrapper;

import java.util.List;
import java.util.Map;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.json.JSONArray;
import org.json.JSONObject;

public class WorldModel 
{
	private Building baseModel;
	
	public WorldModel(Building model) 
	{
		baseModel = model;
	}
	
	public ModelChangePercept generateChangePercept() 
	{
		return new ModelChangePercept();
	}
	
	private class ModelChangePercept extends Percept 
	{
		
		@Override
		public String getName() 
		{
			return "modelChange";
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
				carJson.put("id", car.id);
				
				JSONArray servicedFloorsJsonArray = new JSONArray();
				List<Floor> servicedFloors = car.getFloorRequestPanel()
						.getServicedFloors();
				for (Floor floor : servicedFloors)
				{
					servicedFloorsJsonArray.put(floor.getFloorNumber());
				}
				
				carJson.put("servicedFloors", servicedFloors);
				carJson.put("capacity", car.getCapacity());
				
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