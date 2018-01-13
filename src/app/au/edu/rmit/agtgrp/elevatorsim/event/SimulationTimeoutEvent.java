
package au.edu.rmit.agtgrp.elevatorsim.event;

import org.intranet.sim.event.Event;
import org.json.JSONObject;

/**
 *
 */
public class SimulationTimeoutEvent extends Event
{
    public SimulationTimeoutEvent(long time)
    {
        super(time);
    }

    @Override
    public String getName()
    {
        return "simulationTimeout";
    }

    @Override
    public JSONObject getDescription()
    {
        JSONObject eventDescription = new JSONObject();
        eventDescription.put("timeout", getTime());
        return eventDescription;
    }

    @Override
    public void perform()
    {
    }

}
