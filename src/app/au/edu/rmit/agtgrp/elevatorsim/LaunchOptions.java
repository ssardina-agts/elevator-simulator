package au.edu.rmit.agtgrp.elevatorsim;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Represents the arguments that were given at application launch.
 * Uses the singleton pattern. Instance must be created manually.
 * Typically by the application's main method.
 *
 * @author Joshua Richards
 */
public class LaunchOptions {
    private static final String STATS_OPTION_KEY = "f";
    private static final String STATS_OPTION_KEY_L = "filestats";
    private static final String SPEED_OPTION_KEY = "s";
    private static final String SPEED_OPTION_KEY_L = "speed";
    private static final String HEADLESS_OPTION_KEY = "g";
    private static final String HEADLESS_OPTION_KEY_L = "headless";
    private static final String JSON_PARAM_OPTION_KEY = "j";
    private static final String JSON_PARAM_OPTION_KEY_L = "json";
    private static final String HELP_OPTION_KEY = "h";
    private static final String HELP_OPTION_KEY_L = "help";

    private static LaunchOptions instance;

    private Optional<File> statsFile = Optional.empty();
    private Optional<Integer> speedFactor = Optional.empty();
    private boolean isHeadless = false;

    private Options cliOptions = new Options();
    private CommandLineParser cliParser = new DefaultParser();


    // FIXME: This is a bad design. This will circumvent Singleton pattern
    public static void createFromCliArgs(String[] cliArgs) {
        instance = new LaunchOptions(cliArgs);
    }

    public static LaunchOptions get() {
        if (instance == null) {
            throw new IllegalStateException("no instance created");
        }
        return instance;
    }

    private void initOptions() {
        Option statFileOpt = Option.builder(STATS_OPTION_KEY).longOpt(STATS_OPTION_KEY_L)
                .desc("store statistics in a CSV file")
                .hasArg().argName("STAT_FILE")
                .required(false).build();
        Option speedOpt = Option.builder(SPEED_OPTION_KEY).longOpt(SPEED_OPTION_KEY_L)
                .desc("run simulation at speed factor times real-time")
                .hasArg().argName("SPEED")
                .required(false).build();
        Option guiOpt = Option.builder(HEADLESS_OPTION_KEY).longOpt(HEADLESS_OPTION_KEY_L)
                .desc("create a headless instance")
                .hasArg(false)
                .required(false).build();
        Option jsonParamFileOpt = Option.builder(JSON_PARAM_OPTION_KEY).longOpt(JSON_PARAM_OPTION_KEY_L)
                .desc("JSON formatted parameter file for simulators")
                .hasArg().argName("JSON_PARAM")
                .required(false).build();
        Option helpOpt = Option.builder(HELP_OPTION_KEY).longOpt(HELP_OPTION_KEY_L)
                .desc("show this help")
                .hasArg(false)
                .required(false).build();

        cliOptions.addOption(statFileOpt);
        cliOptions.addOption(speedOpt);
        cliOptions.addOption(guiOpt);
        cliOptions.addOption(jsonParamFileOpt);
        cliOptions.addOption(helpOpt);
    }

    private void showHelp() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("elevator", cliOptions, true);
    }

    private void showHelpAndExit() {
        showHelp();
        System.exit(0);
    }

    /**
     * parses the given command line arguments
     *
     * @param cliArgs CLI args passed to the program
     */
    private LaunchOptions(String[] cliArgs) {
        initOptions();

        try {
            CommandLine cmd = cliParser.parse(cliOptions, cliArgs, false);

            if (cmd.hasOption(HELP_OPTION_KEY)) {
                showHelpAndExit();
            }

            if (cmd.hasOption(HEADLESS_OPTION_KEY)) {
                isHeadless = true;
            }

            if (cmd.hasOption(STATS_OPTION_KEY)) {
                initStatsFile(cmd.getOptionValue(STATS_OPTION_KEY));
            }

            if (cmd.hasOption(SPEED_OPTION_KEY)) {
                initSpeedFactor(cmd.getOptionValue(SPEED_OPTION_KEY));
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            showHelpAndExit();
        }
    }

    /**
     * creates the given stats file if required and checks the
     * application has write access.
     *
     * @param filename file where stats will be stored
     */
    private void initStatsFile(String filename) {
        File file = new File(filename);
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Cannot create stats file: " + filename);
            return;
        }

        if (!file.canWrite()) {
            System.err.println("Cannot write to file: " + filename);
            return;
        }

        System.out.println("Simulation statistics will be written to " + file.getAbsolutePath());
        statsFile = Optional.of(file);
    }

    /**
     * parses the give string for the speed factor
     *
     * @param factorStr Speed factor to be used
     */
    private void initSpeedFactor(String factorStr) {
        Integer factor;
        try {
            factor = Integer.parseInt(factorStr);
        } catch (NumberFormatException e) {
            System.err.println("invalid value for speed factor: " + factorStr);
            return;
        }

        speedFactor = Optional.of(factor);
    }

    public Optional<File> getStatsFile() {
        return statsFile;
    }

    public Optional<Integer> getSpeedFactor() {
        return speedFactor;
    }

    public boolean isHeadless() {
        return isHeadless;
    }
}
