package model;

import org.apache.commons.chain.impl.CatalogBase;

import enums.Metric;

public class MetricCatalog extends CatalogBase {
  public MetricCatalog() {
    super();
    addCommand(Metric.all_metric.name(), new AllMetricsChain());
  }

}
