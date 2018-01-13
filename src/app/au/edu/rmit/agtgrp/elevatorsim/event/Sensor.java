package au.edu.rmit.agtgrp.elevatorsim.event;

import org.intranet.sim.event.EventQueue;

/**
 * Waits for something interesting to happen and adds a Percept to the EventQueue
 * @author Matthew McNally
 */
public abstract class Sensor
{
	protected EventQueue perceptSequence;
	
	public Sensor(EventQueue eventQueue)
	{
		perceptSequence = eventQueue;
	}
	
	/**
	 * Add percept to the eventQueue
	 * @param percept the Percept to add
	 */
	protected void enqueuePercept(Percept percept)
	{
		perceptSequence.addEvent(percept);
	}
	
	/**
	 * Performs required operations so the object is notified when something
	 * interesting happens. Typically this method will add a listener to
	 * whatever it is observing
	 */
	public abstract void startPerceiving();
}