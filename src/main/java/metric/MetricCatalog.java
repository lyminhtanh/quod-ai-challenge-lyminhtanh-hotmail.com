package metric;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.chain.impl.CatalogBase;

import enums.Strategy;
import model.StrategyChain;

public class MetricCatalog extends CatalogBase {

  public MetricCatalog() throws IOException {
    super();
    Stream.of(Strategy.values()).forEach(this::addCommand);
  }

  private void addCommand(Strategy strategy) {
    addCommand(strategy.name(), new StrategyChain(strategy));
  }
}
