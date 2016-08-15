package io.sarl.wrapper;

import org.intranet.sim.event.Event;

public abstract class Percept extends Event 
{

	public Percept() 
	{
		// Percepts added to event queue should be
		// processed immediately - We want to minimise
		// delay transmitting to the client
		super(-1);
	}
	
	// Percepts need no processing, they are only used
	// in event queue for centralisation purposes
	@Override
	public void perform() { }
}