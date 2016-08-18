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
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

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
	Map<Integer, Floor> floors;
	private Map<Integer, Car> cars;
	// whether each car is going up after it reaches its next destination
	// a mapping only exists if a car has been sent a floor and has not yet arrived
	private Map<Car, Boolean> carDirections = new HashMap<>();
	
	private DataOutputStream out;
	private DataInputStream in;
	private Socket s;
	private EventQueue eventQueue;

	@Override
	public void initialize(EventQueue eQ, Building building)
	{
		this.eventQueue = eQ;
		initCars(building.getCars());
		initFloors(building.getFloors());
		try
		{
			// create connection
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
				// transmit every event as it's processed
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
		
		// transmit initial model representation to client
		WorldModelSensor modelSensor = new WorldModelSensor(eQ, building);
		modelSensor.startPerceiving();
		
		new ListenThread(in).start();
	}
	
	/**
	 * set up cars map
	 * @param carList cars list from building
	 */
	private void initCars(List<Car> carList)
	{
		cars = new HashMap<>();
		for (Car car : carList)
		{
			cars.put(car.getId(), car);
			FloorRequestSensor sensor = new FloorRequestSensor(car, eventQueue);
			sensor.startPerceiving();
		}
		
	}
	
	/**
	 * set up floors map
	 * @param floorList floor list from building
	 */
	private void initFloors(List<Floor> floorList)
	{
		floors = new HashMap<>();
		for (Floor floor : floorList)
		{
			floors.put(floor.getFloorNumber(), floor);
		}
	}

	/**
	 * Transmit a message to the client
	 * All transmission to the client should be through this method
	 * @param message the message to send
	 * @throws IOException if there is a connection problem
	 */
	private void transmit(String message) throws IOException
	{
		synchronized(out)
		{
			out.writeUTF(message);
		}
	}
	@Override
	public void requestCar(Floor newFloor, Direction d)
	{
	}

	@Override
	public boolean arrive(Car car)
	{
		return carDirections.remove(car);
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
	
	@Deprecated // replace with Action subclass
	private boolean sendCar(JSONObject params)
	{
		int car = params.getInt("car");
		int floor = params.getInt("floor");
		String nextDirection = params.getString("nextDirection");
		
		if (carDirections.containsKey(cars.get(car)))
		{
			return false;
		}

		cars.get(car).setDestination(floors.get(floor));
		carDirections.put(cars.get(car), (nextDirection.equals("up")));
		return true;
	}
	
	@Deprecated // replace with Action subclass
	private boolean changeNextDirection(JSONObject params)
	{
		Car car = cars.get(params.getInt("car"));

		if (!carDirections.containsKey(car))
		{
			return false;
		}

		carDirections.put(car, params.getString("direction").equals("up"));
		return true;
	}

	/**
	 * Listens for actions from client, performs them and sends responses
	 * 
	 * @author Joshua Richards
	 */
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
					boolean success = false;
					switch (name)
					{
						case "sendCar":
							success = sendCar(actionJson.getJSONObject("params"));
							break;
						case "changeNextDirection":
							success = changeNextDirection(actionJson.getJSONObject("params"));
							break;
						default:
							success = false;
							break;
					}
					
					eventQueue.addEvent(new ActionResponse(eventQueue, actionJson.getInt("id"), success));
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

}
