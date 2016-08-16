/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim;

import org.intranet.sim.event.EventQueue;

/**
 * Super class for most elements in the simulator
 * @author Neil McKellar and Chris Dailey
 * @author Joshua Beale
 */
public abstract class ModelElement
{
	protected final EventQueue eventQueue;

	protected ModelElement(EventQueue eQ)
	{
		super();
		eventQueue = eQ;
	}
}
