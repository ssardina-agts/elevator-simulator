package io.sarl.wrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONObject;

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
					sendCar(message.getJSONObject("description"));
					break;
			}
		}
	}
	
	public static void sendCar(JSONObject message) throws IOException
	{
		JSONObject params = new JSONObject();
	
		params.put("car", 0);
		params.put("floor", message.getInt("floor"));
		params.put("nextDirection", "up");
		
		JSONObject toTransmit = new JSONObject();
		toTransmit.put("params", params);
		toTransmit.put("type", "sendCar");
		toTransmit.put("id", id++);
		
		out.writeUTF(toTransmit.toString());
	}

}
