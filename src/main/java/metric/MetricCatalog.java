package metric;

import java.io.IOException;

import org.apache.commons.chain.impl.CatalogBase;

import enums.Strategy;
import model.AllMetricsChain;

public class MetricCatalog extends CatalogBase {

  public MetricCatalog() throws IOException {
    super();
    addCommand(Strategy.ALL_METRIC.name(), new AllMetricsChain());
  }

}
