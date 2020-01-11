package model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import org.apache.commons.chain.impl.ChainBase;

import enums.Metric;
import enums.Strategy;
import metric.CsvExporter;
import metric.HealthMetric;
import metric.HealthScoreAggregator;

public class StrategyChain extends ChainBase {

  public StrategyChain(Strategy strategy) {
    super();
    strategy.getMetrics().stream().sorted(Comparator.comparing(Metric::getType))
        .map(Metric::getChainClazz).map(this::getNoArgsConstructor).forEach(this::addCommand);

    // Add filters
    addCommand(new HealthScoreAggregator());
    addCommand(new CsvExporter());
  }

  private void addCommand(Constructor<? extends HealthMetric> cons) {
    try {
      addCommand(cons.newInstance());
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private Constructor<? extends HealthMetric> getNoArgsConstructor(
      Class<? extends HealthMetric> t) {
    try {
      return t.getConstructor();
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }
}
