package model;

import org.apache.commons.chain.impl.ChainBase;

import metric.AverageCommitHeathMetric;
import metric.AverageIssueOpenedTimeHeathMetric;

public class AllMetricsChain extends ChainBase {

  public AllMetricsChain() {
    super();
    addCommand(new AverageCommitHeathMetric());
    addCommand(new AverageIssueOpenedTimeHeathMetric());
  }
}
