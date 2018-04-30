package org.intranet.sim.runner;

import au.edu.rmit.agtgrp.elevatorsim.ui.ControllerCliMessageView;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.Simulator;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.RealTimeClock;

public class SimulationRunner {
    public void run(Simulator simulator) {
        new Thread() {
            @Override
            public void run() {
                Controller controller = simulator.getController();
                System.out.println(controller.getInitMessage());
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
            }
        }.start();
    }
}
