package au.edu.rmit.elevatorsim;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class LaunchOptions
{
	public static final String STATS_OPTION_KEY = "-stats";
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
