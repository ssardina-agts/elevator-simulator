package io.sarl.wrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

public class NetworkHelper
{
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public NetworkHelper(int port) throws IOException
	{
		ServerSocket ss = new ServerSocket(port);
		socket = ss.accept();
		ss.close();
		
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}
	
	public JSONObject receive() throws IOException
	{
		String messageStr;
		synchronized (in)
		{
			messageStr = in.readUTF();
		}
		return new JSONObject(messageStr);
	}
	
	public void transmit(JSONObject message) throws IOException
	{
		synchronized (out)
		{
			out.writeUTF(message.toString());
		}
	}
}
