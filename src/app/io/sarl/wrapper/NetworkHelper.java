package io.sarl.wrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

import io.sarl.wrapper.event.EventTransmitter;
import io.sarl.wrapper.ui.ControllerDialog;
import io.sarl.wrapper.ui.ControllerDialogCreator;

/**
 * Abstracts networking operations so other classes only need to work with JSON.
 * Read and write operations are synchronized on the input and output streams respectively
 * @author Joshua Richards
 */
public class NetworkHelper
{
	private int port;
	private ServerSocket ss;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	private EventTransmitter eventTransmitter;
	private ControllerDialogCreator cdc;

	private AtomicBoolean reconnecting = new AtomicBoolean(false);
	private CountDownLatch releasedOnReconnect = new CountDownLatch(1);
	
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
		this.port = port;
		initSocket();
	}
	
	private void initSocket() throws IOException
	{
		ss = new ServerSocket(port);
		try
		{
			ss.setSoTimeout(30 * 1000);
			socket = ss.accept();
			socket.setSoTimeout(10 * 1000);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}
		finally
		{
			ss.close();
		}
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
			try
			{
				messageStr = in.readUTF();
			}
			catch (IOException e)
			{
				reconnect();
				messageStr = in.readUTF();
			}
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
			try
			{
				out.writeUTF(message.toString());
			}
			catch (IOException e)
			{
				reconnect();
			}
		}
	}
	
	private void reconnect() throws IOException
	{
		System.out.print("yo what's up im a debug statement");
		if (!reconnecting.compareAndSet(false, true))
		{
			try
			{
				releasedOnReconnect.await();
			}
			catch (InterruptedException e) {}
			if (!reconnecting.get())
			{
				return;
			}
			throw new IOException("failed to reconnect");
		}
		
		ControllerDialog dialog = cdc.createLongCancellableOperationDialog(
				"Reconnecting", "Client disconnected. Reconnecting...", () ->
				{
					try
					{
						ss.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				});
		
		close();
		int attempts = 0;
		IOException toThrow = new IOException("Programmer error: this should never be thrown");
		
		do
		{
			System.out.println("reconnect attempt: " + (attempts + 1));
			try
			{
				initSocket();
				System.out.println("reconnected");
				
				reconnecting.set(false);
				System.out.println("al;sdfkj");
				break;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				toThrow = e;
			}
		} while (attempts++ < 3);
		
		releasedOnReconnect.countDown();
		releasedOnReconnect = new CountDownLatch(1);
		
		dialog.close();

		if (!reconnecting.get())
		{
			eventTransmitter.retransmitUnprocessedEvents();
			return;
		}
		
		throw toThrow;
	}
	
	public void close()
	{
		try
		{
			socket.close();
			in.close();
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setEventTransmitter(EventTransmitter et)
	{
		eventTransmitter = et;
	}
	
	public void setControllerDialogCreator(ControllerDialogCreator cdc)
	{
		this.cdc = cdc;
	}
}
