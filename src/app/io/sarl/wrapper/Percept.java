package io.sarl.wrapper;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;

public abstract class Percept extends Event 
{

	public Percept(EventQueue eq) 
	{
		// Percepts added to event queue should be
		// processed immediately (current time) - 
		// We want to minimise delay transmitting 
		// to the client
		super(eq.getCurrentTime());
	}
	
	// Percepts need no processing, they are only used
	// in event queue for centralisation purposes
	@Override
	public void perform() { }
}