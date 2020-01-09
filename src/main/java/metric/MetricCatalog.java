package metric;

import java.io.IOException;

import org.apache.commons.chain.impl.CatalogBase;

import enums.MetricGroup;
import model.AllMetricsChain;

public class MetricCatalog extends CatalogBase {

  public MetricCatalog() throws IOException {
    super();
    addCommand(MetricGroup.ALL_METRIC.name(), new AllMetricsChain());
  }

}
