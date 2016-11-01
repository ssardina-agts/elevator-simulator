package au.edu.rmit.elevatorsim.action;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

/**
 * Represents the actionProcessed event and attempts to perform
 * the operation requested by the client.
 * @author Matthew McNally
 */
public abstract class Action extends Event 
{
	// ID of action that was generated
	// by client
	private final long actionId;
	
	// Conveys whether the action was able
	// to be completed by the simulator
	public enum ProcessingStatus
	{
		COMPLETED, IN_PROGRESS, FAILED;
		
		@Override
		public String toString()
		{
			// so the status values in messages are consistent
			// with the rest of the api
			switch (this)
			{
				case COMPLETED:
					return "completed";
				case IN_PROGRESS:
					return "inProgress";
				case FAILED:
					return "failed";
				default:
					return super.toString();
			}
		}
	}
	private ProcessingStatus status;
	protected String failureReason;
	
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
		status = performAction();
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
		return "actionProcessed";
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
		if (failureReason != null)
		{
			actionJson.put("failureReason", failureReason);
		}
		
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
}