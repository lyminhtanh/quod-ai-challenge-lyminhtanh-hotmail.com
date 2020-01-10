package metric;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import constant.Constant;
import enums.GitHubEventType;
import enums.IssueState;
import enums.Metric;
import model.GitHubEvent;
import model.HealthScore;
import model.Issue;
import model.Payload;

/**
 * Average time that an issue remains opened healthRatio = total(PushEvent of
 * project A)/total(PushEvent)
 */

public class AverageOpenedToClosedIssueHeathMetric extends HealthMetric {

  public AverageOpenedToClosedIssueHeathMetric() throws IOException {
    super(Metric.average_opened_to_closed_issue_ratio, GitHubEventType.ISSUE_EVENT);
  }

  /**
   * @param entry
   * @return
   */
  @Override
  protected HealthScore calculateHealthScore(
      Map.Entry<Long, List<GitHubEvent>> entry) {
    double score = calculateHealthScore(entry.getValue());

    return buildHealthScore(entry.getKey(), score);
  }

  /**
   * calculate health score for one repo
   * 
   * @param List<GitHubEvent> odered ascending by issueCreatedAt
   * @return double
   */
  private double calculateHealthScore(List<GitHubEvent> repoEvents) {

    ConcurrentMap<Long, List<GitHubEvent>> groupedByIssueId =

        repoEvents.parallelStream().collect(
            Collectors.groupingByConcurrent(event -> event.getPayload().getIssue().getId()));

    ConcurrentMap<Long, IssueState> timeGroupedByIssueId =
        groupedByIssueId.entrySet().parallelStream()
            .collect(Collectors.toConcurrentMap(entry -> entry.getKey(), this::getIssueState));

    return calculateHealthScore(timeGroupedByIssueId.values());
  }

  /**
   * @param entry
   * @return
   */
  private IssueState getIssueState(Map.Entry<Long, List<GitHubEvent>> entry) {
    boolean isClosed =
        entry.getValue().parallelStream().map(GitHubEvent::getPayload).map(Payload::getIssue)
        .map(Issue::getState).anyMatch(IssueState.CLOSED.name()::equalsIgnoreCase);

    return isClosed ? IssueState.CLOSED : IssueState.OPENED;
  }

  /**
   * calculate health score for each project
   *
   * @return List<HealthScore> odered descending by score
   */
  private double calculateHealthScore(Collection<IssueState> entries) {
    if (entries.size() == 0) {
      return 0;
    }
    long numOfClosedIssue =
        entries.parallelStream().filter(IssueState.CLOSED::equals).count();

    if (numOfClosedIssue == 0) {
      return Constant.SKIP_SCORE;
    }

    return (double)(entries.size() - numOfClosedIssue)/numOfClosedIssue;
  }

}
