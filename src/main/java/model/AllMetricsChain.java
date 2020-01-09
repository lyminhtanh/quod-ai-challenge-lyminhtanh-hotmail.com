package model;

import org.apache.commons.chain.impl.ChainBase;

import metric.AverageCommitHeathMetric;
import metric.AverageCommitPerDeveloperRatioHeathMetric;
import metric.AverageIssueOpenedTimeHeathMetric;
import metric.AverageOpenedToClosedIssueHeathMetric;
import metric.AveragePullRequestMergedTimeHeathMetric;
import metric.CsvExporter;
import metric.HealthScoreAggregator;
import metric.NumOfReleaseHeathMetric;

public class AllMetricsChain extends ChainBase {

  public AllMetricsChain() {
    super();
    addCommand(new AverageCommitHeathMetric());
    addCommand(new AverageIssueOpenedTimeHeathMetric());
    addCommand(new AveragePullRequestMergedTimeHeathMetric());
    addCommand(new AverageCommitPerDeveloperRatioHeathMetric());
    addCommand(new AverageOpenedToClosedIssueHeathMetric());
    addCommand(new NumOfReleaseHeathMetric());
    addCommand(new HealthScoreAggregator());
    addCommand(new CsvExporter());
  }
}
