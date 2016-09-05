package io.sarl.wrapper.event;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.event.EventQueue.Listener;
import org.json.JSONObject;

public class SimulationEndSensor extends Sensor implements Listener
{
	private boolean yes = true;

	public SimulationEndSensor(EventQueue eventQueue)
	{
		super(eventQueue);
	}

	@Override
	public void startPerceiving()
	{
		perceptSequence.addListener(this);
	}

	@Override
	public void eventRemoved(Event e)
	{
		// if there are no more events, the simulation has ended.
		// add one more event to notify client of simulation end.
		if (yes && perceptSequence.getEventList().size() == 0)
		{
			perceptSequence.addEvent(new SimulationEndPercept());
			yes = false;
		}
	}

	public class SimulationEndPercept extends Percept
	{
		public SimulationEndPercept()
		{
			super(perceptSequence);
		}

		@Override
		public String getName()
		{
			return "simulationEnded";
		}

		@Override
		public JSONObject getDescription()
		{
			return new JSONObject();
		}
		
	}

	@Override
	public void eventAdded(Event e) {}

	@Override
	public void eventError(Exception ex) {}

	@Override
	public void eventProcessed(Event e) {}
}
