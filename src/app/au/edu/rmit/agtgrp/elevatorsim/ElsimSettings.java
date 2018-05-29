package au.edu.rmit.agtgrp.elevatorsim;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a json settings file. Uses the singleton pattern.
 * The instance is created in the static initializer.
 * @author Joshua Richards
 */
public class ElsimSettings
{
	public static final String SETTINGS_FILENAME = "elsimsettings.json";
	
	public static final int DEFAULT_TIMEOUT = 15;
	public static final boolean DEFAULT_ENABLE_OLD_CONTROLLERS = false;
	public static final int DEFAULT_PORT = 8081;
	public static final boolean DEFAULT_ENABLE_HIDDEN_SIMULATORS = false;
	
	private static final String KEY_TIMEOUT = "timeout";
	private static final String KEY_ENABLE_OLD_CONTROLLERS = "enableOldControllers";
	private static final String KEY_PORT = "port";
	private static final String KEY_ENABLE_HIDDEN_SIMULATORS = "enableHiddenSimulators";
	
	private static Logger logger = Logger.getLogger(
			ElsimSettings.class.getSimpleName()
	);
	
	private static final ElsimSettings instance;
	
	static
	{
		instance = new ElsimSettings();
	}
	
	private File settingsFile;
	private int timeout = DEFAULT_TIMEOUT;
	private boolean enableOldControllers = DEFAULT_ENABLE_OLD_CONTROLLERS;
	private int port = DEFAULT_PORT;
	private boolean enableHiddenSimulators = DEFAULT_ENABLE_HIDDEN_SIMULATORS;
	
	public static ElsimSettings get()
	{
		return instance;
	}

	private ElsimSettings()
	{
		this(SETTINGS_FILENAME);
	}
	
	/**
	 * Loads settings from the given file. Creating a new file if necessary.
	 * @param filename
	 */
	private ElsimSettings(String filename)
	{
		settingsFile = new File(filename);
		if (!settingsFile.exists())
		{
			// make a new file with the defaults
			saveSettings();
			return;
		}

		String jsonStr;
		try
		{
			jsonStr = new String(Files.readAllBytes(settingsFile.toPath()));
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "Failed to read settings file");
			return;
		}

		JSONObject settingsJson = new JSONObject(jsonStr);
		parseFromJson(settingsJson);
	}
	
	/**
	 * writes the current setting to the file
	 */
	private void saveSettings()
	{
		JSONObject toWrite = new JSONObject();
		toWrite.put(KEY_TIMEOUT, timeout);
		toWrite.put(KEY_ENABLE_OLD_CONTROLLERS, enableOldControllers);
		toWrite.put(KEY_PORT, port);
		toWrite.put(KEY_ENABLE_HIDDEN_SIMULATORS, enableHiddenSimulators);
		
		try
		{
			FileOutputStream out = new FileOutputStream(settingsFile);
			out.write(toWrite.toString(4).getBytes());
			out.close();
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "Failed to write to settings file" + e.getMessage());
			return;
		}
	}
	
	/**
	 * loads any existing settings from the given json. If any required fields are
	 * missing, the defaults are used and written to the file.
	 * @param settingsJson
	 */
	private void parseFromJson(JSONObject settingsJson)
	{
		int keysFound = 0;
		int expectedKeys = 4;

		if (settingsJson.has(KEY_TIMEOUT))
		{
			timeout = settingsJson.getInt(KEY_TIMEOUT);
			keysFound++;
		}
		if (settingsJson.has(KEY_ENABLE_OLD_CONTROLLERS))
		{
			enableOldControllers = settingsJson.getBoolean(KEY_ENABLE_OLD_CONTROLLERS);
			keysFound++;
		}
		if (settingsJson.has(KEY_PORT))
		{
			port = settingsJson.getInt(KEY_PORT);
			keysFound++;
		}
		if (settingsJson.has(KEY_ENABLE_HIDDEN_SIMULATORS))
		{
			enableHiddenSimulators = settingsJson.getBoolean(KEY_ENABLE_HIDDEN_SIMULATORS);
			keysFound++;
		}
		
		if (keysFound != expectedKeys)
		{
			saveSettings();
		}
	}
	
	public int getTimeout()
	{
		return timeout;
	}
	
	public boolean getEnableOldControllers()
	{
		return enableOldControllers;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public boolean getEnableHiddenSimulators()
	{
		return enableHiddenSimulators;
	}
}
