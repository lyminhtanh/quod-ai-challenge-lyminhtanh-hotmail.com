package metric;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import enums.Action;
import enums.GitHubEventType;
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
public class AverageIssueOpenedTimeHeathMetric extends HealthMetric {

  public AverageIssueOpenedTimeHeathMetric() throws IOException {
    super(Metric.average_issue_opened_time, GitHubEventType.ISSUE_EVENT);
  }

  @Override
  public List<HealthScore> calculate() throws IOException {

    ConcurrentMap<RepoIssue, List<GitHubEvent>> groupedByRepoIssue =
        events.parallelStream().collect(Collectors.groupingByConcurrent(this::buildRepoIssueKey));

    groupedByRepoIssue.values().parallelStream().filter(event -> event.size() > 1).forEach(event -> events
        .sort(Comparator.comparing(GitHubEvent::getCreatedAt, Comparator.naturalOrder())));

    ConcurrentMap<RepoIssue, Long> timeGroupedByRepoIssue = groupedByRepoIssue.entrySet().parallelStream()
        .collect(Collectors.toConcurrentMap(entry -> entry.getKey(), this::calculateOpenTimeInMinutes));

    List<HealthScore> healthScores = timeGroupedByRepoIssue.entrySet().parallelStream()
        .collect(Collectors.groupingByConcurrent(entry -> entry.getKey().getRepoId())).entrySet().parallelStream()
        .map(entry -> calculateHealthScore(entry))
        .collect(Collectors.toCollection(Vector::new));

    return healthScores;
  }

  /**
   * @param entry
   * @return
   */
  private HealthScore calculateHealthScore(
      Map.Entry<Long, List<Map.Entry<RepoIssue, Long>>> entry) {
    double score = calculateHealthScore(entry.getValue());

    return buildHealthScore(entry.getKey(), score);
  }

  private HealthScore buildHealthScore(Long repoId, double score) {
    HealthScore healthScore = HealthScore.commonBuilder(this.context.getMetricGroup())
        .repoId(repoId).score(score).build();

    healthScore.getSingleMetricScores().put(this.metric, score);

    return healthScore;

  }

  /**
   * @param entry
   * @return
   */
  private Long calculateOpenTimeInMinutes(Map.Entry<RepoIssue, List<GitHubEvent>> entry) {
    final LocalDateTime openedAt = Optional.ofNullable(entry.getValue().get(0))
        .map(GitHubEvent::getPayload).map(Payload::getIssue).map(Issue::getCreatedAt)
        .orElseThrow(() -> new IllegalStateException(
            String.format("No created_at found for Issue: %d", entry.getKey().getIssueId())));

    final LocalDateTime nonOpenedAt = getNonOpenActionCreatedAt(entry.getValue());
    Duration duration = Duration.between(openedAt, nonOpenedAt);

    return duration.toMinutes();
  }

  private LocalDateTime getNonOpenActionCreatedAt(List<GitHubEvent> events) {
    boolean hasOpenAction = hasOpenedAction(events);

    int nonOpenedIndex = 0;

    if (hasOpenAction) {
      if (events.size() == 1) {
        return this.context.getDateTimeEnd().plusHours(1);
      }
      nonOpenedIndex = 1;
    }

    return events.get(nonOpenedIndex).getCreatedAt();

  }

  private boolean hasOpenedAction(List<GitHubEvent> events) {
    return events.stream().map(GitHubEvent::getPayload).map(Payload::getAction)
        .anyMatch(Action.OPENED.value()::equals);
  }

  private RepoIssue buildRepoIssueKey(GitHubEvent event) {
    return new RepoIssue(event.getRepo().getId(), event.getPayload().getIssue().getId());
  }

  /**
   * calculate health score for each project
   *
   * @return List<HealthScore> odered descending by score
   */
  private double calculateHealthScore(List<Map.Entry<RepoIssue, Long>> entries) {
    if (entries.size() == 0) {
      return 0;
    }
    Long avgOpenTime =
        entries.stream().map(Map.Entry::getValue).mapToLong(Long::longValue).sum() / entries.size();
    Long minOpenTime =
        entries.stream().map(Map.Entry::getValue).mapToLong(Long::longValue).min().getAsLong();
    if (minOpenTime == 0) {
      return Double.MAX_VALUE;
    }
    return (double) avgOpenTime / minOpenTime;
  }

}
