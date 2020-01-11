package metric;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import constant.Constant;
import enums.IssueState;
import enums.Metric;
import enums.StatisticData;
import model.GitHubEvent;
import model.Issue;
import model.Payload;

/**
 * Average time that an issue remains opened healthRatio = total(PushEvent of
 * project A)/total(PushEvent)
 */

public class AverageOpenedToClosedIssueHeathMetric extends HealthMetric {

  public AverageOpenedToClosedIssueHeathMetric() throws IOException {
    super(Metric.AVERAGE_OPENING_TO_CLOSED_ISSUE_RATIO);
  }

  /**
   * calculate health score for one repo
   * 
   * @param List<GitHubEvent> odered ascending by issueCreatedAt
   * @return double
   */
  @Override
  protected double calculateHealthScore(List<GitHubEvent> repoEvents) {

    ConcurrentMap<Long, List<GitHubEvent>> groupedByIssueId =
        repoEvents.parallelStream().collect(
            Collectors.groupingByConcurrent(event -> event.getPayload().getIssue().getId()));

    ConcurrentMap<Long, IssueState> stateGroupedByIssueId =
        groupedByIssueId.entrySet().parallelStream()
            .collect(Collectors.toConcurrentMap(entry -> entry.getKey(), this::getIssueState));

    return calculateHealthScore(stateGroupedByIssueId.values(), getRepoId(repoEvents));
  }

  /**
   * check Issue State closed or not
   * 
   * @param entry
   * @return
   */
  private IssueState getIssueState(Map.Entry<Long, List<GitHubEvent>> entry) {
    boolean isClosed =
        entry.getValue().parallelStream().map(GitHubEvent::getPayload).map(Payload::getIssue)
        .map(Issue::getState).anyMatch(IssueState.CLOSED.name()::equalsIgnoreCase);

    return isClosed ? IssueState.CLOSED : IssueState.OPENING;
  }

  /**
   * calculate score = numOfOpeningIssue / numOfClosedIssue
   * 
   * @param repoId
   *
   * @return List<HealthScore> odered descending by score
   */
  private double calculateHealthScore(Collection<IssueState> issueStates, long repoId) {
    if (issueStates.size() == 0) {
      return Constant.SKIP_SCORE;
    }

    long numOfClosedIssue =
        issueStates.parallelStream().filter(IssueState.CLOSED::equals).count();

    if (numOfClosedIssue == 0) {
      return Constant.SKIP_SCORE;
    }

    long numOfOpeningIssue = issueStates.size() - numOfClosedIssue;

    // update statistic data
    getRepoStatistics(repoId).put(StatisticData.NUM_OF_OPENING_ISSUES, numOfOpeningIssue);
    getRepoStatistics(repoId).put(StatisticData.NUM_OF_CLOSED_ISSUES, numOfClosedIssue);

    return (double) numOfOpeningIssue / numOfClosedIssue;
  }

}
