/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui;

import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.ui.multiple.MultipleSimulationArea;
import org.intranet.sim.ui.realtime.SimulationArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ApplicationUI extends JFrame {
    private JComponent simulationArea;

    public ApplicationUI(final SimulationApplication sa) {
        super(sa.getApplicationName() + " - Version " + sa.getVersion());
        setIconImage(sa.getImageIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disposeMainScreenComponents();
            }
        });

        JMenuBar mb = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem fileNewMenu = new JMenuItem("New ...");
        fileNewMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SimulationSelectionDialog(ApplicationUI.this, sa, new SimulationSelection.Listener() {
                    public void simulationSelected(
                            Simulator sim,
                            SimulationApplication app, boolean isMultiple) {
                        disposeMainScreenComponents();
                        createMainScreenComponents(sim, app, isMultiple);
                        // TODO : Figure out why we need to validate() before the simulation area will repaint
                        validate();
                    }
                });
            }
        });
        fileMenu.add(fileNewMenu);
        mb.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpAboutMenu = new JMenuItem("About");
        helpAboutMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Creating a dialog box with the license in it.
                // Stand-alone window, don'w worry about it.
                new AboutDialog(ApplicationUI.this, sa);
            }
        });
        helpMenu.add(helpAboutMenu);
        mb.add(Box.createHorizontalGlue()); // add help to the far right
        mb.add(helpMenu);

        setJMenuBar(mb);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    protected void createMainScreenComponents(Simulator sim,
                                              SimulationApplication app,
                                              boolean isMultiple) {
        if (isMultiple) {
            simulationArea = new MultipleSimulationArea(sim, app, this);
        } else {
            simulationArea = new SimulationArea(sim, app, this);
        }
        getContentPane().add(simulationArea, BorderLayout.CENTER);
    }

    protected void disposeMainScreenComponents() {
        if (simulationArea != null) {
            if (simulationArea instanceof SimulationArea)
                ((SimulationArea) simulationArea).dispose();
            getContentPane().remove(simulationArea);
            simulationArea = null;
        }
    }
}
