package io.sarl.wrapper;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONObject;

public class DummyClient
{

	public static void main(String[] args) throws IOException
	{
		Socket s = new Socket("localhost", 8081);
		DataInputStream in = new DataInputStream(s.getInputStream());
		while (true)
		{
			JSONObject message = new JSONObject(in.readUTF());
			System.out.println(message.toString(4));
		}
	}

}
