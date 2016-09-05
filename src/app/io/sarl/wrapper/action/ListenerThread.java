package io.sarl.wrapper.action;

import java.io.IOException;

import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

import io.sarl.wrapper.NetworkHelper;
import io.sarl.wrapper.WrapperModel;

public class ListenerThread extends Thread
{
	private NetworkHelper connection;
	private WrapperModel model;
	
	private boolean closed = false;

	public ListenerThread(NetworkHelper connection, WrapperModel model)
	{
		this.connection = connection;
		this.model = model;
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				JSONObject actionJson = connection.receive();
				doAction(actionJson);
			}
			catch (IOException e)
			{
				if (!closed)
				{
					throw new RuntimeException(e);
				}
			}

		}
	}
	
	private void doAction(JSONObject actionJson)
	{
		System.out.println(actionJson.toString(4));
		String type = actionJson.getString("type");
		int actionId = actionJson.getInt("id");
		JSONObject params = actionJson.getJSONObject("params");
		
		Action action;
		
		switch (type)
		{
			case "sendCar":
				action = new SendCarAction(actionId, model, params);
				break;
			case "changeNextDirection":
				action = new ChangeNextDirectionAction(actionId, model, params);
				break;
			default:
				action = new ErrorAction(
						actionId,
						model.getEventQueue(),
						"Invalid action type: " + type
				);
				break;
		}
		
		model.getEventQueue().addEvent(action);
	}
	
	public void close()
	{
		closed = true;
	}
}
