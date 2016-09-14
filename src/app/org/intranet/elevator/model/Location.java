/*
* Copyright 2003-2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

import org.intranet.elevator.model.operate.Person;
import org.intranet.sim.ModelElement;
import org.intranet.sim.event.EventQueue;

/**
 * ModelElement that contains people and has a height
 * 
 * @author Neil McKellar and Chris Dailey
 * @author Joshua Beale
 */
public class Location extends ModelElement
{
	private float height;
	private int capacity;
	private List<Person> occupants = new ArrayList<Person>();

	Location(EventQueue eQ, float height, int capacity)
	{
		super(eQ);
		this.height = height;
		this.capacity = capacity;
	}

	/**
	 * @return Height of Location
	 */
	public final float getHeight()
	{
		return height;
	}

	/**
	 * @param newHeight
	 *            Height to set location to
	 */
	protected void setHeight(float newHeight)
	{
		height = newHeight;
	}

	/**
	 * @param person
	 *            Person object to add to Location
	 */
	public final void personEnters(Person person)
	{
		if (isAtCapacity()) throw new IllegalStateException("Location is at capacity: " + capacity);
		occupants.add(person);
	}

	/**
	 * @return List of all person objects contained in location
	 */
	public final List<Person> getOccupants()
	{
		return occupants;
	}

	/**
	 * @param person
	 *            Person object to remove from Location
	 */
	public final void personLeaves(Person person)
	{
		if (!occupants.remove(person)) { throw new IllegalStateException("Person is not in this location."); }
	}

	/**
	 * @return True if number of occupants equals capacity
	 */
	public final boolean isAtCapacity()
	{
		return (occupants.size() == capacity);
	}

	/**
	 * @return Capacity of location
	 */
	public final int getCapacity()
	{
		return capacity;
	}

	/**
	 * @param numOccupants
	 *            New capacity for location
	 */
	public final void setCapacity(int numOccupants)
	{
		capacity = numOccupants;
	}
}