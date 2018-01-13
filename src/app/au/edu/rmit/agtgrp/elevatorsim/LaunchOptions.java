package au.edu.rmit.agtgrp.elevatorsim;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Represents the arguments that were given at application launch.
 * Uses the singleton pattern. Instance must be created manually.
 * Typically by the application's main method.
 * @author Joshua Richards
 */
public class LaunchOptions
{
	public static final String STATS_OPTION_KEY = "-filestats";
	public static final String SPEED_OPTION_KEY = "-speed";
	
	private static LaunchOptions instance;
	
	private Optional<File> statsFile = Optional.empty();
	private Optional<Integer> speedFactor = Optional.empty();

	public static void createFromCliArgs(String[] cliArgs)
	{
		instance = new LaunchOptions(cliArgs);
	}
	
	public static LaunchOptions get()
	{
		if (instance == null)
		{
			throw new IllegalStateException("no instance created");
		}
		return instance;
	}

	/**
	 * parses the given command line arguments
	 * @param cliArgs
	 */
	private LaunchOptions(String[] cliArgs)
	{
		for (int i = 0; i < cliArgs.length; i++)
		{
			switch(cliArgs[i])
			{
				case STATS_OPTION_KEY:
					if (++i >= cliArgs.length)
					{
						System.err.println("no argument given for stats file name");
						return;
					}
					initStatsFile(cliArgs[i]);
					break;
				case SPEED_OPTION_KEY:
					if (++i >= cliArgs.length)
					{
						System.err.println("no argument given for speed factor");
						return;
					}
					initSpeedFactor(cliArgs[i]);
					break;
				default:
					System.err.println("Unkown option: '" + cliArgs[i] + "'");
					break;
			}
		}
	}
	
	/**
	 * creates the given stats file if required and checks the
	 * application has write access.
	 * @param filename
	 */
	private void initStatsFile(String filename)
	{
		File file = new File(filename);
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
				System.err.println("Cannot create stats file: " + filename);
				return;
			}
		}
		
		if (!file.canWrite())
		{
			System.err.println("Cannot write to file: " + filename);
			return;
		}
		
		statsFile = Optional.of(file);
	}
	
	/**
	 * parses the give string for the speed factor
	 * @param factorStr
	 */
	private void initSpeedFactor(String factorStr)
	{
		Integer factor;
		try
		{
			factor = Integer.parseInt(factorStr);
		}
		catch (NumberFormatException e)
		{
			System.err.println("invalid value for speed factor: " + factorStr);
			return;
		}
		
		speedFactor = Optional.of(factor);
	}
	
	public Optional<File> getStatsFile()
	{
		return statsFile;
	}
	
	public Optional<Integer> getSpeedFactor()
	{
		return speedFactor;
	}
}
