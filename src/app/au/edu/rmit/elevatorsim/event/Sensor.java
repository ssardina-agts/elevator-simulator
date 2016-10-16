package au.edu.rmit.elevatorsim.event;

import org.intranet.sim.event.EventQueue;

public abstract class Sensor {
	
	protected EventQueue perceptSequence;
	
	public Sensor(EventQueue eventQueue)
	{
		perceptSequence = eventQueue;
	}
	
	protected void enqueuePercept(Percept percept)
	{
		perceptSequence.addEvent(percept);
	}
	
	public abstract void startPerceiving();
}