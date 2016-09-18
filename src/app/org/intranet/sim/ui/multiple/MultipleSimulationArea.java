/*
 * Copyright 2004-2006 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.ClockFactory;
import org.intranet.sim.clock.RealTimeClock;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.ui.realtime.SimulationArea;
import org.intranet.statistics.Column;
import org.intranet.statistics.Table;
import org.intranet.ui.ChoiceParameter;
import org.intranet.ui.ExceptionDialog;
import org.intranet.ui.InputPanel;
import org.intranet.ui.MultipleChoiceParameter;
import org.intranet.ui.MultipleValueInputPanel;
import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.RangeParameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
// SOON : factor out GUI vs behaviour code
public class MultipleSimulationArea extends JComponent
{
  private boolean foundError;
  private Simulator sim;
  private SimulationApplication simApp;
  private JComponent topPanel = new JPanel();
  private ResultsSelection centerPanel;
  private ResultsTable bottomPanel;
  private List<MultipleValueParameter<?>> multiValueParams;
  private Map<List<SingleValueParameter<?>>, List<Table>> results;
  private Box topBox = new Box(BoxLayout.Y_AXIS); 
  private JFrame parent;

  public MultipleSimulationArea(Simulator simulator, SimulationApplication app, JFrame parent)
  {
    super();
    sim = simulator;
    simApp = app;
    
    setLayout(new BorderLayout());

    add(topBox, BorderLayout.NORTH);
    createTopPanel();
    this.parent = parent;
  }

  private void createTopPanel()
  {
    topPanel.setLayout(new BorderLayout());
    List<SingleValueParameter<?>> simParams = sim.getParameters();
    multiValueParams = createMultiValueParameters(simParams);
    MultipleValueInputPanel ip = new MultipleValueInputPanel(multiValueParams,
        new InputPanel.Listener()
    {
      public void parametersApplied() { runSimulations(); }
    });
    topPanel.add(ip, BorderLayout.CENTER);
    topBox.add(topPanel);
  }

  private void createCenterPanel()
  {
    ResultsSelection.ResultsSelectionListener resultsSelectionListener =
      new ResultsSelection.ResultsSelectionListener()
      {
        public void resultsSelected(MultipleValueParameter<?> primaryVar,
            MultipleValueParameter<?> secondaryVar,
            MultipleValueParameter<?> averageVar,
            List<SingleValueParameter<?>> otherVariables,
            StatisticVariable statistic)
        {
          createBottomPanel(primaryVar, secondaryVar, averageVar,
            otherVariables, statistic);
        }
      };

    centerPanel = new ResultsSelection(multiValueParams,
      createStatisticsVariables(), resultsSelectionListener);

    topBox.add(centerPanel);
    revalidate();
  }

  private void createBottomPanel(MultipleValueParameter<?> primaryVar,
      MultipleValueParameter<?> secondaryVar,
      MultipleValueParameter<?> averageVar,
      List<SingleValueParameter<?>> otherVariables, StatisticVariable statistic)
  {
    boolean needRevalidate = true;
    if (bottomPanel != null)
      remove(bottomPanel);
    else
      needRevalidate = false;
    ResultsGrid grid = new ResultsGrid(results, primaryVar, secondaryVar,
        averageVar, otherVariables, statistic);
    bottomPanel = new ResultsTable(primaryVar, secondaryVar, grid);
    bottomPanel.addResultsTableListener(
        new ResultsTable.ResultsTableListener()
    {
      public void cellSelected(List<SingleValueParameter<?>> params)
      {
        runRealTimeSimulation(params);
      }
    });
    add(bottomPanel, BorderLayout.CENTER);
    if (needRevalidate) revalidate();
  }
  
  private void errorDialog(List<SingleValueParameter<?>> params, Exception e)
  {
    foundError = true;
    Window window = SwingUtilities.windowForComponent(this);
    new ExceptionDialog(window, params, e);
  }

  private void updateSimulationParameters(Simulator updateSim,
    List<SingleValueParameter<?>> params)
  {
    for (SingleValueParameter<?> p : params)
    {
      SingleValueParameter<?> simParameter =
        updateSim.getParameter(p.getDescription());
      simParameter.setValueFromString(p.getValue().toString());
    }
  }

  private List<Table> startSimulation(final List<SingleValueParameter<?>> params)
  {
    // TODO : reconsider how to determine the endtime in a multiple simulation
    final long endTime = 99000000;
    ClockFactory clockFactory = new ClockFactory()
    {
      public Clock createClock(final Clock.FeedbackListener cl)
      {
        return new Clock(cl)
        {
          public void dispose()
          {
          }

          public void pause()
          {
            setRunningState(false);
          }

          public void start()
          {
            if (isRunning())
              throw new IllegalStateException("Can't start while already running");
            setRunningState(true);
            setSimulationTime(endTime);
          }
        };
      }
    };
    //    initialize the sim
    try
    {
      sim.initialize(clockFactory);
    }
    catch (Exception e)
    {
      errorDialog(params, e);
      return null;
    }
    sim.getEventQueue().addListener(new EventQueue.Listener()
    {
      public void eventAdded(Event e) {}
      public void eventRemoved(Event e) {}
      public void eventProcessed(Event e) {}
      public void simulationEnded() {}

      public void eventError(Exception ex)
      {
        if (!foundError)
          errorDialog(params, ex);
      }
    });
    //    run the sim
    sim.getClock().start();
    return sim.getModel().getStatistics();
  }

  protected List<List<SingleValueParameter<?>>> createParameterSet(
      List<MultipleValueParameter<?>> rangeParams)
  {
    Iterator<List<SingleValueParameter<?>>> iterator =
      createParameterSetIterator(rangeParams);

    List<List<SingleValueParameter<?>>> parameterSet =
      new ArrayList<List<SingleValueParameter<?>>>();
    while (iterator.hasNext())
      parameterSet.add(iterator.next());
    return parameterSet;
  }

  // SOON : Make this a List instead of an Iterator
  private Iterator<List<SingleValueParameter<?>>> createParameterSetIterator(
      List<MultipleValueParameter<?>> rangeParams)
  {
    final List<List<SingleValueParameter>> paramListList =
      new ArrayList<List<SingleValueParameter>>();
    for (MultipleValueParameter rp : rangeParams)
      paramListList.add(rp.getParameterList());

    final int[] positions = new int[paramListList.size()];
    return new Iterator<List<SingleValueParameter<?>>>()
    {
      boolean done = paramListList.size() == 0;
      public List<SingleValueParameter<?>> next()
      {
        if (done)
          throw new NoSuchElementException("Can't next after end");
        increment();
        return getCurrent();
      }

      private void increment()
      {
        for (int i = 0; i < paramListList.size(); i++)
        {
          List<SingleValueParameter> paramList = paramListList.get(i);
          positions[i]++;
          if (positions[i] < paramList.size())
            return;  // not done yet
          positions[i] = 0;
        }
        // When the odometer rolls over, the iterator is done with
        done = true;
      }
      private List<SingleValueParameter<?>> getCurrent()
      {
        List<SingleValueParameter<?>> l =
          new ArrayList<SingleValueParameter<?>>();
        for (int i = 0; i < paramListList.size(); i++)
          l.add(paramListList.get(i).get(positions[i]));
        return l;
      }

      public boolean hasNext()
      {
        return !done;
      }

      public void remove()
      {
        throw new IllegalStateException("Can't remove from this iterator!");
      }
    }; 
  }

  private List<MultipleValueParameter<?>> createMultiValueParameters(
      List<SingleValueParameter<?>> simParams)
  {
    List<MultipleValueParameter<?>> newParams =
      new ArrayList<MultipleValueParameter<?>>(simParams.size());
    for (SingleValueParameter<?> p : simParams)
    {
      if (p instanceof ChoiceParameter<?>)
        newParams.add(new MultipleChoiceParameter((ChoiceParameter<?>)p));
      else
        newParams.add(new RangeParameter(p));
    }
    return newParams;
  }

  private List<StatisticVariable> createStatisticsVariables()
  {
    final List<StatisticVariable> statisticsVariables =
      new ArrayList<StatisticVariable>();
    // Only fill the statistics variable with the headers of one set of tables,
    // otherwise the list will contain duplicates of each statistics variable
    // from each of the set of table results
    List<Table> tables = results.values().iterator().next();
    for (Table t : tables)
    {
      String tableName = t.getName();
      for (int colNum = 0; colNum < t.getColumnCount(); colNum++)
      {
        Column c = t.getColumn(colNum);
        String name = c.getHeading();
        statisticsVariables.add(new StatisticVariable(tableName, "Avg", name));
        statisticsVariables.add(new StatisticVariable(tableName, "Min", name));
        statisticsVariables.add(new StatisticVariable(tableName, "Max", name));
      }
    }
    return statisticsVariables;
  }

  private void runSimulations()
  {
    if (bottomPanel != null)
    {
      remove(bottomPanel);
      bottomPanel = null;
    }
    if (centerPanel != null)
    {
      topBox.remove(centerPanel);
      centerPanel = null;
    }
    repaint();
    foundError = false;
    results = new HashMap<List<SingleValueParameter<?>>, List<Table>>();
    Frame windowAncestor =
      (Frame)SwingUtilities.getWindowAncestor(MultipleSimulationArea.this);
    final ProgressDialog pd =
      new ProgressDialog(windowAncestor, "Progress");
    // SOON: WE ARE HERE -- add a component to the progressdialog to display parameters
    Runnable r = new Runnable()
    {
      public void run()
      {
        int i = 1;
        List<List<SingleValueParameter<?>>> parameterSetList =
          createParameterSet(multiValueParams);
        for (List<SingleValueParameter<?>> params : parameterSetList)
        {
          updateSimulationParameters(sim, params);
          List<Table> statistics = startSimulation(params);
          if (pd.isCancelled()) foundError = true;
          if (foundError) break;
          results.put(params, statistics);
          pd.progress(i++, parameterSetList.size());
        }
        
        if (foundError)
          results = null;  // Allow garbage collection
        else
          createCenterPanel();
      }
    };
    Thread t = new Thread(r);
    t.setPriority(Thread.MIN_PRIORITY);
    t.start();
    pd.setVisible(true);
  }

  private void runRealTimeSimulation(List<SingleValueParameter<?>> params)
  {
    Simulator newSim = sim.duplicate();
    // Parameters must be set before initializing the model.
    updateSimulationParameters(newSim, params);
    newSim.initialize(new RealTimeClock.RealTimeClockFactory());
    JFrame simFrame = new JFrame("Real Time Simulation Run");
    simFrame.setIconImage(simApp.getImageIcon());
    simFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    SimulationArea simulationArea = new SimulationArea(newSim, simApp, parent);
    simFrame.getContentPane().add(simulationArea, BorderLayout.CENTER);
    simFrame.setSize(800, 600);
    simFrame.setVisible(true);
  }
}