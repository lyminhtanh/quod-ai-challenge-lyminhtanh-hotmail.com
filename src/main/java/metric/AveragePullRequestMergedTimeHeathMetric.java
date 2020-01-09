package metric;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import enums.GitHubEventType;
import enums.Metric;
import model.GitHubEvent;
import model.HealthScore;
import model.Payload;
import model.PullRequest;
import model.RepoPullRequest;

/**
 * Average time for a pull request to get merged If an issue has not yet merged in the search
 * period, it will be ignored in calculation
 */
public class AveragePullRequestMergedTimeHeathMetric extends HealthMetric {

  public AveragePullRequestMergedTimeHeathMetric() throws IOException {
    super(Metric.average_pull_request_merge_time, GitHubEventType.PULL_REQUEST_EVENT);
  }

  @Override
  public List<HealthScore> calculate() throws IOException {

    Map<RepoPullRequest, List<GitHubEvent>> groupedByRepoPullRequest =
        events.stream().collect(Collectors.groupingBy(this::buildRepoPullRequestKey));

    Map<RepoPullRequest, Long> timeGroupedByRepoIssue =
        groupedByRepoPullRequest.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), this::calculateOpenTimeInMinutes));

    List<HealthScore> healthScores = timeGroupedByRepoIssue.entrySet().stream()
        .collect(Collectors.groupingBy(entry -> entry.getKey().getRepoId())).entrySet().stream()
        .map(entry -> calculateHealthScore(entry))
        .collect(Collectors.toList());

    return healthScores;
  }

  /**
   * @param entry
   * @return
   */
  private HealthScore calculateHealthScore(
      Map.Entry<Long, List<Map.Entry<RepoPullRequest, Long>>> entry) {
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
  private Long calculateOpenTimeInMinutes(Map.Entry<RepoPullRequest, List<GitHubEvent>> entry) {
    final LocalDateTime openedAt = Optional.ofNullable(entry.getValue().get(0))
        .map(GitHubEvent::getPayload).map(Payload::getPullRequest).map(PullRequest::getCreatedAt)
        .orElseThrow(() -> new IllegalStateException(String
            .format("No created_at found for PullRequest: %d", entry.getKey().getPullRequestId())));

    final LocalDateTime mergedAt = getMergedAt(entry.getValue());

    // the PR has not been merged yet
    if (mergedAt == null) {
      return Long.valueOf(-1);
    }

    Duration duration = Duration.between(openedAt, mergedAt);

    return duration.toMinutes();
  }

  private LocalDateTime getMergedAt(List<GitHubEvent> events) {

    return events.stream().map(GitHubEvent::getPayload).map(Payload::getPullRequest)
        .map(PullRequest::getMergedAt).filter(Objects::nonNull).findAny()
        .orElse(null);

  }

  private RepoPullRequest buildRepoPullRequestKey(GitHubEvent event) {
    return new RepoPullRequest(event.getRepo().getId(),
        event.getPayload().getPullRequest().getId());
  }

  /**
   * calculate health score for each project
   *
   * @return List<HealthScore> odered descending by score
   */
  private double calculateHealthScore(List<Map.Entry<RepoPullRequest, Long>> entries) {

    long sum = entries.stream().map(Map.Entry::getValue).mapToLong(Long::longValue).filter(n -> n >= 0)
        .sum();
    long count = entries.stream().map(Map.Entry::getValue).filter(n -> n >= 0).count();

    if (count == 0) {
      return 0.0;
    }

    Long avgMergedTime =
        sum / count;
    Long minMergedTime =
        entries.stream().map(Map.Entry::getValue).mapToLong(Long::longValue).filter(n -> n >= 0)
            .min().getAsLong();

    if (minMergedTime == 0) {
      return Double.MAX_VALUE;
    }
    return (double) avgMergedTime / minMergedTime;
  }

}
