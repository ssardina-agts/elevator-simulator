/*
 * Copyright 2003, 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.realtime;

import au.edu.rmit.agtgrp.elevatorsim.LaunchOptions;
import au.edu.rmit.agtgrp.elevatorsim.ui.ControllerDialogCreator;
import au.edu.rmit.agtgrp.elevatorsim.ui.ControllerDialogCreatorImpl;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.Model;
import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.Simulator.SimulatorListener;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.RealTimeClock;
import org.intranet.ui.InputPanel;
import org.intranet.ui.SingleValueInputPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class SimulationArea
        extends JComponent {
    private JButton startButton = new JButton();
    private Simulator sim;
    private JComponent bView;
    private Statistics statistics;
    private JComponent leftPane = new JPanel();
    private JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JPanel bottomPanel = new JPanel(new BorderLayout());
    private JFrame parent;
    ClockDisplay clockDisplay = new ClockDisplay();

    private EventQueueDisplay eventQueueDisplay;

    public SimulationArea(Simulator simulator, SimulationApplication simApp, JFrame parent) {
        super();
        sim = simulator;

        setLayout(new BorderLayout());

        createRightPane();

        createBottomPanel();

        createLeftPane(simApp);

        sim.addListener(new SimulatorListener() {
            public void modelUpdate(long time) {
                // TODO : The model should be responsible for telling the view when it updates
                bView.repaint();
            }
        });

        this.parent = parent;
    }

    private void createLeftPane(final SimulationApplication simApp) {
        leftPane.setLayout(new BorderLayout());
        SingleValueInputPanel ip = new SingleValueInputPanel(sim.getParameters(), null);
        ip.addListener(new InputPanel.Listener() {
            public void parametersApplied() {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        Controller controller = sim.getController();
                        ip.showIndeterminateProgress(controller.getInitMessage());
                        ControllerDialogCreator cdc = new ControllerDialogCreatorImpl(parent);
                        try {
                            sim.initialize(new RealTimeClock.RealTimeClockFactory());
                        } catch (RuntimeException e) {
                            // Unrecoverable error while initializing sim. Show error and close application.
                            // TODO: Change exception type?
                            cdc.showErrorDialog(e.getMessage());
                            parent.dispose();
                            return null;
                        }
                        controller.setControllerDialogCreator(cdc);
                        return null;
                    }

                    @Override
                    protected void done() {
                        ip.hideIndeterminateProgress();
                        reconfigureSimulation(simApp);
                    }
                }.execute();
            }
        });
        leftPane.add(ip, BorderLayout.NORTH);
        statistics = new Statistics();
        leftPane.add(statistics, BorderLayout.CENTER);

        add(leftPane, BorderLayout.WEST);
    }

    private void createRightPane() {
        add(rightSplitPane, BorderLayout.CENTER);
        rightSplitPane.setResizeWeight(1.0);
        rightSplitPane.setDividerLocation(425);
        eventQueueDisplay = new EventQueueDisplay();
        rightSplitPane.setRightComponent(eventQueueDisplay);
    }

    private void createBottomPanel() {
        JPanel timeFactorPanel = new JPanel();

        JLabel timeFactorLabel = new JLabel("Time Factor");

        final SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0, -20, 20, 1);
        LaunchOptions.get().getSpeedFactor().ifPresent(
                factor -> spinnerNumberModel.setValue(factor)
        );
        JSpinner timeFactor = new JSpinner(spinnerNumberModel);
        timeFactor.setEnabled(!LaunchOptions.get().getStatsFile().isPresent());
        spinnerNumberModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!sim.isInitialized()) return;

                // get the clock from the simulator object sim and change it 
                // with what the spinner widget has now
                int factor = ((Number) spinnerNumberModel.getValue()).intValue();
                RealTimeClock rtClock = (RealTimeClock) sim.getClock();
                synchronized (rtClock) {
                    rtClock.setTimeConversion(factor);
                }
            }
        });

        timeFactorPanel.add(timeFactorLabel);
        timeFactorPanel.add(timeFactor);
        bottomPanel.add(timeFactorPanel, BorderLayout.EAST);

        JPanel startButtonPanel = new JPanel();
        startButtonPanel.add(startButton);
        bottomPanel.add(startButtonPanel, BorderLayout.CENTER);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                Clock clock = sim.getClock();
                synchronized (clock) {
                    if (clock.isRunning()) {
                        clock.pause();
                    } else {
                        int factor = ((Number) spinnerNumberModel.getValue()).intValue();
                        ((RealTimeClock) clock).setTimeConversion(factor);
                        clock.start();
                    }
                }
            }
        });
        startButton.setEnabled(false);
        add(bottomPanel, BorderLayout.SOUTH);
        updateButtonText(false);
        bottomPanel.add(clockDisplay, BorderLayout.WEST);
    }

    private void updateButtonText(boolean running) {
        startButton.setText(running ? "Pause" : "Go, Dude!");
    }

    public void paint(Graphics g) {
        Model model = sim.getModel();
        if (model == null) {
            super.paint(g);
        } else
            synchronized (model) {
                super.paint(g);
            }
    }

    private void reconfigureSimulation(final SimulationApplication simApp) {
        startButton.setEnabled(true);
        bView = simApp.createView(sim.getModel());
        rightSplitPane.setLeftComponent(bView);

        final Clock clock = sim.getClock();
        clock.addListener(new Clock.Listener() {
            public void timeUpdate(long time) {
            }

            public void stateUpdate(boolean running) {
                updateButtonText(running);
                if (!running)
                    statistics.updateStatistics();
            }
        });

        clockDisplay.setClock(sim.getClock());
        eventQueueDisplay.initialize(sim.getEventQueue());

        statistics.setModel(sim.getModel());
        validate();
    }

    public void dispose() {
        // If the sim has not been initialized, we can't get a clock (and also don't
        // really need to worry about disposing of anything).  bView will be set
        // by this component shortly after calling initialize on the simulator,
        // so we can check its value.
        if (bView == null)
            return;
        Clock clock = sim.getClock();
        if (clock.isRunning())
            clock.pause();
        sim.getController().close();
    }
}
