package io.sarl.wrapper;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

public abstract class Action extends Event 
{
	// ID of action that was generated
	// by client
	private long actionId;
	
	// TODO: Error reasons?
	// Conveys whether the action was able
	// to be completed by the simulator
	public enum ProcessingStatus
	{
		SUCCESS, FAILURE
	}
	private ProcessingStatus status;
	
	public Action(long actionId, EventQueue eq) 
	{
		// Actions should be processed immediately
		// (pushed to the event queue immediately)
		super(eq.getCurrentTime());
		this.actionId = actionId;
	}
	
	@Override
	public final void perform()
	{
		ProcessingStatus status = performAction();
		setStatus(status);
	}
	
	/**
	 * Used for processing of all necessary
	 * invocations to successfully enact action in
	 * the simulation. 
	 * @return Status indicating result of the action
	 */
	protected abstract ProcessingStatus performAction();
	
	@Override
	public String getName()
	{
		return "action";
	}
	
	@Override
	public JSONObject getDescription()
	{
		// JSON description of actions should include
		// ID corresponding to this action and its status
		// once processed
		JSONObject actionJson = new JSONObject();
		actionJson.put("actionId", actionId);
		actionJson.put("status", status);
		
		return actionJson;
	}
	
	public long getActionId()
	{
		return actionId;
	}
	
	public ProcessingStatus getStatus()
	{
		return status;
	}
	
	protected void setStatus(ProcessingStatus status)
	{
		this.status = status;
	}
}