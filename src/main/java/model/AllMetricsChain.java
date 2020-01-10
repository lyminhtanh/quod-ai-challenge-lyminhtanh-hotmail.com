package model;

import java.io.IOException;

import org.apache.commons.chain.impl.ChainBase;

import enums.Strategy;
import lombok.extern.log4j.Log4j2;
import metric.AverageCommitHeathMetric;
import metric.AverageCommitPerDeveloperRatioHeathMetric;
import metric.AverageIssueOpenedTimeHeathMetric;
import metric.AverageOpenedToClosedIssueHeathMetric;
import metric.AveragePullRequestMergedTimeHeathMetric;
import metric.CsvExporter;
import metric.HealthScoreAggregator;
import metric.NumOfReleaseHeathMetric;

@Log4j2
public class AllMetricsChain extends ChainBase {

  public AllMetricsChain() throws IOException {
    super();
    log.info("--- Start chain with the strategy {}", Strategy.ALL_METRIC);
    addCommand(new AveragePullRequestMergedTimeHeathMetric());
    addCommand(new AverageCommitHeathMetric());
    addCommand(new AverageIssueOpenedTimeHeathMetric());
    addCommand(new AverageCommitPerDeveloperRatioHeathMetric());
    addCommand(new AverageOpenedToClosedIssueHeathMetric());
    addCommand(new NumOfReleaseHeathMetric());
    addCommand(new HealthScoreAggregator());
    addCommand(new CsvExporter());
  }
}
