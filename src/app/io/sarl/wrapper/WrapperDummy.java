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
import org.intranet.elevator.model.operate.controller.Direction;
import org.intranet.elevator.model.operate.controller.MetaController;
import org.intranet.elevator.model.operate.controller.SimpleController;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.intranet.elevator.model.operate.*;

public class WrapperDummy implements Controller
{
	Map<Integer, Floor> floors;
	private Map<Integer, Car> cars;
	//whether each car is going up after it reaches its next destination
	private Map<Car, Boolean> carDirections = new HashMap<>();
	private Controller wrapped = new MetaController();
	
	private DataOutputStream out;
	private DataInputStream in;
	private Socket s;

	@Override
	public void initialize(EventQueue eQ, Building building)
	{
		wrapped.initialize(eQ, building);
		cars = new HashMap<>();
		for (Car car : building.getCars())
		{
			cars.put(car.id, car);
		}
		floors = new HashMap<>();
		for (Floor floor : building.getFloors())
		{
			floors.put(floor.getFloorNumber(), floor);
		}
		try
		{
			ServerSocket ss = new ServerSocket(8081);
			s = ss.accept();
			ss.close();
			out = new DataOutputStream(s.getOutputStream());
			in = new DataInputStream(s.getInputStream());
		}
		catch (IOException e)
		{
			//TODO: handle this gracefully
			throw new RuntimeException(e);
		}
		
		eQ.addListener(new EventQueue.Listener()
		{
			public void eventRemoved(Event e) {}
			public void eventError(Exception ex) {}
			public void eventAdded(Event e) {}

			public void eventProcessed(Event e)
			{
				JSONObject toTransmit = new JSONObject();
				toTransmit.put("type", e.getName());
				toTransmit.put("description", e.getDescription());
				toTransmit.put("time", e.getTime());
				toTransmit.put("id", e.getId());
				try
				{
					transmit(toTransmit.toString());
				}
				catch (IOException e1)
				{
					throw new RuntimeException(e1);
				}
			}
		});
		
		WorldModelSensor modelSensor = new WorldModelSensor(eQ, building);
		modelSensor.startPerceiving();
		
		new ListenThread(in).start();
	}

	private synchronized void transmit(String message) throws IOException
	{
		out.writeUTF(message);
	}
	@Override
	public void requestCar(Floor newFloor, Direction d)
	{
		//wrapped.requestCar(newFloor, d);
	}

	@Override
	public void addCar(Car car, float stoppingDistance)
	{
		//wrapped.addCar(car, stoppingDistance);
	}

	@Override
	public boolean arrive(Car car)
	{
		//return wrapped.arrive(car);
		return carDirections.remove(car);
	}

	@Override
	public void setNextDestination(Car car)
	{
		//wrapped.setNextDestination(car);
	}
	
	private void sendCar(JSONObject params)
	{
		int car = params.getInt("car");
		int floor = params.getInt("floor");
		String nextDirection = params.getString("nextDirection");
		
		if (carDirections.containsKey(
				cars.get(car)))
		{
			return;
		}
		cars.get(car).setDestination(floors.get(floor));
		carDirections.put(cars.get(car), (nextDirection.equals("up")));
	}

	public class ListenThread extends Thread
	{
		private DataInputStream in;

		public ListenThread(DataInputStream in)
		{
			this.in = in;
		}

		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					JSONObject actionJson = new JSONObject(in.readUTF());
					String name = actionJson.getString("type");
					switch (name)
					{
						case "sendCar":
							sendCar(actionJson.getJSONObject("params"));
							break;
					}
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

}
