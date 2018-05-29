/*
 * Copyright 2004-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import au.edu.rmit.agtgrp.elevatorsim.ElsimSettings;
import au.edu.rmit.agtgrp.elevatorsim.LaunchOptions;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.view.BuildingView;
import org.intranet.sim.Model;
import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.runner.SimulationRunner;
import org.intranet.sim.ui.ApplicationUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ElevatorSimulationApplication
        implements SimulationApplication {
    public static final String COPYRIGHT =
            "Copyright 2016 Joshua Beale, Matthew McNally, Joshua Richards, Dylan Rock, Sebastian Sardina.\n" +
                    "Forked from Elevator Simulator (https://sourceforge.net/projects/elevatorsim/)\n" +
                    "Copyright 2004-2005 Chris Dailey & Neil McKellar";
    public static final String VERSION = "1.0";
    public static final String APPLICATION_NAME = "RMIT Elevator Simulator";
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private Image iconImage;

    public static void main(String[] args) {
        LOG.info("Starting Application {}", APPLICATION_NAME);
        LaunchOptions.createFromCliArgs(args);

        ElevatorSimulationApplication sc = new ElevatorSimulationApplication();

        if (LaunchOptions.get().isHeadless()) {
            Simulator defaultSimulator = new RandomElevatorSimulator();
            SimulationRunner runner = new SimulationRunner();
            runner.run(defaultSimulator);
        } else {
            new ApplicationUI(sc);
        }
        LOG.info("Started Application {}", APPLICATION_NAME);
    }

    public ElevatorSimulationApplication() {
        super();
    }

    // Why call it createView if it is not a generic view creator?
    public JComponent createView(Model m) {
        return new BuildingView((Building) m);
    }

    public List<Simulator> getSimulations() {
        List<Simulator> simulations = new ArrayList<Simulator>();
        simulations.add(new RandomElevatorSimulator());
        simulations.add(new MorningTrafficElevatorSimulator());
        simulations.add(new EveningTrafficElevatorSimulator());
        if (ElsimSettings.get().getEnableHiddenSimulators()) {
            simulations.add(new ThreePersonBugSimulator());
            simulations.add(new ThreePersonElevatorSimulator());
            simulations.add(new UpToFourThenDownSimulator());
            simulations.add(new NoIdleElevatorCarSimulator());
            simulations.add(new ThreePersonTwoElevatorSimulator());
        }
        return simulations;
    }

    public String getApplicationName() {
        return APPLICATION_NAME;
    }

    public String getCopyright() {
        return COPYRIGHT;
    }

    public String getVersion() {
        return VERSION;
    }

    public Image getImageIcon() {
        if (iconImage == null) {
            URL iconImageURL = ElevatorSimulationApplication.class.getResource("icon.gif");
            iconImage = Toolkit.getDefaultToolkit().createImage(iconImageURL);
        }
        return iconImage;
    }
}
