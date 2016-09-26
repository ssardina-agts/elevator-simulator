package au.edu.rmit.elevatorsim;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

/**
 * Abstracts networking operations so other classes only need to work with JSON.
 * Read and write operations are synchronized on the input and output streams respectively
 * @author Joshua Richards
 */
public class NetworkHelper
{
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	/**
	 * Starts a ServerSocket, accepts one client and retrieves input and
	 * output streams to the client. This constructor will block until a
	 * client connects to the ServerSocket or an Exception if thrown.
	 * @param port The port to listen on
	 * @throws IOException If the ServerSocket times out while waiting
	 * for a client to connect to it or there is a problem retrieving the
	 * input and output streams
	 */
	public NetworkHelper(int port) throws IOException
	{
		ServerSocket ss = new ServerSocket(port);
		socket = ss.accept();
		ss.close();
		
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}
	
	/**
	 * Reads a message from the client and returns it as JSON.
	 * This method is synchronized on the input stream and will
	 * block until a message is received.
	 * @return The message from the client
	 * @throws IOException If there is a connection problem
	 */
	public JSONObject receive() throws IOException
	{
		String messageStr;
		synchronized (in)
		{
			messageStr = in.readUTF();
		}
		return new JSONObject(messageStr);
	}
	
	/**
	 * Sends a message to the client
	 * @param message The message to send
	 * @throws IOException If there is a connection problem
	 */
	public void transmit(JSONObject message) throws IOException
	{
		synchronized (out)
		{
			out.writeUTF(message.toString());
		}
	}
}
