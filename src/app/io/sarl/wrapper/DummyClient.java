package io.sarl.wrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONObject;

/**
 * A stupid client to demo the API
 * @author Joshua Richards
 */
public class DummyClient
{
	private static DataOutputStream out;
	private static int id = 0;

	public static void main(String[] args) throws IOException
	{
		Socket s = new Socket("localhost", 8081);
		DataInputStream in = new DataInputStream(s.getInputStream());
		out = new DataOutputStream(s.getOutputStream());
		while (true)
		{
			JSONObject message = new JSONObject(in.readUTF());
			System.out.println(message.toString(4));
			
			String type = message.getString("type");
			switch (type)
			{
				case "carRequested":
					// send car 0 to the floor where a car was requested
					// don't care where the occupants want to go
					sendCar(message.getJSONObject("description"));
					break;
			}
		}
	}
	
	public static void sendCar(JSONObject message) throws IOException
	{
		JSONObject params = new JSONObject();
	
		// The car to send
		params.put("car", 0);
		// The floor to send it to
		params.put("floor", message.getInt("floor"));
		// The direction the car will go after it reaches the given floor
		// Only used to determine which direction indicator to light up
		// It's fine to lie
		params.put("nextDirection", "up");
		
		// every transmission from the client is an action
		JSONObject action = new JSONObject();
		// used by server to categorize actions
		action.put("type", "sendCar");
		// this will be sent back to the client when it reports success/failure
		// should be unique but this is not currently enforced
		action.put("id", id++);
		// details specific to the action
		action.put("params", params);
		
		out.writeUTF(action.toString());
	}

}
