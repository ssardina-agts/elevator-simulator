package io.sarl.wrapper.action;

import org.intranet.sim.event.EventQueue;

public class ErrorAction extends Action
{
	public ErrorAction(long actionId, EventQueue eq, String errorReason)
	{
		super(actionId, eq);
		failureReason = errorReason;
	}

	@Override
	protected ProcessingStatus performAction()
	{
		return ProcessingStatus.FAILED;
	}
}
