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
    private static       SimulatorParams _instance        = new SimulatorParams();
    private              boolean         paramsLoaded     = false;
    private              JSONObject      jsonParams       = null;
    private              JSONObject      simulators       = null;
    private              JSONObject      controllers      = null;
    private              JSONObject      activeController = null;
    private              JSONObject      activeSimulator  = null;
    private static final Logger          LOG              = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

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
                activeSimulator = simulators.getJSONObject(jsonParams.getString("activeSimulator"));
                activeController = controllers.getJSONObject(jsonParams.getString("activeController"));

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

    public String getActiveSimulatorName() {
        if (paramsLoaded) {
            return activeSimulator.getString("name");
        } else {
            throw new IllegalStateException("JSON parameters haven't been loaded yet");
        }
    }

    public String getActiveControllerName() {
        if (paramsLoaded) {
            return activeController.getString("name");
        } else {
            throw new IllegalStateException("JSON parameters haven't been loaded yet");
        }
    }

    public String getActiveSimulatorClass() {
        if (paramsLoaded) {
            return activeSimulator.getString("class");
        } else {
            throw new IllegalStateException("JSON parameters haven't been loaded yet");
        }
    }

    public String getActiveControllerClass() {
        if (paramsLoaded) {
            return activeController.getString("class");
        } else {
            throw new IllegalStateException("JSON parameters haven't been loaded yet");
        }
    }

    public <T> T getParamValue(String paramKey, Class<T> type) {
        if (paramsLoaded) {
            return type.cast(activeSimulator.getJSONObject("params").get(paramKey));
        } else {
            throw new IllegalStateException("JSON parameters haven't been loaded yet");
        }
    }

    public int getParamValueInt(String paramKey) {
        if (paramsLoaded) {
            return activeSimulator.getJSONObject("params").getInt(paramKey);
        } else {
            throw new IllegalStateException("JSON parameters haven't been loaded yet");
        }
    }

    public long getParamValueLong(String paramKey) {
        if (paramsLoaded) {
            return activeSimulator.getJSONObject("params").getLong(paramKey);
        } else {
            throw new IllegalStateException("JSON parameters haven't been loaded yet");
        }
    }

    public boolean getParamValueBoolean(String paramKey) {
        if (paramsLoaded) {
            return activeSimulator.getJSONObject("params").getBoolean(paramKey);
        } else {
            throw new IllegalStateException("JSON parameters haven't been loaded yet");
        }
    }

    public double getParamValueDouble(String paramKey) {
        if (paramsLoaded) {
            return activeSimulator.getJSONObject("params").getDouble(paramKey);
        } else {
            throw new IllegalStateException("JSON parameters haven't been loaded yet");
        }
    }
}
