package au.edu.rmit.agtgrp.elevatorsim;

import org.json.JSONObject;

/**
 * Represents a change to the model that a controller should be notified about.
 * Typically, Events will implement this interface but there are other operations
 * that controllers also need to be notified about.
 */
public interface Transmittable
{
	/**
	 * @return A name that a client can use to identify the type of event
	 */
	public String getName();
	/**
	 * @return A JSONObject containing necessary details of the event
	 * specific to its type
	 */
	public JSONObject getDescription();
}
