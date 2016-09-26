package io.sarl.wrapper.event;

import java.io.IOException;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

import io.sarl.wrapper.NetworkHelper;
import io.sarl.wrapper.WrapperModel;

/**
 * EventQueue listener that transmits json representations of Events
 * as they are processed
 * @author Joshua Richards
 */
public class EventTransmitter implements EventQueue.Listener
{
	WrapperModel model;
	NetworkHelper connection;

	public EventTransmitter(NetworkHelper connection, WrapperModel model)
	{
		this.connection = connection;
		this.model = model;
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
