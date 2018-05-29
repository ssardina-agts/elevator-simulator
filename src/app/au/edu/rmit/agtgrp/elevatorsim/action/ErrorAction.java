package au.edu.rmit.agtgrp.elevatorsim.action;

import org.intranet.sim.event.EventQueue;

/**
 * Action that does nothing. Used if a client sends an invalid action request
 * @author Joshua Richards
 *
 */
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
