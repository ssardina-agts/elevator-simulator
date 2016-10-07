/*
 * Copyright 2004-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.view.BuildingView;
import org.intranet.sim.Model;
import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.ui.ApplicationUI;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class ElevatorSimulationApplication
  implements SimulationApplication
{
  public static final String COPYRIGHT =
          "Copyright 2016 Joshua Beale, Matthew McNally, Joshua Richards, Dylan Rock, Sebastian Sardina.\n" +
          "Forked from Elevator Simulator (https://sourceforge.net/projects/elevatorsim/)\n" +
          "Copyright 2004-2005 Chris Dailey & Neil McKellar";
  public static final String VERSION = "0.2-SNAPSHOT";
  public static final String APPLICATION_NAME = "RMIT Elevator Simulator";

  private Image iconImage;

  public static void main(String[] args)
  {
    ElevatorSimulationApplication sc = new ElevatorSimulationApplication();
 
//    new SimulationSelection(sc);
    new ApplicationUI(sc);
  }

  public ElevatorSimulationApplication()
  {
    super();
  }

  public JComponent createView(Model m)
  {
    return new BuildingView((Building)m);
  }
  
  public List<Simulator> getSimulations()
  {
    List<Simulator> simulations = new ArrayList<Simulator>();
    simulations.add(new RandomElevatorSimulator());
    simulations.add(new MorningTrafficElevatorSimulator());
    simulations.add(new EveningTrafficElevatorSimulator());
    simulations.add(new ThreePersonBugSimulator());
    simulations.add(new ThreePersonElevatorSimulator());
    simulations.add(new UpToFourThenDownSimulator());
    simulations.add(new NoIdleElevatorCarSimulator());
    simulations.add(new ThreePersonTwoElevatorSimulator());
    return simulations;
  }

  public String getApplicationName()
  {
    return APPLICATION_NAME;
  }

  public String getCopyright()
  {
    return COPYRIGHT;
  }

  public String getVersion()
  {
    return VERSION;
  }

  public Image getImageIcon()
  {
    if (iconImage == null)
    {
      URL iconImageURL = ElevatorSimulationApplication.class.getResource("icon.gif");
      iconImage = Toolkit.getDefaultToolkit().createImage(iconImageURL);
    }
    return iconImage;
  }
}
