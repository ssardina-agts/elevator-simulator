/*
* Copyright 2004-2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.sim.ui.multiple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.intranet.statistics.Column;
import org.intranet.statistics.Table;
import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ResultsGrid
{
  MultipleValueParameter<?> primaryVar;
  MultipleValueParameter<?> secondaryVar;
  MultipleValueParameter<?> averageVar;
  List<SingleValueParameter<?>> otherVariables;
  List<List<List<SingleValueParameter<?>>>> parameterLists;
  List<List<AverageNumber>> statisticLists;

  ResultsGrid(Map<List<SingleValueParameter<?>>, List<Table>> results,
    MultipleValueParameter<?> primaryVar,
    MultipleValueParameter<?> secondaryVar,
    MultipleValueParameter<?> averageVar,
    List<SingleValueParameter<?>> otherVariables,
    StatisticVariable statisticsSelection)
  {
    this.primaryVar = primaryVar;
    this.secondaryVar = secondaryVar;
    this.otherVariables = otherVariables;
    this.averageVar = averageVar;

    int primarySize = primaryVar == null ? 1 :
      primaryVar.getParameterList().size();
    int secondarySize = secondaryVar == null ? 1 :
      secondaryVar.getParameterList().size();

    parameterLists = new ArrayList<List<List<SingleValueParameter<?>>>>();
    statisticLists = new ArrayList<List<AverageNumber>>();
    for (int i = 0; i < primarySize; i++)
    {
      ArrayList<List<SingleValueParameter<?>>> parameterRow =
        new ArrayList<List<SingleValueParameter<?>>>();
      parameterLists.add(parameterRow);
      ArrayList<AverageNumber> statisticRow = new ArrayList<AverageNumber>();
      statisticLists.add(statisticRow);
      
      for (int j = 0; j < secondarySize; j++)
      {
        parameterRow.add(null);
        statisticRow.add(null);
      }
    }

    // Iterate through the statistics results to extract the values for the
    // variables we are interested in (based on primary and secondary selections).
    // Get column "names" (actually variable values) and row names (to do later)
    // along the way.
    for (Map.Entry<List<SingleValueParameter<?>>, List<Table>> entry :
      results.entrySet())
    {
      List<SingleValueParameter<?>> params = entry.getKey();
      List<Table> statistics = entry.getValue();

      if (!variablesMatch(params))
        continue;
      int column = primaryVar == null ? 0 : findPrimaryColumn(params);
      int row = secondaryVar == null ? 0 : findSecondaryRow(params);
      Number result = getStatistic(statistics, statisticsSelection);
      AverageNumber num = statisticLists.get(column).get(row);

      if (num == null)
        statisticLists.get(column).set(row,
            new AverageNumber(result.doubleValue()));
      else
        num.add(result.doubleValue());
      parameterLists.get(column).set(row, params);
    }
  }

  private Number getStatistic(List<Table> statistics,
    StatisticVariable statisticsSelection)
  {
    // find the statistic that was requested from the statistics chooser
    for (Table table : statistics)
    {
      if (statisticsSelection.getTableName().equals(table.getName()))
      {
        for (int colNum = 0; colNum < table.getColumnCount(); colNum++)
        {
          Column column = table.getColumn(colNum);
          if (column.getHeading().equals(statisticsSelection.getStatisticName()))
          {
            // TODO: factor out explicit case analysis to classes
            if ("Avg".equals(statisticsSelection.getFunctionName()))
              return column.getAverage();
            else if ("Min".equals(statisticsSelection.getFunctionName()))
              return column.getMin();
            else if ("Max".equals(statisticsSelection.getFunctionName()))
              return column.getMax();
          }
        }
      }
    }
    throw new IllegalArgumentException("Couldn't find value for statistic " +
        statisticsSelection.getFunctionName());
  }

  private int findSecondaryRow(List<SingleValueParameter<?>> params)
  {
    for (SingleValueParameter<?> p : params)
    {
      if (secondaryVar.getDescription().equals(p.getDescription()))
      {
        int idx = 0;
        for (SingleValueParameter<?> sec : secondaryVar.getParameterList())
        {
          if (p.getValue().equals(sec.getValue()))
            return idx;
          idx++;
        }
      }
    }
    throw new IllegalArgumentException("Could not find parameter named " +
        secondaryVar.getDescription());
  }

  private int findPrimaryColumn(List<SingleValueParameter<?>> params)
  {
    for (SingleValueParameter<?> p : params)
    {
      if (primaryVar.getDescription().equals(p.getDescription()))
      {
        int idx = 0;
        for (SingleValueParameter<?> prim : primaryVar.getParameterList())
        {
          if (p.getValue().equals(prim.getValue()))
            return idx;
          idx++;
        }
      }
    }
    throw new IllegalArgumentException("Could not find parameter named " +
        primaryVar.getDescription());
  }

  private boolean variablesMatch(List<SingleValueParameter<?>> params)
  {
    for (SingleValueParameter<?> p : params)
    {
      String desc = p.getDescription();
      Object val = p.getValue();
      if (primaryVar != null && desc.equals(primaryVar.getDescription()))
        continue;
      if (secondaryVar != null && desc.equals(secondaryVar.getDescription()))
        continue;
      for (SingleValueParameter<?> other : otherVariables)
      {
        // If this is an average variable, we want all instances of it to
        // match.  So just continue here and don't risk doing a test that
        // will result in returning false.
        if (averageVar != null &&
          averageVar.getDescription().equals(other.getDescription()))
        {
          continue;
        }
        if (desc.equals(other.getDescription()))
        {
          if (!val.equals(other.getValue()))
            return false;
          break;
        }
      }
    }
    return true;
  }

  List<SingleValueParameter<?>> getParameters(int col, int row)
  {
    return parameterLists.get(col).get(row);
  }

  AverageNumber getResult(int col, int row)
  {
    return statisticLists.get(col).get(row);
  }

  String getColumnName(int col)
  {
    SingleValueParameter<?> param = primaryVar.getParameterList().get(col);
    return param.getValue().toString();
  }

  String getRowName(int row)
  {
    SingleValueParameter<?> param = secondaryVar.getParameterList().get(row);
    return param.getValue().toString();
  }

  float getMin()
  {
    int primarySize = primaryVar == null ? 1 :
      primaryVar.getParameterList().size();
    int secondarySize = secondaryVar == null ? 1 :
      secondaryVar.getParameterList().size();

    float min = Float.MAX_VALUE;
    for (int col = 0; col < primarySize; col++)
    {
      for (int row = 0; row < secondarySize; row++)
      {
        AverageNumber val = statisticLists.get(col).get(row);
        if (min > val.floatValue())
          min = val.floatValue();
      }
    }
    return min;
  }

  float getMax()
  {
    int primarySize = primaryVar == null ? 1 :
      primaryVar.getParameterList().size();
    int secondarySize = secondaryVar == null ? 1 :
      secondaryVar.getParameterList().size();

    float max = Float.MIN_VALUE;
    for (int col = 0; col < primarySize; col++)
    {
      for (int row = 0; row < secondarySize; row++)
      {
        Number val = statisticLists.get(col).get(row);
        if (max < val.floatValue())
          max = val.floatValue();
      }
    }
    return max;
  }
}
