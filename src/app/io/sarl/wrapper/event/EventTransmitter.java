package io.sarl.wrapper.event;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.json.JSONArray;
import org.json.JSONObject;

import io.sarl.wrapper.NetworkHelper;

/**
 * EventQueue listener that transmits json representations of Events
 * as they are processed
 * @author Joshua Richards
 */
public class EventTransmitter implements EventQueue.Listener, NetworkHelper.Listener
{
	private NetworkHelper connection;
	private EventQueue eventQueue;
	
	private List<Listener> listeners = new ArrayList<>();
	
	private Map<Long, Event> unprocessedEvents = new HashMap<>();

	public EventTransmitter(NetworkHelper connection, EventQueue eventQueue)
	{
		this.connection = connection;
		this.eventQueue = eventQueue;
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
		eventQueue.waitForEvents();
	}
	
	@Override
	public void simulationEnded()
	{
		eventProcessed(new Percept(eventQueue)
		{
			@Override
			public String getName()
			{
				return "simulationEnded";
			}

			@Override
			public JSONObject getDescription()
			{
				return new JSONObject();
			}
	
		});
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
	
	public void onEventProcessed(long id)
	{
		Event e = unprocessedEvents.remove(id);
		if (unprocessedEvents.size() == 0)
		{
			eventQueue.stopWaitingForEvents();
		}
		if (e != null && e.getName().equals("simulationEnded"))
		{
			for (Listener l : listeners)
			{
				l.onEnd();
			}
		}
	}
	
	@Override
	public void onReconnect()
	{
		eventProcessed(new ReconnectPercept(eventQueue));
	}
	
	@Override
	public void onConnectionClosed()
	{
		// end the simulation
		eventQueue.stopWaitingForEvents();
		for (Event e : new ArrayList<Event>(eventQueue.getEventList()))
		{
			eventQueue.removeEvent(e);
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
