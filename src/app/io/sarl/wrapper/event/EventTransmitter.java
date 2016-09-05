package io.sarl.wrapper.event;

import java.io.IOException;
import java.net.SocketException;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
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

	public EventTransmitter(NetworkHelper connection, Runnable onEnd)
	{
		this.connection = connection;
		this.onEnd = onEnd;
	}

	@Override
	public void eventProcessed(Event e)
	{
		// create the message
		JSONObject toTransmit = new JSONObject();
		toTransmit.put("type", e.getName());
		toTransmit.put("description", e.getDescription());
		toTransmit.put("time", e.getTime());
		toTransmit.put("id", e.getId());

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
		
		if (e.getName().equals("simulationEnded"))
		{
			onEnd.run();
		}
	}

	@Override
	public void eventAdded(Event e) {}

	@Override
	public void eventRemoved(Event e) {}

	@Override
	public void eventError(Exception ex) {}
}
