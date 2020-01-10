package metric;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import enums.GitHubEventType;
import enums.IssueState;
import enums.Metric;
import model.GitHubEvent;
import model.HealthScore;
import model.Issue;
import model.Payload;
import model.RepoIssue;

/**
 * Average time that an issue remains opened healthRatio = total(PushEvent of
 * project A)/total(PushEvent)
 */

public class AverageOpenedToClosedIssueHeathMetric extends HealthMetric {

  public AverageOpenedToClosedIssueHeathMetric() throws IOException {
    super(Metric.average_opened_to_closed_issue_ratio, GitHubEventType.ISSUE_EVENT);
  }

  @Override
  public List<HealthScore> calculate() throws IOException {

    ConcurrentMap<RepoIssue, List<GitHubEvent>> groupedByRepoIssue =
        events.parallelStream().collect(Collectors.groupingByConcurrent(this::buildRepoIssueKey));

    ConcurrentMap<RepoIssue, IssueState> timeGroupedByRepoIssue =
        groupedByRepoIssue.entrySet().parallelStream()
            .collect(Collectors.toConcurrentMap(entry -> entry.getKey(), this::getIssueState));

    List<HealthScore> healthScores = timeGroupedByRepoIssue.entrySet().parallelStream()
        .collect(Collectors.groupingByConcurrent(entry -> entry.getKey().getRepoId())).entrySet()
        .parallelStream()
        .map(entry -> calculateHealthScore(entry))
        .collect(Collectors.toCollection(Vector::new));

    return healthScores;
  }

  /**
   * @param entry
   * @return
   */
  private HealthScore calculateHealthScore(
      Map.Entry<Long, List<Map.Entry<RepoIssue, IssueState>>> entry) {
    double score = calculateHealthScore(entry.getValue());

    return buildHealthScore(entry.getKey(), score);
  }

  private HealthScore buildHealthScore(Long repoId, double score) {
    HealthScore healthScore = HealthScore.commonBuilder(this.context.getMetricGroup())
        .repoId(repoId).build();

    healthScore.getSingleMetricScores().put(this.metric, score);

    return healthScore;

  }

  /**
   * @param entry
   * @return
   */
  private IssueState getIssueState(Map.Entry<RepoIssue, List<GitHubEvent>> entry) {
    boolean isClosed =
        entry.getValue().parallelStream().map(GitHubEvent::getPayload).map(Payload::getIssue)
        .map(Issue::getState).anyMatch(IssueState.CLOSED.name()::equalsIgnoreCase);

    return isClosed ? IssueState.CLOSED : IssueState.OPENED;
  }

  private RepoIssue buildRepoIssueKey(GitHubEvent event) {
    return new RepoIssue(event.getRepo().getId(), event.getPayload().getIssue().getId());
  }

  /**
   * calculate health score for each project
   *
   * @return List<HealthScore> odered descending by score
   */
  private double calculateHealthScore(List<Map.Entry<RepoIssue, IssueState>> entries) {
    if (entries.size() == 0) {
      return 0;
    }
    long numOfClosedIssue =
        entries.parallelStream().map(Map.Entry::getValue).filter(IssueState.CLOSED::equals).count();

    if (numOfClosedIssue == 0) {
      return Double.MAX_VALUE;
    }

    return (double)(entries.size() - numOfClosedIssue)/numOfClosedIssue;
  }

}
