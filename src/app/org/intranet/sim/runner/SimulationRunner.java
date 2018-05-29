package org.intranet.sim.runner;

import au.edu.rmit.agtgrp.elevatorsim.ui.ControllerCliMessageView;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.Simulator;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.RealTimeClock;
import org.intranet.ui.SingleValueParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class SimulationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public void run(Simulator simulator) {
        new Thread(() -> {
            Controller controller = simulator.getController();

            LOG.info("Starting simulation with {} using following parameters:", simulator.getController());
            for (SingleValueParameter param : simulator.getParameters()) {
                LOG.info("{}: {}", param.getDescription(), param.getValue());
            }
            LOG.info(controller.getInitMessage());

            ControllerCliMessageView cdc = new ControllerCliMessageView();
            try {
                simulator.initialize(new RealTimeClock.RealTimeClockFactory());
                Clock clock = simulator.getClock();
                clock.start();
            } catch (RuntimeException e) {
                // Unrecoverable error while initializing sim. Show error and close application.
                // TODO: Change exception type?
                cdc.showErrorDialog(e.getMessage());
            }
            controller.setControllerDialogCreator(cdc);
        }).start();
    }
}
