package au.edu.rmit.elevatorsim;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

import au.edu.rmit.elevatorsim.ui.ControllerDialog;
import au.edu.rmit.elevatorsim.ui.ControllerDialogCreator;

/**
 * Abstracts networking operations so other classes only need to work with JSON.
 * Read and write operations are synchronized on the input and output streams respectively
 * @author Joshua Richards
 */
public class NetworkHelper
{
	private int port;
	private EventQueue eventQueue;
	private ServerSocket ss;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	private List<Listener> listeners = new ArrayList<>();
	private ControllerDialogCreator cdc;
	
	private boolean closed = true;

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
	public NetworkHelper(int port, EventQueue eventQueue) throws IOException
	{
		this.port = port;
		this.eventQueue = eventQueue;
		initSocket(0);
	}
	
	private void initSocket(int serverTimeoutSeconds) throws IOException
	{
		ss = new ServerSocket(port);
		try
		{
			ss.setSoTimeout(serverTimeoutSeconds * 1000);
			socket = ss.accept();
			socket.setSoTimeout(ElsimSettings.instance.getTimeout() * 1000);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			closed = false;
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
			catch (SocketException | EOFException e)
			{
				// connection reset
				// probably client closed
				if (!closed)
				{
					handleConnectionClose("Connection closed by client");
				}
				throw e;
			}
			catch (SocketTimeoutException e)
			{
				// disconnected from network probably
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
				if (!reconnecting.get())
				{
					e.printStackTrace();
					return;
				}
				try
				{
					releasedOnReconnect.await();
				}
				catch (InterruptedException e1) {}
			}
		}
	}
	
	private void reconnect() throws IOException
	{
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
						handleConnectionClose("Cancelled reconnect");
					}
					catch (IOException e)
					{
						System.out.println("sadf;lkjasdf");
						e.printStackTrace();
					}
				});
		
		IOException toThrow = new IOException("Programmer error: this should never be thrown");
		// prevent simulation progressing any further until connection reestablished
		synchronized (eventQueue)
		{
		close();
		int attempts = 0;
			
			do
			{
				System.out.println("reconnect attempt: " + (attempts + 1));
				try
				{
					initSocket(30);
					
					reconnecting.set(false);
					break;
				}
				catch (SocketException e)
				{
					// serversocket closed
					reconnecting.set(false);
					releasedOnReconnect.countDown();
					releasedOnReconnect = new CountDownLatch(1);
					return;
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
				for (Listener l : listeners)
				{
					l.onReconnect();
				}
				return;
			}
		}
		
		handleConnectionClose("Failed to reconnect");
		throw toThrow;
	}
	
	public void close()
	{
		closed = true;
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
	
	private void handleConnectionClose(String message)
	{
		cdc.showErrorDialog(message);
		close();
		for (Listener l : listeners)
		{
			l.onConnectionClosed();
		}
	}
	
	public void setControllerDialogCreator(ControllerDialogCreator cdc)
	{
		this.cdc = cdc;
	}
	
	public void addListener(Listener l)
	{
		listeners.add(l);
	}
	
	public void removeListener(Listener l)
	{
		listeners.remove(l);
	}
	
	public interface Listener
	{
		public void onReconnect();
		public void onConnectionClosed();
	}
}
