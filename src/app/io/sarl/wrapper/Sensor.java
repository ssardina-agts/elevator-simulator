package io.sarl.wrapper;

import org.intranet.sim.event.EventQueue;

public abstract class Sensor {
	
	private EventQueue perceptSequence;
	
	public Sensor(EventQueue eventQueue)
	{
		perceptSequence = eventQueue;
	}
	
	protected void enqueuePercept(Percept percept)
	{
		perceptSequence.addEvent(percept);
	}
	
	public abstract void perceive();
}