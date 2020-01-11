package enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import metric.AverageCommitHeathMetric;
import metric.AverageCommitPerDeveloperRatioHeathMetric;
import metric.AverageIssueOpenedTimeHeathMetric;
import metric.AverageOpenedToClosedIssueHeathMetric;
import metric.AveragePullRequestMergedTimeHeathMetric;
import metric.HealthMetric;
import metric.NumOfReleaseHeathMetric;
@Getter
public enum Metric {
  AVERAGE_COMMIT(GitHubEventType.PUSH_EVENT,
      AverageCommitHeathMetric.class, new HashSet<>(Arrays.asList(StatisticData.NUM_OF_COMMITS))), 
  
  AVERAGE_ISSUE_OPEN_TIME(GitHubEventType.ISSUE_EVENT,
      AverageIssueOpenedTimeHeathMetric.class, new HashSet<>(
          Arrays.asList(StatisticData.ISSUE_AVERAGE_OPEN_TIME_IN_SECOND, StatisticData.ISSUE_MIN_MERGING_TIME_IN_SECOND))),
  
  AVERAGE_PULL_REQUEST_MERGE_TIME(
              GitHubEventType.PULL_REQUEST_EVENT,
              AveragePullRequestMergedTimeHeathMetric.class, new HashSet<>(Arrays.asList(StatisticData.NUM_OF_PULL_REQUESTS))),
  
  AVERAGE_COMMITS_PER_DEVELOPERS_RATIO(
                  GitHubEventType.PUSH_EVENT,
                  AverageCommitPerDeveloperRatioHeathMetric.class, new HashSet<>(Arrays.asList(StatisticData.NUM_OF_COMMITS, StatisticData.NUM_OF_DEVELOPERS))),
  
  AVERAGE_OPENING_TO_CLOSED_ISSUE_RATIO(
                      GitHubEventType.ISSUE_EVENT,
                      AverageOpenedToClosedIssueHeathMetric.class, new HashSet<>(Arrays.asList(StatisticData.NUM_OF_OPENING_ISSUES, StatisticData.NUM_OF_CLOSED_ISSUES))),
  
  NUMBER_OF_RELEASES(GitHubEventType.RELEASE_EVENT, NumOfReleaseHeathMetric.class, new HashSet<>(Arrays.asList(StatisticData.NUM_OF_RELEASES)));

  private GitHubEventType type;

  private Class<? extends HealthMetric> chainClazz;

  private Set<StatisticData> statDatas;

  private Metric(final GitHubEventType type, final Class<? extends HealthMetric> chainClazz, final Set<StatisticData> statDatas) {
    this.type = type;
    this.chainClazz = chainClazz;
    this.statDatas = statDatas;
  }
}
