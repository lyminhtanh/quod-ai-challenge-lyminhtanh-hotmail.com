package metric;

import org.apache.commons.chain.impl.CatalogBase;

import enums.MetricGroup;
import model.AllMetricsChain;

public class MetricCatalog extends CatalogBase {

  public MetricCatalog() {
    super();
    addCommand(MetricGroup.ALL_METRIC.name(), new AllMetricsChain());
  }

}
