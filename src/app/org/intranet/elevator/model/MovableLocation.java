/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.event.TrackingUpdateEvent;
import org.json.JSONObject;

import au.edu.rmit.elevatorsim.Transmittable;

/**
 * A location that can move. The state of movement is kept between height and
 * destinationHeight. If they are equal, then the MovableLocation is still. If
 * they are not equal, then the MovableLocation is moving. Valid states:
 * <table border="1" cellspacing="0" cellpadding="2">
 * <tr>
 * <th rowspan="2">State</th>
 * <th colspan="1">Variables</th>
 * <th colspan="10">Transitions</th>
 * </tr>
 * <tr>
 * <th>destinationHeight</th>
 * <th>setDestinationHeight()</th>
 * <th>[ArrivalEvent]</th>
 * </tr>
 * <tr>
 * <td>IDLE</td>
 * <td>height</td>
 * <td>IDLE:[arrive()] or travel(): MOVING</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>MOVING</td>
 * <td><i>other</i></td>
 * <td>IDLE:[arrive()] or travel(): MOVING</td>
 * <td>IDLE:[arrive()]</td>
 * </tr>
 * </table>
 * <p>
 * Note that <code>arrive()</code> is like an event that is picked up by the
 * subclass, as it is an abstract method.
 * </p>
 * 
 * @author Neil McKellar and Chris Dailey
 * @author Joshua Beale
 */
public abstract class MovableLocation extends Location
{
	MovableLocation(EventQueue eQ, float height, int capacity)
	{
		super(eQ, height, capacity);
		destinationHeight = height;
		eQ.addListener(new EventQueue.Listener()
		{
			@Override
			public void eventAdded(Event e)
			{
			}

			@Override
			public void eventError(Exception ex)
			{
			}

			@Override
			public void eventProcessed(Event e)
			{
			}

			@Override
			public void eventRemoved(Event e)
			{
				if (e == arrivalEvent) arrivalEvent = null;
			}
			
			@Override
			public void simulationEnded()
			{
			}
		});
	}

	private float destinationHeight;
	private float totalDistance = 0.0F;
	private int numTravels = 0;
	private Event arrivalEvent;
	private List<Listener> listeners = new ArrayList<>();

	/**
	 * @return Total distance travelled by MovableLocation
	 */
	public final float getTotalDistance()
	{
		return totalDistance;
	}

	/**
	 * @return Number of travels
	 */
	public final int getNumTravels()
	{
		return numTravels;
	}

	/**
	 * @param distance
	 *            Distance to travel
	 * @return Time required to travel distance
	 */
	public final float getTravelTime(float distance)
	{
		return Math.abs(distance / getRatePerSecond());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.intranet.elevator.model.Location#setHeight(float)
	 */
	public final void setHeight(float newHeight)
	{
		totalDistance += Math.abs(newHeight - getHeight());
		super.setHeight(newHeight);
	}

	/**
	 * Travel to specified height
	 * 
	 * @param h
	 *            Height to travel to
	 */
	protected final void setDestinationHeight(float h)
	{
		if (arrivalEvent != null)
		{
			eventQueue.removeEvent(arrivalEvent);
			arrivalEvent = null;
		}

		checkDirectionChange(h);
		destinationHeight = h;

		if (getHeight() == h) arrive();
		else
			travel();
	}

	/**
	 * Arrive at destination height
	 */
	protected abstract void arrive();

	/**
	 * @return Travel speed
	 */
	protected abstract float getRatePerSecond();

	private class ArrivalEvent extends TrackingUpdateEvent
	{
		private Transmittable arrivalMessage;

		public ArrivalEvent(float departureHeight, long departureTime, long arrivalTime)
		{
			super(departureTime, departureHeight, arrivalTime, destinationHeight);
			arrivalMessage = getArrivalMessage();
		}

		public void perform()
		{
			setHeight(destinationHeight);
			numTravels++;
			arrive();
		}

		public void updateTime()
		{
			setHeight(currentValue(eventQueue.getCurrentTime()));
		}

		@Override
		public String getName()
		{
			return arrivalMessage.getName();
		}

		@Override
		public JSONObject getDescription()
		{
			return arrivalMessage.getDescription();
		}
	}

	public abstract Transmittable getArrivalMessage();

	/**
	 * Check if the MovableLocation needs to change direction
	 * 
	 * @param h
	 *            Destination height
	 */
	private void checkDirectionChange(float h)
	{
		boolean areMoving = getHeight() != destinationHeight;
		if (areMoving)
		{
			boolean oldUp = destinationHeight > getHeight();
			boolean newUp = h > getHeight();
			if (oldUp != newUp)
			{
				System.err.println("MovableLocation.setDestination: destinationHeight=" + destinationHeight
						+ ", currentHeight=" + getHeight() + ", new destHeight=" + h);
				// TODO : Make fault-tolerant -- remove assignments from list
				// and give them back to the building for re-assignment
				// TODO : Or rejecting the destination
				throw new IllegalArgumentException("Can't change directions in mid-travel.");
			}
		}
	}

	/**
	 * Travel to destination
	 */
	private void travel()
	{
		float ratePerMillisecond = getRatePerSecond() / 1000;
		long arrivalTime = eventQueue.getCurrentTime()
				+ (long) (Math.abs(getHeight() - destinationHeight) / ratePerMillisecond);
		long departureTime = eventQueue.getCurrentTime();
		float departureHeight = getHeight();
		// create and remember IncrementalUpdateEvent(arrivalTime)
		arrivalEvent = new ArrivalEvent(departureHeight, departureTime, arrivalTime);
		try
		{
			eventQueue.addEvent(arrivalEvent);
		}
		catch (IllegalArgumentException iae)
		{
			System.err.println("MovableLocation.travel():eventQueue.getCurrentTime()=" + eventQueue.getCurrentTime());
			System.err.println("departureTime=" + departureTime);
			System.err.println("arrivalTime  =" + arrivalTime);
			throw iae;
		}
	}
	
	public void addListener(Listener listener)
	{
		listeners.add(listener);
	}
	
	public boolean removeListener(Listener listener)
	{
		return listeners.remove(listener);
	}
	
	public interface Listener
	{
		public void heightChanged(int height);
	}
}
