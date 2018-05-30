package au.edu.rmit.agtgrp.elevatorsim;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;

public class SimulatorParams {
    private static       SimulatorParams _instance    = new SimulatorParams();
    private              boolean         paramsLoaded = false;
    private              JSONObject      jsonParams   = null;
    private              JSONObject      simulators   = null;
    private              JSONObject      controllers  = null;
    private static final Logger          LOG          = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    /**
     * Private constructor to prevent public object creation for this singleton class.
     */
    private SimulatorParams() {
    }

    /**
     * Returns the singleton instance of this class.
     */
    public static SimulatorParams instance() {
        return _instance;
    }

    /**
     * Load the simulation parameters from JSON formatted file
     *
     * @param jsonFile File object representing a JSON formatted file with simulation parameters
     * @return true if the jsonFile was parsed properly, false otherwise
     */
    boolean loadParamsJson(File jsonFile) {
        if (!paramsLoaded) {
            String jsonParamsString;
            try {
                jsonParamsString = new String(Files.readAllBytes(jsonFile.toPath()));
                jsonParams = new JSONObject(jsonParamsString);
                simulators = jsonParams.getJSONObject("simulators");
                controllers = jsonParams.getJSONObject("controllers");

                paramsLoaded = true;
            } catch (IOException | JSONException e) {
                LOG.error(e.getMessage());
            }
        }
        return paramsLoaded;
    }

    public boolean isParamsLoaded() {
        return paramsLoaded;
    }

    public String getActiveSimulator() {
        return jsonParams.getString("activeSimulator");
    }

    public String getActiveController() {
        return jsonParams.getString("activeController");
    }

    public String getActiveSimulatorClass() {
        return simulators.getJSONObject(getActiveSimulator()).getString("class");
    }

    public String getActiveControllerClass() {
        return controllers.getJSONObject(getActiveController()).getString("class");
    }
}
