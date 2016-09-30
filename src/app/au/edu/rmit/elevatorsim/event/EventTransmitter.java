package au.edu.rmit.elevatorsim.event;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.statistics.Column;
import org.intranet.statistics.Table;
import org.json.JSONArray;
import org.json.JSONObject;

import au.edu.rmit.elevatorsim.NetworkHelper;
import au.edu.rmit.elevatorsim.WrapperModel;

/**
 * EventQueue listener that transmits json representations of Events
 * as they are processed
 * @author Joshua Richards
 */
public class EventTransmitter implements EventQueue.Listener, NetworkHelper.Listener
{
	private NetworkHelper connection;
	private WrapperModel model;
	
	private List<Listener> listeners = new ArrayList<>();
	
	private Map<Long, Event> unprocessedEvents = new ConcurrentHashMap<>();

	public EventTransmitter(NetworkHelper connection, WrapperModel model)
	{
		this.connection = connection;
		this.model = model;
	}

	@Override
	public void eventProcessed(Event e)
	{
		// create the message
		JSONObject toTransmit = makeEventJson(e);

		try
		{
			// send message
			connection.transmit(toTransmit);
		}
		catch (SocketException e1)
		{
			System.err.println("event processed after simulation end: " + toTransmit.toString(4));
			throw new RuntimeException(e1);
		}
		catch (IOException e1)
		{
			// TODO: handle this gracefully
			throw new RuntimeException(e1);
		}
		
		unprocessedEvents.put(e.getId(), e);
		model.getEventQueue().waitForEvents();
	}
	
	@Override
	public void simulationEnded()
	{
		eventProcessed(new SimulationEndedPercept());
	}
	
	private JSONObject makeEventJson(Event e)
	{
		JSONObject ret = new JSONObject();
		ret.put("type", e.getName());
		ret.put("description", e.getDescription());
		ret.put("time", e.getTime());
		ret.put("id", e.getId());
		return ret;
	}
	
	public void onEventProcessedByClient(long id)
	{
		Event e;
		synchronized (unprocessedEvents)
		{
			e = unprocessedEvents.remove(id);
		}
		if (e == null)
		{
			System.err.println("Clients reports event " + id +
					" processed but server has no record");
			return;
		}
		if (unprocessedEvents.size() == 0)
		{
			model.getEventQueue().stopWaitingForEvents();
		}
		if (e.getName().equals("simulationEnded"))
		{
			model.getEventQueue().stopWaitingForEvents();
			for (Listener l : listeners)
			{
				l.onEnd();
			}
		}
	}
	
	@Override
	public void onReconnect()
	{
		eventProcessed(new ReconnectPercept(model.getEventQueue()));
	}
	
	@Override
	public void onConnectionClosed()
	{
		// end the simulation
		model.getEventQueue().stopWaitingForEvents();
		for (Event e : new ArrayList<Event>(model.getEventQueue().getEventList()))
		{
			model.getEventQueue().removeEvent(e);
		}
		for (Listener l : listeners)
		{
			l.onEnd();
		}
	}
	
	private class ReconnectPercept extends Percept
	{
		public ReconnectPercept(EventQueue eq)
		{
			super(eq);
		}

		@Override
		public String getName()
		{
			return "reconnected";
		}


		@Override
		public JSONObject getDescription()
		{
			JSONObject ret = new JSONObject();
			JSONArray unprocessedEventsJson = new JSONArray();
			for (Event event : unprocessedEvents.values())
			{
				unprocessedEventsJson.put(makeEventJson(event));
			}
			ret.put("unprocessedEvents", unprocessedEventsJson);
			return ret;
		}
	}
	
	private class SimulationEndedPercept extends Percept
	{

		public SimulationEndedPercept()
		{
			super(model.getEventQueue());
		}

		@Override
		public String getName()
		{
			return "simulationEnded";
		}

		@Override
		public JSONObject getDescription()
		{
			List<Table> statistics = model.getStatistics();
			JSONObject ret = new JSONObject();
			
			for (Table table : statistics)
			{
				JSONObject tableJson = new JSONObject();
				for (int i = 0; i < table.getColumnCount(); i++)
				{
					Column column = table.getColumn(i);
					JSONArray columnJson = new JSONArray();
					
					for (int j = 0; j < column.getValueCount(); j++)
					{
						columnJson.put(column.getValue(j));
					}
					tableJson.put(column.getHeading(), columnJson);
				}
				ret.put(table.getName(), tableJson);
			}

			return ret;
		}
		
	}

	public void addListener(Listener l)
	{
		listeners.add(l);
	}
	
	public void removeListener(Listener l)
	{
		listeners.remove(l);
	}

	public interface Listener
	{
		public void onEnd();
	}

	// unused methods from EventQueue.Listener
	@Override
	public void eventAdded(Event e) {}

	@Override
	public void eventRemoved(Event e) {}

	@Override
	public void eventError(Exception ex) {}
}
