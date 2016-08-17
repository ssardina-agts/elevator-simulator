package io.sarl.wrapper;


import org.intranet.sim.event.EventQueue;
import org.json.JSONObject;

public class ActionResponse extends Percept
{
	private int actionId;
	private boolean success;

	public ActionResponse(EventQueue eq, int actionId, boolean success)
	{
		super(eq);
		this.actionId = actionId;
		this.success = success;
	}
	@Override
	public String getName()
	{
		return "actionResponse";
	}

	@Override
	public JSONObject getDescription()
	{
		JSONObject ret = new JSONObject();
		ret.put("actionId", actionId);
		ret.put("success", success);
		return ret;
	}

}
