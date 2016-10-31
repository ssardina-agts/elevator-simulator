/*
 * Copyright 2003, 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;


/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public final class SimulationSelection
  extends JPanel
{
  private JList simulationJList = new JList();
  private List<Simulator> simulations = new ArrayList<Simulator>();
  private SimulationApplication simulationApp;
  private Listener listener;

  interface Listener
  {
    void simulationSelected(Simulator sim, SimulationApplication app, boolean multiple);
  }
  
  public SimulationSelection(final SimulationApplication simApp, Listener l)
  {
    super();
    setLayout(new BorderLayout());
    listener = l;
    simulationApp = simApp;
    simulations = simApp.getSimulations();

    JScrollPane scrollPane = new JScrollPane(simulationJList);
    add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    add(buttonPanel, BorderLayout.SOUTH);
    
    JButton realtimeButton = new JButton("Open Real-Time Simulation");
    buttonPanel.add(realtimeButton);
    
    ListModel listModel = new AbstractListModel()
    {
      public Object getElementAt(int arg0)
      {
        return simulations.get(arg0).getDescription();
      }

      public int getSize()
      {
        return simulations.size();
      }
    };
    
    realtimeButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent arg0)
      {
        int index = simulationJList.getSelectedIndex();
        apply(index, false);
      }
    });
    
    simulationJList.setModel(listModel);
    simulationJList.setSelectedIndex(0);
//    simulationJList.addMouseListener(new MouseAdapter()
//    {
//      public void mouseClicked(MouseEvent e)
//      {
//        if (e.getClickCount() == 2)
//          apply(simulationJList.locationToIndex(e.getPoint()), false);
//      }
//     });
  }

  private void apply(int index, boolean isMultiple)
  {
    if (index < 0) return;
        
    Simulator sim = simulations.get(index);
    listener.simulationSelected(sim, simulationApp, isMultiple);
  }
}
