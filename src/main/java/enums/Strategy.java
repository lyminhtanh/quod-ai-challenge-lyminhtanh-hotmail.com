package enums;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

public enum Strategy {
  ALL_METRIC(Arrays.asList(Metric.values())),

  AVERAGE_ISSUE_OPEN_TIME(Arrays.asList(Metric.AVERAGE_ISSUE_OPEN_TIME)),

  AVERAGE_COMMIT(Arrays.asList(Metric.AVERAGE_COMMIT)),

  AVERAGE_PULL_REQUEST_MERGE_TIME(Arrays.asList(Metric.AVERAGE_PULL_REQUEST_MERGE_TIME)),

  AVERAGE_COMMITS_PER_DEVELOPERS_RATIO(Arrays.asList(Metric.AVERAGE_COMMITS_PER_DEVELOPERS_RATIO)),

  AVERAGE_OPENING_TO_CLOSED_ISSUE_RATIO(
      Arrays.asList(Metric.AVERAGE_OPENING_TO_CLOSED_ISSUE_RATIO)),

  NUMBER_OF_RELEASES(Arrays.asList(Metric.NUMBER_OF_RELEASES));


  @Getter
  private List<Metric> metrics;

  private Strategy(List<Metric> metrics) {
    this.metrics = metrics;
  }
}
