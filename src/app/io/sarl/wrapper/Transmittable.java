package io.sarl.wrapper;

import org.json.JSONObject;

/**
 * Represents a change to the model that a controller should be notified about.
 * Typically, Events will implement this interface but there are other operations
 * that controllers also need to be notified about.
 */
public interface Transmittable
{
	public String getName();
	public JSONObject getDescription();
}
