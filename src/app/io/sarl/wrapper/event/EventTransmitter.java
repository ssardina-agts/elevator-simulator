package io.sarl.wrapper.event;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
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
public class EventTransmitter implements EventQueue.Listener
{
	private NetworkHelper connection;
	private Runnable onEnd;
	private EventQueue eventQueue;
	
	private Map<Long, Event> unprocessedEvents = new HashMap<>();

	public EventTransmitter(NetworkHelper connection, Runnable onEnd, EventQueue eventQueue)
	{
		this.connection = connection;
		this.onEnd = onEnd;
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
			onEnd.run();
		}
	}
	
	public void retransmitUnprocessedEvents()
	{
		eventProcessed(new ReconnectPercept(eventQueue));
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
			return "reconnect";
		}

		@Override
		public JSONObject getDescription()
		{
			JSONObject ret = new JSONObject();
			ret.put("time", eventQueue.getCurrentTime());
			JSONArray unprocessedEventsJson = new JSONArray();
			for (Event event : unprocessedEvents.values())
			{
				unprocessedEventsJson.put(makeEventJson(event));
			}
			ret.put("unprocessedEvents", unprocessedEventsJson);
			return ret;
		}
		
	}

	@Override
	public void eventAdded(Event e) {}

	@Override
	public void eventRemoved(Event e) {}

	@Override
	public void eventError(Exception ex) {}
}
