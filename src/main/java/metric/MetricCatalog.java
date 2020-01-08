package metric;

import org.apache.commons.chain.impl.CatalogBase;

import enums.Metric;
import enums.MetricGroup;
import model.AllMetricsChain;

public class MetricCatalog extends CatalogBase {

  public MetricCatalog() {
    super();
    addCommand(MetricGroup.all_metric.name(), new AllMetricsChain());

  }

}
