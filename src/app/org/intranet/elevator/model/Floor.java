/*
* Copyright 2003-2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

import org.intranet.sim.event.EventQueue;

/**
 * Floor of Building with a CarRequestPanel and CarEntrance(s) leading to Car(s)
 * 
 * @author Neil McKellar and Chris Dailey
 * @author Joshua Beale
 */
public final class Floor extends Location
{
	private int number;
	// distance from the ground relative to the floor's height
	private float ceiling;
	private CarRequestPanel callPanel = new CarRequestPanel(new CarRequestPanel.Approver()
	{
		// Don't keep the request light on if there is an entrance with a down
		// light
		public boolean approveDownRequest()
		{
			for (CarEntrance entrance : carEntrances)
				if (entrance.isDown()) return false;
			return true;
		}

		// Don't keep the request light on if there is an entrance with an up
		// light
		public boolean approveUpRequest()
		{
			for (CarEntrance entrance : carEntrances)
				if (entrance.isUp()) return false;
			return true;
		}
	});
	private List<CarEntrance> carEntrances = new ArrayList<CarEntrance>();

	// TODO: Make a sequence diagram with all the passing off of notification
	private CarEntrance.CarEntranceListener carEntranceListener = new CarEntrance.CarEntranceListener()
	{
		public void arrivedUp(CarEntrance entrance)
		{
			callPanel.arrivedUp(entrance);
		}

		public void arrivedDown(CarEntrance entrance)
		{
			callPanel.arrivedDown(entrance);
		}
	};

	public Floor(EventQueue eQ, int number, float height, float ceiling)
	{
		super(eQ, height, 500);
		this.number = number;
		this.ceiling = ceiling;
	}

	/**
	 * @return Floor number
	 */
	public int getFloorNumber()
	{
		return number;
	}

	/**
	 * @return Ceiling relative to floor
	 */
	public float getCeiling()
	{
		return ceiling;
	}

	/**
	 * @return Ceiling relative to ground
	 */
	public float getAbsoluteCeiling()
	{
		return getHeight() + ceiling;
	}

	/**
	 * @return CarRequestPanel for Floor
	 */
	public CarRequestPanel getCallPanel()
	{
		return callPanel;
	}

	/**
	 * Connect Floor to Car via new CarEntrance
	 * 
	 * @param car
	 *            Car to connect to
	 */
	public void createCarEntrance(Car car)
	{
		carEntrances.add(new CarEntrance(eventQueue, this, car, carEntranceListener));
	}

	/**
	 * @return List of car entrances on the floor
	 */
	public List<CarEntrance> getCarEntrances()
	{
		return carEntrances;
	}

	/**
	 * @param up
	 *            Direction of travel
	 * @return First CarEntrance travelling in the specified direction
	 */
	public CarEntrance getOpenCarEntrance(boolean up)
	{
		for (CarEntrance carEntrance : carEntrances)
		{
			if (carEntrance.getDoor().isOpen())
			{
				if (up && carEntrance.isUp()) return carEntrance;
				if (!up && carEntrance.isDown()) return carEntrance;
			}
		}
		return null;
	}

	/**
	 * @param destination
	 *            Location to travel to
	 * @return First CarEntrance leading to destination
	 */
	public CarEntrance getCarEntranceForCar(Location destination)
	{
		for (CarEntrance carEntrance : carEntrances)
			if (carEntrance.getDoor().getTo() == destination) return carEntrance;
		return null;
	}

	public String toString()
	{
		return "Floor" + number + "@" + getHeight();
	}
}