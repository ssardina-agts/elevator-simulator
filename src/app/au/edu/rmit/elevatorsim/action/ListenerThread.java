package au.edu.rmit.elevatorsim.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.intranet.sim.event.EventQueue;
import org.json.JSONArray;
import org.json.JSONObject;

import au.edu.rmit.elevatorsim.NetworkHelper;
import au.edu.rmit.elevatorsim.WrapperModel;

public class ListenerThread extends Thread
{
	private NetworkHelper connection;
	private WrapperModel model;
	
	private boolean closed = false;

	private Set<Integer> processedActions = new HashSet<>();
	private Map<String, MessageHandler> specialTypes = new HashMap<>();

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

		MessageHandler handler = specialTypes.get(type);
		if (handler != null)
		{
			handler.handleMessage(actionJson);
			return;
		}

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
			case "reconnected":
				action = new ReconnectedAction(actionId, model, params);
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
	
	/**
	 * Used to signify that a certain type of message is special and
	 * should not be treated as an action
	 * @param type the type field of the messages that should be handled
	 * @param handler a listener that will be called when a matching message
	 * is received
	 */
	public void setMessageHandler(String type, MessageHandler handler)
	{
		specialTypes.put(type, handler);
	}
	
	public void close()
	{
		closed = true;
	}
	
	/**
	 * Listener that should be associated with a special type of message
	 * that should be handled as an action
	 * @author Joshua Richards
	 *
	 */
	public interface MessageHandler
	{
		public void handleMessage(JSONObject message);
	}
	
	private class ReconnectedAction extends Action
	{
		private JSONObject params;

		public ReconnectedAction(long actionId, WrapperModel model, JSONObject params)
		{
			super(actionId, model.getEventQueue());
			this.params = params;
		}

		@Override
		protected ProcessingStatus performAction()
		{
			JSONArray unprocessedActions = params.getJSONArray("unprocessedActions");
			
			for (int i = 0; i < unprocessedActions.length(); i++)
			{
				JSONObject unprocessedAction = unprocessedActions.getJSONObject(i);
				int actionId = unprocessedAction.getInt("id");
				
				if (!processedActions.contains(actionId))
				{
					doAction(unprocessedAction);
				}
			}

			return ProcessingStatus.COMPLETED;
		}
		
	}
}
