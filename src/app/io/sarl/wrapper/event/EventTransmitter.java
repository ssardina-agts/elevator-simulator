package io.sarl.wrapper.event;

import java.io.IOException;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.event.EventQueue.Listener;
import org.json.JSONObject;

import io.sarl.wrapper.NetworkHelper;

public class EventTransmitter implements Listener
{
	NetworkHelper connection;

	public EventTransmitter(NetworkHelper connection)
	{
		this.connection = connection;
	}

	@Override
	public void eventProcessed(Event e)
	{
		JSONObject toTransmit = new JSONObject();
		toTransmit.put("type", e.getName());
		toTransmit.put("description", e.getDescription());
		toTransmit.put("time", e.getTime());
		toTransmit.put("id", e.getId());

		try
		{
			connection.transmit(toTransmit);
		}
		catch (IOException e1)
		{
			// TODO: handle this gracefully
			throw new RuntimeException(e1);
		}
	}

	@Override
	public void eventAdded(Event e) {}

	@Override
	public void eventRemoved(Event e) {}

	@Override
	public void eventError(Exception ex) {}
}
